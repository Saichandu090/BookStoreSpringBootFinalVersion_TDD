package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.CartNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.CartMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.requestdto.CartRequest;
import com.example.bookstore.responsedto.CartResponse;
import com.example.bookstore.service.CartService;
import com.example.bookstore.util.ResponseStructure;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

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
    public ResponseEntity<ResponseStructure<CartResponse>> addToCart(String email, CartRequest cartRequest)
    {
        User user=getUser(email);
        Book book=getBook(cartRequest.getBookId());
        if(book.getBookQuantity()<1 || Boolean.FALSE.equals(book.getStatus()))
            return cartMapper.mapToBookOutOfStock(book);
        if(user.getCarts()==null)
            user.setCarts(new ArrayList<>());
        return addBookToCart(user,book.getBookId());
    }


    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<CartResponse>> removeFromCart(String email, Long cartId)
    {
        User user=getUser(email);
        Cart cart=getCart(cartId,user.getUserId());
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            throw new CartNotFoundException("Cart is empty");
        Iterator<Cart> cartIterator=userCarts.iterator();
        return removeBookFromUserCart(cartIterator,cart);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<CartResponse>>> getCartItems(String email)
    {
        User user=getUser(email);
        List<Cart> userCarts=user.getCarts();
        if(userCarts.isEmpty())
            return cartMapper.mapToCartIsEmpty();
        List<CartResponse> cartResponse =userCarts.parallelStream().map(cartMapper::mapToCartResponse).toList();//Converting userCarts into list of CartResponse using Stream and CartMapper
        return cartMapper.mapToSuccessGetCart(cartResponse);
    }

    @Transactional
    @Override
    public ResponseEntity<ResponseStructure<CartResponse>> clearCart(String email)
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
            increaseBookQuantity(getBook(cart.getBookId()));
            if (cart.getCartQuantity() == 0) {
                cartIterator.remove();
                cartRepository.delete(cart);
                break;
            } else {
                cartRepository.save(cart);
            }
        }
    }


    private ResponseEntity<ResponseStructure<CartResponse>> removeBookFromUserCart(Iterator<Cart> carts,Cart userCart)
    {
        Long bookId = processUserCart(carts,userCart);
        Book book=increaseBookQuantity(getBook(bookId));
        return cartMapper.mapToSuccessRemoveFromCart(book.getBookName());
    }

    private Long processUserCart(Iterator<Cart> iterator,Cart cart)
    {
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

    private Book increaseBookQuantity(Book book)
    {
        book.setBookQuantity(book.getBookQuantity()+1);
        book.setCartBookQuantity(book.getCartBookQuantity()-1);
        return bookRepository.save(book);
    }


    private ResponseEntity<ResponseStructure<CartResponse>> addBookToCart(User user, Long bookId)
    {
        Book book=getBook(bookId);
        book.setBookQuantity(book.getBookQuantity()-1);
        book.setCartBookQuantity(book.getCartBookQuantity()+1);
        Cart userCart = user.getCarts()
                .stream()
                .filter(cart -> cart.getBookId().equals(bookId))
                .findFirst()
                .orElseGet(() -> Cart.builder().bookId(bookId).cartQuantity(0).userId(user.getUserId()).build());//If user has the book in the cart already then fetching that or else creating a new cart for user with that book
        userCart.setCartQuantity(userCart.getCartQuantity()+1);
        bookRepository.save(book);
        return cartMapper.mapToSuccessAddToCart(cartRepository.save(userCart));
    }


    private Cart getCart(Long cartId,Long userId)
    {
        Optional<Cart> cart=cartRepository.findByCartIdAndUserId(cartId,userId);
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
