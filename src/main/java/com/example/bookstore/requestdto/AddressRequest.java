package com.example.bookstore.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequest
{
    @NotNull(message = "StreetName should not be empty")
    private String streetName;

    @NotNull(message = "City should not be empty")
    private String city;

    @NotNull(message = "State should not be empty")
    private String state;

    @NotNull(message = "PinCode should not be empty")
    private Integer pinCode;
}
