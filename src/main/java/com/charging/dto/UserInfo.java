package com.charging.dto;

public class UserInfo {
    public Long id;
    public String username;
    public String role;
    public String phone;
    public Integer electricityCards;

    public UserInfo(Long id, String username, String role, String phone) {
        this.id = id; this.username = username; this.role = role; this.phone = phone;
    }

    public UserInfo(Long id, String username, String role, String phone, Integer electricityCards) {
        this.id = id; this.username = username; this.role = role; this.phone = phone;
        this.electricityCards = electricityCards;
    }
}
