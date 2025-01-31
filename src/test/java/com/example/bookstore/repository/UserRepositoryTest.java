package com.example.bookstore.repository;

import com.example.bookstore.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class UserRepositoryTest
{
    @Autowired
    private UserRepository userRepository;

    @Test
    void registerExistsByEmailTest()
    {
        String email="marrisaichandu143@gmail.com";
        User user=User.builder()
                .firstName("Sai chandu")
                .lastName("Marri")
                .dob(LocalDate.of(2002,8,24))
                .email(email)
                .build();
        userRepository.save(user);

        boolean userShouldExist=userRepository.existsByEmail(email);
        boolean userShouldNotExist=userRepository.existsByEmail("someemail@gmail.com");

        assertTrue(userShouldExist);
        assertFalse(userShouldNotExist);
    }

    @Test
    void findByEmailTest()
    {
        String email="testing@gmail.com";
        User user=User.builder()
                .email(email)
                .build();
        userRepository.save(user);

        Optional<User> userShouldReturn=userRepository.findByEmail(email);
        Optional<User> userShouldNotReturn=userRepository.findByEmail("test@gmail.com");

        assertNotNull(userShouldReturn.get());
        assertEquals(email,userShouldReturn.get().getEmail());
        assertEquals(Optional.empty(),userShouldNotReturn);
    }
}