package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.InvalidRequest;
import com.carsharing.carsharing.exception.LoggingException;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.LogTask;
import com.carsharing.carsharing.model.TaskStatus;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogService {

    private static final String LOGS_DIR = "logs";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ExecutorService executor;
    private final ConcurrentHashMap<String, LogTask> taskMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.executor = Executors.newCachedThreadPool();
    }

    public Resource getLogFileForDate(String date) {
        LocalDate parsedDate = parseDate(date);
        String formattedDate = parsedDate.format(LOG_DATE_FORMATTER);

        Path logFilePath = Paths.get(LOGS_DIR, "application.log");
        if (!Files.exists(logFilePath)) {
            throw new NotFound("Log file does not exist.");
        }

        List<String> filteredLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) {
                    filteredLines.add(line);
                }
            }
        } catch (IOException ex) {
            throw new LoggingException("Error reading log file.");
        }

        if (filteredLines.isEmpty()) {
            throw new NotFound("No logs found for this date.");
        }

        String fileContent = String.join(System.lineSeparator(), filteredLines);
        return new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));
    }

    public String getDownloadFileName(String date) {
        LocalDate parsedDate = parseDate(date);
        return String.format("application-%s.log", parsedDate.format(LOG_DATE_FORMATTER));
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw new InvalidRequest("Date cannot be null or empty");
        }
        try {
            return LocalDate.parse(date, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new InvalidRequest("Incorrect date format. Use dd.MM.yyyy.");
        }
    }

    public String createLogFileAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        LogTask task = new LogTask(taskId, date);
        taskMap.put(taskId, task);
        log.info("Created async log task: {}", taskId);

        executor.submit(() -> {
            try {
                Thread.sleep(30000);
                Resource resource = getLogFileForDate(date);
                task.setResource(resource);
                task.setStatus(TaskStatus.COMPLETED);
                log.info("Log task {} completed", taskId);
            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED);
                task.setErrorMessage(e.getMessage());
                log.error("Log task {} failed: {}", taskId, e.getMessage());
            }
        });

        return taskId;
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        LogTask task = taskMap.get(taskId);
        if (task == null) {
            throw new NotFound("Task not found with ID: " + taskId);
        }

        Map<String, Object> status = new HashMap<>();
        status.put("taskId", task.getTaskId());
        status.put("date", task.getDate());
        status.put("status", task.getStatus().name());

        if (task.getErrorMessage() != null) {
            status.put("error", task.getErrorMessage());
        }

        return status;
    }

    public Resource getLogFileByTaskId(String taskId) {
        LogTask task = taskMap.get(taskId);
        if (task == null) {
            throw new NotFound("Task not found with ID: " + taskId);
        }

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new LoggingException("Log file is not ready. Current status: "
                    + task.getStatus());
        }

        return task.getResource();
    }
}
