package com.example.demo.service;

import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookService
{
    ResponseEntity<ResponseStructure<BookResponseDto>> addBook(BookRequestDto bookRequestDTO);

    ResponseEntity<ResponseStructure<BookResponseDto>> getBookByName(String bookName);

    ResponseEntity<ResponseStructure<BookResponseDto>> getBookById(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponseDto>>> getAllBooks();

    ResponseEntity<ResponseStructure<BookResponseDto>> updateBook(Long bookId, BookRequestDto bookRequestDTO);

    ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponseDto>>> findBooksWithSorting(String field);

    ResponseEntity<ResponseStructure<List<BookResponseDto>>> searchBooks(String query);

    ResponseEntity<ResponseStructure<List<BookResponseDto>>> findBooksWithPagination(int pageNumber,int pageSize);
}
