package com.yd.vibecode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableConfigurationProperties
public class VibecodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibecodeApplication.class, args);
	}

}
