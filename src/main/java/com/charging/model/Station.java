package com.charging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address = "";

    private Double longitude = 0.0;
    private Double latitude = 0.0;

    @Column(length = 100)
    private String operator = "";

    @Column(name = "total_spots")
    private Integer totalSpots = 0;

    @Column(name = "available_spots")
    private Integer availableSpots = 0;

    private Double power = 0.0;

    @Column(name = "charger_types", columnDefinition = "TEXT")
    private String chargerTypes = "[]";

    private Double price = 0.0;

    @Column(name = "open_hours", length = 50)
    private String openHours = "00:00-24:00";

    @Column(length = 30)
    private String phone = "";

    @Column(length = 20)
    private String status = "open";

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Pile> piles = new ArrayList<>();

    public Station() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Integer getTotalSpots() { return totalSpots; }
    public void setTotalSpots(Integer totalSpots) { this.totalSpots = totalSpots; }
    public Integer getAvailableSpots() { return availableSpots; }
    public void setAvailableSpots(Integer availableSpots) { this.availableSpots = availableSpots; }
    public Double getPower() { return power; }
    public void setPower(Double power) { this.power = power; }
    public String getChargerTypes() { return chargerTypes; }
    public void setChargerTypes(String chargerTypes) { this.chargerTypes = chargerTypes; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Pile> getPiles() { return piles; }
    public void setPiles(List<Pile> piles) { this.piles = piles; }
}
