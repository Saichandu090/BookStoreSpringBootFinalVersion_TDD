package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.serviceimpl.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest
{
    private JWTService jwtService;
    private UserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp()
    {
        jwtService = new JWTService();
        user= User.builder()
                .email("test@example.com")
                .userId(100L)
                .password("Password123")
                .dob(LocalDate.of(1999,8,12))
                .firstName("Mock")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities()
            {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };
    }

    @Test
    void generateSecretKeyShouldGenerateValidKey()
    {
        String secretKey = jwtService.generateSecretKey();
        assertNotNull(secretKey);
        assertTrue(Base64.getDecoder().decode(secretKey).length > 0);
    }

    @Test
    void generateTokenShouldCreateValidToken()
    {
        String token = jwtService.generateToken("test@example.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmailShouldReturnCorrectEmail()
    {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void validateTokenWithValidTokenShouldReturnTrue()
    {
        String token = jwtService.generateToken(userDetails.getUsername());
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateTokenWithExpiredTokenShouldReturnFalse() throws Exception
    {
        Field secretKeyField = JWTService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        String secretKey = (String) secretKeyField.get(jwtService);
        String expiredToken = Jwts.builder()
                .claims()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 1000*60*100))
                .expiration(new Date(System.currentTimeMillis() - 1000*60*10))
                .and()
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                .compact();

        assertThrows(ExpiredJwtException.class,()->jwtService.validateToken(expiredToken, userDetails));
    }

    @Test
    void validateTokenWithDifferentUsernameShouldReturnFalse()
    {
        String token = jwtService.generateToken("different@example.com");
        boolean isValid = jwtService.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void validateTokenWithInvalidTokenShouldThrowException()
    {
        String invalidToken = "invalid.token.string";
        assertThrows(JwtException.class, () -> jwtService.validateToken(invalidToken, userDetails));
    }

    @Test
    void extractClaimShouldExtractCustomClaim()
    {
        String token = jwtService.generateToken("test@example.com");
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() <= System.currentTimeMillis());
    }
}