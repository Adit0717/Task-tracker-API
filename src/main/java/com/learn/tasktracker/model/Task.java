package com.learn.tasktracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING) // Stores "TODO" not "0" in DB
    private TaskStatus status = TaskStatus.TODO;

    private int priority;

    private LocalDateTime createdAt;
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist // Auto-set createdAt before saving
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
