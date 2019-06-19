package com.transformedge.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transformedge.apps.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
