package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.OrderNotFoundException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.OrderRequestDto;
import com.example.demo.responsedto.OrderResponseDto;
import com.example.demo.serviceimpl.OrderServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.aspectj.weaver.ast.Or;
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
    private OrderRequestDto orderRequestDto;
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
                .bookId(1L).build();

        cart=Cart.builder()
                .cartId(1L)
                .cartQuantity(1)
                .bookId(1L)
                .userId(1L).build();

        List<Cart> carts=new ArrayList<>();
        carts.add(cart);

        orderRequestDto= OrderRequestDto.builder().addressId(1L).build();
        order=Order.builder()
                .orderId(1L)
                .orderQuantity(3)
                .carts(carts)
                .cancelOrder(false)
                .orderPrice(999.9)
                .orderDate(LocalDate.now())
                .addressId(orderRequestDto.getAddressId())
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
    public void orderService_PlaceOrder_ValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<ResponseStructure<OrderResponseDto>> response=orderService.placeOrder(user.getEmail(),orderRequestDto);
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
    }

    @Test
    public void orderService_PlaceOrder_IfAddressIdIsNotValid()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->orderService.placeOrder(user.getEmail(),orderRequestDto));

        verify(addressRepository,times(1)).findById(anyLong());
    }


    @Test
    public void orderService_CancelOrder_ValidTest()
    {
        Order cancelledOrder=Order.builder().cancelOrder(true).orderId(1L).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        ResponseEntity<ResponseStructure<String>> response=orderService.cancelOrder(user.getEmail(),1L);
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }


    @Test
    public void orderService_CancelOrder_IfOrderIdNotFound()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->orderService.cancelOrder(user.getEmail(),1L));

        verify(orderRepository,times(1)).findByOrderIdAndUserId(anyLong(),anyLong());
    }


    @Test
    public void orderService_getOrder_ValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));

        ResponseEntity<ResponseStructure<OrderResponseDto>> response=orderService.getOrder(user.getEmail(),1L);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getOrderBooks().size());
    }

    @Test
    public void orderService_getOrder_IfOrderIdIsInvalid()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,()->orderService.getOrder(user.getEmail(),1L));

        verify(orderRepository,times(1)).findByOrderIdAndUserId(anyLong(),anyLong());
    }


    @Test
    public void orderService_getAllOrders_ValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<List<OrderResponseDto>>> response=orderService.getAllOrdersForUser(user.getEmail());
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getFirst().getOrderBooks().size());
        assertEquals(1,response.getBody().getData().getFirst().getOrderId());
        assertFalse(response.getBody().getData().getFirst().getCancelOrder());
        assertEquals("User orders fetched successfully",response.getBody().getMessage());
    }


    @Test
    public void orderService_getAllOrders_IfOrdersAreEmpty()
    {
        User user1=User.builder().userId(1L).email("something@gmail.com").orders(new ArrayList<>()).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        ResponseEntity<ResponseStructure<List<OrderResponseDto>>> response=orderService.getAllOrdersForUser(user1.getEmail());
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode(),"If user orders are empty");
    }
}