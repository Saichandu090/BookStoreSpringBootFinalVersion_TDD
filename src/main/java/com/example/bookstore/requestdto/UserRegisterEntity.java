package com.example.bookstore.requestdto;

import jakarta.validation.constraints.*;
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
    @Pattern(regexp = "^[A-Z][a-zA-Z .,'-_=+]{2,}$",message = "First Name Should start with Capital and AtLeast contain 3 characters")
    private String firstName;

    @NotNull
    @Pattern(regexp = "^[A-Z][a-zA-Z .,'-_=+]{2,}$",message = "Last Name Should start with Capital and AtLeast contain 3 characters")
    private String lastName;

    @Past
    private LocalDate dob;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern.List({
            @Pattern(regexp = ".*[a-z].*", message = "Password must contain lowercase letter"),
            @Pattern(regexp = ".*[A-Z].*", message = "Password must contain uppercase letter"),
            @Pattern(regexp = ".*\\d.*", message = "Password must contain number"),
            @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",./<>?].*", message = "Password must contain special character")
    })
    private String password;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String role;
}
