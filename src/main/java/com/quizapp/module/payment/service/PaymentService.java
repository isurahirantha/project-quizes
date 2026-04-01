package com.quizapp.module.payment.service;

import com.quizapp.common.PageResponse;
import com.quizapp.common.enums.PaymentStatus;
import com.quizapp.exception.BadRequestException;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.accesscode.service.AccessCodeService;
import com.quizapp.module.category.entity.Category;
import com.quizapp.module.category.repository.CategoryRepository;
import com.quizapp.module.payment.dto.PaymentResponse;
import com.quizapp.module.payment.dto.RejectPaymentRequest;
import com.quizapp.module.payment.entity.Payment;
import com.quizapp.module.payment.repository.PaymentRepository;
import com.quizapp.service.EmailService;
import com.quizapp.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final AccessCodeService accessCodeService;
    private final EmailService emailService;

    @Transactional
    public PaymentResponse submit(String userName, String userEmail, String userMobile,
                                   Long categoryId, MultipartFile paymentSlip) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        String slipUrl = fileStorageService.storePaymentSlip(paymentSlip);

        Payment payment = Payment.builder()
                .category(category)
                .userName(userName)
                .userEmail(userEmail)
                .userMobile(userMobile)
                .paymentSlipUrl(slipUrl)
                .status(PaymentStatus.PENDING)
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    public PageResponse<PaymentResponse> listByStatus(PaymentStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null) {
            return PageResponse.of(paymentRepository.findByStatus(status, pageable).map(this::toResponse));
        }
        return PageResponse.of(paymentRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional
    public PaymentResponse approve(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setReviewedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Generate access code and send email
        accessCodeService.generateAndSend(payment);

        log.info("Payment approved: id={}", paymentId);
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse reject(Long paymentId, RejectPaymentRequest request) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectReason(request.getReason());
        payment.setReviewedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        emailService.sendPaymentRejectionEmail(
                payment.getUserEmail(),
                payment.getUserName(),
                payment.getCategory().getName(),
                request.getReason()
        );
        return toResponse(payment);
    }

    private Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setCategoryId(p.getCategory().getId());
        r.setCategoryName(p.getCategory().getName());
        r.setUserName(p.getUserName());
        r.setUserEmail(p.getUserEmail());
        r.setUserMobile(p.getUserMobile());
        r.setPaymentSlipUrl(p.getPaymentSlipUrl());
        r.setStatus(p.getStatus().name());
        r.setRejectReason(p.getRejectReason());
        r.setReviewedAt(p.getReviewedAt());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
