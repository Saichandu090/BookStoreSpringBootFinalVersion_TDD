package com.example.demo.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto
{
    private Long cartId;
    private Double cartPrice;
    private Integer cartQuantity;
}
