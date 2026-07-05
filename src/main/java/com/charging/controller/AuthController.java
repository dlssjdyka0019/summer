package com.charging.controller;

import com.charging.dto.*;
import com.charging.model.User;
import com.charging.repository.UserRepository;
import com.charging.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    public AuthController(AuthService authService, UserRepository userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserInfo me(@RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo info = authService.authenticate(auth.replace("Bearer ", ""));
        User user = userRepo.findById(info.userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        return new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getPhone());
    }
}
