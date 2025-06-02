package com.chinmay.GPSService1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GpsService1Application {

	public static void main(String[] args) {
		SpringApplication.run(GpsService1Application.class, args);
	}
}
