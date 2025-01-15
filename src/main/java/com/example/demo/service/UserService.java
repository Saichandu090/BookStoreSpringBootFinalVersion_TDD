package com.example.demo.service;

import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.util.ResponseStructure;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface UserService
{
    ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(@Valid UserRegisterDTO registerDTO);

    ResponseEntity<ResponseStructure<LoginResponseDto>> login(@Valid UserLoginDTO loginDTO);
}
