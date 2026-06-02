package com.learn.tasktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.tasktracker.dto.CreateUserRequest;
import com.learn.tasktracker.model.User;
import com.learn.tasktracker.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    // ==========================================
    // POST /users Tests
    // ==========================================

    @Test
    @DisplayName("shouldCreateUserSuccessfullyWithValidRequest")
    void shouldCreateUserSuccessfullyWithValidRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john.doe@example.com");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateUserEmailIsInvalid")
    void shouldReturn400WhenCreateUserEmailIsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("invalid-email-format");

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateUserNameIsBlank")
    void shouldReturn400WhenCreateUserNameIsBlank() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("   ");
        request.setEmail("john.doe@example.com");

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn400WhenCreateUserEmailIsBlank")
    void shouldReturn400WhenCreateUserEmailIsBlank() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("");

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn500WhenCreateUserEmailAlreadyExists")
    void shouldReturn500WhenCreateUserEmailAlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    // ==========================================
    // GET /users/{id} Tests
    // ==========================================

    @Test
    @DisplayName("shouldGetUserSuccessfullyWhenIdExists")
    void shouldGetUserSuccessfullyWhenIdExists() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenGetUserByIdDoesNotExist")
    void shouldReturn500WhenGetUserByIdDoesNotExist() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found: 999"));

        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    @DisplayName("shouldReturn400WhenGetUserIdIsInvalidType")
    void shouldReturn400WhenGetUserIdIsInvalidType() throws Exception {
        mockMvc.perform(get("/users/{id}", "abc"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserById(anyLong());
    }
}
