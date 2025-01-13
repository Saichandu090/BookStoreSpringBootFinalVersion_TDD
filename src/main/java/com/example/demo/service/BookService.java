package com.example.demo.service;

import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface BookService
{
    ResponseEntity<ResponseStructure<BookResponseDTO>> addBook(BookRequestDTO bookRequestDTO);
}
