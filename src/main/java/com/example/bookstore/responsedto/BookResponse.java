package com.example.bookstore.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookResponse
{
    private Long bookId;
    private String bookName;
    private String bookAuthor;
    private Integer bookQuantity;
    private String bookDescription;
    private Double bookPrice;
    private String bookLogo;
    private Boolean status;
}
