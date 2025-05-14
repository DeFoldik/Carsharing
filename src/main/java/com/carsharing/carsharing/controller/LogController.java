package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/logs")
@Tag(name = "Log API", description = "API для работы с логами")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/download")
    @Operation(summary = "Получить лог-файл за указанную дату (синхронно)")
    public ResponseEntity<Resource> downloadLogFile(@RequestParam String date) {
        Resource resource = logService.getLogFileForDate(date);
        String fileName = logService.getDownloadFileName(date);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping("/async")
    @Operation(summary = "Асинхронно создать лог-файл. Возвращает taskId")
    public ResponseEntity<Map<String, String>> createLogFileAsync(@RequestParam String date) {
        String taskId = logService.createLogFileAsync(date);
        return ResponseEntity.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/status")
    @Operation(summary = "Получить статус асинхронной задачи по taskId")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@RequestParam String taskId) {
        return ResponseEntity.ok(logService.getTaskStatus(taskId));
    }

    @GetMapping("/downloadAsync")
    @Operation(summary = "Скачать лог-файл по taskId (если готов)")
    public ResponseEntity<?> downloadLogFileAsync(@RequestParam String taskId) {
        Map<String, Object> status = logService.getTaskStatus(taskId);
        String currentStatus = status.get("status").toString();

        if (!"COMPLETED".equals(currentStatus)) {
            return ResponseEntity
                    .status(202)
                    .body(Map.of(
                            "message", "Log file is not ready yet. Status: " + currentStatus,
                            "taskId", taskId,
                            "status", currentStatus
                    ));
        }

        Resource resource = logService.getLogFileByTaskId(taskId);
        String fileName = logService.getDownloadFileName(status.get("date").toString());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
