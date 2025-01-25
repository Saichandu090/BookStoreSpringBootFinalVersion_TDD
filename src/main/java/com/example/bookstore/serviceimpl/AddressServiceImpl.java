package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.Address;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.AddressNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.AddressMapper;
import com.example.bookstore.repository.AddressRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.requestdto.AddressRequest;
import com.example.bookstore.responsedto.AddressResponse;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.util.ResponseStructure;
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
    public ResponseEntity<ResponseStructure<AddressResponse>> addAddress(String email, AddressRequest addressRequest)
    {
        User realUser=getUser(email);
        Address address=addressMapper.mapAddressRequestToAddress(realUser, addressRequest);
        if(realUser.getAddresses()==null)
            realUser.setAddresses(new ArrayList<>());
        realUser.getAddresses().add(address);
        Address savedAddress=addressRepository.save(address);
        return addressMapper.mapToSuccessAddAddress(savedAddress);
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponse>> updateAddress(String email, Long addressId, AddressRequest addressRequest)
    {
        User user=getUser(email);
        Address realAddress=getAddress(addressId,user.getUserId());
        Address updatedAddress=addressMapper.mapOldAddressToNewAddress(realAddress, addressRequest);
        Address saved=addressRepository.save(updatedAddress);
        return addressMapper.mapToSuccessUpdateAddress(saved,user.getEmail());
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponse>> getAddressById(String email, Long addressId)
    {
        User user=getUser(email);
        Address resultAddress=getAddress(addressId,user.getUserId());
        return addressMapper.mapToSuccessGetAddressById(resultAddress);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<AddressResponse>>> getAllAddress(String email)
    {
        User realUser=getUser(email);
        List<Address> addresses=addressRepository.findByUserId(realUser.getUserId());
        if(addresses.isEmpty())
            return addressMapper.mapToNoContentForGetAllAddress();
        List<AddressResponse> responseDtoList=addresses.stream().map(addressMapper::mapAddressToAddressResponse).toList();//Converting addressList into list of AddressResponse using stream and AddressMapper
        return addressMapper.mapToSuccessGetAllAddress(responseDtoList);
    }


    @Override
    public ResponseEntity<ResponseStructure<AddressResponse>> deleteAddress(String email, Long addressId)
    {
        User realUser=getUser(email);
        Address realAddress=getAddress(addressId,realUser.getUserId());
        realUser.getAddresses().remove(realAddress);
        realAddress.setUserId(null);
        addressRepository.save(realAddress);
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

    private Address getAddress(Long addressId,Long userId)
    {
        Optional<Address> address=addressRepository.findByAddressIdAndUserId(addressId,userId);
        if(address.isEmpty())
            throw new AddressNotFoundException("Address with id "+addressId+" not found");
        return address.get();
    }
}
