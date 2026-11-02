package com.agriself.security;

import com.agriself.model.AuditLog;
import com.agriself.repository.AuditLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles role-based redirection immediately upon successful authentication.
 * Ensures Farmers land on /farmer/dashboard, Businessmen on /market/dashboard,
 * and Administrators on /admin/dashboard, preventing users from seeing wrong pages.
 */
@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuditLogRepository auditLogRepository;

    public CustomAuthenticationSuccessHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        String targetUrl = "/login";

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_FARMER".equals(role)) {
                targetUrl = "/farmer/dashboard";
                break;
            } else if ("ROLE_BUSINESSMAN".equals(role)) {
                targetUrl = "/market/dashboard";
                break;
            } else if ("ROLE_ADMIN".equals(role)) {
                targetUrl = "/admin/dashboard";
                break;
            }
        }

        // Record security audit log
        try {
            AuditLog logEntry = AuditLog.builder()
                    .eventType("LOGIN_SUCCESS")
                    .username(username)
                    .ipAddress(getClientIp(request))
                    .details("User successfully logged in and redirected to: " + targetUrl)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to write audit log for user login", e);
        }

        log.info("User [{}] authenticated with roles {}. Redirecting to: {}",
                username, authentication.getAuthorities(), targetUrl);

        response.sendRedirect(targetUrl);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf == null || xf.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xf.split(",")[0].trim();
    }
}
