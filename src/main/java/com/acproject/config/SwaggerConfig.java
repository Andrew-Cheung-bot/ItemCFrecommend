package com.acproject.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
public class SwaggerConfig {

    @Bean
    //Docket是Swagger中的全局配置对象
    public Docket docket() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        //API帮助文档的描述信息.information
        ApiInfo apiInfo =
                new ApiInfoBuilder()
                        .contact( //配置swagger文档主体内容
                                new Contact(
                                        "Andrew Cheung", null, "dg8882219@gmail.com"
                                )
                        )
                        .title("ACProject开发文档")
                        .description("电子图书管理系统Api文档")
                        .version("1.0.0")
                        .build();
        docket.apiInfo(apiInfo);


        return docket;
    }
}
