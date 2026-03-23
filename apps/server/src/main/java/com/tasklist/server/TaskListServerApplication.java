package com.tasklist.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.tasklist.server.**.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class TaskListServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskListServerApplication.class, args);
    }
}
