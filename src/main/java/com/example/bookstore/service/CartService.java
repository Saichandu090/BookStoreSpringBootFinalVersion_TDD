package com.example.bookstore.service;

import com.example.bookstore.requestdto.CartRequest;
import com.example.bookstore.responsedto.CartResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CartService
{
    ResponseEntity<ResponseStructure<CartResponse>> addToCart(String email, CartRequest cartRequest);

    ResponseEntity<ResponseStructure<CartResponse>> removeFromCart(String email, Long cartId);

    ResponseEntity<ResponseStructure<List<CartResponse>>> getCartItems(String email);

    ResponseEntity<ResponseStructure<CartResponse>> clearCart(String username);
}
