package com.charging.controller;

import com.charging.dto.*;
import com.charging.service.AuthService;
import com.charging.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;
    private final AuthService authService;

    public SessionController(SessionService sessionService, AuthService authService) {
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @GetMapping("/sessions")
    public List<SessionDTO> getSessions(@RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo info = authService.authenticate(auth.replace("Bearer ", ""));
        return sessionService.getUserSessions(info.userId);
    }

    @PostMapping("/sessions")
    public Map<String, Object> create(@RequestBody SessionRequest req, @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo info = authService.authenticate(auth.replace("Bearer ", ""));
        if (!"user".equals(info.role))
            throw new RuntimeException("只有用户可以进行充电");
        SessionDTO created = sessionService.createSession(info.userId, req);
        return Map.of("ok", true, "id", created.id);
    }

    @PutMapping("/sessions/{id}/stop")
    public Map<String, Object> stop(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo info = authService.authenticate(auth.replace("Bearer ", ""));
        sessionService.stopSession(id, info.userId);
        return Map.of("ok", true);
    }
}
