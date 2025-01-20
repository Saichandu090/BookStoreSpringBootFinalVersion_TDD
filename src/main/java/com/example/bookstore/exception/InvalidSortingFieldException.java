package com.example.bookstore.exception;

public class InvalidSortingFieldException extends RuntimeException
{
    public InvalidSortingFieldException(String message)
    {
        super(message);
    }
}
