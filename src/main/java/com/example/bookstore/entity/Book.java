package com.example.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book
{
    @Id
    private Long bookId;
    private String bookName;
    private String bookAuthor;
    private String bookDescription;
    private Double bookPrice;
    private String bookLogo;
    private Integer bookQuantity;
    private int cartBookQuantity;
}
