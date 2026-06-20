package com.project.urlshortener.dtos;

import com.project.urlshortener.model.Role;

public record CreateUserCmd(
        String email,
        String password,
        String name,
        Role role) {
}
