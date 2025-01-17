package com.example.demo.serviceimpl;

import com.example.demo.mapper.CartMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService
{
    private CartRepository cartRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private final CartMapper cartMapper=new CartMapper();
}
