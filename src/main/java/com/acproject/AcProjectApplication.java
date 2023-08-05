package com.acproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan("com.acproject.mapper")
//EnableSwagger2会扫描当前类所在包以及子包中所有类型中的注解,做swagger文档的定值
@EnableSwagger2
public class AcProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcProjectApplication.class, args);
    }
}
