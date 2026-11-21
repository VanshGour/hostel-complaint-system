package com.hostel.complaint.service;

import com.hostel.complaint.dto.AuthRequest;
import com.hostel.complaint.dto.AuthResponse;
import com.hostel.complaint.dto.RegisterRequest;
import com.hostel.complaint.exception.UnauthorizedActionException;
import com.hostel.complaint.model.User;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final String allowedDomain;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       @Value("${app.college.allowed-domain:campus.edu}") String allowedDomain) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.allowedDomain = allowedDomain;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        // 1. Enforce College Email Domain Restriction
        if (!email.endsWith("@" + allowedDomain.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("Registration restricted: Email must belong to the college domain '@%s'", allowedDomain)
            );
        }

        // 2. Check for duplicate registration
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // 3. Save User
        User user = new User(
                email,
                passwordEncoder.encode(req.getPassword()),
                req.getFullName().trim(),
                req.getPhoneNumber(),
                req.getRole(),
                req.getHostelBlock(),
                req.getRoomNumber(),
                req.getTradeSpecialty()
        );
        userRepository.save(user);

        // 4. Authenticate and Generate JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getHostelBlock(),
                user.getRoomNumber(),
                user.getTradeSpecialty()
        );
    }

    public AuthResponse login(AuthRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("User account not found"));

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getHostelBlock(),
                user.getRoomNumber(),
                user.getTradeSpecialty()
        );
    }
}
