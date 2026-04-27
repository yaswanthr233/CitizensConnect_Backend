package com.citizensconnect.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String aadhaar;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String name, String aadhaar, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.name = name;
        this.aadhaar = aadhaar;
        this.roles = roles;
    }
}
