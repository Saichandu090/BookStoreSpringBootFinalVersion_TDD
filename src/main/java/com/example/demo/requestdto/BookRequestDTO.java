package com.example.demo.requestdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDTO
{
    private Long bookId;
    private String bookName;
    private String author;
    private String description;
    private Double price;
    private String bookLogo;
    private Integer quantity;
}
