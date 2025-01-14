package com.example.demo.service;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.serviceimpl.AddressServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest
{
    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @MockitoBean
    private AddressMapper addressMapper;

    private User user;
    private Address address;
    private AddressRequestDto addressRequestDto;

    @BeforeEach
    void init()
    {
        addressRequestDto=AddressRequestDto.builder()
                .streetName("Banner")
                .city("Pune")
                .state("Maharashtra")
                .pinCode(414004).build();

        address=Address.builder()
                .addressId(1L)
                .state(addressRequestDto.getState())
                .city(addressRequestDto.getCity())
                .pinCode(addressRequestDto.getPinCode())
                .streetName(addressRequestDto.getStreetName())
                .user(user).build();

        List<Address> addresses=new ArrayList<>();
        addresses.add(address);

        user=User.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .userId(1L)
                .addresses(addresses)
                .email("chandu@gmail.com")
                .password("chandler1234")
                .dob(LocalDate.of(2002,8,24))
                .role("USER").build();
    }


    @Test
    public void addressService_AddAddress_MustReturnOKStatus()
    {
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        when(addressRepository.save(Mockito.any(Address.class))).thenReturn(address);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.addAddress(user.getEmail(),addressRequestDto);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertEquals(address.getAddressId(),response.getBody().getData().getAddressId());
        Assertions.assertThat(response.getBody().getData().getPinCode()).isEqualTo(address.getPinCode());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Response status should be CREATED");
        assertEquals("Address added successfully", response.getBody().getMessage(), "Message should match");

        verify(addressRepository, times(1)).save(any(Address.class));
    }


    @Test
    public void addressService_AddAddress_MustReturnNotFoundStatus()
    {
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->addressService.addAddress(user.getEmail(),addressRequestDto));

        verify(userRepository,times(1)).findByEmail(anyString());
    }


    //===========================================================//

    @Test
    public void addressService_UpdateAddress_MustReturnOKStatusCode()
    {
        when(userRepository.findByEmail("chandu@gmail.com")).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(Mockito.any(Address.class))).thenReturn(address);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.updateAddress("chandu@gmail.com",1L,addressRequestDto);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(HttpStatus.OK,response.getStatusCode(),"User have the address to edit");
        assertEquals(200,response.getBody().getStatus());
        assertEquals("Address with id "+address.getAddressId()+" updated successfully for the user "+user.getEmail(),response.getBody().getMessage());

        verify(userRepository,times(1)).findByEmail(anyString());
        verify(addressRepository,times(1)).findById(anyLong());
        verify(addressRepository,times(1)).save(any(Address.class));
    }

    @Test
    public void addressService_UpdateAddress_MustReturnNotFoundStatusCodeIfUserNotFound()
    {
        when(userRepository.findByEmail("chandu@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->addressService.updateAddress("chandu@gmail.com",1L,addressRequestDto),"If user is null or invalid");
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    public void addressService_UpdateAddress_MustReturnNotFoundStatusCodeIfAddressNotFound()
    {
        when(userRepository.findByEmail("chandu@gmail.com")).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.updateAddress("chandu@gmail.com",1L,addressRequestDto),"If address is not found by the Id from repository");
        verify(userRepository,times(1)).findByEmail(anyString());
        verify(addressRepository,times(1)).findById(anyLong());
    }
    //=================================================//

    @Test
    public void addressService_GetAddressById_MustReturnOkStatusCode()
    {
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.getAddressById(1L);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(address.getAddressId(),response.getBody().getData().getAddressId());
        assertEquals(address.getPinCode(),response.getBody().getData().getPinCode());
    }


    @Test
    public void addressService_GetAddressById_MustThrowException()
    {
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.getAddressById(1L));

        verify(addressRepository,times(1)).findById(anyLong());
    }

    //================================================//

    @Test
    public void addressService_GetAllAddress_MustReturnOkStatusCode()
    {
        when(addressRepository.findByUser(any(User.class))).thenReturn(List.of(address));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<List<AddressResponseDto>>> response=addressService.getAllAddress(user.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(address.getAddressId(),response.getBody().getData().getFirst().getAddressId());
        assertEquals("User AddressList fetched successfully",response.getBody().getMessage());
        assertEquals(address.getPinCode(),response.getBody().getData().getFirst().getPinCode());
    }

    @Test
    public void addressService_GetAllAddress_MustThrowException()
    {
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.getAddressById(1L));

        verify(addressRepository,times(1)).findById(anyLong());
    }

    //================================================//


}