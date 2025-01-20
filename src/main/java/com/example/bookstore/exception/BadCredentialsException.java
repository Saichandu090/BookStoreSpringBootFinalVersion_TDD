package com.example.bookstore.exception;

public class BadCredentialsException extends RuntimeException
{
    public BadCredentialsException(String message)
    {
        super(message);
    }
}
