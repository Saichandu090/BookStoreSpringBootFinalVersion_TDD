package com.example.demo.exception;

import com.example.demo.entity.Book;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseStructure<String>> illegalArgument(IllegalArgumentException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData("Failure")
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> userNotFound(UserNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData("Failure")
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> bookNotFound(BookNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData("Failure")
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> addressNotFound(AddressNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData("Failure")
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }
}
