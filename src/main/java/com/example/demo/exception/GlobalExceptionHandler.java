package com.example.demo.exception;

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
}
