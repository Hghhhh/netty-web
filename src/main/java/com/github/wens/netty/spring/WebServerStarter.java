package com.github.wens.netty.spring;

import com.github.wens.netty.web.WebServer;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author hgh
 * 19-12-10
 */
public class WebServerStarter implements ApplicationListener<ApplicationStartedEvent> {

    private WebServer webServer;

    public WebServerStarter(WebServer webServer){
        super();
        this.webServer = webServer;
    }

    /**
     * SpringBoot启动后启动WebServer
     * @param applicationStartedEvent
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        webServer.scanRouters(webServer.getServerConfig().getScanPackage());
        webServer.run();
    }
}
