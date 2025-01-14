package com.example.demo.serviceimpl;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressServiceImpl implements AddressService
{
    private AddressRepository addressRepository;
    private UserRepository userRepository;
    private final AddressMapper addressMapper=new AddressMapper();

    @Override
    public ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(String email, AddressRequestDto addressRequestDto)
    {
        Optional<User> user=userRepository.findByEmail(email);

        if(user.isEmpty())
            throw new UserNotFoundException("User with email "+email+" not found");

        User realUser=user.get();

        Address address=addressMapper.mapAddressRequestToAddress(realUser,addressRequestDto);

        if(realUser.getAddresses()==null)
            realUser.setAddresses(new ArrayList<>());
        realUser.getAddresses().add(address);

        Address savedAddress=addressRepository.save(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<AddressResponseDto>()
                .setStatus(HttpStatus.CREATED.value())
                .setMessage("Address added successfully")
                .setData(addressMapper.mapAddressToAddressResponse(savedAddress)));
    }

    @Override
    public ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(String email, Long addressId, AddressRequestDto addressRequestDto)
    {
        Optional<User> user=userRepository.findByEmail(email);

        if(user.isEmpty())
            throw new UserNotFoundException("User with email "+email+" not found");

        Optional<Address> address=addressRepository.findById(addressId);

        if(address.isEmpty())
            throw new AddressNotFoundException("Address with id "+addressId+" not found");

        User realUser=user.get();
        Address realAddress=address.get();

        Address updatedAddress=addressMapper.mapOldAddressToNewAddress(realAddress,addressRequestDto);
        Address saved=addressRepository.save(updatedAddress);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Address with id "+addressId+" updated successfully for the user "+email)
                .setData(addressMapper.mapAddressToAddressResponse(saved)));
    }
}
