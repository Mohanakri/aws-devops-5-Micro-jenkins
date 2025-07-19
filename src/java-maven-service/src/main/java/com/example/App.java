package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("hi hello this is from java maven");
        System.out.println("Running on Java " + Runtime.version());
        System.out.println("Application: java-maven-service");
        System.out.println("Version: 1.0.0");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Status: Running successfully!");
    }
}
