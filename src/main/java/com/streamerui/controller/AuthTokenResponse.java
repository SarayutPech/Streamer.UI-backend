package com.streamerui.controller;

/** Response body for both /api/auth/register and /api/auth/login. */
public class AuthTokenResponse {

    private final String token;

    public AuthTokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
