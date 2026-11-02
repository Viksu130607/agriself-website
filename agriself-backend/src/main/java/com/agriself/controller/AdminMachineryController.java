package com.agriself.controller;

import com.agriself.model.AuditLog;
import com.agriself.model.Machinery;
import com.agriself.model.MachineryStatus;
import com.agriself.model.User;
import com.agriself.repository.AuditLogRepository;
import com.agriself.repository.MachineryRepository;
import com.agriself.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Slf4j
public class AdminMachineryController {

    private final MachineryRepository machineryRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminMachineryController(MachineryRepository machineryRepository,
                                    UserRepository userRepository,
                                    AuditLogRepository auditLogRepository) {
        this.machineryRepository = machineryRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Authentication auth, Model model) {
        String adminEmail = auth.getName();
        User adminUser = userRepository.findByEmail(adminEmail).orElse(null);

        List<Machinery> machineries = machineryRepository.findAllByOrderByEquipmentCodeAsc();
        long totalCount = machineries.size();
        long inUseCount = machineryRepository.countByStatus(MachineryStatus.IN_USE);
        long availableCount = machineryRepository.countByStatus(MachineryStatus.AVAILABLE);
        long maintenanceCount = machineryRepository.countByStatus(MachineryStatus.UNDER_MAINTENANCE);

        List<User> users = userRepository.findAll();
        List<AuditLog> recentLogs = auditLogRepository.findTop50ByOrderByTimestampDesc();

        model.addAttribute("adminUser", adminUser);
        model.addAttribute("machineries", machineries);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("inUseCount", inUseCount);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("maintenanceCount", maintenanceCount);
        model.addAttribute("users", users);
        model.addAttribute("auditLogs", recentLogs);

        return "admin/dashboard";
    }

    @PostMapping("/machinery/{id}/status")
    public String updateMachineryStatus(@PathVariable Long id,
                                        @RequestParam("newStatus") MachineryStatus newStatus,
                                        @RequestParam(value = "assignedTo", required = false) String assignedTo,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {

        Machinery machine = machineryRepository.findById(id).orElse(null);
        if (machine == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Machinery not found.");
            return "redirect:/admin/dashboard";
        }

        MachineryStatus oldStatus = machine.getStatus();
        machine.setStatus(newStatus);
        if (assignedTo != null && !assignedTo.isBlank()) {
            machine.setAssignedToUser(assignedTo);
        } else if (newStatus == MachineryStatus.AVAILABLE) {
            machine.setAssignedToUser("Unassigned (In Depot)");
        }
        machineryRepository.save(machine);

        // Security Audit Log
        AuditLog auditLog = AuditLog.builder()
                .eventType("MACHINERY_STATUS_CHANGED")
                .username(auth.getName())
                .details(String.format("Updated machine [%s] status from %s to %s",
                        machine.getEquipmentCode(), oldStatus, newStatus))
                .build();
        auditLogRepository.save(auditLog);

        redirectAttributes.addFlashAttribute("successMessage",
                String.format("✅ Machine [%s - %s] status updated to %s",
                        machine.getEquipmentCode(), machine.getName(), newStatus));

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/dashboard";
        }

        if (user.getEmail().equalsIgnoreCase(auth.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot deactivate your own administrator account.");
            return "redirect:/admin/dashboard";
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        AuditLog auditLog = AuditLog.builder()
                .eventType("USER_STATUS_TOGGLED")
                .username(auth.getName())
                .details(String.format("User [%s] enabled status set to: %b", user.getEmail(), user.isEnabled()))
                .build();
        auditLogRepository.save(auditLog);

        redirectAttributes.addFlashAttribute("successMessage",
                String.format("User [%s] account is now %s", user.getEmail(), user.isEnabled() ? "ACTIVE" : "DISABLED"));

        return "redirect:/admin/dashboard";
    }
}
