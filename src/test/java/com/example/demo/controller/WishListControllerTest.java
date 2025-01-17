package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WishListMapper;
import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.service.WishListService;
import com.example.demo.util.ResponseStructure;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = WishListController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class WishListControllerTest
{
    @Autowired
    private MockMvc mockMvc;

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
    private WishListRequestDto wishListRequestDto;
    private WishListResponseDto wishListResponseDto;

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

        wishListRequestDto=WishListRequestDto.builder().bookId(1L).build();
        wishListResponseDto=WishListResponseDto.builder().wishListId(1L).bookId(wishListRequestDto.getBookId()).build();
    }



    @Test
    public void wishListController_AddToWishList_MustReturnCreatedStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<WishListResponseDto> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added to wishlist successfully",wishListResponseDto);
        given(wishListService.addToWishList(anyString(),any(WishListRequestDto.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(wishListRequestDto)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseStructure.getMessage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(wishListResponseDto))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId").value(1));
    }

    @Test
    public void wishListController_AddToWishList_TestForMissingHeader() throws Exception
    {
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wishListRequestDto)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }

    @Test
    public void wishListController_AddToWishList_TestForInvalidBody() throws Exception
    {
        WishListRequestDto wishListRequestDto1=WishListRequestDto.builder().build();
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(wishListRequestDto1)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()));
    }

    @Test
    public void wishListController_AddToWishListWishList_IfBookAlreadyPresent() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<WishListResponseDto> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book removed from wishlist successfully",null);
        given(wishListService.addToWishList(anyString(),any(WishListRequestDto.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/wishlist/addToWishList")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(wishListRequestDto)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Book removed from wishlist successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()));
    }
}