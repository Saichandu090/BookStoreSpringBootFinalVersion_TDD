package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private String author;
    private String description;
    private Double price;
    private String bookLogo;
    private Integer quantity;
    private Integer cartBookQuantity;
}
