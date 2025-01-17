package com.example.demo.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequestDto
{
    @NotNull(message = "Book id should not be empty")
    private Long bookId;
}
