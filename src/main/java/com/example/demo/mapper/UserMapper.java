package com.example.demo.mapper;

import com.example.demo.responsedto.JsonResponseDTO;
import com.example.demo.responsedto.LoginResponseDTO;
import com.example.demo.serviceimpl.JWTService;
import com.example.demo.serviceimpl.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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
        String token=null;
        String email=null;
        if(authHeader!=null && authHeader.startsWith("Bearer "))
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

    public JsonResponseDTO userDetailsFailure()
    {
        return JsonResponseDTO.builder()
                .result(false)
                .message("Invalid User Details")
                .data(null).build();
    }

    public JsonResponseDTO noAuthority()
    {
        return JsonResponseDTO.builder()
                .result(false)
                .message("No Authority")
                .data(null).build();
    }

    public JsonResponseDTO userAlreadyExists()
    {
        return JsonResponseDTO.builder()
                .result(false)
                .message("User Already Exists")
                .data(null).build();
    }

    public JsonResponseDTO userNotExists()
    {
        return JsonResponseDTO.builder()
                .result(false)
                .message("User not Exists")
                .data(null).build();
    }

    public JsonResponseDTO loginSuccess(String token,String email,String role)
    {
        List<LoginResponseDTO> list=new ArrayList<>();
        list.add(new LoginResponseDTO(email,role));

        return JsonResponseDTO.builder()
                .result(true)
                .message(token)
                .data(list).build();
    }
}
