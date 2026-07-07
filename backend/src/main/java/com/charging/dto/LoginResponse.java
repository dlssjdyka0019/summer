package com.charging.dto;

public class LoginResponse {
    public boolean ok;
    public String token;
    public UserInfo user;

    public LoginResponse(boolean ok, String token, UserInfo user) {
        this.ok = ok; this.token = token; this.user = user;
    }
}
