package com.project.arbaz.aaspass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Creating a bean which manage by spring
    @Bean
    public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                req -> req
                        .requestMatchers(HttpMethod.GET , "/request").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/request/add").authenticated()
                        .requestMatchers(HttpMethod.GET, "/session-debug").authenticated()
                        .anyRequest().authenticated()
        )
                .oauth2Login(oauth -> {
                    // Spring Security handles the Google OIDC flow.
                });
        return http.build();
    }
}
