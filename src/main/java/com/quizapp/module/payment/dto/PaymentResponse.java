package com.quizapp.module.payment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String userName;
    private String userEmail;
    private String userMobile;
    private String paymentSlipUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
