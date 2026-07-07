package com.charging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemption_codes")
public class RedemptionCode {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private Double amount;  // value of this code (yuan)

    @Column(length = 20)
    private String status = "unused"; // unused / used

    @Column(name = "used_by")
    private Long usedBy;

    @Column(name = "used_by_name", length = 50)
    private String usedByName;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RedemptionCode() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUsedBy() { return usedBy; }
    public void setUsedBy(Long usedBy) { this.usedBy = usedBy; }
    public String getUsedByName() { return usedByName; }
    public void setUsedByName(String usedByName) { this.usedByName = usedByName; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
