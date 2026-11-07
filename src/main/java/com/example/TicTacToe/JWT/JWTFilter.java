package com.example.TicTacToe.JWT;

import com.example.TicTacToe.SpringSecurity.MyUserDetailServices;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final MyUserDetailServices myUserDetailServices;

    public JWTFilter(JWTUtils jwtUtils, MyUserDetailServices myUserDetailServices) {
        this.jwtUtils = jwtUtils;
        this.myUserDetailServices = myUserDetailServices;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            //extract jwt
            String token = jwtUtils.extractJWTFromHeader(request);
            if(token != null && jwtUtils.validateJwtToken(token)){
                //extract email
                String email = jwtUtils.extractEmailFromJWT(token);
                UserDetails userDetails = myUserDetailServices.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        filterChain.doFilter(request,response);
    }
}
