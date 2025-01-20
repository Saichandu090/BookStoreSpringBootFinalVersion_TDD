package com.example.bookstore.controller;

import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.ResponseStructure;
import com.example.bookstore.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@RestController
@RequestMapping("/book")
@AllArgsConstructor
public class BookController
{
    private BookService bookService;
    private UserMapper userMapper;
    private final BookMapper bookMapper=new BookMapper();
    private static final String HEADER="Authorization";

    @PostMapping("/addBook")
    public ResponseEntity<ResponseStructure<BookResponse>> addBook(
            @RequestHeader(value = HEADER) String authHeader,
            @Valid @RequestBody BookRequest bookRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.addBook(bookRequest);
        }
        return bookMapper.noAuthority();
    }


    @GetMapping("/getBookByName/{bookName}")
    public ResponseEntity<ResponseStructure<BookResponse>> getBookByName(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable String bookName)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getBookByName(bookName);
        }
        return bookMapper.noAuthority();
    }


    @GetMapping("/getBookById/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponse>> getBookById(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getBookById(bookId);
        }
        return bookMapper.noAuthority();
    }


    @GetMapping("/getBooks")
    public ResponseEntity<ResponseStructure<List<BookResponse>>> getAllBooks(
            @RequestHeader(value = HEADER) String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getAllBooks();
        }
        return bookMapper.noAuthorityForUser();
    }


    @GetMapping("/pagination")
    public ResponseEntity<ResponseStructure<List<BookResponse>>> sortBYBookName(
            @RequestHeader(value = HEADER) String authHeader
            ,@RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber
            ,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails!=null)
        {
            return bookService.findBooksWithPagination(pageNumber,pageSize);
        }
        return bookMapper.noAuthorityForUser();
    }


    @GetMapping("/search/{query}")
    public ResponseEntity<ResponseStructure<List<BookResponse>>> searchQuery(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable String query)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.searchBooks(query);
        }
        return bookMapper.noAuthorityForUser();
    }


    @GetMapping("/sortBy/{field}")
    public ResponseEntity<ResponseStructure<List<BookResponse>>> sortByField(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable String field)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.findBooksWithSorting(field);
        }
        return bookMapper.noAuthorityForUser();
    }


    @PutMapping("/updateBook/{bookId}")
    public ResponseEntity<ResponseStructure<BookResponse>> editBook(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable Long bookId,
            @Valid @RequestBody BookRequest bookRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.updateBook(bookId, bookRequest);
        }
        return bookMapper.noAuthority();
    }


    @DeleteMapping("/deleteBook/{bookId}")
    public ResponseEntity<ResponseStructure<String>> deleteBook(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.deleteBook(bookId);
        }
        return bookMapper.noAuthority("Failure");
    }
}
