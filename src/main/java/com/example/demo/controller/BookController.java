package com.example.demo.controller;

import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController
{
    private BookService bookService;
    private UserMapper userMapper;
    private final BookMapper bookMapper=new BookMapper();

    @PostMapping("/addBook")
    public ResponseEntity<ResponseStructure<BookResponseDto>> addBook(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody BookRequestDto bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.addBook(bookRequestDTO);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBookByName/{bookName}")
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookByName(@RequestHeader("Authorization") String authHeader, @PathVariable String bookName)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getBookByName(bookName);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBookById/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookById(@RequestHeader("Authorization") String authHeader, @PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getBookById(bookId);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBooks")
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> getAllBooks(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getAllBooks();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/sortByBookName")
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortBYBookName(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails!=null)
        {
            return bookService.sortByBookName();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/sortByBookPrice")
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortBYBookPrice(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.sortByBookPrice();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("/updateBook/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponseDto>> editBook(@RequestHeader("Authorization") String authHeader, @PathVariable Long bookId, @Valid @RequestBody BookRequestDto bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.updateBook(bookId,bookRequestDTO);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @DeleteMapping("/deleteBook/{bookId}")
    public ResponseEntity<ResponseStructure<String>> deleteBook(@RequestHeader("Authorization") String authHeader,@PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.deleteBook(bookId);
        }
        return new ResponseEntity<>(bookMapper.noAuthority("Failure"), HttpStatus.UNAUTHORIZED);
    }
}
