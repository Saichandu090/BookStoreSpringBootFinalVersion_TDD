package com.example.bookstore.mapper;

import com.example.bookstore.entity.*;
import com.example.bookstore.responsedto.AddressResponse;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.responsedto.OrderResponse;
import com.example.bookstore.util.ResponseStructure;
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

    public BookResponse mapBookToBookResponse(Book savedBook)
    {
        return BookResponse.builder()
                .bookId(savedBook.getBookId())
                .bookName(savedBook.getBookName())
                .bookDescription(savedBook.getBookDescription())
                .bookPrice(savedBook.getBookPrice())
                .bookLogo(savedBook.getBookLogo())
                .bookAuthor(savedBook.getBookAuthor()).build();

    }

    public ResponseEntity<ResponseStructure<OrderResponse>> mapToSuccessCancelOrder(Order savedOrder,Address address, List<BookResponse> bookResponse)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<OrderResponse>()
                .setMessage("Order with id "+savedOrder.getOrderId()+" cancelled successfully")
                .setData(mapToOrderResponse(savedOrder,address,bookResponse))
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<OrderResponse>> mapToAlreadyCancelled()
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<OrderResponse>()
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
