package com.example.demo.serviceimpl;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.CartNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.service.CartService;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService
{
    private CartRepository cartRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private final CartMapper cartMapper=new CartMapper();

    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<CartResponseDto>> addToCart(String email, CartRequestDto cartRequestDto)
    {
        User user=getUser(email);
        Book book=getBook(cartRequestDto.getBookId());
        if(book.getBookQuantity()<1)
            return cartMapper.mapToBookOutOfStock(book);
        if(user.getCarts()==null)
            user.setCarts(new ArrayList<>());
        return addBookToCart(user,book.getBookId());
    }


    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<CartResponseDto>> removeFromCart(String email, Long cartId)
    {
        User user=getUser(email);
        Cart cart=getCart(cartId);
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            throw new CartNotFoundException("Cart is empty");
        Iterator<Cart> iterator = userCarts.iterator();
        return removeBookFromUserCart(iterator,cart);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> getCartItems(String email)
    {
        User user=getUser(email);
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            return cartMapper.mapToCartIsEmpty();
        List<CartResponseDto> cartResponseDto=userCarts.stream().map(cart -> new CartResponseDto(cart.getCartId(),cart.getBookId(),cart.getCartQuantity())).toList();
        return cartMapper.mapToSuccessGetCart(cartResponseDto);
    }

    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<CartResponseDto>> clearCart(String email)
    {
        User user=getUser(email);
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            return cartMapper.mapToNoContentCartIsEmpty();
        Iterator<Cart> cartIterator=userCarts.iterator();
        while (cartIterator.hasNext()) {
            Cart cart = cartIterator.next();
            deleteCartAndUpdateBook(cart, cartIterator);
        }
        return cartMapper.mapToSuccessClearCart();
    }


    //Helper Methods
    private void deleteCartAndUpdateBook(Cart cart,Iterator<Cart> cartIterator)
    {
        while (cart.getCartQuantity() > 0) {
            cart.setCartQuantity(cart.getCartQuantity() - 1);
            updateBookQuantity(getBook(cart.getBookId()));
            if (cart.getCartQuantity() == 0) {
                cartIterator.remove();
                cartRepository.delete(cart);
                break;
            } else {
                cartRepository.save(cart);
            }
        }
    }


    private ResponseEntity<ResponseStructure<CartResponseDto>> removeBookFromUserCart(Iterator<Cart> iterator,Cart cart)
    {
        Long bookId = processUserCart(iterator, cart);
        Book book=updateBookQuantity(getBook(bookId));
        return cartMapper.mapToSuccessRemoveFromCart(book.getBookName());
    }

    private Long processUserCart(Iterator<Cart> iterator, Cart cart) {
        while (iterator.hasNext()) {
            Cart userCart = iterator.next();
            if (userCart.getCartId().equals(cart.getCartId())) {
                return handleCartUpdate(userCart, iterator);
            }
        }
        throw new CartNotFoundException("Cart not found");
    }

    private Long handleCartUpdate(Cart userCart, Iterator<Cart> iterator) {
        Long bookId = userCart.getBookId();
        if (userCart.getCartQuantity() <= 1) {
            iterator.remove();
            cartRepository.delete(userCart);
        } else {
            userCart.setCartQuantity(userCart.getCartQuantity() - 1);
            cartRepository.save(userCart);
        }
        return bookId;
    }

    private Book updateBookQuantity(Book book)
    {
        book.setBookQuantity(book.getBookQuantity()+1);
        book.setCartBookQuantity(book.getCartBookQuantity()-1);
        return bookRepository.save(book);
    }


    private ResponseEntity<ResponseStructure<CartResponseDto>> addBookToCart(User user,Long bookId)
    {
        Book book=getBook(bookId);
        book.setBookQuantity(book.getBookQuantity()-1);
        book.setCartBookQuantity(book.getCartBookQuantity()+1);
        Cart userCart = user.getCarts()
                .stream()
                .filter(cart -> cart.getBookId().equals(bookId))
                .findFirst()
                .orElseGet(() -> Cart.builder().bookId(bookId).cartQuantity(0).userId(user.getUserId()).build());
        userCart.setCartQuantity(userCart.getCartQuantity()+1);
        bookRepository.save(book);
        return cartMapper.mapToSuccessAddToCart(cartRepository.save(userCart));
    }


    private Cart getCart(Long cartId)
    {
        Optional<Cart> cart=cartRepository.findById(cartId);
        if(cart.isEmpty())
            throw new CartNotFoundException("Cart not found with Id "+cartId);
        return cart.get();
    }

    private Book getBook(Long bookId)
    {
        Optional<Book> book=bookRepository.findByIdForUpdate(bookId);
        if(book.isEmpty())
            throw new BookNotFoundException("Book not found with Id "+bookId);
        return book.get();
    }

    private User getUser(String email)
    {
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new UserNotFoundException("User not found with username "+email);
        return user.get();
    }
}
