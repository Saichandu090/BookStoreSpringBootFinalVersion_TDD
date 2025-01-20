package com.example.bookstore.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequest
{
    @NotNull
    private String streetName;

    @NotNull
    private String city;

    @NotNull
    private String state;

    @NotNull
    private Integer pinCode;
}
