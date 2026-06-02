package com.learn.tasktracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email cannot be blank")
    private String email;
}