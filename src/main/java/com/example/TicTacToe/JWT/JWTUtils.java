package com.example.TicTacToe.JWT;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtils {

    @Value("${spring.app.secretKey}")
    private String secretKey;
    @Value("${spring.app.expiration}")
    private int jwtExpiration;

    //extract header
    public String extractJWTFromHeader(HttpServletRequest request){
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")){
            return bearer.substring(7);
        }
        return null;
    }

    //MAKE KEY
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    //GENERATE JWT FROM EMAIL
    public String generateJWTFromEmail(UserDetails userDetails){
        String email = userDetails.getUsername();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpiration))
                .signWith(key())
                .compact();
    }
    //EXTRACT EMAIL(VERIFY)
    public String extractEmailFromJWT(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //VALIDATE JWTS
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (SignatureException e) {
            throw new SignatureException("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token is expired");
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("Unsupported JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT claims string is empty: " + e.getMessage());
        }
    }

}
