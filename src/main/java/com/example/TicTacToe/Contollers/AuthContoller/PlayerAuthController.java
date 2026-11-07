package com.example.TicTacToe.Contollers.AuthContoller;

import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOGoogleRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOLoginResponse;
import com.example.TicTacToe.Interfaces.ContollerInterfaces.AuthControllerInterface;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Services.AuthServices.PlayerAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class PlayerAuthController implements AuthControllerInterface {

    private final PlayerAuthService playerAuthService;

    public PlayerAuthController(PlayerAuthService playerAuthService) {
        this.playerAuthService = playerAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<Player> registerPlayerController(
            @Valid @RequestBody DTOAuthRegisterRequest requestDTO) {
        Player registeredPlayer = playerAuthService.registerPlayerService(requestDTO);
        return new ResponseEntity<>(registeredPlayer, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Player> loginPLayerController(@Valid
                                                                     @RequestBody DTOAuthLoginRequest request) {
        DTOLoginResponse response = playerAuthService.loginPlayerService(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + response.getJwt());
        Player player = (Player) response.getPlayer();
        return new ResponseEntity<>(player, headers, HttpStatus.OK);
    }

    @GetMapping("/player/{name}")
    public ResponseEntity<Player> getPlayerByUsernameController(@PathVariable String name) {
        System.out.println("Name is: " + name);
        Player player = playerAuthService.getPlayerByUsernameService(name);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/google/callback")
    public ResponseEntity<Player> loginUserViaGoogle(@RequestBody DTOGoogleRequest request) {
        DTOLoginResponse response = playerAuthService.loginViaGoogle(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + response.getJwt());
        Player player = (Player) response.getPlayer();
        return new ResponseEntity<>(player, headers, HttpStatus.OK);
    }
}
