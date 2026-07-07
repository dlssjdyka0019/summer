package com.charging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "station_photos")
public class StationPhoto {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "station_id", nullable = false, length = 50)
    private String stationId;

    @Column(name = "station_name", length = 200)
    private String stationName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "original_name")
    private String originalName;

    @Column(length = 20)
    private String status = "pending"; // pending / approved / rejected / hidden

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "card_count")
    private Integer cardCount = 1;  // how many cards rewarded

    @Column(name = "card_amount")
    private Double cardAmount = 10.0;  // value of each card (yuan)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public StationPhoto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }
    public Integer getCardCount() { return cardCount; }
    public void setCardCount(Integer cardCount) { this.cardCount = cardCount; }
    public Double getCardAmount() { return cardAmount; }
    public void setCardAmount(Double cardAmount) { this.cardAmount = cardAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
