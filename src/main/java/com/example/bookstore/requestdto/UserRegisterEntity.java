package com.example.bookstore.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterEntity
{
    @NotNull
    @Pattern(regexp = "^[A-Z][A-Za-z .]{2,}$",message = "First Name Should start with Capital and AtLeast contain 3 characters")
    private String firstName;

    @NotNull
    @Pattern(regexp = "^[A-Z][A-Za-z .]{2,}$",message = "Last Name Should start with Capital and AtLeast contain 3 characters")
    private String lastName;

    @Past
    private LocalDate dob;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z 0-9.@_-]{8,}$",message = "Password should be atleast 8 Characters")
    private String password;

    @NotNull
    @Email
    private String email;
    @NotNull
    private String role;
}
