package com.example.TicTacToe.Contollers;

import com.example.TicTacToe.Contollers.AuthContoller.PlayerAuthController;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOGoogleRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOLoginResponse;
import com.example.TicTacToe.JWT.JWTErrorHandling;
import com.example.TicTacToe.JWT.JWTUtils;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Services.AuthServices.PlayerAuthService;
import com.example.TicTacToe.SpringSecurity.MyUserDetailServices;
import com.example.TicTacToe.SpringSecurity.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerAuthController.class)
@Import({SecurityConfig.class, JWTErrorHandling.class})
class PlayerLookupSecurityTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PlayerAuthService playerAuthService;
    @MockitoBean
    private JWTUtils jwtUtils;
    @MockitoBean
    private MyUserDetailServices userDetailsService;

    @BeforeEach
    void setUp() {
        when(jwtUtils.extractJWTFromHeader(any())).thenAnswer(invocation -> {
            var request = (jakarta.servlet.http.HttpServletRequest) invocation.getArgument(0);
            String header = request.getHeader("Authorization");
            return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        });
        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.extractEmailFromJWT("valid-token")).thenReturn("test@example.com");
        UserDetails user = User.withUsername("test@example.com").password("unused").authorities("USER").build();
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(user);
    }

    @Test
    void lookupRequiresAuthenticatedUser() throws Exception {
        mvc.perform(get("/api/auth/player/test")).andExpect(status().isUnauthorized());
        verifyNoInteractions(playerAuthService);
    }

    @Test
    void validBearerTokenAllowsLookup() throws Exception {
        when(playerAuthService.getPlayerByUsernameService("test"))
                .thenReturn(new Player("test@example.com", "test", "unused"));

        mvc.perform(get("/api/auth/player/test").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
        verify(playerAuthService).getPlayerByUsernameService("test");
    }

    @Test
    void accessDeniedIsNotConvertedToInternalServerError() throws Exception {
        when(playerAuthService.getPlayerByUsernameService("test"))
                .thenThrow(new AccessDeniedException("Denied"));
        mvc.perform(get("/api/auth/player/test").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicAuthEndpointsRemainReachableWithoutToken() throws Exception {
        Player player = new Player("test@example.com", "test", "unused");
        when(playerAuthService.registerPlayerService(any(DTOAuthRegisterRequest.class)))
                .thenReturn(player);
        when(playerAuthService.loginPlayerService(any(DTOAuthLoginRequest.class)))
                .thenReturn(new DTOLoginResponse("test-token", player));
        when(playerAuthService.loginViaGoogle(any(DTOGoogleRequest.class)))
                .thenReturn(new DTOLoginResponse("test-token", player));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"username\":\"test\",\"password\":\"password\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/google/callback")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"googleAccessToken\":\"invalid\"}"))
                .andExpect(status().isOk());
        verify(playerAuthService).loginPlayerService(any(DTOAuthLoginRequest.class));
        verify(playerAuthService).loginViaGoogle(any(DTOGoogleRequest.class));
    }
}
