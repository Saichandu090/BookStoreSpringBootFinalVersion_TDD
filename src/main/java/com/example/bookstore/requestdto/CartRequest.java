package com.example.bookstore.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest
{
    @NotNull(message = "Book id should not be empty")
    private Long bookId;
}
