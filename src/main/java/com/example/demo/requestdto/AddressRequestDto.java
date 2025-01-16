package com.example.demo.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequestDto
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
