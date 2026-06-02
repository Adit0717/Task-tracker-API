package com.learn.tasktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.tasktracker.dto.CreateTaskRequest;
import com.learn.tasktracker.dto.UpdateStatusRequest;
import com.learn.tasktracker.model.Task;
import com.learn.tasktracker.model.TaskStatus;
import com.learn.tasktracker.model.User;
import com.learn.tasktracker.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @org.springframework.web.bind.annotation.ControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler({
            org.springframework.web.bind.MethodArgumentNotValidException.class,
            org.springframework.validation.BindException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
        })
        public ResponseEntity<String> handleBadRequest(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleInternalServerError(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    // ==========================================
    // POST /users/{userId}/tasks Tests
    // ==========================================

    @Test
    @DisplayName("shouldCreateTaskWithAllFieldsInResponse")
    void shouldCreateTaskWithAllFieldsInResponse() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Complete OA");
        request.setDescription("Practice debugging");
        request.setPriority(4);
        request.setDueDate(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Complete OA");
        task.setDescription("Practice debugging");
        task.setPriority(4);
        task.setStatus(TaskStatus.TODO);
        task.setUser(user);

        when(taskService.createTask(eq(1L), any(CreateTaskRequest.class))).thenReturn(task);

        mockMvc.perform(post("/users/{userId}/tasks", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Complete OA"))
                .andExpect(jsonPath("$.description").value("Practice debugging"))
                .andExpect(jsonPath("$.priority").value(4))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService, times(1)).createTask(eq(1L), any(CreateTaskRequest.class));
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateTaskTitleIsBlank")
    void shouldReturn400WhenCreateTaskTitleIsBlank() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(""); // blank
        request.setPriority(3);

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users/{userId}/tasks", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateTaskPriorityIsTooLow")
    void shouldReturn400WhenCreateTaskPriorityIsTooLow() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Valid Title");
        request.setPriority(0); // below min (1)

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users/{userId}/tasks", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateTaskPriorityIsTooHigh")
    void shouldReturn400WhenCreateTaskPriorityIsTooHigh() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Valid Title");
        request.setPriority(6); // above max (5)

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users/{userId}/tasks", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn500WhenCreateTaskUserDoesNotExist")
    void shouldReturn500WhenCreateTaskUserDoesNotExist() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Valid Title");
        request.setPriority(3);

        when(taskService.createTask(eq(999L), any(CreateTaskRequest.class)))
                .thenThrow(new RuntimeException("User not found: 999"));

        mockMvc.perform(post("/users/{userId}/tasks", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).createTask(eq(999L), any(CreateTaskRequest.class));
    }

    // ==========================================
    // GET /tasks Tests
    // ==========================================

    @Test
    @DisplayName("shouldReturnAllTasksWhenNoFiltersApplied")
    void shouldReturnAllTasksWhenNoFiltersApplied() throws Exception {
        Task t1 = new Task(); t1.setId(1L); t1.setTitle("T1");
        Task t2 = new Task(); t2.setId(2L); t2.setTitle("T2");
        List<Task> allTasks = Arrays.asList(t1, t2);

        when(taskService.getAllTasks(null, null)).thenReturn(allTasks);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("T1"))
                .andExpect(jsonPath("$[1].title").value("T2"));

        verify(taskService, times(1)).getAllTasks(null, null);
    }

    @Test
    @DisplayName("shouldFilterByStatusOnly")
    void shouldFilterByStatusOnly() throws Exception {
        Task t1 = new Task(); t1.setId(1L); t1.setTitle("T1"); t1.setStatus(TaskStatus.IN_PROGRESS);
        List<Task> filtered = Collections.singletonList(t1);

        when(taskService.getAllTasks(TaskStatus.IN_PROGRESS, null)).thenReturn(filtered);

        mockMvc.perform(get("/tasks").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        verify(taskService, times(1)).getAllTasks(TaskStatus.IN_PROGRESS, null);
    }

    @Test
    @DisplayName("shouldFilterByPriorityOnly")
    void shouldFilterByPriorityOnly() throws Exception {
        Task t1 = new Task(); t1.setId(1L); t1.setTitle("T1"); t1.setPriority(5);
        List<Task> filtered = Collections.singletonList(t1);

        when(taskService.getAllTasks(null, 5)).thenReturn(filtered);

        mockMvc.perform(get("/tasks").param("priority", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].priority").value(5));

        verify(taskService, times(1)).getAllTasks(null, 5);
    }

    @Test
    @DisplayName("shouldFilterByStatusAndPriority")
    void shouldFilterByStatusAndPriority() throws Exception {
        Task t1 = new Task(); t1.setId(1L); t1.setTitle("T1"); t1.setStatus(TaskStatus.DONE); t1.setPriority(2);
        List<Task> filtered = Collections.singletonList(t1);

        when(taskService.getAllTasks(TaskStatus.DONE, 2)).thenReturn(filtered);

        mockMvc.perform(get("/tasks")
                .param("status", "DONE")
                .param("priority", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("DONE"))
                .andExpect(jsonPath("$[0].priority").value(2));

        verify(taskService, times(1)).getAllTasks(TaskStatus.DONE, 2);
    }

    // ==========================================
    // PUT /tasks/{id}/status Tests
    // ==========================================

    @Test
    @DisplayName("shouldUpdateTaskStatusSuccessfully")
    void shouldUpdateTaskStatusSuccessfully() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TaskStatus.DONE);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("T1");
        updatedTask.setStatus(TaskStatus.DONE);

        when(taskService.updateStatus(eq(1L), any(UpdateStatusRequest.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/tasks/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(taskService, times(1)).updateStatus(eq(1L), any(UpdateStatusRequest.class));
    }

    @Test
    @DisplayName("shouldReturn400WhenUpdateStatusIsNull")
    void shouldReturn400WhenUpdateStatusIsNull() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(null);

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(put("/tasks/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn500WhenUpdateTaskDoesNotExist")
    void shouldReturn500WhenUpdateTaskDoesNotExist() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TaskStatus.DONE);

        when(taskService.updateStatus(eq(999L), any(UpdateStatusRequest.class)))
                .thenThrow(new RuntimeException("Task not found: 999"));

        mockMvc.perform(put("/tasks/{id}/status", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).updateStatus(eq(999L), any(UpdateStatusRequest.class));
    }

    // ==========================================
    // DELETE /tasks/{id} Tests
    // ==========================================

    @Test
    @DisplayName("shouldDeleteTaskSuccessfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenDeleteTaskDoesNotExist")
    void shouldReturn500WhenDeleteTaskDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Task not found: 999")).when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/tasks/{id}", 999L))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).deleteTask(999L);
    }

    // ==========================================
    // GET /users/{id}/tasks/overdue Tests
    // ==========================================

    @Test
    @DisplayName("shouldOnlyReturnOverdueTasksForUser")
    void shouldOnlyReturnOverdueTasksForUser() throws Exception {
        Task t1 = new Task(); t1.setId(1L); t1.setTitle("Overdue T1");
        List<Task> overdue = Collections.singletonList(t1);

        when(taskService.getOverdueTasks(1L)).thenReturn(overdue);

        mockMvc.perform(get("/users/{id}/tasks/overdue", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Overdue T1"));

        verify(taskService, times(1)).getOverdueTasks(1L);
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenNoTasksAreOverdue")
    void shouldReturnEmptyListWhenNoTasksAreOverdue() throws Exception {
        when(taskService.getOverdueTasks(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/{id}/tasks/overdue", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(taskService, times(1)).getOverdueTasks(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenOverdueTasksUserDoesNotExist")
    void shouldReturn500WhenOverdueTasksUserDoesNotExist() throws Exception {
        when(taskService.getOverdueTasks(999L))
                .thenThrow(new RuntimeException("User not found: 999"));

        mockMvc.perform(get("/users/{id}/tasks/overdue", 999L))
                .andExpect(status().isInternalServerError());

        verify(taskService, times(1)).getOverdueTasks(999L);
    }
}
