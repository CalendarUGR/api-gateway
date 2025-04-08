package com.calendarugr.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
