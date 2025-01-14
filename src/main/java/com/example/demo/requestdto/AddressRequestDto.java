package com.example.demo.requestdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequestDto
{
    private String streetName;
    private String city;
    private String state;
    private int pinCode;
}
