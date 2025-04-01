package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/logs")
@Tag(name = "Log API", description = "API для работы с логами")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/download")
    @Operation(summary = "Получить лог-файл за указанную дату")
    public ResponseEntity<Resource> downloadLogFile(@RequestParam String date) {
        Resource resource = logService.getLogFileForDate(date);
        String downloadFileName = logService.getDownloadFileName(date);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadFileName + "\"")
                .body(resource);
    }
}
