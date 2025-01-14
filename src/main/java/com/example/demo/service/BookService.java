package com.example.demo.service;

import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookService
{
    ResponseEntity<ResponseStructure<BookResponseDTO>> addBook(BookRequestDTO bookRequestDTO);

    ResponseEntity<ResponseStructure<BookResponseDTO>> getBookByName(String bookName);

    ResponseEntity<ResponseStructure<BookResponseDTO>> getBookById(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponseDTO>>> getAllBooks();

    ResponseEntity<ResponseStructure<BookResponseDTO>> updateBook(Long bookId,BookRequestDTO bookRequestDTO);

    ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponseDTO>>> sortByBookName();

    ResponseEntity<ResponseStructure<List<BookResponseDTO>>> sortByBookPrice();
}
