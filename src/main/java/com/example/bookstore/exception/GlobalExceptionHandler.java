package com.example.bookstore.exception;

import com.example.bookstore.util.ResponseStructure;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
                .setMessage(exception.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(",")))
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

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ResponseStructure<String>> orderNotFound(OrderNotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseStructure<String>> badCredentials(BadCredentialsException exception)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ResponseStructure<String>> invalidPagination(InvalidPaginationException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(InvalidSortingFieldException.class)
    public ResponseEntity<ResponseStructure<String>> invalidSortingField(InvalidSortingFieldException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(BookUrlLengthException.class)
    public ResponseEntity<ResponseStructure<String>> bookUrlLength(BookUrlLengthException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ResponseStructure<String>> bookAlreadyExists(BookAlreadyExistsException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(SecretKeyGenerationException.class)
    public ResponseEntity<ResponseStructure<String>> secretKeyGeneration(SecretKeyGenerationException exception)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseStructure<String>()
                .setMessage(exception.getMessage())
                .setData(data)
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
