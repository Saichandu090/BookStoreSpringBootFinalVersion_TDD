package com.example.demo.service;

import com.example.demo.requestdto.OrderRequestDto;
import com.example.demo.responsedto.OrderResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService
{
    ResponseEntity<ResponseStructure<OrderResponseDto>> placeOrder(String email, OrderRequestDto orderRequestDto);

    ResponseEntity<ResponseStructure<String>> cancelOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<OrderResponseDto>> getOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<List<OrderResponseDto>>> getAllOrdersForUser(String username);
}
