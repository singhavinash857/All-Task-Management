package com.transformedge.apps.service;

import com.transformedge.apps.entity.User;

public interface UserService {
    void save(User user);
    User findByUsername(String username);
	boolean requestPasswordReset(String email);
	boolean resetPassword(String token, String password);
}
