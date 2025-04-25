package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Посещаемость", description = "Статистика посещаемости")
@RestController
@RequestMapping("/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @Operation(summary = "Получить статистику посещаемости по конкретному URL")
    @GetMapping("/count")
    public long getVisitCount(@RequestParam String url) {
        return visitService.getVisitCount(url);
    }

    @Operation(summary = "Получить статистику посещаемости по всему сайту")
    @GetMapping("/all")
    public Map<String, Long> getAllVisitCounts() {
        return visitService.getAllVisitCounts();
    }
}
