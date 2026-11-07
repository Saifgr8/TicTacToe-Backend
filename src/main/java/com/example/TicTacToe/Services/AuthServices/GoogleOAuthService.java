package com.example.TicTacToe.Services.AuthServices;

import com.example.TicTacToe.DTOs.AuthDTOs.DTOGoogleRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {
    private final RestTemplate template;
    private final ObjectMapper objectMapper;

    public GoogleOAuthService(RestTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public static class GoogleUserInfo {
        private String googleId;
        private String email;
        private String name;
        private String picture;

        public GoogleUserInfo(String googleId, String picture, String name, String email) {
            this.googleId = googleId;
            this.picture = picture;
            this.name = name;
            this.email = email;
        }

        public String getGoogleId() {
            return googleId;
        }

        public void setGoogleId(String googleId) {
            this.googleId = googleId;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public GoogleUserInfo verifyGoogleAccessTokenAndGetUserInfo(DTOGoogleRequest request) {
        String token = request.getGoogleAccessToken();
        String googleTokenInfoURL = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/oauth2/v3/userinfo")
                .queryParam("access_token", token)
                .toUriString();

        try {
            String response = template.getForObject(googleTokenInfoURL, String.class);
            JsonNode root = objectMapper.readTree(response);

            String googleId = root.path("sub").asText();
            String email = root.path("email").asText();
            String name = root.path("name").asText();
            String picture = root.path("picture").asText();

            if (email == null || googleId == null) {
                throw new RuntimeException("Missing required fields from Google user info.");
            }
            return new GoogleUserInfo(googleId, picture, name, email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token or fetch user info.", e);
        }
    }

}
