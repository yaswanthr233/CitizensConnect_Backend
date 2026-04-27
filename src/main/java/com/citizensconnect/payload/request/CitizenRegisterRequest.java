package com.citizensconnect.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CitizenRegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 12, max = 12)
    private String aadhaar;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
