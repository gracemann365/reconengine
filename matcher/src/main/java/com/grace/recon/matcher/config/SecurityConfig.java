package com.grace.recon.matcher.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    // Require ADMIN role for the 'rules' actuator endpoint
                    .requestMatchers("/actuator/rules")
                    .hasRole("ADMIN")
                    // Allow all other actuator endpoints for monitoring
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(withDefaults());
    return http.build();
  }
}
