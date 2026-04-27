package com.citizensconnect.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String aadhaar;

    @NotBlank
    private String password;
}
