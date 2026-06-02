package com.learn.tasktracker.dto;

import com.learn.tasktracker.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private TaskStatus status;
}
