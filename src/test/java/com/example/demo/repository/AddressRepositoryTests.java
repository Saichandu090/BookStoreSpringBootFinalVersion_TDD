package com.example.demo.repository;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class AddressRepositoryTests
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    @Test
    public void testAddAddress()
    {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setRegisteredDate(LocalDate.now());
        user.setUpdatedDate(LocalDate.now());
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        addressService.addAddress(user.getEmail(), addressRequestDto);

        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        assertTrue(optionalUser.isPresent(), "User should be present");

        User retrievedUser = optionalUser.get();
        assertNotNull(retrievedUser.getAddresses(), "Addresses list should not be null");
        assertEquals(1, retrievedUser.getAddresses().size(), "User should have one address");

        Address addedAddress = retrievedUser.getAddresses().getFirst();
        assertEquals("123 Main St", addedAddress.getStreetName(), "Street should match");
        assertEquals("Test City", addedAddress.getCity(), "City should match");
        assertEquals("Test State", addedAddress.getState(), "State should match");
        assertEquals(12345, addedAddress.getPinCode(), "Zip Code should match");

        List<Address> addresses=addressRepository.findAll();
        assertEquals(1,addresses.size());
    }


    @Test
    public void testUpdateAddress()
    {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setRegisteredDate(LocalDate.now());
        user.setUpdatedDate(LocalDate.now());
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.addAddress(user.getEmail(), addressRequestDto);

        AddressResponseDto responseDto=response.getBody().getData();

        // Updating the address

        AddressRequestDto addressRequestDto2 = new AddressRequestDto();
        addressRequestDto2.setStreetName("1234 Main St");
        addressRequestDto2.setCity("Test1 City");
        addressRequestDto2.setState("Test1 State");
        addressRequestDto2.setPinCode(123456);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response2=addressService.updateAddress("test@example.com", responseDto.getAddressId(), addressRequestDto2);
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(200,response2.getBody().getStatus());

        AddressResponseDto result=response2.getBody().getData();
        assertEquals("Test1 City",result.getCity());
        assertEquals("Test1 State",result.getState());
        assertEquals(123456,result.getPinCode());
        assertEquals(result.getAddressId(),responseDto.getAddressId());

        List<Address> addresses=addressRepository.findAll();
        assertEquals(1,addresses.size());
    }


    @Test
    public void getByAddressIdTest()
    {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setRegisteredDate(LocalDate.now());
        user.setUpdatedDate(LocalDate.now());
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponseDto>> responseEntity=addressService.addAddress(user.getEmail(),addressRequestDto);

        AddressResponseDto responseDto=responseEntity.getBody().getData();

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.getAddressById(user.getEmail(),responseDto.getAddressId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(addressRequestDto.getCity(),response.getBody().getData().getCity());
        assertEquals(addressRequestDto.getPinCode(),response.getBody().getData().getPinCode());
    }


    @Test
    public void getAllAddressTest()
    {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setRegisteredDate(LocalDate.now());
        user.setUpdatedDate(LocalDate.now());
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        addressService.addAddress(user.getEmail(),addressRequestDto);

        ResponseEntity<ResponseStructure<List<AddressResponseDto>>> response=addressService.getAllAddress(user.getEmail());

        User user1=userRepository.findByEmail(user.getEmail()).get();
        Address addressResult=addressRepository.findById(response.getBody().getData().getFirst().getAddressId()).get();

        Address address=user1.getAddresses().getFirst();
        assertEquals(address,addressResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(addressRequestDto.getCity(),response.getBody().getData().getFirst().getCity());
        assertEquals(addressRequestDto.getPinCode(),response.getBody().getData().getFirst().getPinCode());
    }


    @Test
    public void deleteAddressByIdTest()
    {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setRegisteredDate(LocalDate.now());
        user.setUpdatedDate(LocalDate.now());
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response=addressService.addAddress(user.getEmail(),addressRequestDto);

        AddressResponseDto addressResponseDto=response.getBody().getData();

        ResponseEntity<ResponseStructure<AddressResponseDto>> responseEntity=addressService.deleteAddress(user.getEmail(), addressResponseDto.getAddressId());

        AddressResponseDto addressResponseDto1=responseEntity.getBody().getData();

        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        assertEquals(200,responseEntity.getBody().getStatus());
        assertEquals("Address deleted successfully",responseEntity.getBody().getMessage());
    }
}
