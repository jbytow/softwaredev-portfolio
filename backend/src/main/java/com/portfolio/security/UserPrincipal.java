package com.portfolio.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final String email;
    private final String name;
    private final boolean isAdmin;
}
