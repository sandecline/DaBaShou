package com.dabashou.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 搭把手应用启动类
 */
@SpringBootApplication(scanBasePackages = "com.dabashou")
@MapperScan("com.dabashou.**.mapper")
@EnableScheduling
public class DabashouApplication {

    public static void main(String[] args) {
        SpringApplication.run(DabashouApplication.class, args);
    }
}
