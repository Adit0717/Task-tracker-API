package com.learn.tasktracker.controller;

import com.learn.tasktracker.dto.CreateTaskRequest;
import com.learn.tasktracker.dto.UpdateStatusRequest;
import com.learn.tasktracker.model.Task;
import com.learn.tasktracker.model.TaskStatus;
import com.learn.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/users/{userId}/tasks")
    public ResponseEntity<Task> createTask(
            @PathVariable Long userId,
            @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(userId, request));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Integer priority) {

        return ResponseEntity.ok(taskService.getAllTasks(status, priority));
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<Task> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/users/{id}/tasks/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getOverdueTasks(id));
    }
}
