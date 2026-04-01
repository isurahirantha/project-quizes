package com.quizapp.module.accesscode.service;

import com.quizapp.common.PageResponse;
import com.quizapp.exception.BadRequestException;
import com.quizapp.module.accesscode.dto.AccessCodeValidateResponse;
import com.quizapp.module.accesscode.dto.ValidateCodeRequest;
import com.quizapp.module.accesscode.entity.AccessCode;
import com.quizapp.module.accesscode.entity.AccessCodeUsage;
import com.quizapp.module.accesscode.repository.AccessCodeRepository;
import com.quizapp.module.accesscode.repository.AccessCodeUsageRepository;
import com.quizapp.module.payment.entity.Payment;
import com.quizapp.module.quiz.dto.QuizResponse;
import com.quizapp.module.quiz.repository.QuizRepository;
import com.quizapp.module.quiz.service.QuizService;
import com.quizapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessCodeService {

    private final AccessCodeRepository accessCodeRepository;
    private final AccessCodeUsageRepository usageRepository;
    private final QuizRepository quizRepository;
    private final QuizService quizService;
    private final EmailService emailService;

    @Transactional
    public void generateAndSend(Payment payment) {
        String code = generateUniqueCode();

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(payment.getCategory().getValidityDays());

        AccessCode accessCode = AccessCode.builder()
                .code(code)
                .payment(payment)
                .category(payment.getCategory())
                .userName(payment.getUserName())
                .userEmail(payment.getUserEmail())
                .userMobile(payment.getUserMobile())
                .expiresAt(expiresAt)
                .active(true)
                .build();

        accessCodeRepository.save(accessCode);

        String expiryFormatted = expiresAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        emailService.sendAccessCodeEmail(
                payment.getUserEmail(),
                payment.getUserName(),
                code,
                payment.getCategory().getName(),
                expiryFormatted
        );
        log.info("Access code generated: {} for email: {}", code, payment.getUserEmail());
    }

    @Transactional
    public AccessCodeValidateResponse validate(ValidateCodeRequest request) {
        AccessCode accessCode = accessCodeRepository.findByCodeAndActiveTrue(request.getCode())
                .orElse(null);

        if (accessCode == null) {
            return buildInvalidResponse(request.getCode(), "Invalid access code");
        }

        if (accessCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return buildInvalidResponse(request.getCode(), "Access code has expired");
        }

        // Device binding check
        if (accessCode.getBoundDeviceId() != null && request.getDeviceId() != null
                && !accessCode.getBoundDeviceId().equals(request.getDeviceId())) {
            logUsage(accessCode, request, "REJECTED_DEVICE_MISMATCH");
            return buildInvalidResponse(request.getCode(), "Access code is bound to a different device");
        }

        // Bind on first use
        if (accessCode.getBoundDeviceId() == null && request.getDeviceId() != null) {
            accessCode.setBoundDeviceId(request.getDeviceId());
            accessCodeRepository.save(accessCode);
        }

        logUsage(accessCode, request, "VALIDATED");

        // Fetch all non-deleted, active quizzes under the category
        var quizzes = quizRepository.findPublic(null, null, null,
                PageRequest.of(0, 200, Sort.by("title"))).getContent();

        List<QuizResponse> categoryQuizzes = quizzes.stream()
                .filter(q -> {
                    var qEntity = quizRepository.findById(q.getId()).orElse(null);
                    return qEntity != null && qEntity.getSubcategory().getCategory()
                            .getId().equals(accessCode.getCategory().getId());
                })
                .map(q -> quizService.toResponse(quizRepository.findById(q.getId()).get()))
                .toList();

        AccessCodeValidateResponse r = new AccessCodeValidateResponse();
        r.setCode(accessCode.getCode());
        r.setCategoryName(accessCode.getCategory().getName());
        r.setUserName(accessCode.getUserName());
        r.setExpiresAt(accessCode.getExpiresAt());
        r.setValid(true);
        r.setMessage("Access code is valid");
        r.setAccessibleQuizzes(categoryQuizzes);
        return r;
    }

    public PageResponse<AccessCodeValidateResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(accessCodeRepository.findAll(pageable).map(ac -> {
            AccessCodeValidateResponse r = new AccessCodeValidateResponse();
            r.setCode(ac.getCode());
            r.setCategoryName(ac.getCategory().getName());
            r.setUserName(ac.getUserName());
            r.setExpiresAt(ac.getExpiresAt());
            r.setValid(ac.isActive() && ac.getExpiresAt().isAfter(LocalDateTime.now()));
            return r;
        }));
    }

    private String generateUniqueCode() {
        Random rng = new Random();
        String code;
        do {
            code = String.format("%08d", rng.nextInt(100_000_000));
        } while (accessCodeRepository.existsByCode(code));
        return code;
    }

    private void logUsage(AccessCode accessCode, ValidateCodeRequest request, String action) {
        AccessCodeUsage usage = AccessCodeUsage.builder()
                .accessCode(accessCode)
                .deviceId(request.getDeviceId())
                .userEmail(request.getEmail())
                .userMobile(request.getMobile())
                .action(action)
                .ipAddress(request.getIpAddress())
                .build();
        usageRepository.save(usage);
    }

    private AccessCodeValidateResponse buildInvalidResponse(String code, String message) {
        AccessCodeValidateResponse r = new AccessCodeValidateResponse();
        r.setCode(code);
        r.setValid(false);
        r.setMessage(message);
        return r;
    }
}
