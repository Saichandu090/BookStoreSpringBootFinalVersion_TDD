package com.example.demo.service;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.AddressRequest;
import com.example.demo.responsedto.AddressResponse;
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
    private AddressRequest addressRequest;

    @BeforeEach
    void init()
    {
        addressRequest = AddressRequest.builder()
                .streetName("Banner")
                .city("Pune")
                .state("Maharashtra")
                .pinCode(414004).build();

        address=Address.builder()
                .addressId(1L)
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .pinCode(addressRequest.getPinCode())
                .streetName(addressRequest.getStreetName())
                .userId(1L).build();

        List<Address> addresses=new ArrayList<>();
        addresses.add(address);

        user=User.builder()
                .userId(1L)
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
    public void addAddressMustReturnOKStatus()
    {
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        when(addressRepository.save(Mockito.any(Address.class))).thenReturn(address);

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.addAddress(user.getEmail(), addressRequest);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertEquals(address.getAddressId(),response.getBody().getData().getAddressId());
        Assertions.assertThat(response.getBody().getData().getPinCode()).isEqualTo(address.getPinCode());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Response status should be CREATED");
        assertEquals("Address added successfully", response.getBody().getMessage(), "Message should match");

        verify(addressRepository, times(1)).save(any(Address.class));
    }


    @Test
    public void addAddressMustReturnNotFoundStatus()
    {
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->addressService.addAddress(user.getEmail(), addressRequest));

        verify(userRepository,times(1)).findByEmail(anyString());
    }




    @Test
    public void updateAddressMustReturnOKStatusCode()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.save(Mockito.any(Address.class))).thenReturn(address);

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.updateAddress("chandu@gmail.com",1L, addressRequest);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(HttpStatus.OK,response.getStatusCode(),"User have the address to edit");
        assertEquals(200,response.getBody().getStatus());
        assertEquals("Address with id "+address.getAddressId()+" updated successfully for the user "+user.getEmail(),response.getBody().getMessage());

        verify(userRepository,times(1)).findByEmail(anyString());
        verify(addressRepository,times(1)).findByAddressIdAndUserId(anyLong(),anyLong());
        verify(addressRepository,times(1)).save(any(Address.class));
    }


    @Test
    public void updateAddressMustReturnNotFoundStatusCodeIfUserNotFound()
    {
        when(userRepository.findByEmail("chandu@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->addressService.updateAddress("chandu@gmail.com",1L, addressRequest),"If user is null or invalid");
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    public void updateAddressMustReturnNotFoundStatusCodeIfAddressNotFound()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.updateAddress("chandu@gmail.com",1L, addressRequest),"If address is not found by the Id from repository");
        verify(userRepository,times(1)).findByEmail(anyString());
        verify(addressRepository,times(1)).findByAddressIdAndUserId(anyLong(),anyLong());
    }



    @Test
    public void getAddressByIdMustReturnOkStatusCode()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(address));

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.getAddressById(user.getEmail(),1L);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(address.getAddressId(),response.getBody().getData().getAddressId());
        assertEquals(address.getPinCode(),response.getBody().getData().getPinCode());
    }


    @Test
    public void getAddressByIdMustThrowException()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.getAddressById(user.getEmail(),1L));

        verify(addressRepository,times(1)).findByAddressIdAndUserId(anyLong(),anyLong());
    }



    @Test
    public void getAllAddressMustReturnOkStatusCode()
    {
        when(addressRepository.findByUserId(anyLong())).thenReturn(List.of(address));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<List<AddressResponse>>> response=addressService.getAllAddress(user.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(address.getAddressId(),response.getBody().getData().getFirst().getAddressId());
        assertEquals("User AddressList fetched successfully",response.getBody().getMessage());
        assertEquals(address.getPinCode(),response.getBody().getData().getFirst().getPinCode());
    }

    @Test
    public void getAllAddressMustThrowException()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(anyLong())).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<AddressResponse>>> response=addressService.getAllAddress(user.getEmail());
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        verify(addressRepository,times(1)).findByUserId(anyLong());
    }



    @Test
    public void deleteAddressByIdMustReturnOkStatusCode()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(address));

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.deleteAddress(user.getEmail(),address.getAddressId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals("Address deleted successfully",response.getBody().getMessage());

        verify(userRepository,times(1)).findByEmail(anyString());
        verify(addressRepository,times(1)).findByAddressIdAndUserId(anyLong(),anyLong());
    }


    @Test
    public void deleteAddressByIdMustThrowUserNotFoundException()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->addressService.deleteAddress(user.getEmail(),address.getAddressId()));

        verify(userRepository,times(1)).findByEmail(anyString());
    }


    @Test
    public void deleteAddressByIdMustThrowAddressNotFoundException()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findByAddressIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,()->addressService.deleteAddress(user.getEmail(),address.getAddressId()));

        verify(addressRepository,times(1)).findByAddressIdAndUserId(anyLong(),anyLong());
        verify(userRepository,times(1)).findByEmail(anyString());
    }
}