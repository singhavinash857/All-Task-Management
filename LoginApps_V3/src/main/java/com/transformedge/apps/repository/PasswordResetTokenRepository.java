package com.transformedge.apps.repository;

import org.springframework.data.repository.CrudRepository;

import com.transformedge.apps.entity.PasswordResetTokenEnity;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetTokenEnity, Long>{
  public PasswordResetTokenEnity findByToken(String token);
}
