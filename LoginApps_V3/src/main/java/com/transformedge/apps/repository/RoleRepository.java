package com.transformedge.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transformedge.apps.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Role findByName(String role);
}
