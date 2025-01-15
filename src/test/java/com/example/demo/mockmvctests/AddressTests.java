package com.example.demo.mockmvctests;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.AddressRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("h2")
public class AddressTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @MockitoBean
    private UserMapper userMapper;

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp()
    {
        user=User.builder()
                .email("sai@gmail.com")
                .userId(1L)
                .password("saichandu090")
                .dob(LocalDate.of(2002,8,24))
                .registeredDate(LocalDate.now())
                .firstName("Sai")
                .lastName("Chandu")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUsername() {
                return "";
            }
        };
    }

    @Test
    public void testAddAddressThroughController() throws Exception
    {
        AddressRequestDto addressRequestDto = new AddressRequestDto();
        addressRequestDto.setStreetName("123 Main St");
        addressRequestDto.setCity("Test City");
        addressRequestDto.setState("Test State");
        addressRequestDto.setPinCode(12345);

        mockMvc.perform(post("/address/addAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer some-valid-token")
                        .content(objectMapper.writeValueAsString(addressRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Address added successfully"))
                .andExpect(jsonPath("$.data.streetName").value("123 Main St"));


        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertEquals(1, user.getAddresses().size());
        Address addedAddress = user.getAddresses().getFirst();
        assertEquals("123 Main St", addedAddress.getStreetName());
        assertEquals("Test City", addedAddress.getCity());
    }
}
