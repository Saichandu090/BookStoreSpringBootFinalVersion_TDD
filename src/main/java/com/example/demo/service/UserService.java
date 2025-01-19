package com.example.demo.service;

import com.example.demo.requestdto.UserLoginEntity;
import com.example.demo.requestdto.UserRegisterEntity;
import com.example.demo.responsedto.LoginResponse;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface UserService
{
    ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(UserRegisterEntity registerDTO);

    ResponseEntity<ResponseStructure<LoginResponse>> login(UserLoginEntity loginDTO);

    ResponseEntity<ResponseStructure<Boolean>> isUserExists(String email);

    ResponseEntity<ResponseStructure<Boolean>> forgetPassword(String email,String newPassword);
}
