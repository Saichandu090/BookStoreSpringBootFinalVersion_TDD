package com.example.demo.controller;

import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
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
    @Autowired
    private BookService bookService;

    private final BookMapper bookMapper=new BookMapper();

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/addBook")
    public ResponseEntity<ResponseStructure<BookResponseDTO>> addBook(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody BookRequestDTO bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.addBook(bookRequestDTO);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBookByName/{bookName}")
    public ResponseEntity<ResponseStructure<BookResponseDTO>> getBookByName(@RequestHeader("Authorization") String authHeader, @PathVariable String bookName)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.getBookByName(bookName);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBookById/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponseDTO>> getBookById(@RequestHeader("Authorization") String authHeader, @PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.getBookById(bookId);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getBooks")
    public ResponseEntity<ResponseStructure<List<BookResponseDTO>>> getAllBooks(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.getAllBooks();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/sortByBookName")
    public ResponseEntity<ResponseStructure<List<BookResponseDTO>>> sortBYBookName(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.sortByBookName();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/sortByBookPrice")
    public ResponseEntity<ResponseStructure<List<BookResponseDTO>>> sortBYBookPrice(@RequestHeader("Authorization") String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.sortByBookPrice();
        }
        return new ResponseEntity<>(bookMapper.noAuthorityForUser(), HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("/updateBook/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponseDTO>> editBook(@RequestHeader("Authorization") String authHeader,@PathVariable Long bookId, @Valid @RequestBody BookRequestDTO bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.updateBook(bookId,bookRequestDTO);
        }
        return new ResponseEntity<>(bookMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @DeleteMapping("/deleteBook/{bookId}")
    public ResponseEntity<ResponseStructure<String>> deleteBook(@RequestHeader("Authorization") String authHeader,@PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")))
        {
            return bookService.deleteBook(bookId);
        }
        return new ResponseEntity<>(bookMapper.noAuthority("Failure"), HttpStatus.UNAUTHORIZED);
    }
}
