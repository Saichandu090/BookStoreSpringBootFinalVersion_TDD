package com.example.bookstore.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse
{
    private Long cartId;
    private Long bookId;
    private Integer cartQuantity;
}
