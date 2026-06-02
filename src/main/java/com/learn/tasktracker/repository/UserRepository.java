package com.learn.tasktracker.repository;

import com.learn.tasktracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // Spring generates this query!
}
