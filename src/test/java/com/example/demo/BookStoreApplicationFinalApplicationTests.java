package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.requestdto.BookRequest;
import com.example.demo.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
class BookStoreApplicationFinalApplicationTests {

	@Autowired
	private BookService bookService;

	@Mock
	private BookRepository bookRepository;

	@MockitoBean
	private UserMapper userMapper;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private BookRequest bookRequest;
	private User user;
	private UserDetails userDetails;
	private String token;

	@BeforeEach
	void init()
	{
		token="Bearer token";

		bookRequest = BookRequest.builder()
				.bookName("ABC")
				.bookPrice(123.3)
				.bookQuantity(85)
				.bookAuthor("XYZ")
				.bookDescription("UHH")
				.bookLogo("URL")
				.bookId(789654L).build();

		user=User.builder()
				.firstName("Sai")
				.lastName("Chandu")
				.userId(1L)
				.email("chandu@gmail.com")
				.password("chandu1234")
				.dob(LocalDate.of(2002,8,24))
				.role("ADMIN").build();

		userDetails=new UserDetails() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
			}

			@Override
			public String getPassword() {
				return user.getPassword();
			}

			@Override
			public String getUsername() {
				return user.getEmail();
			}
		};
	}

	@Test
	void addBookTest() throws Exception
	{
		when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

		mockMvc.perform(post("/book/addBook")
				.characterEncoding(StandardCharsets.UTF_8)
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bookRequest)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isCreated());
	}
}
