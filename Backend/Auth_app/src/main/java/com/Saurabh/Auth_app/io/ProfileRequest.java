package com.Saurabh.Auth_app.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "name should not be empty")
    private String name;
    @Email(message = "Enter valid email address")
    @NotNull(message = "email should not be empty")
    private String email;
    @Size(min = 6, message = "Password should have atleast 6 characters")
    private String password;
}
