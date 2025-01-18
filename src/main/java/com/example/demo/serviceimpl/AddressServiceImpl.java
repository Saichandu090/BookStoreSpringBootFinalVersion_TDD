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
import lombok.AllArgsConstructor;
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
        User realUser=getUser(email);
        Address address=addressMapper.mapAddressRequestToAddress(realUser,addressRequestDto);
        if(realUser.getAddresses()==null)
            realUser.setAddresses(new ArrayList<>());
        realUser.getAddresses().add(address);
        Address savedAddress=addressRepository.save(address);
        return addressMapper.mapToSuccessAddAddress(savedAddress);
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(String email, Long addressId, AddressRequestDto addressRequestDto)
    {
        User user=getUser(email);
        Address realAddress=getAddress(addressId);
        Address updatedAddress=addressMapper.mapOldAddressToNewAddress(realAddress,addressRequestDto);
        Address saved=addressRepository.save(updatedAddress);
        return addressMapper.mapToSuccessUpdateAddress(saved,user.getEmail());
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponseDto>> getAddressById(Long addressId)
    {
        Address resultAddress=getAddress(addressId);
        return addressMapper.mapToSuccessGetAddressById(resultAddress);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<AddressResponseDto>>> getAllAddress(String email)
    {
        User realUser=getUser(email);
        List<Address> addresses=addressRepository.findByUser(realUser);
        List<AddressResponseDto> responseDtoList=addresses.stream().map(addressMapper::mapAddressToAddressResponse).toList();
        return addressMapper.mapToSuccessGetAllAddress(responseDtoList);
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponseDto>> deleteAddress(String email, Long addressId)
    {
        User realUser=getUser(email);
        Address realAddress=getAddress(addressId);
        realUser.getAddresses().remove(realAddress);
        addressRepository.delete(realAddress);
        return addressMapper.mapToSuccessDeleteAddress();
    }

    //Helper Methods
    private User getUser(String email)
    {
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new UserNotFoundException("User with email "+email+" not found");
        return user.get();
    }

    private Address getAddress(Long addressId)
    {
        Optional<Address> address=addressRepository.findById(addressId);
        if(address.isEmpty())
            throw new AddressNotFoundException("Address with id "+addressId+" not found");
        return address.get();
    }
}
