package com.quizapp.module.accesscode.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.common.PageResponse;
import com.quizapp.module.accesscode.dto.AccessCodeValidateResponse;
import com.quizapp.module.accesscode.dto.ValidateCodeRequest;
import com.quizapp.module.accesscode.service.AccessCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access-codes")
@RequiredArgsConstructor
@Tag(name = "Access Code", description = "Access code validation and management")
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    @PostMapping("/validate")
    @Operation(summary = "Validate access code and get accessible quizzes [Public]")
    public ResponseEntity<ApiResponse<AccessCodeValidateResponse>> validate(
            @Valid @RequestBody ValidateCodeRequest request,
            HttpServletRequest httpRequest) {
        // Auto-capture IP if not provided
        if (request.getIpAddress() == null) {
            request.setIpAddress(httpRequest.getRemoteAddr());
        }
        return ResponseEntity.ok(ApiResponse.ok(accessCodeService.validate(request)));
    }

    @GetMapping
    @Operation(summary = "List all access codes [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<AccessCodeValidateResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(accessCodeService.listAll(page, size)));
    }
}
