package com.example.bookstore.service;

import com.example.bookstore.requestdto.NewPasswordRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface UserService
{
    ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(UserRegisterEntity registerDTO);

    ResponseEntity<ResponseStructure<LoginResponse>> login(UserLoginEntity loginDTO);

    ResponseEntity<ResponseStructure<Boolean>> isUserExists(String email);

    ResponseEntity<ResponseStructure<Boolean>> forgetPassword(NewPasswordRequest newPasswordRequest);
}
