package com.example.demo.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse
{
    private Long orderId;
    private LocalDate orderDate;
    private Double orderPrice;
    private Integer orderQuantity;
    private Boolean cancelOrder;
    private AddressResponse orderAddress;
    private List<BookResponse> orderBooks;
}
