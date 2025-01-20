package com.example.bookstore.exception;

public class AddressNotFoundException extends RuntimeException
{
    public AddressNotFoundException(String message)
    {
        super(message);
    }
}
