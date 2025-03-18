package com.project.cheerha.domain.user.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.List;

public enum Role {
    USER, ADMIN;

    public List<GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + this.name());
    }
}
