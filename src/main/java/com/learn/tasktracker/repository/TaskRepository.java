package com.learn.tasktracker.repository;

import com.learn.tasktracker.model.Task;
import com.learn.tasktracker.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Spring derives SQL from the method name:
    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(int priority);

    List<Task> findByStatusAndPriority(TaskStatus status, int priority);

    // Custom JPQL query:
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasksByUser(Long userId, LocalDateTime now);
}
