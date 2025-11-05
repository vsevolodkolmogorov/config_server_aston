package ru.astondevs.configserveraston.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.astondevs.configserveraston.service.ConfigSourceService;

import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigController {
    private final ConfigSourceService configSourceService;

    public ConfigController(ConfigSourceService configSourceService) {
        this.configSourceService = configSourceService;
    }

    @GetMapping("/{application}/{profile}/{label:.*}")
    public ResponseEntity<Map<String,Object>> getConfigWithLabel(
            @PathVariable String application,
            @PathVariable String profile,
            @PathVariable(required = false) String label) {

        return ResponseEntity.ok(configSourceService.loadAsPropertySources(application, profile, label));
    }

    @GetMapping("/{application}/{profile}")
    public ResponseEntity<Map<String,Object>> getConfig(
            @PathVariable String application,
            @PathVariable String profile) {
        return ResponseEntity.ok(configSourceService.loadAsPropertySources(application, profile, null));
    }
}