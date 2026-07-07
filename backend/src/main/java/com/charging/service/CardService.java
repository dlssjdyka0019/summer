package com.charging.service;

import com.charging.model.*;
import com.charging.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final ElectricityCardRepository cardRepo;
    private final StationPhotoRepository photoRepo;
    private final RedemptionCodeRepository codeRepo;
    private final UserRepository userRepo;

    public CardService(ElectricityCardRepository cardRepo, StationPhotoRepository photoRepo,
                       RedemptionCodeRepository codeRepo, UserRepository userRepo) {
        this.cardRepo = cardRepo;
        this.photoRepo = photoRepo;
        this.codeRepo = codeRepo;
        this.userRepo = userRepo;
    }

    // === Audit log ===
    public List<Map<String, Object>> getAuditLog() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<StationPhoto> approved = photoRepo.findByStatusOrderByCreatedAtDesc("approved");
        for (StationPhoto p : approved) {
            List<ElectricityCard> cards = cardRepo.findByPhotoId(p.getId());
            long activeCount = cards.stream().filter(c -> "active".equals(c.getStatus())).count();
            long revokedCount = cards.stream().filter(c -> "revoked".equals(c.getStatus())).count();
            Map<String, Object> m = new HashMap<>();
            m.put("photoId", p.getId());
            m.put("stationName", p.getStationName());
            m.put("username", p.getUsername());
            m.put("cardCount", p.getCardCount());
            m.put("cardAmount", p.getCardAmount());
            m.put("approvedAt", p.getCreatedAt());
            m.put("activeCards", activeCount);
            m.put("revokedCards", revokedCount);
            m.put("totalValue", cards.stream().filter(c -> "active".equals(c.getStatus())).mapToDouble(ElectricityCard::getAmount).sum());
            m.put("cards", cards.stream().map(this::cardToMap).collect(Collectors.toList()));
            result.add(m);
        }
        return result;
    }

    // === Revoke cards by photo ===
    public Map<String, Object> revokeByPhoto(String photoId, String adminName) {
        List<ElectricityCard> cards = cardRepo.findByPhotoId(photoId);
        int count = 0;
        for (ElectricityCard c : cards) {
            if ("active".equals(c.getStatus())) {
                c.setStatus("revoked");
                c.setRevokedBy(adminName);
                c.setRevokedAt(LocalDateTime.now());
                cardRepo.save(c);
                count++;
            }
        }
        return Map.of("ok", true, "message", "已追回" + count + "张电费卡");
    }

    // === Revoke single card ===
    public Map<String, Object> revokeCard(String cardId, String adminName) {
        ElectricityCard c = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException("卡不存在"));
        if ("revoked".equals(c.getStatus())) throw new RuntimeException("该卡已被追回");
        c.setStatus("revoked");
        c.setRevokedBy(adminName);
        c.setRevokedAt(LocalDateTime.now());
        cardRepo.save(c);
        return Map.of("ok", true, "message", "已追回电费卡");
    }

    // === Delete revoked card (by user) ===
    public Map<String, Object> deleteCard(String cardId, Long userId) {
        ElectricityCard c = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException("卡不存在"));
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"revoked".equals(c.getStatus())) throw new RuntimeException("只能删除已追回的卡片");
        cardRepo.delete(c);
        return Map.of("ok", true, "message", "已删除");
    }

    // === Generate redemption codes ===
    public Map<String, Object> generateCodes(int count, double amount, String adminName) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String cd = generateCode();
            RedemptionCode rc = new RedemptionCode();
            rc.setId("cd" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            rc.setCode(cd);
            rc.setAmount(amount);
            rc.setStatus("unused");
            rc.setCreatedBy(adminName);
            rc.setCreatedAt(LocalDateTime.now());
            codeRepo.save(rc);
            codes.add(cd);
        }
        return Map.of("ok", true, "codes", codes, "count", count, "amount", amount,
                      "message", "已生成" + count + "个兑换码，每个¥" + String.format("%.2f", amount));
    }

    // === List codes ===
    public List<Map<String, Object>> listCodes() {
        return codeRepo.findAllByOrderByCreatedAtDesc().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("code", c.getCode());
            m.put("amount", c.getAmount());
            m.put("status", c.getStatus());
            m.put("usedByName", c.getUsedByName());
            m.put("usedAt", c.getUsedAt());
            m.put("createdBy", c.getCreatedBy());
            m.put("createdAt", c.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }

    // === Redeem code (by user) ===
    public Map<String, Object> redeemCode(String code, Long userId, String username) {
        RedemptionCode rc = codeRepo.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException("兑换码无效"));
        if ("used".equals(rc.getStatus())) throw new RuntimeException("兑换码已被使用");

        rc.setStatus("used");
        rc.setUsedBy(userId);
        rc.setUsedByName(username);
        rc.setUsedAt(LocalDateTime.now());
        codeRepo.save(rc);

        // Create electricity card
        ElectricityCard card = new ElectricityCard();
        card.setId("ec" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        card.setUserId(userId);
        card.setAmount(rc.getAmount());
        card.setStationName("兑换码兑换");
        card.setPhotoId("redeem-" + rc.getId());
        card.setAdminName("兑换码");
        card.setStatus("active");
        card.setCreatedAt(LocalDateTime.now());
        cardRepo.save(card);

        return Map.of("ok", true, "message", "兑换成功！获得¥" + String.format("%.2f", rc.getAmount()) + "电费卡",
                      "amount", rc.getAmount());
    }

    // === Admin: send cards to specific user ===
    public Map<String, Object> sendToUser(String targetUsername, int count, double amount, String adminName) {
        User target = userRepo.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        double total = 0;
        for (int i = 0; i < count; i++) {
            ElectricityCard card = new ElectricityCard();
            card.setId("ec" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            card.setUserId(target.getId());
            card.setAmount(amount);
            card.setStationName("管理员发放");
            card.setPhotoId("send-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
            card.setAdminName(adminName);
            card.setStatus("active");
            card.setCreatedAt(LocalDateTime.now());
            cardRepo.save(card);
            total += amount;
        }
        return Map.of("ok", true, "message", "已向" + targetUsername + "发放" + count + "张电费卡，共¥" + String.format("%.2f", total));
    }

    // === Get user cards (with status) ===
    public List<Map<String, Object>> getUserCards(Long userId) {
        return cardRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::cardToMap).collect(Collectors.toList());
    }

    public int getUserActiveCardCount(Long userId) {
        return cardRepo.countByUserIdAndStatus(userId, "active");
    }

    // === Helpers ===
    private Map<String, Object> cardToMap(ElectricityCard c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("amount", c.getAmount());
        m.put("stationName", c.getStationName());
        m.put("photoId", c.getPhotoId());
        m.put("adminName", c.getAdminName() != null ? c.getAdminName() : "系统");
        m.put("status", c.getStatus());
        m.put("revokedBy", c.getRevokedBy());
        m.put("revokedAt", c.getRevokedAt());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
            if (i > 0 && i % 4 == 0 && i < 11) sb.append('-');
        }
        return sb.toString();
    }
}
