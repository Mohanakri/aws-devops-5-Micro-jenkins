# src/java-maven-service/src/main/java/com/example/App.java
package com.example;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @GetMapping("/")
  public String home() {
    return "Hi this is from java and maven is build package";
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
