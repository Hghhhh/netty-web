package com.github.wens.netty.web;

import com.github.wens.file.FileMessage;
import com.github.wens.file.ImageMessage;
import com.github.wens.netty.web.impl.RequestImp;
import com.github.wens.netty.web.impl.ResponseImp;
import com.github.wens.netty.web.route.RouteMatcher;
import com.github.wens.netty.web.util.JsonUtils;
import com.github.wens.netty.web.util.Threads;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by wens on 15-5-13.
 */
public class WebServer {

    private static final Logger log = LoggerFactory.getLogger("netty-server");

    private ServerConfig serverConfig;

    private RouteMatcher routeMatcher;
    private ControllerScanner controllerScanner;
    private ControllerInvoker controllerInvoker;

    public WebServer(ServerConfig config) {
        this.serverConfig = config;
        initServer();
        routeMatcher = new RouteMatcher(config.getContextPath());
        controllerScanner = new ControllerScanner(this.routeMatcher, this.serverConfig.getObjectFactory());
        controllerInvoker = new ControllerInvoker(this.routeMatcher, this.serverConfig.getObjectFactory(), this.serverConfig);
    }

    public void get(String route, String handle) {
        this.addRoute(route, HttpMethod.GET.name(), handle);
    }

    public void post(String route, String handle) {
        this.addRoute(route, HttpMethod.POST.name(), handle);
    }

    public void put(String route, String handle) {
        this.addRoute(route, HttpMethod.PUT.name(), handle);
    }

    public void delete(String route, String handle) {
        this.addRoute(route, HttpMethod.DELETE.name(), handle);
    }


    private void addRoute(String route, String method, String handle) {
        routeMatcher.addRouter(route, method, handle);
    }


    public void run() {
        try {

            EventLoopGroup bossGroup = new NioEventLoopGroup(1, Threads.makeName(serverConfig.getServerName()));
            EventLoopGroup workerGroup = new NioEventLoopGroup(this.serverConfig.getWorkerThreads(), Threads.makeName(String.format("%s-%s", serverConfig.getServerName(), "worker")));

            try {
                ServerBootstrap b = new ServerBootstrap();
                //b.option(ChannelOption.SO_BACKLOG, 256 );
                //b.option(ChannelOption.SO_RCVBUF, 128);
                //b.option(ChannelOption.SO_SNDBUF, 128);
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ServerHandlerInitializer(null));

                Channel ch = b.bind(this.serverConfig.getAddr(), this.serverConfig.getPort()).sync().channel();
                log.info("start server on " + this.serverConfig.getAddr() + ":" + this.serverConfig.getPort());
                ch.closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            throw new WebException("Run server fail.", e);
        }

    }


    private void initServer() {
        if (this.serverConfig == null) {
            this.serverConfig = new ServerConfig();
        }
    }

    public void scanRouters(String packageName) {
        controllerScanner.scanControllers(packageName);
    }

    private class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        private EventExecutorGroup executor;

        public ServerHandlerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
            this.executor = new DefaultEventExecutorGroup(serverConfig.getExecutorThreads(), Threads.makeName("bus-thread"));
        }

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(1073741824));
            p.addLast(new ChunkedWriteHandler());
            p.addLast(this.executor, new ServerHandler());
            p.addLast(this.executor, new DownloadHandler());
            p.addLast(this.executor, new ImageDownloadHandler());
        }
    }

    private class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

            if (!req.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST, BAD_REQUEST.reasonPhrase());
                return;
            }
            //如果是下载任务，交由下面的handler处理
            if(req.getUri().contains(serverConfig.getDownloadFlag()) ||
                    req.getUri().contains(serverConfig.getImageFlag())){
                ReferenceCountUtil.retain(req);
                ctx.fireChannelRead(req);
                return;
            }
            process(ctx, req);
        }

        protected void process(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

            HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
            final boolean keepAlive = HttpHeaders.isKeepAlive(httpRequest);
            if (keepAlive) {
                httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }

            final String method = httpRequest.getMethod().name();

            if(method.equals("OPTIONS") && serverConfig.getCorsSupport()){
                httpResponse.headers().set("Access-Control-Allow-Origin", serverConfig.getAccessControlAllowOrigin());
                httpResponse.headers().set("Access-Control-Allow-Credentials", serverConfig.getAccessControlAllowCredentials());
                httpResponse.headers().set("Access-Control-Allow-Methods", serverConfig.getAccessControlAllowMethods());
                httpResponse.headers().set("Access-Control-Allow-Headers", serverConfig.getAccessControlAllowHeaders());
                httpResponse.headers().set("Content-Length", 0);
                ctx.write(httpResponse);
                if (!keepAlive) {
                    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                }
                return ;
            } else if(serverConfig.getCorsSupport()){
                httpResponse.headers().set("Access-Control-Allow-Origin", serverConfig.getAccessControlAllowOrigin());
                httpResponse.headers().set("Access-Control-Allow-Credentials", serverConfig.getAccessControlAllowCredentials());
                httpResponse.headers().set("Access-Control-Allow-Methods", serverConfig.getAccessControlAllowMethods());
                httpResponse.headers().set("Access-Control-Allow-Headers", serverConfig.getAccessControlAllowHeaders());
            }

            final String uri = httpRequest.getUri();
            final RequestImp request = new RequestImp(ctx, httpRequest);
            final ResponseImp response = new ResponseImp(serverConfig.getCharset(), ctx, httpResponse);
            final WebContext webContext = new WebContext(request, response);

            long start = 0;
            long end = 0;

            Object res = null;

            if (log.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }

            try {
                res = controllerInvoker.invoke(method, uri, webContext);
            }catch (Exception e){
                log.error("invoke controller fail.", e);
                sendError(ctx, INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }

            if (log.isDebugEnabled()) {
                end = System.currentTimeMillis();
            }

            if (log.isDebugEnabled()) {
                log.debug("Request:" + request + ",Response:" + response + ",elapse:" + (end - start) + "ms");
            }

            processResult(res, response, keepAlive);

        }

        protected void processResult(Object res, ResponseImp response, Boolean keepAlive){
            if (res != null) {
                if(res instanceof DontAutoJsonResult){
                    Object data = ((DontAutoJsonResult) res).getRes();
                    if(((DontAutoJsonResult) res).getContentType() != null){
                        response.setContentType(((DontAutoJsonResult) res).getContentType());
                    }
                    if(data instanceof byte[]){
                        response.writeBody((byte[]) data);
                    }
                    else{
                        response.writeBody(data.toString());
                    }
                } else{
                    response.setContentType(String.format("application/json; charset=%s", serverConfig.getCharset()));
                    response.writeBody(JsonUtils.serialize((res)));
                }
            }
            if (!response.hasFinish()) {
                response.finish(keepAlive);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("An exception occurs, close the connect.", cause);
            if (ctx.channel().isActive()) {//try send 500
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }
        }

        protected void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + errorMsg + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, String.format("text/plain; charset=%s", serverConfig.getCharset()));

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


    private class DownloadHandler extends ServerHandler {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            if (!req.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST, BAD_REQUEST.reasonPhrase());
                return;
            }
            //如果不是下载任务，则交由下面的handler处理
            if(!req.getUri().contains(serverConfig.getDownloadFlag())){
                ReferenceCountUtil.retain(req);
                ctx.fireChannelRead(req);
                return;
            }
            process(ctx, req);

        }

        @Override
        protected void processResult(Object result, ResponseImp response, Boolean keepAlive){
            if(result == null) {
                throw new WebException("Should not return null where your router value cotains downloadFlag");
            } else {
                FileMessage fileMessage = (FileMessage) result;
                ChannelHandlerContext ctx = response.getCtx();
                HttpResponse httpResponse = response.getResponse();
                if(fileMessage.isFile()) {
                    File file = null;
                    try {
                        String[] filePaths = (String[]) fileMessage.getAtt();
                        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileMessage.getFileLength());
                        httpResponse.headers().set(CONTENT_TYPE, "application/octet-stream");
                        httpResponse.headers().set("Content-Disposition", "attachment ;filename=" + URLEncoder.encode(fileMessage.getFileName(), "UTF-8"));
                        ctx.write(httpResponse);
                        for(String filePath : filePaths){
                            file = new File(filePath);
                            final RandomAccessFile raf = new RandomAccessFile(file, "r");
                            long fileLength = raf.length();
                            //这里用ChunkedNioFile，以支持ChunkedWriteHandler的分块异步发送
                            //如果使用FileRegion的话，ChunkedWriteHandler不做处理
                            ChannelFuture sendFileFuture = ctx.write(new ChunkedNioFile(raf.getChannel(), 0,
                                    fileLength, 8192), ctx.newProgressivePromise());
                            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                                @Override
                                public void operationComplete(ChannelProgressiveFuture future)
                                        throws Exception {
                                    raf.close();
                                }
                                @Override
                                public void operationProgressed(ChannelProgressiveFuture future,
                                                                long progress, long total) throws Exception {
                                }
                            });
                        }
                        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        if (!keepAlive) {
                            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
                        }
                    } catch (FileNotFoundException e) {
                        log.error("file {} not found", file.getPath());
                        sendError(ctx, INTERNAL_SERVER_ERROR, e.getMessage());
                    } catch (IOException e) {
                        log.error("file {} has a IOException: {}", file.getName(), e.getMessage());
                        sendError(ctx, INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                } else {
                    super.processResult(fileMessage.getAtt(),response,keepAlive);
                }
            }
        }
    }


    private class ImageDownloadHandler extends ServerHandler {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            if (!req.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST, BAD_REQUEST.reasonPhrase());
                return;
            }
            //如果最后不是image任务，则返回错误信息
            if(!req.getUri().contains(serverConfig.getImageFlag())){
                sendError(ctx, BAD_REQUEST, BAD_REQUEST.reasonPhrase());
                return;
            }
            process(ctx, req);
        }

        @Override
        protected void processResult(Object result, ResponseImp response, Boolean keepAlive){
            if(result == null) {
                throw new WebException("Should not return null where your router value cotains imageFlag");
            } else {
                ImageMessage imageMessage = (ImageMessage) result;
                ChannelHandlerContext ctx = response.getCtx();
                HttpResponse httpResponse = response.getResponse();
                File file = null;
                if(!imageMessage.getImage()){
                    super.processResult(imageMessage.getAtt(),response,keepAlive);
                } else{
                    try {
                        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, imageMessage.getSize());
                        httpResponse.headers().set(CONTENT_TYPE, "image/" + imageMessage.getImageType());
                        ctx.write(httpResponse);
                        for (String filePath : imageMessage.getPaths()) {
                            file = new File(filePath);
                            final RandomAccessFile raf = new RandomAccessFile(file, "r");
                            long fileLength = raf.length();
                            ChannelFuture sendFileFuture = ctx.write(new ChunkedNioFile(raf.getChannel(), 0,
                                fileLength, 8192), ctx.newProgressivePromise());
                            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                                @Override
                                public void operationComplete(ChannelProgressiveFuture future)
                                    throws Exception {
                                    raf.close();
                                }
                                @Override
                                public void operationProgressed(ChannelProgressiveFuture future,
                                    long progress, long total) throws Exception {
                                }});
                        }

                        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        if (!keepAlive) {
                                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
                        }
                    } catch (FileNotFoundException e) {
                        log.error("file {} not found", file.getPath());
                        sendError(ctx, INTERNAL_SERVER_ERROR, e.getMessage());
                    } catch (IOException e) {
                        log.error("file {} has a IOException: {}", file.getName(), e.getMessage());
                        sendError(ctx, INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                }
            }
        }
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

}
