package com.example.TicTacToe.WebSocketConfiguration;

import jakarta.servlet.ServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.web.socket.WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
     String username = null;

     if (request instanceof ServletServerHttpRequest servletRequest){
         username = servletRequest.getServletRequest().getParameter("username");
     }
        if (username == null || username.trim().isEmpty()) {
            username = "anon_" + UUID.randomUUID();
        }

        return new StompPrincipal(username);
    }
}