package com.example.extra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExtraApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExtraApplication.class, args);
	}

}
