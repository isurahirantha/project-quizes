package com.quizapp.module.payment.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.common.PageResponse;
import com.quizapp.common.enums.PaymentStatus;
import com.quizapp.module.payment.dto.PaymentResponse;
import com.quizapp.module.payment.dto.RejectPaymentRequest;
import com.quizapp.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment submission and admin approval workflow")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(value = "/api/payments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit payment request with slip [Public]")
    public ResponseEntity<ApiResponse<PaymentResponse>> submit(
            @RequestParam("userName") String userName,
            @RequestParam("userEmail") String userEmail,
            @RequestParam("userMobile") String userMobile,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("paymentSlip") MultipartFile paymentSlip) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment submitted successfully", 
                      paymentService.submit(userName, userEmail, userMobile, categoryId, paymentSlip)));
    }

    @GetMapping("/api/admin/payments")
    @Operation(summary = "List payments (filterable by status) [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> listPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.listByStatus(status, page, size)));
    }

    @PostMapping("/api/admin/payments/{id}/approve")
    @Operation(summary = "Approve payment and generate access code [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PaymentResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment approved. Access code sent to user.",
                paymentService.approve(id)));
    }

    @PostMapping("/api/admin/payments/{id}/reject")
    @Operation(summary = "Reject payment with reason [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PaymentResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment rejected", paymentService.reject(id, request)));
    }
}
