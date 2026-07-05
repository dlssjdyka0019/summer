package com.charging.dto;

import java.util.List;

public class StationDTO {
    public String id;
    public String name;
    public String address = "";
    public Double longitude = 0.0;
    public Double latitude = 0.0;
    public String operator = "";
    public Integer totalSpots = 0;
    public Integer availableSpots = 0;
    public Double power = 0.0;
    public List<String> chargerTypes = List.of();
    public Double price = 0.0;
    public String openHours = "00:00-24:00";
    public String phone = "";
    public String status = "open";
    public String desc = "";
    public Long created = 0L;
    public Long updated = 0L;
    public List<PileDTO> piles = List.of();
}
