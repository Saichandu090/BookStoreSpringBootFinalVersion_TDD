package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

public class BookMapper
{
    public ResponseStructure<BookResponseDTO> noAuthority()
    {
        return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "No Authority to access", null);
    }

    public ResponseStructure<String> noAuthority(String message)
    {
        return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "No Authority to access", message);
    }

    public ResponseStructure<List<BookResponseDTO>> noAuthorityForUser()
    {
        return new ResponseStructure<>(HttpStatus.UNAUTHORIZED.value(), "No Authority to access", null);
    }


    public Book addBook(BookRequestDTO requestDTO)
    {
        return Book.builder()
                .bookId(requestDTO.getBookId())
                .bookName(requestDTO.getBookName())
                .bookLogo(requestDTO.getBookLogo())
                .bookAuthor(requestDTO.getBookAuthor())
                .bookQuantity(requestDTO.getBookQuantity())
                .bookPrice(requestDTO.getBookPrice())
                .bookDescription(requestDTO.getBookDescription())
                .cartBookQuantity(0).build();
    }

    public BookResponseDTO mapBookToBookResponse(Book savedBook)
    {
        return BookResponseDTO.builder()
                .bookId(savedBook.getBookId())
                .bookName(savedBook.getBookName())
                .bookDescription(savedBook.getBookDescription())
                .bookPrice(savedBook.getBookPrice())
                .bookLogo(savedBook.getBookLogo())
                .bookAuthor(savedBook.getBookAuthor()).build();

    }

    public Book updateCurrentBook(Long bookId, BookRequestDTO requestDTO,int cartQuantity)
    {
        return Book.builder()
                .bookId(bookId)
                .bookName(requestDTO.getBookName())
                .bookLogo(requestDTO.getBookLogo())
                .bookAuthor(requestDTO.getBookAuthor())
                .bookQuantity(requestDTO.getBookQuantity())
                .bookPrice(requestDTO.getBookPrice())
                .bookDescription(requestDTO.getBookDescription())
                .cartBookQuantity(cartQuantity).build();
    }
}
