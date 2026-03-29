package com.alvin.bookingsystem.config;

import com.alvin.bookingsystem.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/swagger-ui/index.html",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/swagger-ui.html/**"
                ).permitAll()
                
                .requestMatchers("/actuator/**").permitAll()
                
                .requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/api/users/verify-email",
                    "/api/users/resend-verification",
                    "/api/users/forgot-password",
                    "/api/users/reset-password"
                ).permitAll()

                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
