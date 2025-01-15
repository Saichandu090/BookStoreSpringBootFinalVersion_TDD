package com.example.demo.mapper;

import com.example.demo.entity.User;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.serviceimpl.JWTService;
import com.example.demo.serviceimpl.MyUserDetailsService;
import com.example.demo.util.ResponseStructure;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper
{
    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    public UserDetails validateUserToken(String authHeader)
    {
        if(authHeader==null)
            return null;
        String token=null;
        String email=null;
        if(authHeader.startsWith("Bearer "))
        {
            token=authHeader.substring(7);
            email=jwtService.extractEmail(token);
        }
        UserDetails userDetails=context.getBean(MyUserDetailsService.class).loadUserByUsername(email);
        if(jwtService.validateToken(token,userDetails))
            return userDetails;
        else
            return null;
    }

    public ResponseEntity<ResponseStructure<RegisterResponse>> userAlreadyExists()
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseStructure<RegisterResponse>()
                .setMessage("User Already Registered")
                .setStatus(HttpStatus.CONFLICT.value())
                .setData(null));
    }

    public User convertFromRegisterDTO(UserRegisterDTO registerDTO)
    {
        return User.builder()
                .email(registerDTO.getEmail())
                .dob(registerDTO.getDob())
                .registeredDate(LocalDate.now())
                .updatedDate(null)
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .role(registerDTO.getRole())
                .password(registerDTO.getPassword()).build();
    }

    public ResponseEntity<ResponseStructure<RegisterResponse>> convertUser(User savedUser)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<RegisterResponse>()
                .setMessage("User "+savedUser.getEmail()+" has registered successfully")
                .setStatus(HttpStatus.CREATED.value())
                .setData(RegisterResponse.builder().userId(savedUser.getUserId()).email(savedUser.getEmail()).role(savedUser.getRole()).build()));
    }

    public ResponseEntity<ResponseStructure<LoginResponseDto>> loginSuccess(String token, String email, String role)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<LoginResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage(token)
                .setData(LoginResponseDto.builder().role(role).email(email).build()));
    }

    public ResponseEntity<ResponseStructure<LoginResponseDto>> userNotExists()
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<LoginResponseDto>()
                .setStatus(HttpStatus.NOT_FOUND.value())
                .setMessage("User not found")
                .setData(null));
    }
}
