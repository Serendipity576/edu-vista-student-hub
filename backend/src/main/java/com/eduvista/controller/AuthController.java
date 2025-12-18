package com.eduvista.controller;

import com.eduvista.service.AuthService;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public CommonResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return authService.login(request.get("username"), request.get("password"));
    }
    
    @PostMapping("/register")
    public CommonResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        return authService.register(
            request.get("username"),
            request.get("password"),
            request.get("email")
        );
    }
}

