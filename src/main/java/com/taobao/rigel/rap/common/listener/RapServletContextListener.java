package com.taobao.rigel.rap.common.listener;

import com.taobao.rigel.rap.common.utils.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Bosn Ma on 15/9/7.
 */
public class RapServletContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(RapServletContextListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("RAP Server initializing...");

        logger.info("Initializing Jedis Server...");
        CacheUtils.init();

        logger.info("RAP Server ready.");
    }


    public void contextDestroyed(ServletContextEvent arg0) {
        logger.info("Context destroyed.");
    }
}
