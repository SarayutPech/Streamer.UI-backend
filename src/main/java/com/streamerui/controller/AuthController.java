package com.streamerui.controller;

import com.streamerui.security.CurrentStreamerService;
import com.streamerui.security.MeResponse;
import com.streamerui.security.MeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/me - who the frontend is currently logged in as. In local/
 * single-tenant dev mode (no login) this returns a stand-in "local streamer"
 * (see FixedMeService); in production it reflects the authenticated
 * streamer's account (see JpaMeService).
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final MeService meService;
    private final CurrentStreamerService currentStreamer;

    public AuthController(MeService meService, CurrentStreamerService currentStreamer) {
        this.meService = meService;
        this.currentStreamer = currentStreamer;
    }

    @GetMapping("/me")
    public MeResponse me() {
        return meService.getMe(currentStreamer.requireStreamerId());
    }
}
