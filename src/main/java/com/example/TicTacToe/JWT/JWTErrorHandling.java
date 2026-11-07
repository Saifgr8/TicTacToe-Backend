package com.example.TicTacToe.JWT;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
public class JWTErrorHandling implements AuthenticationEntryPoint {
    public static final Logger logger = LoggerFactory.getLogger(JWTErrorHandling.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Auth exception: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final HashMap<String, Object> body = new HashMap<>();
        body.put("STATUS", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("ERROR", HttpStatus.UNAUTHORIZED);
        body.put("MESSAGE", authException.getMessage());
        body.put("PATH", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}