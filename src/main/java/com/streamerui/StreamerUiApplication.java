package com.streamerui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StreamerUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamerUiApplication.class, args);
    }
}
