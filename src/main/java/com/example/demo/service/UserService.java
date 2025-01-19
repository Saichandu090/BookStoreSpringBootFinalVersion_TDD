package com.example.demo.service;

import com.example.demo.requestdto.UserLogin;
import com.example.demo.requestdto.UserRegister;
import com.example.demo.responsedto.LoginResponse;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface UserService
{
    ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(UserRegister registerDTO);

    ResponseEntity<ResponseStructure<LoginResponse>> login(UserLogin loginDTO);

    ResponseEntity<ResponseStructure<Boolean>> isUserExists(String email);

    ResponseEntity<ResponseStructure<Boolean>> forgetPassword(String email,String newPassword);
}
