package com.demo.testcase.webapi;

import com.demo.testcase.webapi.config.ApplicationProperties;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.demo.testcase")
@EnableConfigurationProperties(ApplicationProperties.class)
public class WebApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}
}
