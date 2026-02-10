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
    public String dashboard(Model model) {
        // 사용자 정보
        model.addAttribute("userName", "홍길동");
        model.addAttribute("userRole", "교사");
        model.addAttribute("userDepartment", "컴퓨터공학과");
        
        // 읽지 않은 메시지 수
        model.addAttribute("unreadMessages", 5);
        
        // 최근 공지사항 (샘플 데이터)
        java.util.List<java.util.Map<String, String>> notices = java.util.Arrays.asList(
            java.util.Map.of(
                "title", "2026년 1학기 수업 일정 안내",
                "date", "2026-02-10",
                "isNew", "true"
            ),
            java.util.Map.of(
                "title", "교직원 회의 공지",
                "date", "2026-02-09",
                "isNew", "true"
            ),
            java.util.Map.of(
                "title", "시설 보수 공사 안내",
                "date", "2026-02-08",
                "isNew", "false"
            )
        );
        model.addAttribute("notices", notices);
        
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
