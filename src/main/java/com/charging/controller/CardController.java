package com.charging.controller;

import com.charging.service.AuthService;
import com.charging.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;
    private final AuthService authService;

    public CardController(CardService cardService, AuthService authService) {
        this.cardService = cardService;
        this.authService = authService;
    }

    /** User: get my cards */
    @GetMapping("/user/cards")
    public Map<String, Object> getUserCards(@RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo user = authService.authenticate(auth.replace("Bearer ", ""));
        List<Map<String, Object>> cards = cardService.getUserCards(user.userId);
        return Map.of("cards", cards, "total", cards.size(),
                      "activeTotal", cardService.getUserActiveCardCount(user.userId));
    }

    /** User: delete revoked card */
    @DeleteMapping("/user/cards/{cid}")
    public Map<String, Object> deleteCard(@PathVariable String cid,
                                          @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo user = authService.authenticate(auth.replace("Bearer ", ""));
        return cardService.deleteCard(cid, user.userId);
    }

    /** User: redeem a code */
    @PostMapping("/user/redeem")
    public Map<String, Object> redeemCode(@RequestBody Map<String, String> body,
                                          @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo user = authService.authenticate(auth.replace("Bearer ", ""));
        return cardService.redeemCode(body.get("code"), user.userId, body.getOrDefault("username", ""));
    }

    /** Admin: audit log (all approved photos with card details) */
    @GetMapping("/admin/cards/audit")
    public List<Map<String, Object>> getAuditLog(@RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return cardService.getAuditLog();
    }

    /** Admin: revoke all cards for a photo */
    @PutMapping("/admin/cards/revoke-by-photo/{photoId}")
    public Map<String, Object> revokeByPhoto(@PathVariable String photoId,
                                              @RequestHeader("Authorization") String auth) {
        AuthService.TokenInfo admin = authService.requireAdmin(auth.replace("Bearer ", ""));
        return cardService.revokeByPhoto(photoId, "admin"); // uses admin username from token
    }

    /** Admin: revoke single card */
    @PutMapping("/admin/cards/{cid}/revoke")
    public Map<String, Object> revokeCard(@PathVariable String cid,
                                          @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return cardService.revokeCard(cid, "admin");
    }

    /** Admin: generate redemption codes */
    @PostMapping("/admin/codes/generate")
    public Map<String, Object> generateCodes(@RequestBody Map<String, Object> body,
                                              @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        int count = body.containsKey("count") ? ((Number) body.get("count")).intValue() : 1;
        double amount = body.containsKey("amount") ? ((Number) body.get("amount")).doubleValue() : 10.0;
        return cardService.generateCodes(count, amount, "admin");
    }

    /** Admin: list codes */
    @GetMapping("/admin/codes")
    public List<Map<String, Object>> listCodes(@RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return cardService.listCodes();
    }

    /** Admin: send cards to specific user */
    @PostMapping("/admin/cards/send")
    public Map<String, Object> sendToUser(@RequestBody Map<String, Object> body,
                                           @RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        String username = (String) body.get("username");
        int count = body.containsKey("count") ? ((Number) body.get("count")).intValue() : 1;
        double amount = body.containsKey("amount") ? ((Number) body.get("amount")).doubleValue() : 10.0;
        return cardService.sendToUser(username, count, amount, "admin");
    }

    /** Admin: batch issue cards (placeholder - just returns ok for UI) */
    @PostMapping("/admin/cards/batch")
    public Map<String, Object> batchIssue(@RequestHeader("Authorization") String auth) {
        authService.requireAdmin(auth.replace("Bearer ", ""));
        return Map.of("ok", true, "message", "批量发放功能开发中，敬请期待");
    }
}
