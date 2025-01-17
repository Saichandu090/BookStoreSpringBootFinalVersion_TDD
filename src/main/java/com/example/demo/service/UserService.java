package com.example.demo.service;

import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.util.ResponseStructure;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface UserService
{
    ResponseEntity<ResponseStructure<RegisterResponseDto>> registerUser(UserRegisterDTO registerDTO);

    ResponseEntity<ResponseStructure<LoginResponseDto>> login(UserLoginDTO loginDTO);
}
