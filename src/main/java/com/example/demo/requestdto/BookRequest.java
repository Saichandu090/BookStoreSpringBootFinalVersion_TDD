package com.example.demo.requestdto;

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
    @NotNull
    private Long bookId;

    @NotNull
    private String bookName;

    @NotNull
    private String bookAuthor;

    @NotNull
    private String bookDescription;

    @Min(value = 50)
    @Max(value = 999)
    private Double bookPrice;

    @NotNull
    private String bookLogo;

    @Min(value = 16)
    private Integer bookQuantity;
}
