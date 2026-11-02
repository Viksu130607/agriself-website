package com.agriself.controller;

import com.agriself.model.AuditLog;
import com.agriself.model.CropMarketListing;
import com.agriself.model.MandiPrice;
import com.agriself.model.User;
import com.agriself.repository.AuditLogRepository;
import com.agriself.repository.CropMarketListingRepository;
import com.agriself.repository.MandiPriceRepository;
import com.agriself.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/market")
@PreAuthorize("hasAuthority('ROLE_BUSINESSMAN')")
@Slf4j
public class MarketController {

    private final MandiPriceRepository mandiPriceRepository;
    private final CropMarketListingRepository cropMarketListingRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public MarketController(MandiPriceRepository mandiPriceRepository,
                            CropMarketListingRepository cropMarketListingRepository,
                            UserRepository userRepository,
                            AuditLogRepository auditLogRepository) {
        this.mandiPriceRepository = mandiPriceRepository;
        this.cropMarketListingRepository = cropMarketListingRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public String showMarketDashboard(Authentication auth, Model model) {
        String email = auth.getName();
        User businessman = userRepository.findByEmail(email).orElse(null);

        List<MandiPrice> mandiPrices = mandiPriceRepository.findAllByOrderByModalPricePerQuintalDesc();
        List<CropMarketListing> cropListings = cropMarketListingRepository.findByStatusOrderByListedAtDesc("AVAILABLE");

        model.addAttribute("businessman", businessman);
        model.addAttribute("mandiPrices", mandiPrices);
        model.addAttribute("cropListings", cropListings);

        return "market/dashboard";
    }

    @PostMapping("/order")
    public String placeOrder(@RequestParam("listingId") Long listingId,
                             @RequestParam("orderQuantity") double orderQuantity,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        CropMarketListing listing = cropMarketListingRepository.findById(listingId).orElse(null);
        if (listing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/market/dashboard";
        }

        if (orderQuantity <= 0 || orderQuantity > listing.getQuantityQuintals()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Requested quantity is invalid or exceeds available stock.");
            return "redirect:/market/dashboard";
        }

        double totalAmount = orderQuantity * listing.getPricePerQuintal();

        // Update remaining quantity
        double remaining = listing.getQuantityQuintals() - orderQuantity;
        if (remaining <= 0) {
            listing.setStatus("SOLD");
            listing.setQuantityQuintals(0);
        } else {
            listing.setQuantityQuintals(remaining);
        }
        cropMarketListingRepository.save(listing);

        // Record Audit log
        AuditLog orderLog = AuditLog.builder()
                .eventType("CROP_PURCHASE_ORDER")
                .username(auth.getName())
                .details(String.format("Purchased %.1f Quintals of %s at ₹%.2f/Quintal. Total: ₹%.2f",
                        orderQuantity, listing.getCropName(), listing.getPricePerQuintal(), totalAmount))
                .build();
        auditLogRepository.save(orderLog);

        redirectAttributes.addFlashAttribute("successMessage",
                String.format("✅ Order Confirmed! Booked %.1f Quintals of %s for ₹%,.2f. Farmer contact: %s",
                        orderQuantity, listing.getCropName(), totalAmount, listing.getContactNumber()));

        return "redirect:/market/dashboard";
    }
}
