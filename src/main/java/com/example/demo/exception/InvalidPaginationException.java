package com.example.demo.exception;

public class InvalidPaginationException extends RuntimeException
{
    public InvalidPaginationException(String message)
    {
        super(message);
    }
}
