package com.example.bookstore.controller;

import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.CartMapper;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.requestdto.CartRequest;
import com.example.bookstore.responsedto.CartResponse;
import com.example.bookstore.service.CartService;
import com.example.bookstore.util.ResponseStructure;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private CartRequest cartRequest;
    private CartResponse cartResponse;

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

        cartRequest = CartRequest.builder().bookId(1L).build();
        cartResponse = CartResponse.builder().cartId(1L).bookId(cartRequest.getBookId()).cartQuantity(1).build();
    }


    @Test
    void addToCartValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<CartResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book added to cart successfully", cartResponse);
        when(cartService.addToCart(ArgumentMatchers.anyString(),ArgumentMatchers.any(CartRequest.class))).thenReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/cart/addToCart")
                        .header("Authorization",token)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookId").value(1))
                .andExpect(jsonPath("$.data.cartQuantity").value(1))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
    }

    @Test
    void addToCartIfTokenIsMissing() throws Exception
    {
        mockMvc.perform(post("/cart/addToCart")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }

    @Test
    void addToCartIfBodyIsInvalid() throws Exception
    {
        String token="Bearer token";
        CartRequest dto=new CartRequest();
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
    void addToCartIfNoAuthority() throws Exception
    {
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);
        String token="Bearer token";
        mockMvc.perform(post("/cart/addToCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    void removeFromCartValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<CartResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book removed from cart successfully",null);
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
    void removeFromCartIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(delete("/cart/removeFromCart/{cartId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void removeFromCartIfPathVariableIsMissing() throws Exception
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


    @Test
    void getCartValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<CartResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"User cart fetched successfully",List.of(cartResponse));
        when(cartService.getCartItems(ArgumentMatchers.anyString())).thenReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/cart/getCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].cartId").value(1))
                .andExpect(jsonPath("$.data[0]").value(cartResponse));
    }

    @Test
    void getCartIfTokenIsMissing() throws Exception
    {
        mockMvc.perform(get("/cart/getCart")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }

    @Test
    void getCartIfTokenIsInvalid() throws Exception
    {
        String token="jwt";
        when(userMapper.validateUserToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/cart/getCart")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }


    @Test
    void clearCartValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<CartResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"User cart cleared successfully",null);
        when(cartService.clearCart(ArgumentMatchers.anyString())).thenReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/cart/clearCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User cart cleared successfully"));
    }


    @Test
    void clearCartIfTokenIsMissing() throws Exception
    {
        mockMvc.perform(delete("/cart/clearCart")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void clearCartIfTokenIsInvalid() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(null);

        mockMvc.perform(delete("/cart/clearCart")
                        .header("Authorization",token)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}