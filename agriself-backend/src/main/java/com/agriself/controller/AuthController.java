package com.agriself.controller;

import com.agriself.dto.RegisterFormDTO;
import com.agriself.model.AuditLog;
import com.agriself.model.User;
import com.agriself.model.UserRole;
import com.agriself.repository.AuditLogRepository;
import com.agriself.repository.UserRepository;
import com.agriself.security.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.agriself.model.CropMarketListing;
import com.agriself.model.MachineryStatus;
import com.agriself.model.MandiPrice;
import com.agriself.repository.CropMarketListingRepository;
import com.agriself.repository.MachineryRepository;
import com.agriself.repository.MandiPriceRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;
    private final AuditLogRepository auditLogRepository;
    private final MandiPriceRepository mandiPriceRepository;
    private final MachineryRepository machineryRepository;
    private final CropMarketListingRepository cropMarketListingRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          InputSanitizer inputSanitizer,
                          AuditLogRepository auditLogRepository,
                          MandiPriceRepository mandiPriceRepository,
                          MachineryRepository machineryRepository,
                          CropMarketListingRepository cropMarketListingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.inputSanitizer = inputSanitizer;
        this.auditLogRepository = auditLogRepository;
        this.mandiPriceRepository = mandiPriceRepository;
        this.machineryRepository = machineryRepository;
        this.cropMarketListingRepository = cropMarketListingRepository;
    }

    @GetMapping("/")
    public String showHomePage(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("authenticatedUser", auth.getName());
            boolean isFarmer = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FARMER"));
            boolean isBusiness = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESSMAN"));
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isFarmer", isFarmer);
            model.addAttribute("isBusiness", isBusiness);
            model.addAttribute("isAdmin", isAdmin);
        }

        List<MandiPrice> mandiPrices = mandiPriceRepository.findAllByOrderByModalPricePerQuintalDesc();
        model.addAttribute("mandiPrices", mandiPrices);

        List<CropMarketListing> topListings = cropMarketListingRepository.findByStatusOrderByListedAtDesc("AVAILABLE");
        model.addAttribute("topListings", topListings);

        long totalMachinery = machineryRepository.count();
        long inUseMachinery = machineryRepository.countByStatus(MachineryStatus.IN_USE);
        long availableMachinery = machineryRepository.countByStatus(MachineryStatus.AVAILABLE);
        model.addAttribute("totalMachinery", totalMachinery);
        model.addAttribute("inUseMachinery", inUseMachinery);
        model.addAttribute("availableMachinery", availableMachinery);

        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email/phone or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out securely.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerForm", new RegisterFormDTO());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registerForm") RegisterFormDTO form,
                                      BindingResult bindingResult,
                                      HttpServletRequest request,
                                      Model model) {

        // 1. Validate Form Constraints
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // 2. Sanitize Inputs against XSS
        String cleanEmail = inputSanitizer.sanitize(form.getEmail()).toLowerCase();
        String cleanPhone = inputSanitizer.sanitize(form.getPhoneNumber());
        String cleanFullName = inputSanitizer.sanitize(form.getFullName());
        String cleanLocation = inputSanitizer.sanitize(form.getLocation());

        // 3. Prevent Duplicate Registration
        if (userRepository.existsByEmail(cleanEmail)) {
            model.addAttribute("errorMessage", "An account with this email address already exists.");
            return "register";
        }
        if (userRepository.existsByPhoneNumber(cleanPhone)) {
            model.addAttribute("errorMessage", "An account with this phone number already exists.");
            return "register";
        }

        // 4. Strict RBAC Privilege Escalation Protection:
        // Users can ONLY register as FARMER or BUSINESSMAN.
        // ROLE_ADMIN can NEVER be chosen via public registration form.
        UserRole assignedRole;
        if ("ROLE_BUSINESSMAN".equalsIgnoreCase(form.getRole())) {
            assignedRole = UserRole.ROLE_BUSINESSMAN;
        } else {
            assignedRole = UserRole.ROLE_FARMER;
        }

        // 5. Secure Hash Password (BCrypt with strength 12)
        String hashedPassword = passwordEncoder.encode(form.getPassword());

        User newUser = User.builder()
                .fullName(cleanFullName)
                .email(cleanEmail)
                .phoneNumber(cleanPhone)
                .password(hashedPassword)
                .role(assignedRole)
                .location(cleanLocation != null && !cleanLocation.isEmpty() ? cleanLocation : "India")
                .enabled(true)
                .build();

        userRepository.save(newUser);

        // 6. Security Audit Log
        try {
            AuditLog logEntry = AuditLog.builder()
                    .eventType("USER_REGISTRATION")
                    .username(cleanEmail)
                    .ipAddress(request.getRemoteAddr())
                    .details("New user registered with role: " + assignedRole.name())
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to write registration audit log", e);
        }

        log.info("New user registered successfully: [{}] as [{}]", cleanEmail, assignedRole);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage(@RequestParam(value = "uri", required = false) String uri, Model model) {
        model.addAttribute("requestedUri", uri != null ? inputSanitizer.sanitize(uri) : "Restricted Page");
        return "access-denied";
    }
}
