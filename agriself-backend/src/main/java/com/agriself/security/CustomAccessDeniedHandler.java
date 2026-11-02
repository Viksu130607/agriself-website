package com.agriself.security;

import com.agriself.model.AuditLog;
import com.agriself.repository.AuditLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles 403 Forbidden attempts.
 * Protects administrator and role routes from unauthorized privilege escalation.
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditLogRepository auditLogRepository;

    public CustomAccessDeniedHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "ANONYMOUS";
        String requestedUri = request.getRequestURI();

        log.warn("Unauthorized access attempt by [{}] to protected route: {}", username, requestedUri);

        try {
            AuditLog logEntry = AuditLog.builder()
                    .eventType("ROLE_ACCESS_DENIED")
                    .username(username)
                    .ipAddress(request.getRemoteAddr())
                    .details("Forbidden attempt to access protected resource: " + requestedUri)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Could not write security access denied audit log", e);
        }

        response.sendRedirect("/access-denied?uri=" + requestedUri);
    }
}
