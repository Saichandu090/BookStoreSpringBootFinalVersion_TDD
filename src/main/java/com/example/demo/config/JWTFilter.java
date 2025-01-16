package com.example.demo.config;

import com.example.demo.serviceimpl.JWTService;
import com.example.demo.serviceimpl.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter
{
    private HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    public JWTFilter(HandlerExceptionResolver handlerExceptionResolver)
    {
        this.handlerExceptionResolver=handlerExceptionResolver;
    }

    @Autowired
    private JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        try {

            String requestPath = request.getRequestURI();
            if (requestPath.equals("/register") || requestPath.equals("/login"))
            {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");
            String token = null;
            String userName = null;

            if (authHeader != null && authHeader.startsWith("Bearer "))
            {
                token = authHeader.substring(7);
                userName = jwtService.extractEmail(token);
            }

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null)
            {
                UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(userName);
                if (jwtService.validateToken(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        }
        catch(Exception exception)
        {
            handlerExceptionResolver.resolveException(request,response,null,exception);
        }
    }
}
