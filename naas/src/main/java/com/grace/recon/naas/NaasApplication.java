package com.grace.recon.naas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.grace.recon.naas", "com.grace.recon.common"})
public class NaasApplication {

  public static void main(String[] args) {
    SpringApplication.run(NaasApplication.class, args);
  }
}
