package com.example.bookstore.requestdto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequest
{
    @NotNull(message = "BookName should not be empty")
    private String bookName;

    @NotNull(message = "BookAuthor should not be empty")
    private String bookAuthor;

    @NotNull(message = "BookDescription should not be empty")
    private String bookDescription;

    @Min(value = 50,message = "BookPrice minimum should be 50")
    @Max(value = 999,message = "BookPrice should not exceed 999")
    private Double bookPrice;

    @NotNull(message = "BookLogo should not be empty")
    private String bookLogo;

    @Min(value = 16,message = "BookQuantity should be atLeast 16")
    private Integer bookQuantity;
}
