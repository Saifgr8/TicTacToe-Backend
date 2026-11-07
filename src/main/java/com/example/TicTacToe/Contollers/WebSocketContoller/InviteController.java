package com.example.TicTacToe.Contollers.WebSocketContoller;

import com.example.TicTacToe.DTOs.GameDTOs.DTOGameInvite;
import com.example.TicTacToe.WebSocketConfiguration.ActiveUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ActiveUsersService activeUsersService;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @PostMapping
    public ResponseEntity<?> sendInvite(@RequestBody DTOGameInvite gameInvite) {

        System.out.println("Connected users:");
        simpUserRegistry.getUsers().forEach(user -> {
            System.out.println("  • " + user.getName());
        });

        System.out.println("INVITE DTO IS: " + gameInvite);
        String toUser = gameInvite.getTo();

        if (activeUsersService.isUserOnline(toUser)) {
            messagingTemplate.convertAndSendToUser(
                    toUser, "/queue/invites", gameInvite
            );
            return ResponseEntity.ok("Invite Sent");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User is not online");
        }
    }

    @MessageMapping("/invite/accept") // Changed endpoint for clarity
    public void handleInviteAccept(DTOGameInvite invite, Principal principal) {
        // Send ACCEPTED status to the inviter
        messagingTemplate.convertAndSendToUser(
                invite.getFrom(), "/queue/lobby",
                Map.of("status", "INVITE_ACCEPTED", "from", invite.getTo(), "by", principal.getName())
        );
        // Send ACCEPTED status to the invitee (confirming their acceptance)
        messagingTemplate.convertAndSendToUser(
                invite.getTo(), "/queue/lobby",
                Map.of("status", "INVITE_ACCEPTED", "from", invite.getFrom(), "by", principal.getName())
        );
    }

    private final Map<String, Boolean> playerReadyState = new HashMap<>();

    @MessageMapping("/lobby/ready")
    public void handleReadyState(DTOGameInvite invite, Principal principal) {
        String currentPlayer = principal.getName();
        String opponentPlayer = invite.getTo().equals(currentPlayer) ? invite.getFrom() : invite.getTo();

        System.out.println("inviting current player: "+ currentPlayer);


        //mark current player as ready
        playerReadyState.put(currentPlayer + "_" + opponentPlayer, true);

        //inform current player that their state is acknowledged.
        messagingTemplate.convertAndSendToUser(
                currentPlayer, "/queue/lobby",
                Map.of("status", "YOU_ARE_READY")
        );
        //inform opponent that current player is ready
        messagingTemplate.convertAndSendToUser(
                opponentPlayer, "queue/lobby",
                Map.of("status", "OPPONENT_READY", "opponent", opponentPlayer)
        );

        //check if both players are ready
        boolean currentPlayerReady = playerReadyState.getOrDefault(currentPlayer + "_" + opponentPlayer, false);
        boolean opponentPlayerReady = playerReadyState.getOrDefault(opponentPlayer + "_" + currentPlayer, false);

        //if both are ready send start message
        if (currentPlayerReady && opponentPlayerReady){
            messagingTemplate.convertAndSendToUser(
                    currentPlayer, "/queue/lobby",
                    Map.of("status", "GAME_START", "opponent", opponentPlayer)
            );
            messagingTemplate.convertAndSendToUser(
                    opponentPlayer, "/queue/lobby",
                    Map.of("status", "GAME_START", "opponent", currentPlayer)
            );
            playerReadyState.remove(currentPlayer + "_" + opponentPlayer);
            playerReadyState.remove(opponentPlayer + "_" + currentPlayer);
        }

    }

//    @MessageMapping("/lobby/ready")
//    public void handleReadyState(DTOGameInvite invite, Principal principal) {
//        messagingTemplate.convertAndSendToUser(
//                invite.getFrom(), "/queue/lobby",
//                Map.of("inviteStatus", "OPPONENT_READY", "from", invite.getTo())
//        );
//        messagingTemplate.convertAndSendToUser(
//                invite.getTo(), "/queue/lobby",
//                Map.of("inviteStatus", "READY", "from", invite.getFrom())
//        );
//    }

}
