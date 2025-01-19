package com.example.demo.exception;

public class InvalidSortingFieldException extends RuntimeException
{
    public InvalidSortingFieldException(String message)
    {
        super(message);
    }
}
