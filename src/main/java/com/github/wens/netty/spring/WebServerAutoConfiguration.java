package com.github.wens.netty.spring;

import com.github.wens.netty.web.ObjectFactory;
import com.github.wens.netty.web.ServerConfig;
import com.github.wens.netty.web.WebException;
import com.github.wens.netty.web.WebServer;
import com.github.wens.netty.web.impl.DefaultObjectFactory;
import com.github.wens.netty.web.impl.SpringObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author hgh
 * 19-12-10
 */
@Configuration
@EnableConfigurationProperties(WebServerProperties.class)
@ConditionalOnClass(ServerConfig.class)
@ConditionalOnProperty(prefix = "netty.web",value = "enable", matchIfMissing = true)
public class WebServerAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger("netty-server");

    @Autowired
    private WebServerProperties webServerProperties;

    @Bean
    @ConditionalOnMissingBean(ObjectFactory.class)
    public ObjectFactory objectFactory(){
        if(webServerProperties.getObjectFactoryType() == null){
            return new SpringObjectFactory();
        }
        else if("default".equals(webServerProperties.getObjectFactoryType().toLowerCase())){
            return new DefaultObjectFactory();
        }
        else if("spring".equals(webServerProperties.getObjectFactoryType().toLowerCase())){
            log.info("SpringObjectFactory init");
            return new SpringObjectFactory();
        }
        else{
            throw new WebException("ObjectFactoryType shoud be 'spring' or 'default'");
        }
    }

    @Bean
    @ConditionalOnMissingBean(ServerConfig.class)
    public ServerConfig serverConfig(@Autowired ObjectFactory objectFactory){
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setAddr(webServerProperties.getAddr());
        serverConfig.setCharset(webServerProperties.getCharset());
        serverConfig.setContextPath(webServerProperties.getContextPath());
        serverConfig.setExecutorThreads(webServerProperties.getExecutorThreads());
        serverConfig.setWorkerThreads(webServerProperties.getWorkerThreads());
        serverConfig.setPort(webServerProperties.getPort());
        serverConfig.setServerName(webServerProperties.getServerName());
        serverConfig.setScanPackage(webServerProperties.getScanPackage());
        serverConfig.setDownloadFlag(webServerProperties.getDownloadFlag());
        serverConfig.setObjectFactory(objectFactory);
        serverConfig.setCorsSupport(webServerProperties.getCorsSupport());
        serverConfig.setAccessControlAllowCredentials(webServerProperties.getAccessControlAllowCredentials());
        serverConfig.setAccessControlAllowHeaders(webServerProperties.getAccessControlAllowHeaders());
        serverConfig.setAccessControlAllowMethods(webServerProperties.getAccessControlAllowMethods());
        serverConfig.setAccessControlAllowOrigin(webServerProperties.getAccessControlAllowOrigin());
        serverConfig.setImageFlag(webServerProperties.getImageFlag());
        log.info("ServerConfig init");
        log.info("{}",webServerProperties);
        return serverConfig;
    }

    @Bean
    @ConditionalOnMissingBean(WebServer.class)
    public WebServer webServer(@Autowired ServerConfig serverConfig){
        return new WebServer(serverConfig);
    }

    @Bean
    @ConditionalOnMissingBean(WebServerStarter.class)
    public WebServerStarter webServerStarter(@Autowired WebServer webServer){
        return new WebServerStarter(webServer);
    }


}
