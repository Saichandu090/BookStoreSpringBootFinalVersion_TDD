package com.example.demo.exception;

public class AddressNotFoundException extends RuntimeException
{
    public AddressNotFoundException(String message)
    {
        super(message);
    }
}
