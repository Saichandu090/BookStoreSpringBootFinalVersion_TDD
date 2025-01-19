package com.example.demo.controller;

import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
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
    public ResponseEntity<ResponseStructure<BookResponseDto>> addBook(
            @RequestHeader(value = HEADER) String authHeader,
            @Valid @RequestBody BookRequestDto bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.addBook(bookRequestDTO);
        }
        return bookMapper.noAuthority();
    }


    @GetMapping("/getBookByName/{bookName}")
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookByName(
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
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookById(
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
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> getAllBooks(
            @RequestHeader(value = HEADER) String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null)
        {
            return bookService.getAllBooks();
        }
        return bookMapper.noAuthorityForUser();
    }


    @GetMapping("/pagination/{pageSize}")
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortBYBookName(
            @RequestHeader(value = HEADER) String authHeader
            ,@RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber
            ,@PathVariable int pageSize)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails!=null)
        {
            return bookService.findBooksWithPagination(pageNumber,pageSize);
        }
        return bookMapper.noAuthorityForUser();
    }


    @GetMapping("/search/{query}")
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> searchQuery(
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
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortByField(
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
    public ResponseEntity<ResponseStructure<BookResponseDto>> editBook(
            @RequestHeader(value = HEADER) String authHeader,
            @PathVariable Long bookId,
            @Valid @RequestBody BookRequestDto bookRequestDTO)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
        {
            return bookService.updateBook(bookId,bookRequestDTO);
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
