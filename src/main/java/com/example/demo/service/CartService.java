package com.example.demo.service;

import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface CartService
{
    ResponseEntity<ResponseStructure<CartResponseDto>> addToCart(String email, CartRequestDto cartRequestDto);

    ResponseEntity<ResponseStructure<CartResponseDto>> removeFromCart(String username, Long cartId);
}
