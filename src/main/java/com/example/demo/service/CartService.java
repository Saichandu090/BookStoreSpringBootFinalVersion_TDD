package com.example.demo.service;

import com.example.demo.requestdto.CartRequest;
import com.example.demo.responsedto.CartResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CartService
{
    ResponseEntity<ResponseStructure<CartResponse>> addToCart(String email, CartRequest cartRequest);

    ResponseEntity<ResponseStructure<CartResponse>> removeFromCart(String email, Long cartId);

    ResponseEntity<ResponseStructure<List<CartResponse>>> getCartItems(String email);

    ResponseEntity<ResponseStructure<CartResponse>> clearCart(String username);
}
