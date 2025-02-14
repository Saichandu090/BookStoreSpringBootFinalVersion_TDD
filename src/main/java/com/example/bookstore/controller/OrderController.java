package com.example.bookstore.controller;

import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.requestdto.OrderRequest;
import com.example.bookstore.responsedto.OrderResponse;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.util.ResponseStructure;
import com.example.bookstore.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController
{
    private OrderService orderService;
    private UserMapper userMapper;
    private final OrderMapper orderMapper=new OrderMapper();
    private static final String HEADER="Authorization";

    @PostMapping("/placeOrder")
    public ResponseEntity<ResponseStructure<OrderResponse>> placeOrder(
            @RequestHeader(value = HEADER)String authHeader,
            @Valid @RequestBody OrderRequest orderRequest)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.placeOrder(userDetails.getUsername(), orderRequest);
        }
        return orderMapper.noAuthority();
    }


    @DeleteMapping("/cancelOrder/{orderId}")
    public ResponseEntity<ResponseStructure<OrderResponse>> cancelOrder(
            @RequestHeader(value = HEADER)String authHeader,
            @PathVariable Long orderId)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.cancelOrder(userDetails.getUsername(),orderId);
        }
        return orderMapper.noAuthority();
    }


    @GetMapping("/getOrder")
    public ResponseEntity<ResponseStructure<OrderResponse>> getOrder(
            @RequestHeader(value = HEADER)String authHeader,
            @RequestParam Long orderId)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.getOrder(userDetails.getUsername(),orderId);
        }
        return orderMapper.noAuthority();
    }


    @GetMapping("/getAllOrders")
    public ResponseEntity<ResponseStructure<List<OrderResponse>>> getAllOrdersForUser(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return orderService.getAllOrdersForUser(userDetails.getUsername());
        }
        return orderMapper.unAuthorized();
    }
}
