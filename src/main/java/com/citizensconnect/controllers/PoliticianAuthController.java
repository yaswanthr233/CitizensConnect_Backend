package com.citizensconnect.controllers;

import com.citizensconnect.models.Role;
import com.citizensconnect.models.Politician;
import com.citizensconnect.models.UserStatus;
import com.citizensconnect.payload.request.PoliticianRegisterRequest;
import com.citizensconnect.payload.request.LoginRequest;
import com.citizensconnect.payload.response.JwtResponse;
import com.citizensconnect.payload.response.MessageResponse;
import com.citizensconnect.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/politician")
public class PoliticianAuthController {

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
                
        if(!roles.contains("POLITICIAN")) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Unauthorized for politician portal."));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 userDetails.getId(), 
                                                 userDetails.getName(), 
                                                 userDetails.getAadhaar(), 
                                                 roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody PoliticianRegisterRequest signUpRequest) {
        if (userRepository.existsByAadhaar(signUpRequest.getAadhaar())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Aadhaar is already in use!"));
        }

        Politician user = new Politician();
        user.setName(signUpRequest.getName());
        user.setAadhaar(signUpRequest.getAadhaar());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.POLITICIAN);
        user.setStatus(UserStatus.PENDING); // Politician requires approval
        user.setPartyName(signUpRequest.getPartyName());
        user.setConstituency(signUpRequest.getConstituency());
        user.setGovtIdProof(signUpRequest.getGovtIdProof());

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Politician registered successfully. Pending Admin approval!"));
    }
}
