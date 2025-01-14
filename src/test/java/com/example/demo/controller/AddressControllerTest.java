package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AddressControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private UserMapper userMapper;

    private String token;
    private User user;
    private UserDetails userDetails;
    private User admin;
    private UserDetails adminDetails;
    private AddressRequestDto addressRequestDto;
    private AddressResponseDto addressResponseDto;

    @BeforeEach
    public void init()
    {
        token="Bearer token";

        addressRequestDto=AddressRequestDto.builder()
                .streetName("Banner")
                .city("Pune")
                .state("Maharashtra")
                .pinCode(414004).build();

        addressResponseDto=AddressResponseDto.builder()
                .addressId(1L)
                .streetName(addressRequestDto.getStreetName())
                .city(addressRequestDto.getCity())
                .state(addressRequestDto.getState())
                .pinCode(addressRequestDto.getPinCode()).build();

        user=User.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .userId(1L)
                .email("chandu@gmail.com")
                .password("chandu1234")
                .dob(LocalDate.of(2002,8,24))
                .role("USER").build();

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };

        admin=User.builder()
                .firstName("M.Sai")
                .lastName("Chandu")
                .userId(1L)
                .email("chandu@gmail.com")
                .password("chandu1234")
                .dob(LocalDate.of(2002,8,24))
                .role("ADMIN").build();

        adminDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(admin.getRole()));
            }

            @Override
            public String getPassword() {
                return admin.getPassword();
            }

            @Override
            public String getUsername() {
                return admin.getEmail();
            }
        };
    }


    //====================================================//

    @Test
    public void addressController_AddAddress_MustReturnOkStatusCode() throws Exception
    {
        ResponseStructure<AddressResponseDto> response=new ResponseStructure<>(HttpStatus.CREATED.value(),"Address added successfully",addressResponseDto);
        given(addressService.addAddress(ArgumentMatchers.anyString(),ArgumentMatchers.any(AddressRequestDto.class))).willReturn(new ResponseEntity<>(response,HttpStatus.CREATED));
        given(userMapper.validateUserToken(ArgumentMatchers.anyString())).willReturn(userDetails);

        mockMvc.perform(post("/address/addAddress")
                .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                .header("Authorization",token)
                .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(addressResponseDto))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.addressId").value(addressResponseDto.getAddressId()));
    }

    @Test
    public void addressController_AddAddress_MustReturnUnauthorizedStatusCode() throws Exception
    {
        given(userMapper.validateUserToken(ArgumentMatchers.anyString())).willReturn(adminDetails);

        mockMvc.perform(post("/address/addAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }

    //============================================//

    @Test
    public void addressController_EditAddress_MustReturnOkStatusCode() throws Exception
    {
        ResponseStructure<AddressResponseDto> response=new ResponseStructure<>(HttpStatus.OK.value(),"Address edited successfully",addressResponseDto);
        given(addressService.updateAddress(ArgumentMatchers.anyString(),ArgumentMatchers.anyLong(),ArgumentMatchers.any(AddressRequestDto.class))).willReturn(new ResponseEntity<>(response,HttpStatus.OK));
        given(userMapper.validateUserToken(ArgumentMatchers.anyString())).willReturn(userDetails);

        mockMvc.perform(put("/address/editAddress/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(addressResponseDto))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.addressId").value(addressResponseDto.getAddressId()));
    }

    @Test
    public void addressController_EditAddress_MustReturnUnauthorizedStatusCode() throws Exception
    {
        given(userMapper.validateUserToken(ArgumentMatchers.anyString())).willReturn(adminDetails);

        mockMvc.perform(put("/address/editAddress/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }
}