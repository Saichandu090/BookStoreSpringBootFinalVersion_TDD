package com.example.demo.controller;

import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.service.UserService;
import com.example.demo.util.ResponseStructure;
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
    public ResponseEntity<ResponseStructure<RegisterResponseDto>> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO)
    {
        return userService.registerUser(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<LoginResponseDto>> login(@Valid @RequestBody UserLoginDTO loginDTO)
    {
        return userService.login(loginDTO);
    }

    @GetMapping("/isUserExists/{email}")
    public ResponseEntity<ResponseStructure<Boolean>> isUserExists(@PathVariable String email)
    {
        return userService.isUserExists(email);
    }

    @PutMapping("/forgetPassword/{email}")
    public ResponseEntity<ResponseStructure<Boolean>> forgetPassword(
            @PathVariable String email,
            @RequestParam String newPassword)
    {
        return userService.forgetPassword(email,newPassword);
    }
}
