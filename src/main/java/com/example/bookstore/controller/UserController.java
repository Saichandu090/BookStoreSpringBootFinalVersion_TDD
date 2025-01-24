package com.example.bookstore.controller;

import com.example.bookstore.requestdto.NewPasswordRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.ResponseStructure;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@AllArgsConstructor
@RestController
public class UserController
{
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(@Valid @RequestBody UserRegisterEntity registerDTO)
    {
        return userService.registerUser(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<LoginResponse>> login(@Valid @RequestBody UserLoginEntity loginDTO)
    {
        return userService.login(loginDTO);
    }

    @GetMapping("/isUserExists/{email}")
    public ResponseEntity<ResponseStructure<Boolean>> isUserExists(@PathVariable String email)
    {
        return userService.isUserExists(email);
    }

    @PutMapping("/forgetPassword")
    public ResponseEntity<ResponseStructure<Boolean>> forgetPassword(
            @RequestBody NewPasswordRequest newPasswordRequest)
    {
        return userService.forgetPassword(newPasswordRequest);
    }
}
