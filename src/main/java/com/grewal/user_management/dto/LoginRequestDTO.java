package com.grewal.user_management.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;;
    private String email;
    private String password;
}
