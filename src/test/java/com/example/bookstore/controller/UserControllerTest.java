package com.example.bookstore.controller;

import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.ResponseStructure;
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
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    public void registerUserMustReturnCreatedStatusCode() throws Exception
    {
        UserRegisterEntity registerDTO= UserRegisterEntity.builder()
                .email("test@gmail.com")
                .password("saichandu@090")
                .dob(LocalDate.of(2002,8,24))
                .role("ADMIN")
                .firstName("Sai")
                .lastName("Chandu").build();

        RegisterResponse registerResponse = RegisterResponse.builder()
                .userId(1L)
                .role(registerDTO.getRole())
                .email(registerDTO.getEmail()).build();

        ResponseEntity<ResponseStructure<RegisterResponse>> response=ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<RegisterResponse>().setStatus(HttpStatus.CREATED.value()).setMessage("User registered successfully").setData(registerResponse));
        given(userService.registerUser(any(UserRegisterEntity.class))).willReturn(response);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.data").value(registerResponse));
    }


    @Test
    public void registerUserMustReturnBadRequestStatusCodeForInvalidBody() throws Exception
    {
        UserRegisterEntity registerDTO= UserRegisterEntity.builder()
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


    @Test
    public void isUserExistsIfExists() throws Exception
    {
        ResponseEntity<ResponseStructure<Boolean>> response=ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>().setStatus(HttpStatus.OK.value()).setMessage("User exists").setData(true));
        when(userService.isUserExists(anyString())).thenReturn(response);

        mockMvc.perform(get("/isUserExists/{email}","sai@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("User exists"));
    }


    @Test
    public void isUserExistsIfNotExists() throws Exception
    {
        ResponseEntity<ResponseStructure<Boolean>> response=ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<Boolean>().setStatus(HttpStatus.NOT_FOUND.value()).setMessage("User not exists").setData(false));
        when(userService.isUserExists(anyString())).thenReturn(response);

        mockMvc.perform(get("/isUserExists/{email}","sai@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(false))
                .andExpect(jsonPath("$.message").value("User not exists"));
    }


    @Test
    public void forgetPasswordIfUserExists() throws Exception
    {
        ResponseEntity<ResponseStructure<Boolean>> response=ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>().setStatus(HttpStatus.OK.value()).setMessage("User password updated successfully").setData(true));
        when(userService.forgetPassword(anyString(),anyString())).thenReturn(response);

        mockMvc.perform(put("/forgetPassword/{email}","sai@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("newPassword","chandu@090")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("User password updated successfully"));
    }


    @Test
    public void forgetPasswordIfUserNotExists() throws Exception
    {
        ResponseEntity<ResponseStructure<Boolean>> response=ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<Boolean>().setStatus(HttpStatus.NOT_FOUND.value()).setMessage("User not found with email").setData(false));
        when(userService.forgetPassword(anyString(),anyString())).thenReturn(response);

        mockMvc.perform(put("/forgetPassword/{email}","sai@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("newPassword","chandu@090")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void forgetPasswordIfParamIsMissing() throws Exception
    {
        mockMvc.perform(put("/forgetPassword/{email}","sai@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingServletRequestParameterException.class,result.getResolvedException()));
    }
}