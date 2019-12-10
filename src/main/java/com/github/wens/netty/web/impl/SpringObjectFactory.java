package com.github.wens.netty.web.impl;

import com.github.wens.netty.web.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * integrated with spring
 * @author  hgh
 * 19-12-10.
 */
public class SpringObjectFactory implements ObjectFactory,ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger("netty-server");

    protected ApplicationContext appContext;

    @Override
    public Object instance(Class bean, boolean singleton) {
        return appContext.getBean(bean);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
