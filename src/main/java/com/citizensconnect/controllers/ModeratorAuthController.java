package com.citizensconnect.controllers;

import com.citizensconnect.payload.request.LoginRequest;
import com.citizensconnect.payload.response.JwtResponse;
import com.citizensconnect.payload.response.MessageResponse;
import com.citizensconnect.security.jwt.JwtUtils;
import com.citizensconnect.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/moderator")
public class ModeratorAuthController {

    @Autowired
    AuthenticationManager authenticationManager;

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
                
        if(!roles.contains("MODERATOR")) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized for moderator portal."));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 userDetails.getId(), 
                                                 userDetails.getName(), 
                                                 userDetails.getAadhaar(), 
                                                 roles));
    }
}
