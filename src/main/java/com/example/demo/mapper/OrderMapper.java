package com.example.demo.mapper;

import com.example.demo.entity.*;
import com.example.demo.responsedto.AddressResponse;
import com.example.demo.responsedto.BookResponse;
import com.example.demo.responsedto.OrderResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.List;

public class OrderMapper
{
    public ResponseEntity<ResponseStructure<OrderResponse>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ResponseStructure<String>> noAuthorityForMethod()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ResponseStructure<OrderResponse>> mapToCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ResponseStructure<OrderResponse>> mapToSuccessPlaceOrder(Order order, Address address, List<BookResponse> bookResponse)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<OrderResponse>()
                .setMessage("Order with id "+order.getOrderId()+" has places successfully")
                .setStatus(HttpStatus.CREATED.value())
                .setData(mapToOrderResponse(order,address, bookResponse)));
    }

    public OrderResponse mapToOrderResponse(Order order, Address address, List<BookResponse> bookResponse)
    {
        return OrderResponse.builder()
                .orderDate(order.getOrderDate())
                .cancelOrder(order.getCancelOrder())
                .orderId(order.getOrderId())
                .orderPrice(order.getOrderPrice())
                .orderQuantity(order.getOrderQuantity())
                .orderBooks(bookResponse)
                .orderAddress(new AddressResponse(address.getAddressId(),address.getStreetName(),address.getCity(),address.getState(),address.getPinCode())).build();
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

    public ResponseEntity<ResponseStructure<OrderResponse>> mapToSuccessGetOrder(Order order, Address address, List<BookResponse> bookResponse)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<OrderResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Order fetched successfully")
                .setData(mapToOrderResponse(order,address, bookResponse)));
    }

    public ResponseEntity<ResponseStructure<List<OrderResponse>>> unAuthorized()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ResponseStructure<List<OrderResponse>>> mapToNoContentForGetAllOrders()
    {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ResponseStructure<List<OrderResponse>>> mapToSuccessGetAllOrders(List<OrderResponse> orderResponse)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<OrderResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("User orders fetched successfully")
                .setData(orderResponse));
    }
}
