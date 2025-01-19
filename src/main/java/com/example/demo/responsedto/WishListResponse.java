package com.example.demo.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WishListResponse
{
    private Long wishListId;
    private Long bookId;
}
