package com.example.bookstore.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseStructure<T>
{
    private int status;
    private String message;
    private T data;

    public ResponseStructure<T> setStatus(int status)
    {
        this.status=status;
        return this;
    }

    public ResponseStructure<T> setMessage(String message)
    {
        this.message=message;
        return this;
    }

    public ResponseStructure<T>  setData(T data)
    {
        this.data=data;
        return this;
    }
}
