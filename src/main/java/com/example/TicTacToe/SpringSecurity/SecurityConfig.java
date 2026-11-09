package com.example.TicTacToe.SpringSecurity;

import com.example.TicTacToe.JWT.JWTErrorHandling;
import com.example.TicTacToe.JWT.JWTFilter;
import com.example.TicTacToe.JWT.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(proxyTargetClass = true)
public class SecurityConfig {

    private final JWTErrorHandling jwtErrorHandling;
    private final JWTUtils jwtUtils;
    private final MyUserDetailServices myUserDetailServices;

    public SecurityConfig(JWTErrorHandling jwtErrorHandling, JWTUtils jwtUtils, MyUserDetailServices myUserDetailServices) {
        this.jwtErrorHandling = jwtErrorHandling;
        this.jwtUtils = jwtUtils;
        this.myUserDetailServices = myUserDetailServices;
    }

    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter(jwtUtils, myUserDetailServices);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.formLogin(form -> form.disable());

        http.authorizeHttpRequests((auth ->
                auth.requestMatchers("/api/auth/**", "/gameplay/**").permitAll()
                        .anyRequest().authenticated()));

        http.sessionManagement(ses ->
                ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(exe ->
                exe.authenticationEntryPoint(jwtErrorHandling));

        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://tic-tac-twist-nine.vercel.app/"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization")); // ✅ THIS IS NEEDED
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
