package com.alrimjang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 홈 컨트롤러
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("serverName", "알림장 (Alrimjang)");
        model.addAttribute("description", "학교/회사 범용 조직 관리 시스템");
        model.addAttribute("port", "8080");
        model.addAttribute("currentTime", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    /**
     * 서버 정보 API
     */
    @GetMapping("/api/info")
    @ResponseBody
    public Map<String, Object> serverInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serverName", "알림장 (Alrimjang)");
        info.put("version", "1.0.0");
        info.put("port", 8080);
        info.put("framework", "Spring Boot 3.2.2");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("currentTime", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("status", "running");
        return info;
    }
}
