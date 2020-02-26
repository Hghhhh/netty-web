package com.github.wens.netty.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hgh
 * 19-12-10
 */
@ConfigurationProperties(prefix = "netty.web")
public class WebServerProperties {
    private String serverName = "netty-web-server";

    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;

    private int executorThreads = Runtime.getRuntime().availableProcessors() * 2;

    private String addr = "127.0.0.1";

    private int port = 8080;

    private String charset = "UTF-8";

    private String contextPath = "";

    private String objectFactoryType = "spring";

    private String scanPackage = "";

    private String downloadFlag = "download";

    private String imageFlag = "image";

    private boolean corsSupport = false;

    private String accessControlAllowOrigin = "*";

    private String accessControlAllowCredentials = "false";

    private String accessControlAllowMethods = "GET,PUT,POST,DELETE,OPTIONS";

    private String accessControlAllowHeaders = "Content-Type,Access-Control-Allow-Headers,Authorization,X-Requested-With";

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getExecutorThreads() {
        return executorThreads;
    }

    public void setExecutorThreads(int executorThreads) {
        this.executorThreads = executorThreads;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getObjectFactoryType() {
        return objectFactoryType;
    }

    public void setObjectFactoryType(String objectFactoryType) {
        this.objectFactoryType = objectFactoryType;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public String getDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(String downloadFlag) {
        this.downloadFlag = downloadFlag;
    }

    public String getAccessControlAllowOrigin() {
        return accessControlAllowOrigin;
    }

    public void setAccessControlAllowOrigin(String accessControlAllowOrigin) {
        this.accessControlAllowOrigin = accessControlAllowOrigin;
    }

    public String getAccessControlAllowCredentials() {
        return accessControlAllowCredentials;
    }

    public void setAccessControlAllowCredentials(String accessControlAllowCredentials) {
        this.accessControlAllowCredentials = accessControlAllowCredentials;
    }

    public String getAccessControlAllowMethods() {
        return accessControlAllowMethods;
    }

    public void setAccessControlAllowMethods(String accessControlAllowMethods) {
        this.accessControlAllowMethods = accessControlAllowMethods;
    }

    public String getAccessControlAllowHeaders() {
        return accessControlAllowHeaders;
    }

    public void setAccessControlAllowHeaders(String accessControlAllowHeaders) {
        this.accessControlAllowHeaders = accessControlAllowHeaders;
    }

    public boolean getCorsSupport() {
        return corsSupport;
    }

    public void setCorsSupport(boolean corsSupport) {
        this.corsSupport = corsSupport;
    }

    @Override
    public String toString() {
        return "WebServerProperties{" +
                "serverName='" + serverName + '\'' +
                ", workerThreads=" + workerThreads +
                ", executorThreads=" + executorThreads +
                ", addr='" + addr + '\'' +
                ", port=" + port +
                ", charset='" + charset + '\'' +
                ", contextPath='" + contextPath + '\'' +
                ", objectFactoryType='" + objectFactoryType + '\'' +
                ", scanPackage='" + scanPackage + '\'' +
                ", downloadFlag='" + downloadFlag + '\'' +
                ", corsSupport=" + corsSupport +
                ", accessControlAllowOrigin='" + accessControlAllowOrigin + '\'' +
                ", accessControlAllowCredentials='" + accessControlAllowCredentials + '\'' +
                ", accessControlAllowMethods='" + accessControlAllowMethods + '\'' +
                ", accessControlAllowHeaders='" + accessControlAllowHeaders + '\'' +
                '}';
    }

    public String getImageFlag() {
        return imageFlag;
    }

    public void setImageFlag(String imageFlag) {
        this.imageFlag = imageFlag;
    }
}
