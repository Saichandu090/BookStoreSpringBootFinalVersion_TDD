package com.example.bookstore.mapper;

import com.example.bookstore.entity.Book;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class BookMapper
{
    public ResponseEntity<ResponseStructure<BookResponse>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<BookResponse>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage("No Authority to access"));
    }

    public ResponseEntity<ResponseStructure<String>> noAuthority(String message)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<String>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage(message));
    }

    public ResponseEntity<ResponseStructure<List<BookResponse>>> noAuthorityForUser()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<List<BookResponse>>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage("No Authority to access"));
    }


    public Book addBook(BookRequest requestDTO)
    {
        return Book.builder()
                .bookId(requestDTO.getBookId())
                .bookName(requestDTO.getBookName())
                .bookLogo(requestDTO.getBookLogo())
                .bookAuthor(requestDTO.getBookAuthor())
                .bookQuantity(requestDTO.getBookQuantity())
                .bookPrice(requestDTO.getBookPrice())
                .bookDescription(requestDTO.getBookDescription())
                .status(true)
                .cartBookQuantity(0).build();
    }

    public BookResponse mapBookToBookResponse(Book savedBook)
    {
        return BookResponse.builder()
                .bookId(savedBook.getBookId())
                .bookName(savedBook.getBookName())
                .bookQuantity(savedBook.getBookQuantity())
                .bookDescription(savedBook.getBookDescription())
                .bookPrice(savedBook.getBookPrice())
                .bookLogo(savedBook.getBookLogo())
                .status(savedBook.getStatus())
                .bookAuthor(savedBook.getBookAuthor()).build();

    }

    public Book updateCurrentBook(Long bookId, BookRequest requestDTO, int cartQuantity)
    {
        return Book.builder()
                .bookId(bookId)
                .status(true)
                .bookName(requestDTO.getBookName())
                .bookLogo(requestDTO.getBookLogo())
                .bookAuthor(requestDTO.getBookAuthor())
                .bookQuantity(requestDTO.getBookQuantity())
                .bookPrice(requestDTO.getBookPrice())
                .bookDescription(requestDTO.getBookDescription())
                .cartBookQuantity(cartQuantity).build();
    }

    public ResponseEntity<ResponseStructure<List<BookResponse>>> mapToSuccessGetAllBooks(String message, List<BookResponse> bookResponses)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<BookResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setMessage(message)
                .setData(bookResponses));
    }

    public ResponseEntity<ResponseStructure<List<BookResponse>>> noContent()
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<BookResponse>>()
                .setStatus(HttpStatus.NO_CONTENT.value())
                .setMessage("No Books Available")
                .setData(new ArrayList<>()));
    }

    public ResponseEntity<ResponseStructure<BookResponse>> mapToSuccessFetchBook(Book book)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<BookResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book fetched successfully")
                .setData(mapBookToBookResponse(book)));
    }

    public ResponseEntity<ResponseStructure<BookResponse>> mapToSuccessAddBook(Book savedBook)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<BookResponse>().
                setStatus(HttpStatus.CREATED.value())
                .setMessage("Book with name "+savedBook.getBookName()+" added successfully")
                .setData(mapBookToBookResponse(savedBook)));
    }

    public ResponseEntity<ResponseStructure<BookResponse>> mapToSuccessUpdateBook(Book saveUpdatedBook)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<BookResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book Updated successfully")
                .setData(mapBookToBookResponse(saveUpdatedBook)));
    }

    public ResponseEntity<ResponseStructure<String>> mapToSuccessDeleteBook(String message)
    {
        return  ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<String>()
                .setStatus(HttpStatus.OK.value())
                .setMessage(message)
                .setData("Success"));
    }
}
