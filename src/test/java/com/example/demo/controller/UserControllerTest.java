package com.example.demo.controller;

import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.service.UserService;
import com.example.demo.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    public void userController_RegisterUser_MustReturnCreatedStatusCode() throws Exception
    {
        UserRegisterDTO registerDTO=UserRegisterDTO.builder()
                .email("test@gmail.com")
                .password("saichandu@090")
                .dob(LocalDate.of(2002,8,24))
                .role("ADMIN")
                .firstName("Sai")
                .lastName("Chandu").build();

        RegisterResponseDto registerResponseDto = RegisterResponseDto.builder()
                .userId(1L)
                .role(registerDTO.getRole())
                .email(registerDTO.getEmail()).build();

        ResponseEntity<ResponseStructure<RegisterResponseDto>> response=ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<RegisterResponseDto>().setStatus(HttpStatus.CREATED.value()).setMessage("User registered successfully").setData(registerResponseDto));
        given(userService.registerUser(any(UserRegisterDTO.class))).willReturn(response);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data").value(registerResponseDto));
    }


    @Test
    public void userController_RegisterUser_MustReturnBadRequestStatusCode_ForInvalidBody() throws Exception
    {
        UserRegisterDTO registerDTO=UserRegisterDTO.builder()
                .email("test@gmail.com")
                .password("saichandu@090")
                .dob(LocalDate.of(2032,8,24))
                .role("ADMIN")
                .firstName("Sai")
                .lastName("Chandu").build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()));
    }
}