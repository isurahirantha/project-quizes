package com.quizapp.module.accesscode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateCodeRequest {
    @NotBlank(message = "Access code is required")
    private String code;

    private String deviceId;
    private String email;
    private String mobile;
    private String ipAddress;
}
