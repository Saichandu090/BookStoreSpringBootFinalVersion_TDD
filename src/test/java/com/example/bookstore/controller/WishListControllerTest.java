package com.example.bookstore.controller;

import com.example.bookstore.config.JWTFilter;
import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.mapper.WishListMapper;
import com.example.bookstore.requestdto.WishListRequest;
import com.example.bookstore.responsedto.WishListResponse;
import com.example.bookstore.service.WishListService;
import com.example.bookstore.serviceimpl.JWTService;
import com.example.bookstore.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = WishListController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class WishListControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishListService wishListService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private WishListMapper wishListMapper;

    private User user;
    private UserDetails userDetails;
    private WishListRequest wishListRequest;
    private WishListResponse wishListResponse;

    @BeforeEach
    public void init() {
        user = User.builder()
                .email("test@gmail.com")
                .userId(19L)
                .password("test@09090")
                .dob(LocalDate.of(1999, 8, 24))
                .firstName("Test")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        userDetails = new UserDetails()
        {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return user.getPassword();
            }

            @Override
            public String getUsername()
            {
                return user.getEmail();
            }
        };

        wishListRequest = WishListRequest.builder().bookId(1L).build();
        wishListResponse = WishListResponse.builder().wishListId(1L).bookId(wishListRequest.getBookId()).build();
    }



    @Test
    public void addToWishListMustReturnCreatedStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<WishListResponse> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added to wishlist successfully", wishListResponse);
        given(wishListService.addToWishList(anyString(),any(WishListRequest.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(wishListRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseStructure.getMessage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(wishListResponse))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId").value(1));
    }

    @Test
    public void addToWishListTestForMissingHeader() throws Exception
    {
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wishListRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }

    @Test
    public void addToWishListTestForInvalidBody() throws Exception
    {
        WishListRequest wishListRequest1 = WishListRequest.builder().build();
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(wishListRequest1)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()));
    }

    @Test
    public void addToWishListIfBookAlreadyPresent() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<WishListResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book removed from wishlist successfully",null);
        given(wishListService.addToWishList(anyString(),any(WishListRequest.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(wishListRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Book removed from wishlist successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()));
    }


    @Test
    public void getWishListValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<WishListResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"User wishlist fetched successfully",List.of(wishListResponse));
        given(wishListService.getWishList(anyString())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/wishlist/getWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User wishlist fetched successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0]").value(wishListResponse));
    }


    @Test
    public void getWishListIfTokenIsInvalidOrMissing() throws Exception
    {
        mockMvc.perform(get("/wishlist/getWishList")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }
}