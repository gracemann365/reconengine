package com.grace.recon.matcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Main entry point for the Matcher Service. */
@SpringBootApplication
@ComponentScan(basePackages = {"com.grace.recon.matcher", "com.grace.recon.common"})
public class MatcherApplication {

  public static void main(String[] args) {
    SpringApplication.run(MatcherApplication.class, args);
  }
}
