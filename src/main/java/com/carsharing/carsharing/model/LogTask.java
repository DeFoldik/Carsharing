package com.carsharing.carsharing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

public class LogTask {
    private final String taskId;
    private final String date;
    private volatile TaskStatus status = TaskStatus.IN_PROGRESS;
    private volatile Resource resource;
    private volatile String errorMessage;

    public LogTask(String taskId, String date) {
        this.taskId = taskId;
        this.date = date;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDate() {
        return date;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Resource getResource() {
        return resource;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
