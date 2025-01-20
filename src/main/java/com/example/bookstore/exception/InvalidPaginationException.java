package com.example.bookstore.exception;

public class InvalidPaginationException extends RuntimeException
{
    public InvalidPaginationException(String message)
    {
        super(message);
    }
}
