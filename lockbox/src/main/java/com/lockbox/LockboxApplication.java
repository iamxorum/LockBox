package com.lockbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients
public class LockboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(LockboxApplication.class, args);
	}

}
