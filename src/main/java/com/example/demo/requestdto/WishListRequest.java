package com.example.demo.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishListRequest
{
    @NotNull(message = "Book Id must not be null")
    private Long bookId;
}
