package com.charging.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank public String username;
    @NotBlank public String password;
    public String phone = "";
}
