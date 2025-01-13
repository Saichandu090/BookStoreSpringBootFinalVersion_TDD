package com.example.demo.controller;

import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
public class BookController
{
    @Autowired
    private BookService bookService;

    @PostMapping("/addBook")
    public ResponseEntity<ResponseStructure<BookResponseDTO>> addBook(@RequestHeader("Authorization")String authHeader, @RequestBody BookRequestDTO bookRequestDTO)
    {
        return null;
    }
}
