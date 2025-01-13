package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_orders")
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private LocalDate orderDate;
    private Double orderPrice;
    private Integer orderQuantity;

    @ManyToMany
    private List<Cart> carts=new ArrayList<>();

    private Boolean cancelOrder;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "user_id")
    private Long userId;
}
