package com.charging.service;

import com.charging.model.*;
import com.charging.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationPhotoService {

    private final StationPhotoRepository photoRepo;
    private final StationRepository stationRepo;
    private final UserRepository userRepo;
    private final ElectricityCardRepository cardRepo;

    // Upload directory
    private static final String UPLOAD_DIR = "uploads/photos/";

    // Default reward config for admin convenience
    private int defaultCardCount = 1;
    private double defaultCardAmount = 10.0;

    public StationPhotoService(StationPhotoRepository photoRepo,
                               StationRepository stationRepo,
                               UserRepository userRepo,
                               ElectricityCardRepository cardRepo) {
        this.photoRepo = photoRepo;
        this.stationRepo = stationRepo;
        this.userRepo = userRepo;
        this.cardRepo = cardRepo;
    }

    /** User uploads a photo */
    public Map<String, Object> uploadPhoto(String stationId, Long userId, MultipartFile file) throws IOException {
        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new RuntimeException("充电站不存在"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String ext = getExtension(file.getOriginalFilename());
        String photoId = "ph" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String savedName = photoId + ext;
        Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR + savedName), StandardCopyOption.REPLACE_EXISTING);

        StationPhoto photo = new StationPhoto();
        photo.setId(photoId);
        photo.setStationId(stationId);
        photo.setStationName(station.getName());
        photo.setUserId(userId);
        photo.setUsername(user.getUsername());
        photo.setFilePath(savedName);
        photo.setOriginalName(file.getOriginalFilename());
        photo.setStatus("pending");
        photo.setCreatedAt(LocalDateTime.now());
        photoRepo.save(photo);

        return Map.of("ok", true, "id", photoId, "message", "照片已上传，等待管理员审核");
    }

    /** Get approved photos for a station */
    public List<Map<String, Object>> getApprovedPhotos(String stationId) {
        return photoRepo.findByStationIdAndStatusOrderByCreatedAtDesc(stationId, "approved")
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    /** Get pending photos for admin review, with hints */
    public List<Map<String, Object>> getPendingPhotos() {
        return photoRepo.findByStatusOrderByCreatedAtDesc("pending")
                .stream().map(p -> {
                    Map<String, Object> m = toMap(p);
                    // Add hints for admin
                    m.put("hints", generateHints(p));
                    // Add station last update info
                    m.put("stationUpdatedAt", getStationUpdateTime(p.getStationId()));
                    m.put("stationPhotoCount", countStationPhotos(p.getStationId()));
                    return m;
                }).collect(Collectors.toList());
    }

    /** Admin approves a photo with custom card reward */
    public Map<String, Object> approvePhoto(String photoId, int cardCount, double cardAmount, Long adminId, String adminName) {
        StationPhoto photo = photoRepo.findById(photoId)
                .orElseThrow(() -> new RuntimeException("照片不存在"));
        if (!"pending".equals(photo.getStatus()))
            throw new RuntimeException("只能审核待审核的照片");

        photo.setStatus("approved");
        photo.setCardCount(cardCount);
        photo.setCardAmount(cardAmount);
        photoRepo.save(photo);

        User user = userRepo.findById(photo.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Create individual electricity cards
        double totalValue = 0;
        for (int i = 0; i < cardCount; i++) {
            ElectricityCard card = new ElectricityCard();
            card.setId("ec" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            card.setUserId(user.getId());
            card.setAmount(cardAmount);
            card.setStationId(photo.getStationId());
            card.setStationName(photo.getStationName());
            card.setPhotoId(photoId);
            card.setAdminId(adminId);
            card.setAdminName(adminName);
            card.setCreatedAt(LocalDateTime.now());
            cardRepo.save(card);
            totalValue += cardAmount;
        }

        // Update cached count in user table
        user.setElectricityCards(cardRepo.countByUserId(user.getId()));
        userRepo.save(user);

        return Map.of("ok", true, "message", "审核通过！发放" + cardCount + "张电费卡，共¥" + String.format("%.2f", totalValue),
                      "cardCount", cardCount, "cardAmount", cardAmount, "totalValue", totalValue);
    }

    /** Admin rejects a photo */
    public Map<String, Object> rejectPhoto(String photoId) {
        StationPhoto photo = photoRepo.findById(photoId)
                .orElseThrow(() -> new RuntimeException("照片不存在"));
        if (!"pending".equals(photo.getStatus()))
            throw new RuntimeException("只能审核待审核的照片");
        photo.setStatus("rejected");
        photoRepo.save(photo);
        return Map.of("ok", true, "message", "照片已驳回");
    }

    // === Default config ===
    public Map<String, Object> getDefaultConfig() {
        return Map.of("defaultCardCount", defaultCardCount, "defaultCardAmount", defaultCardAmount);
    }
    public void setDefaultConfig(int count, double amount) {
        this.defaultCardCount = count;
        this.defaultCardAmount = amount;
    }

    // === Hint generation ===
    private List<String> generateHints(StationPhoto photo) {
        List<String> hints = new ArrayList<>();
        // Check if first photo for this station (any status)
        long stationPhotoCount = photoRepo.findByStationIdOrderByCreatedAtDesc(photo.getStationId()).size();
        if (stationPhotoCount == 1) {
            hints.add("💡 这是该站点的首张照片，建议多给点电费卡鼓励用户！");
        }
        // Check if station hasn't been updated in a while
        Station station = stationRepo.findById(photo.getStationId()).orElse(null);
        if (station != null && station.getUpdatedAt() != null) {
            long days = ChronoUnit.DAYS.between(station.getUpdatedAt(), LocalDateTime.now());
            if (days > 30) {
                hints.add("⏰ 该站点已 " + days + " 天未更新，此照片较珍贵，建议多给点！");
            } else if (days > 7) {
                hints.add("📅 该站点已 " + days + " 天未更新");
            }
        }
        return hints;
    }

    private Object getStationUpdateTime(String stationId) {
        Station s = stationRepo.findById(stationId).orElse(null);
        return s != null ? s.getUpdatedAt() : null;
    }

    private int countStationPhotos(String stationId) {
        return (int) photoRepo.findByStationIdOrderByCreatedAtDesc(stationId).size();
    }

    private Map<String, Object> toMap(StationPhoto p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("stationId", p.getStationId());
        m.put("stationName", p.getStationName());
        m.put("userId", p.getUserId());
        m.put("username", maskUsername(p.getUsername()));
        m.put("filePath", p.getFilePath());
        m.put("originalName", p.getOriginalName());
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt());
        m.put("cardCount", p.getCardCount());
        m.put("cardAmount", p.getCardAmount());
        return m;
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return username + "**";
        int show = Math.max(1, username.length() / 3);
        return username.substring(0, show) + "***" + username.substring(username.length() - show);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp").contains(ext)) return ext;
        return ".jpg";
    }
}
