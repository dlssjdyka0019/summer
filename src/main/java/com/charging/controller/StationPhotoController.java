package com.charging.controller;

import com.charging.repository.UserRepository;
import com.charging.service.AuthService;
import com.charging.service.StationPhotoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StationPhotoController {

    private final StationPhotoService photoService;
    private final AuthService authService;
    private final UserRepository userRepo;

    public StationPhotoController(StationPhotoService photoService, AuthService authService, UserRepository userRepo) {
        this.photoService = photoService;
        this.authService = authService;
        this.userRepo = userRepo;
    }

    /** User uploads a photo */
    @PostMapping("/stations/{sid}/photos")
    public Map<String, Object> upload(@PathVariable String sid,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestHeader("Authorization") String auth) throws IOException {
        AuthService.TokenInfo user = authService.authenticate(auth.replace("Bearer ", ""));
        return photoService.uploadPhoto(sid, user.userId, file);
    }

    /** Get approved photos for a station */
    @GetMapping("/stations/{sid}/photos")
    public List<Map<String, Object>> getStationPhotos(@PathVariable String sid) {
        return photoService.getApprovedPhotos(sid);
    }

    /** Admin: list pending photos (includes hints) */
    @GetMapping("/admin/photos/pending")
    public List<Map<String, Object>> getPending(@RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return photoService.getPendingPhotos();
    }

    /** Admin: approve with card count and amount */
    @PutMapping("/admin/photos/{pid}/approve")
    public Map<String, Object> approve(@PathVariable String pid,
                                       @RequestBody Map<String, Object> body,
                                       @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo admin = authService.requireAdmin(auth.replace("Bearer ", ""));
        int cardCount = body.containsKey("cardCount") ? ((Number) body.get("cardCount")).intValue() : 1;
        double cardAmount = body.containsKey("cardAmount") ? ((Number) body.get("cardAmount")).doubleValue() : 10.0;
        return photoService.approvePhoto(pid, cardCount, cardAmount, admin.userId,
                userRepo.findById(admin.userId).map(u -> u.getUsername()).orElse("admin"));
    }

    /** Admin: reject photo */
    @PutMapping("/admin/photos/{pid}/reject")
    public Map<String, Object> reject(@PathVariable String pid,
                                      @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return photoService.rejectPhoto(pid);
    }

    /** Admin: get/set default reward config */
    @GetMapping("/admin/card-defaults")
    public Map<String, Object> getDefaultConfig(@RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return photoService.getDefaultConfig();
    }

    @PutMapping("/admin/card-defaults")
    public Map<String, Object> setDefaultConfig(@RequestBody Map<String, Object> body,
                                                 @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        int count = body.containsKey("defaultCardCount") ? ((Number) body.get("defaultCardCount")).intValue() : 1;
        double amount = body.containsKey("defaultCardAmount") ? ((Number) body.get("defaultCardAmount")).doubleValue() : 10.0;
        photoService.setDefaultConfig(count, amount);
        return Map.of("ok", true, "message", "默认值已更新");
    }
}
