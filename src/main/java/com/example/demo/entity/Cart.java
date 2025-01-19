package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;
    private Integer cartQuantity;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "user_id")
    private Long userId;
}