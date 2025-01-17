package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.service.CartService;
import com.example.demo.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class CartControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private CartMapper cartMapper;

    private User user;
    private User admin;
    private UserDetails userDetails;
    private UserDetails adminDetails;
    private CartRequestDto cartRequestDto;
    private CartResponseDto cartResponseDto;

    @BeforeEach
    public void init()
    {
        user= User.builder()
                .email("test@gmail.com")
                .userId(100L)
                .password("test@90909")
                .dob(LocalDate.of(1999,8,12))
                .firstName("Mock")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        admin=User.builder()
                .email("sai@gmail.com")
                .userId(1L)
                .password("saichandu090")
                .dob(LocalDate.of(2002,8,24))
                .firstName("Sai")
                .lastName("Chandu")
                .role("ADMIN")
                .registeredDate(LocalDate.now()).build();

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

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities()
            {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };

        cartRequestDto=CartRequestDto.builder().bookId(1L).build();
        cartResponseDto=CartResponseDto.builder().cartId(1L).bookId(cartRequestDto.getBookId()).cartQuantity(1).build();
    }


    @Test
    void cartController_AddToCart_ValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<CartResponseDto> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book added to cart successfully",cartResponseDto);
        when(cartService.addToCart(ArgumentMatchers.anyString(),ArgumentMatchers.any(CartRequestDto.class))).thenReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/cart/addToCart")
                        .header("Authorization",token)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookId").value(1))
                .andExpect(jsonPath("$.data.cartQuantity").value(1))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()));
    }

    @Test
    void cartController_AddToCart_IfTokenIsMissing() throws Exception
    {
        mockMvc.perform(post("/cart/addToCart")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }

    @Test
    void cartController_AddToCart_IfBodyIsInvalid() throws Exception
    {
        String token="Bearer token";
        CartRequestDto dto=new CartRequestDto();
        mockMvc.perform(post("/cart/addToCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()));
    }

    @Test
    void cartController_AddToCart_IfNoAuthority() throws Exception
    {
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);
        String token="Bearer token";
        mockMvc.perform(post("/cart/addToCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    void cartController_RemoveFromCart_ValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<CartResponseDto> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book removed from cart successfully",null);
        when(cartService.removeFromCart(ArgumentMatchers.anyString(),ArgumentMatchers.anyLong())).thenReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/cart/removeFromCart/{cartId}",1)
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Book removed from cart successfully"));
    }


    @Test
    void cartController_RemoveFromCart_IfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(delete("/cart/removeFromCart/{cartId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void cartController_RemoveFromCart_IfPathVariableIsMissing() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/cart/removeFromCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()));
    }
}