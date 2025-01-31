package com.example.bookstore.exception;

public class BookUrlLengthException extends RuntimeException
{
  public BookUrlLengthException(String message)
  {
    super(message);
  }
}
