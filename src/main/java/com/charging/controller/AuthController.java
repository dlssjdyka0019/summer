package com.charging.controller;

import com.charging.dto.*;
import com.charging.model.User;
import com.charging.repository.ElectricityCardRepository;
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
    private final ElectricityCardRepository cardRepo;

    public AuthController(AuthService authService, UserRepository userRepo, ElectricityCardRepository cardRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.cardRepo = cardRepo;
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse res = authService.register(req);
        res.user.electricityCards = 0; // new user has 0 cards
        return res;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        LoginResponse res = authService.login(req);
        User user = userRepo.findById(res.user.id).orElse(null);
        if (user != null) res.user.electricityCards = cardRepo.countByUserId(user.getId());
        return res;
    }

    @GetMapping("/me")
    public UserInfo me(@RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo info = authService.authenticate(auth.replace("Bearer ", ""));
        User user = userRepo.findById(info.userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        int realCardCount = cardRepo.countByUserId(user.getId());
        return new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getPhone(), realCardCount);
    }
}
