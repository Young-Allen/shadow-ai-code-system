package com.shadow.aicodingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiCodingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodingSystemApplication.class, args);
    }

}
