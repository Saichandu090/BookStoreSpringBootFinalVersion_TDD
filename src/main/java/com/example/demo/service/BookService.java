package com.example.demo.service;

import com.example.demo.requestdto.BookRequest;
import com.example.demo.responsedto.BookResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookService
{
    ResponseEntity<ResponseStructure<BookResponse>> addBook(BookRequest bookRequest);

    ResponseEntity<ResponseStructure<BookResponse>> getBookByName(String bookName);

    ResponseEntity<ResponseStructure<BookResponse>> getBookById(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponse>>> getAllBooks();

    ResponseEntity<ResponseStructure<BookResponse>> updateBook(Long bookId, BookRequest bookRequest);

    ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId);

    ResponseEntity<ResponseStructure<List<BookResponse>>> findBooksWithSorting(String field);

    ResponseEntity<ResponseStructure<List<BookResponse>>> searchBooks(String query);

    ResponseEntity<ResponseStructure<List<BookResponse>>> findBooksWithPagination(int pageNumber, int pageSize);
}
