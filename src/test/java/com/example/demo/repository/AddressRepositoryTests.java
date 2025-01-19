package com.example.demo.repository;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.requestdto.AddressRequest;
import com.example.demo.responsedto.AddressResponse;
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

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreetName("123 Main St");
        addressRequest.setCity("Test City");
        addressRequest.setState("Test State");
        addressRequest.setPinCode(12345);

        addressService.addAddress(user.getEmail(), addressRequest);

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

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreetName("123 Main St");
        addressRequest.setCity("Test City");
        addressRequest.setState("Test State");
        addressRequest.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.addAddress(user.getEmail(), addressRequest);

        AddressResponse responseDto=response.getBody().getData();

        // Updating the address

        AddressRequest addressRequest2 = new AddressRequest();
        addressRequest2.setStreetName("1234 Main St");
        addressRequest2.setCity("Test1 City");
        addressRequest2.setState("Test1 State");
        addressRequest2.setPinCode(123456);

        ResponseEntity<ResponseStructure<AddressResponse>> response2=addressService.updateAddress("test@example.com", responseDto.getAddressId(), addressRequest2);
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(200,response2.getBody().getStatus());

        AddressResponse result=response2.getBody().getData();
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

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreetName("123 Main St");
        addressRequest.setCity("Test City");
        addressRequest.setState("Test State");
        addressRequest.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponse>> responseEntity=addressService.addAddress(user.getEmail(), addressRequest);

        AddressResponse responseDto=responseEntity.getBody().getData();

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.getAddressById(user.getEmail(),responseDto.getAddressId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(addressRequest.getCity(),response.getBody().getData().getCity());
        assertEquals(addressRequest.getPinCode(),response.getBody().getData().getPinCode());
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

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreetName("123 Main St");
        addressRequest.setCity("Test City");
        addressRequest.setState("Test State");
        addressRequest.setPinCode(12345);

        addressService.addAddress(user.getEmail(), addressRequest);

        ResponseEntity<ResponseStructure<List<AddressResponse>>> response=addressService.getAllAddress(user.getEmail());

        User user1=userRepository.findByEmail(user.getEmail()).get();
        Address addressResult=addressRepository.findById(response.getBody().getData().getFirst().getAddressId()).get();

        Address address=user1.getAddresses().getFirst();
        assertEquals(address,addressResult);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());
        assertEquals(addressRequest.getCity(),response.getBody().getData().getFirst().getCity());
        assertEquals(addressRequest.getPinCode(),response.getBody().getData().getFirst().getPinCode());
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

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreetName("123 Main St");
        addressRequest.setCity("Test City");
        addressRequest.setState("Test State");
        addressRequest.setPinCode(12345);

        ResponseEntity<ResponseStructure<AddressResponse>> response=addressService.addAddress(user.getEmail(), addressRequest);

        AddressResponse addressResponse =response.getBody().getData();

        ResponseEntity<ResponseStructure<AddressResponse>> responseEntity=addressService.deleteAddress(user.getEmail(), addressResponse.getAddressId());

        AddressResponse addressResponse1 =responseEntity.getBody().getData();

        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        assertEquals(200,responseEntity.getBody().getStatus());
        assertEquals("Address deleted successfully",responseEntity.getBody().getMessage());
    }
}
