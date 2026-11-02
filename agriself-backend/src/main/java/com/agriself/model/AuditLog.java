package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAILURE, ROLE_ACCESS_DENIED, MACHINERY_STATUS_CHANGED, RATE_LIMIT_TRIGGERED

    @Column(nullable = false, length = 120)
    private String username;

    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false, length = 500)
    private String details;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
