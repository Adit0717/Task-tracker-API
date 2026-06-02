package com.learn.tasktracker.service;

import com.learn.tasktracker.dto.CreateTaskRequest;
import com.learn.tasktracker.model.Task;
import com.learn.tasktracker.model.User;
import com.learn.tasktracker.repository.TaskRepository;
import com.learn.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit 5 extension to enable Mockito annotations
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository; // Mock dependency (equivalent to Mock<ITaskRepository> in .NET)

    @Mock
    private UserRepository userRepository; // Mock dependency

    @InjectMocks
    private TaskService taskService; // Injects the two mocks above into TaskService

    @Test
    @DisplayName("Should create task successfully when user exists")
    void createTask_Success() {
        // Arrange (Setup mocks and input data)
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Alice");

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Complete Spring Boot Tutorial");
        request.setDescription("Learn testing and debugging");
        request.setPriority(3);

        // Tell mock how to behave (Equivalent to Setup() in Moq)
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // Mock save to return the task with an auto-generated ID
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            taskToSave.setId(100L); // simulate DB generated ID
            return taskToSave;
        });

        // Act (Call the actual method under test)
        Task result = taskService.createTask(userId, request);

        // Assert (Verify results and behavior)
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Complete Spring Boot Tutorial", result.getTitle());
        assertEquals(user, result.getUser());

        // Verify that userRepository.findById was called exactly once
        verify(userRepository, times(1)).findById(userId);
        // Verify that taskRepository.save was called exactly once
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw Exception when user does not exist")
    void createTask_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task for non-existent user");

        // Mock userRepository to return Empty Optional (user not found)
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert (Equivalent to Assert.ThrowsAsync in .NET)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(userId, request);
        });

        assertEquals("User not found: 999", exception.getMessage());

        // Verify save was NEVER called because the execution stopped at the exception
        verify(taskRepository, never()).save(any(Task.class));
    }
}
