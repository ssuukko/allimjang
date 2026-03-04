package com.alrimjang.controller;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.info.server-name}")
    private String serverName;

    @Value("${app.info.description}")
    private String description;

    @Value("${app.info.version}")
    private String version;

    @Value("${app.info.framework}")
    private String framework;

    @Value("${app.info.status}")
    private String status;

    @Value("${server.port}")
    private int serverPort;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("serverName", serverName);
        model.addAttribute("description", description);
        return "index";
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
        info.put("serverName", serverName);
        info.put("version", version);
        info.put("port", serverPort);
        info.put("framework", framework);
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("currentTime", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("status", status);
        return info;
    }
}
