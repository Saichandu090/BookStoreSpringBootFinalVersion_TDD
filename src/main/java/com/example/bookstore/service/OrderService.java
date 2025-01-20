package com.example.bookstore.service;

import com.example.bookstore.requestdto.OrderRequest;
import com.example.bookstore.responsedto.OrderResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService
{
    ResponseEntity<ResponseStructure<OrderResponse>> placeOrder(String email, OrderRequest orderRequest);

    ResponseEntity<ResponseStructure<String>> cancelOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<OrderResponse>> getOrder(String username, Long orderId);

    ResponseEntity<ResponseStructure<List<OrderResponse>>> getAllOrdersForUser(String username);
}
