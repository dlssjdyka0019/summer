package com.charging.dto;

public class UserInfo {
    public Long id;
    public String username;
    public String role;
    public String phone;

    public UserInfo(Long id, String username, String role, String phone) {
        this.id = id; this.username = username; this.role = role; this.phone = phone;
    }
}
