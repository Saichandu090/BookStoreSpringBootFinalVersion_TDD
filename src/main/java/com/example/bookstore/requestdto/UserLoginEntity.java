package com.example.bookstore.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginEntity
{
    @Email(message = "Email should not be empty")
    private String email;

    @NotNull(message = "Password should not be empty")
    private String password;
}
