package com.example.bookstore.service;

import com.example.bookstore.entity.*;
import com.example.bookstore.exception.AddressNotFoundException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.repository.AddressRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.requestdto.OrderRequest;
import com.example.bookstore.responsedto.OrderResponse;
import com.example.bookstore.serviceimpl.OrderServiceImpl;
import com.example.bookstore.util.ResponseStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest
{
    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private BookRepository bookRepository;

    private User user;
    private OrderRequest orderRequest;
    private Order order;
    private Book book;
    private Cart cart;
    private Address address;

    @BeforeEach
    void init()
    {
        book=Book.builder()
                .bookName("Atom")
                .bookPrice(99.0)
                .bookQuantity(12)
                .status(true)
                .bookId(1L).build();

        cart=Cart.builder()
                .cartId(1L)
                .cartQuantity(1)
                .bookId(1L)
                .userId(1L).build();

        List<Cart> carts=new ArrayList<>();
        carts.add(cart);

        orderRequest = OrderRequest.builder().addressId(1L).build();
        order=Order.builder()
                .orderId(1L)
                .orderQuantity(3)
                .carts(carts)
                .cancelOrder(false)
                .orderPrice(999.9)
                .orderDate(LocalDate.now())
                .addressId(orderRequest.getAddressId())
                .build();

        List<Order> orders=new ArrayList<>();
        orders.add(order);

        user= User.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("sai@gmail.com")
                .password("saichandu")
                .orders(orders)
                .carts(carts)
                .userId(1L)
                .role("USER").build();

        address=Address.builder()
                .addressId(1L)
                .streetName("Baner")
                .city("Pune")
                .state("Maharastra")
                .order(new ArrayList<>())
                .userId(user.getUserId()).build();
    }


    @Test
    public void placeOrderValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<ResponseStructure<OrderResponse>> response=orderService.placeOrder(user.getEmail(), orderRequest);
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
    }

    @Test
    public void placeOrderIfAddressIdIsNotValid()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->orderService.placeOrder(user.getEmail(), orderRequest));

        verify(addressRepository,times(1)).findById(anyLong());
    }


    @Test
    public void cancelOrderValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<ResponseStructure<OrderResponse>> response=orderService.cancelOrder(user.getEmail(),1L);
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }


    @Test
    public void cancelOrderIfOrderIdNotFound()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->orderService.cancelOrder(user.getEmail(),1L));

        verify(orderRepository,times(1)).findByOrderIdAndUserId(anyLong(),anyLong());
    }


    @Test
    public void getOrderValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));

        ResponseEntity<ResponseStructure<OrderResponse>> response=orderService.getOrder(user.getEmail(),1L);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getOrderBooks().size());
    }

    @Test
    public void getOrderIfOrderIdIsInvalid()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->orderService.getOrder(user.getEmail(),1L));

        verify(orderRepository,times(1)).findByOrderIdAndUserId(anyLong(),anyLong());
    }


    @Test
    public void getAllOrdersValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<List<OrderResponse>>> response=orderService.getAllOrdersForUser(user.getEmail());
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getFirst().getOrderBooks().size());
        assertEquals(1,response.getBody().getData().getFirst().getOrderId());
        assertFalse(response.getBody().getData().getFirst().getCancelOrder());
        assertEquals("User orders fetched successfully",response.getBody().getMessage());
    }


    @Test
    public void getAllOrdersIfOrdersAreEmpty()
    {
        User user1=User.builder().userId(1L).email("something@gmail.com").orders(new ArrayList<>()).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        ResponseEntity<ResponseStructure<List<OrderResponse>>> response=orderService.getAllOrdersForUser(user1.getEmail());
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode(),"If user orders are empty");
    }
}