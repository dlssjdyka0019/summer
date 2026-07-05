package com.charging.service;

import com.charging.dto.*;
import com.charging.model.User;
import com.charging.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final Map<String, TokenInfo> tokens = new HashMap<>(); // token -> {userId, role, expires}

    public AuthService(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    public LoginResponse register(RegisterRequest req) {
        if (req.username == null || req.username.length() < 2)
            throw new RuntimeException("用户名至少2个字符");
        if (req.password == null || req.password.length() < 4)
            throw new RuntimeException("密码至少4个字符");
        if (userRepo.existsByUsername(req.username))
            throw new RuntimeException("用户名已存在");

        User user = new User(req.username, encoder.encode(req.password), "user", req.phone);
        user = userRepo.save(user);
        String token = createToken(user);
        return new LoginResponse(true, token, new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getPhone(), 0));
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!encoder.matches(req.password, user.getPassword()))
            throw new RuntimeException("用户名或密码错误");

        String token = createToken(user);
        return new LoginResponse(true, token, new UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getPhone(), 0));
    }

    public TokenInfo authenticate(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null || info.expires < System.currentTimeMillis()) {
            tokens.remove(token);
            throw new RuntimeException("未登录或登录已过期");
        }
        return info;
    }

    public TokenInfo requireAdmin(String token) {
        TokenInfo info = authenticate(token);
        if (!"admin".equals(info.role))
            throw new RuntimeException("需要管理员权限");
        return info;
    }

    private String createToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenInfo info = new TokenInfo();
        info.userId = user.getId();
        info.role = user.getRole();
        info.expires = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L;
        tokens.put(token, info);
        return token;
    }

    public static class TokenInfo {
        public Long userId;
        public String role;
        public long expires;
    }
}
