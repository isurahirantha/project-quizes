package com.quizapp.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectPaymentRequest {
    @NotBlank(message = "Reject reason is required")
    private String reason;
}
