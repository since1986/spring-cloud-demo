package com.github.since1986.demo.eureka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ApplicationContext;

@EnableEurekaServer
@SpringBootApplication
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        new BootStarter().startup(() -> {
            ApplicationContext applicationContext = SpringApplication.run(App.class, args);
            for (String activeProfile : applicationContext.getEnvironment().getActiveProfiles()) {
                LOGGER.warn("***Running with profile: {}***", activeProfile);
            }
        });
    }
}