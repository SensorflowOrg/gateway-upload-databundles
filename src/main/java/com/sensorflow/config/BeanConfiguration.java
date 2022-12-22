package com.sensorflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeanConfiguration {

    @Bean
    Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
