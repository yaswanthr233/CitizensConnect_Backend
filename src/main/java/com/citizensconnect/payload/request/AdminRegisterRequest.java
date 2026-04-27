package com.citizensconnect.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "AdminRegisterRequest", description = "Request payload for registering a new admin")
public class AdminRegisterRequest {
    @Schema(description = "Full name of the admin", example = "Krishna", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Schema(description = "12-digit Aadhaar number", example = "944141254612", minLength = 12, maxLength = 12, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 12, max = 12)
    private String aadhaar;

    @Schema(description = "Admin account password", example = "678910", minLength = 6, maxLength = 40, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Schema(description = "Secret key required to create an admin account", example = "9908", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String adminSecret;
}
