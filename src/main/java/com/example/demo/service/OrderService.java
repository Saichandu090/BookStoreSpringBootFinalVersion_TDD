package com.example.demo.service;

import com.example.demo.requestdto.OrderRequest;
import com.example.demo.responsedto.OrderResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService
{
    ResponseEntity<ResponseStructure<OrderResponse>> placeOrder(String email, OrderRequest orderRequest);

    ResponseEntity<ResponseStructure<String>> cancelOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<OrderResponse>> getOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<List<OrderResponse>>> getAllOrdersForUser(String username);
}
