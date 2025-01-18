package com.example.demo.mapper;

import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.responsedto.OrderResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.List;

public class OrderMapper
{
    public ResponseEntity<ResponseStructure<OrderResponseDto>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ResponseStructure<String>> noAuthorityForMethod()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ResponseStructure<OrderResponseDto>> mapToCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ResponseStructure<OrderResponseDto>> mapToSuccessPlaceOrder(Order order, Address address)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<OrderResponseDto>()
                .setMessage("Order with id "+order.getOrderId()+" has places successfully")
                .setStatus(HttpStatus.CREATED.value())
                .setData(mapToOrderResponse(order,address)));
    }

    private OrderResponseDto mapToOrderResponse(Order order,Address address)
    {
        return OrderResponseDto.builder()
                .orderDate(order.getOrderDate())
                .cancelOrder(order.getCancelOrder())
                .orderId(order.getOrderId())
                .orderPrice(order.getOrderPrice())
                .orderQuantity(order.getOrderQuantity())
                .orderAddress(new AddressResponseDto(address.getAddressId(),address.getStreetName(),address.getCity(),address.getState(),address.getPinCode())).build();
    }

    public Order createAnOrder(List<Cart> cartsForOrder, double totalPrice, int totalQuantity, Address address, User user)
    {
        return Order.builder()
                .cancelOrder(false)
                .orderDate(LocalDate.now())
                .orderPrice(totalPrice)
                .orderQuantity(totalQuantity)
                .carts(cartsForOrder)
                .addressId(address.getAddressId())
                .userId(user.getUserId()).build();
    }

    public ResponseEntity<ResponseStructure<String>> mapToSuccessCancelOrder(Order savedOrder)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<String>()
                .setMessage("Order with id "+savedOrder.getOrderId()+" cancelled successfully")
                .setData(null)
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<String>> mapToAlreadyCancelled()
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<String>()
                .setStatus(HttpStatus.NO_CONTENT.value())
                .setMessage("Order already cancelled")
                .setData(null));
    }
}
