package com.charging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "electricity_cards")
public class ElectricityCard {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double amount;  // value of this card (yuan)

    @Column(name = "station_id", length = 50)
    private String stationId;

    @Column(name = "station_name", length = 200)
    private String stationName;

    @Column(name = "photo_id", length = 50)
    private String photoId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_name", length = 50)
    private String adminName;

    @Column(length = 20)
    private String status = "active"; // active / revoked

    @Column(name = "revoked_by", length = 50)
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ElectricityCard() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
