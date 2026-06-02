package com.learn.tasktracker.service;

import com.learn.tasktracker.dto.CreateTaskRequest;
import com.learn.tasktracker.dto.UpdateStatusRequest;
import com.learn.tasktracker.model.Task;
import com.learn.tasktracker.model.TaskStatus;
import com.learn.tasktracker.model.User;
import com.learn.tasktracker.repository.TaskRepository;
import com.learn.tasktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Task createTask(Long userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUser(user);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks(TaskStatus status, Integer priority) {
        if (status != null && priority != null)
            return taskRepository.findByStatusAndPriority(status, priority);
        if (status != null)
            return taskRepository.findByStatus(status);
        if (priority != null)
            return taskRepository.findByPriority(priority);
        return taskRepository.findAll();
    }

    public Task updateStatus(Long taskId, UpdateStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        task.setStatus(request.getStatus());
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    public List<Task> getOverdueTasks(Long userId) {
        return taskRepository.findOverdueTasksByUser(userId, LocalDateTime.now());
    }
}
