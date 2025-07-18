# src/java-gradle-service/src/main/java/com/example/App.java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    @GetMapping("/")
    public String home() {
        return "Hi this is from java and gradle is build package";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
