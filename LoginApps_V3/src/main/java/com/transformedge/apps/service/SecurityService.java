package com.transformedge.apps.service;

public interface SecurityService {
    String findLoggedInUsername();
    void autoLogin(String username, String password);
}
