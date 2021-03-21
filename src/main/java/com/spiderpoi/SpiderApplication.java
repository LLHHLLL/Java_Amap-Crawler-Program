package com.spiderpoi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(value = "com.spiderpoi.spider.dao")
public class SpiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpiderApplication.class, args);
    }

}
