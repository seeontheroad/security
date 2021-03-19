package demo.security.service;

import demo.security.entity.Admin;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(String username, String password);

    Admin findByName(String username);
}
