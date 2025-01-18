package com.example.demo.serviceimpl;

import com.example.demo.entity.*;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.OrderNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.*;
import com.example.demo.requestdto.OrderRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.responsedto.OrderResponseDto;
import com.example.demo.service.OrderService;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService
{
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private AddressRepository addressRepository;
    private BookRepository bookRepository;
    private final OrderMapper orderMapper=new OrderMapper();

    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<OrderResponseDto>> placeOrder(String email, OrderRequestDto orderRequestDto)
    {
        User user=getUser(email);
        Address address=getAddress(orderRequestDto.getAddressId());
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            return orderMapper.mapToCartIsEmpty();
        Order order=processUserPlaceOrder(userCarts,address,user);
        Order savedOrder=orderRepository.save(order);
        List<BookResponseDto> books=getBooksResponseFromOrder(order);
        return orderMapper.mapToSuccessPlaceOrder(savedOrder,address,books);
    }

    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<String>> cancelOrder(String email, Long orderId)
    {
        User user=getUser(email);
        Order userOrder=getOrder(orderId,user.getUserId());
        if(Boolean.TRUE.equals(userOrder.getCancelOrder()))
            return orderMapper.mapToAlreadyCancelled();
        Order savedOrder=processCancelOrder(userOrder);
        return orderMapper.mapToSuccessCancelOrder(savedOrder);
    }

    @Override
    public ResponseEntity<ResponseStructure<OrderResponseDto>> getOrder(String email, Long orderId)
    {
        User user=getUser(email);
        Order order=getOrder(orderId,user.getUserId());
        Address address=getAddress(order.getAddressId());
        List<BookResponseDto> books=getBooksResponseFromOrder(order);
        return orderMapper.mapToSuccessGetOrder(order,address,books);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<OrderResponseDto>>> getAllOrdersForUser(String email)
    {
        User user=getUser(email);
        List<Order> userOrders=user.getOrders();
        if(userOrders.isEmpty())
            return orderMapper.mapToNoContentForGetAllOrders();
        List<OrderResponseDto> orderResponseDtos = userOrders.stream().map(order -> {
                    Address address = getAddress(order.getAddressId());
                    List<BookResponseDto> bookResponseDtos = getBooksResponseFromOrder(order);
                    return orderMapper.mapToOrderResponse(order, address, bookResponseDtos);
        }).toList();
        return orderMapper.mapToSuccessGetAllOrders(orderResponseDtos);
    }


    //Helper Methods
    private Order processCancelOrder(Order userOrder)
    {
        List<Cart> orderCarts=userOrder.getCarts();
        userOrder.setCancelOrder(true);
        for(Cart cart : orderCarts)
        {
            Book book=getBook(cart.getBookId());
            book.setBookQuantity(book.getBookQuantity()+cart.getCartQuantity());
            book.setCartBookQuantity(book.getCartBookQuantity()-cart.getCartQuantity());
            bookRepository.save(book);
        }
        return orderRepository.save(userOrder);
    }

    private Order processUserPlaceOrder(List<Cart> userCarts, Address address,User user)
    {
        List<Cart> cartsForOrder=new ArrayList<>();
        int totalQuantity=0;
        double totalPrice=0.0;
        for (Cart cart : userCarts)
        {
            Book book=getBook(cart.getBookId());
            totalQuantity=cart.getCartQuantity()+totalQuantity;
            totalPrice=totalPrice+(cart.getCartQuantity()*book.getBookPrice());
            cartsForOrder.add(cart);
        }
        userCarts.clear();
        return orderMapper.createAnOrder(cartsForOrder,totalPrice,totalQuantity,address,user);
    }


    private List<BookResponseDto> getBooksResponseFromOrder(Order order)
    {
        List<Cart> carts=order.getCarts();
        List<Book> books=carts.stream().map(cart ->getBook(cart.getBookId())).toList();
        return books.stream()
                .map(book ->BookResponseDto.builder()
                        .bookId(book.getBookId())
                        .bookAuthor(book.getBookAuthor())
                        .bookDescription(book.getBookDescription())
                        .bookLogo(book.getBookLogo())
                        .bookName(book.getBookName())
                        .bookPrice(book.getBookPrice()).build()).toList();
    }

    private Order getOrder(Long orderId,Long userId)
    {
        Optional<Order> order=orderRepository.findByOrderIdAndUserId(orderId,userId);
        if(order.isEmpty())
            throw new OrderNotFoundException("Order not found");
        return order.get();
    }

    private Address getAddress(Long addressId)
    {
        Optional<Address> address=addressRepository.findById(addressId);
        if(address.isEmpty())
            throw new AddressNotFoundException("Address not found with Id "+addressId);
        return address.get();
    }

    private User getUser(String email)
    {
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new UserNotFoundException("User not found with username "+email);
        return user.get();
    }

    private Book getBook(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);
        if(book.isEmpty())
            throw new BookNotFoundException("Book not found");
        return book.get();
    }
}
