package com.charging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "piles")
public class Pile {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @JsonIgnore
    private Station station;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String type = "快充";

    private Double power = 0.0;
    private Double price = 0.0;

    @Column(length = 20)
    private String status = "open";

    public Pile() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getPower() { return power; }
    public void setPower(Double power) { this.power = power; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
