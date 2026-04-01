package com.quizapp.module.accesscode.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_code_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessCodeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_code_id", nullable = false)
    private AccessCode accessCode;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_mobile", length = 20)
    private String userMobile;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "used_at", nullable = false)
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();
}
