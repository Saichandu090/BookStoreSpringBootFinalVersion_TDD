package com.example.demo.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressResponseDto
{
    private Long addressId;
    private String streetName;
    private String city;
    private String state;
    private int pinCode;
}
