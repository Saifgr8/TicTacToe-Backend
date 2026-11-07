package com.example.TicTacToe.WebSocketConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private ActiveUsersService activeUsersService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = accessor.getUser().getName(); // ✅ Uses StompPrincipal
        String sessionId = accessor.getSessionId();

        activeUsersService.register(username, sessionId);
        System.out.println("User connected: " + username + " [" + sessionId + "]");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        activeUsersService.unregister(sessionId);
        System.out.println("User disconnected: " + sessionId);
    }
}