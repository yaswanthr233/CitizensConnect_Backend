package com.citizensconnect.security.services;

import com.citizensconnect.models.User;
import com.citizensconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String aadhaar) throws UsernameNotFoundException {
        User user = userRepository.findByAadhaar(aadhaar)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with aadhaar: " + aadhaar));

        return UserDetailsImpl.build(user);
    }
}
