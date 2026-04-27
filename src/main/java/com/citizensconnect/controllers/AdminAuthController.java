package com.citizensconnect.controllers;

import com.citizensconnect.models.Admin;
import com.citizensconnect.models.Role;
import com.citizensconnect.models.UserStatus;
import com.citizensconnect.payload.request.AdminRegisterRequest;
import com.citizensconnect.payload.request.LoginRequest;
import com.citizensconnect.payload.response.JwtResponse;
import com.citizensconnect.payload.response.MessageResponse;
import com.citizensconnect.repository.UserRepository;
import com.citizensconnect.security.jwt.JwtUtils;
import com.citizensconnect.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getAadhaar(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
        
        if (!"APPROVED".equals(userDetails.getStatus())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Account is " + userDetails.getStatus() + "."));
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
                
        if(!roles.contains("ADMIN")) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized for admin portal."));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 userDetails.getId(), 
                                                 userDetails.getName(), 
                                                 userDetails.getAadhaar(), 
                                                 roles));
    }

    @Operation(
            summary = "Register admin",
            description = "Creates a new admin account. The request must include the admin secret key.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Admin registration request",
                                    value = """
                                            {
                                              "name": "Krishna",
                                              "aadhaar": "944141254612",
                                              "password": "678910",
                                              "adminSecret": "9908"
                                            }
                                            """
                            )
                    )
            )
    )
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AdminRegisterRequest signUpRequest) {
        
        if (!"9908".equals(signUpRequest.getAdminSecret())) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Invalid admin secret key!"));
        }

        if (userRepository.existsByAadhaar(signUpRequest.getAadhaar())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Aadhaar is already in use!"));
        }

        Admin user = new Admin();
        user.setName(signUpRequest.getName());
        user.setAadhaar(signUpRequest.getAadhaar());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.APPROVED); 

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Admin registered successfully!"));
    }
}
