package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.OrderRequest;
import com.example.demo.responsedto.AddressResponse;
import com.example.demo.responsedto.OrderResponse;
import com.example.demo.service.OrderService;
import com.example.demo.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private OrderMapper orderMapper;

    private User user;
    private UserDetails userDetails;
    private User admin;
    private UserDetails adminDetails;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;
    private String token;

    @BeforeEach
    public void init()
    {
        token="Bearer token";

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

        orderRequest = OrderRequest.builder().addressId(1L).build();
        orderResponse = OrderResponse.builder()
                .cancelOrder(false)
                .orderDate(LocalDate.now())
                .orderId(1L)
                .orderQuantity(3)
                .orderPrice(999.99)
                .orderAddress(new AddressResponse(1L,"Baner","Pune","Maharastra",414004))
                .build();
    }


    @Test
    void orderControllerPlaceOrderValidTest() throws Exception
    {
        ResponseEntity<ResponseStructure<OrderResponse>> response=ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<OrderResponse>()
                .setData(orderResponse)
                .setMessage("Order placed successfully")
                .setStatus(HttpStatus.CREATED.value()));
        when(orderService.placeOrder(anyString(),any(OrderRequest.class))).thenReturn(response);
        when(userMapper.validateUserToken(anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/order/placeOrder")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderAddress.addressId").value(1))
                .andExpect(jsonPath("$.data.orderQuantity").value(3))
                .andExpect(jsonPath("$.data.orderId").value(1));
    }

    @Test
    void orderControllerPlaceOrderIfBodyIsInvalid() throws Exception
    {
        orderRequest = OrderRequest.builder().build();
        mockMvc.perform(post("/order/placeOrder")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()));
    }

    @Test
    void orderControllerPlaceOrderIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(post("/order/placeOrder")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void orderControllerCancelOrderValidTest() throws Exception
    {
        ResponseEntity<ResponseStructure<String>> response=ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<String>()
                .setData(null)
                .setMessage("Order cancelled successfully")
                .setStatus(HttpStatus.OK.value()));
        when(orderService.cancelOrder(anyString(),anyLong())).thenReturn(response);
        when(userMapper.validateUserToken(anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/order/cancelOrder/{orderId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }


    @Test
    void orderControllerCancelOrderIfPathVariableIsMissing() throws Exception
    {
        mockMvc.perform(delete("/order/cancelOrder")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()));
    }


    @Test
    void orderControllerCancelOrderIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(delete("/order/cancelOrder/{orderId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void orderControllerGetOrderValidTest() throws Exception
    {
        ResponseEntity<ResponseStructure<OrderResponse>> response=ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<OrderResponse>()
                .setData(orderResponse)
                .setMessage("Order fetched successfully")
                .setStatus(HttpStatus.OK.value()));
        when(orderService.getOrder(anyString(),anyLong())).thenReturn(response);
        when(userMapper.validateUserToken(anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/order/getOrder?orderId={orderId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderAddress.addressId").value(1))
                .andExpect(jsonPath("$.data.orderQuantity").value(3))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.orderPrice").value(999.99));
    }


    @Test
    void orderControllerGetOrderIfRequestParamIsAbsent() throws Exception
    {
        mockMvc.perform(get("/order/getOrder")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingServletRequestParameterException.class,result.getResolvedException()));
    }

    @Test
    void orderControllerGetOrderIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(get("/order/getOrder?orderId={orderId}",1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }


    @Test
    void orderControllerGetAllOrdersValidTest() throws Exception
    {
        ResponseEntity<ResponseStructure<List<OrderResponse>>> response=ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<OrderResponse>>()
                .setData(List.of(orderResponse))
                .setMessage("Order fetched successfully")
                .setStatus(HttpStatus.OK.value()));
        when(orderService.getAllOrdersForUser(anyString())).thenReturn(response);
        when(userMapper.validateUserToken(anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/order/getAllOrders")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderAddress.addressId").value(1))
                .andExpect(jsonPath("$.data[0].orderQuantity").value(3))
                .andExpect(jsonPath("$.data[0].orderId").value(1))
                .andExpect(jsonPath("$.data[0].orderPrice").value(999.99));
    }


    @Test
    void orderControllerGetAllOrdersIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(get("/order/getAllOrders")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()));
    }
}