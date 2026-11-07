package com.example.TicTacToe.DTOs.AuthDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DTOAuthRegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 12, message = "Password must be between 6 and 12 characters")
    private String password;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 10, message = "Username must be between 4 and 10 characters")
    private String username;

    public DTOAuthRegisterRequest() {
    }

    public DTOAuthRegisterRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
