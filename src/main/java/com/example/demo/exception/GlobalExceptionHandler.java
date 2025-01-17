package com.example.demo.exception;

import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final String data="FAILURE";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseStructure<String>> illegalArgument(IllegalArgumentException exception)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseStructure<String>> methodArgumentNotValid(MethodArgumentNotValidException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> userNotFound(UserNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> bookNotFound(BookNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> addressNotFound(AddressNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> cartNotFound(CartNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }
}
