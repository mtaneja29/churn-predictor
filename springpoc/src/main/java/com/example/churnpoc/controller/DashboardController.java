package com.example.churnpoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.churnpoc.service.DashboardService;
import com.example.churnpoc.service.ScanService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private DashboardService dashboardService;
    private ScanService scanService;

    @Autowired
    public DashboardController(DashboardService theDashboardService,
                               ScanService theScanService) {
        dashboardService = theDashboardService;
        scanService = theScanService;
    }

    @GetMapping
    public String showDashboard(@RequestParam(defaultValue = "HIGH") String band,
                                @RequestParam(defaultValue = "0") int page,
                                Model theModel) {
        theModel.addAttribute("view", dashboardService.getDashboard(band, page));
        return "dashboard";
    }

    @PostMapping("/scan")
    public String rescan(RedirectAttributes theRedirectAttributes) {
        try {
            scanService.scanAll();
        }
        catch (Exception exc) {
            theRedirectAttributes.addFlashAttribute("errorMessage",
                    "Scoring failed: " + ScanErrorMessages.describe(exc));
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/clear")
    public String clearAll() {
        dashboardService.clearAll();
        return "redirect:/upload";
    }

    // bad customer id / not-yet-scored -> back to the dashboard with a message, not a 500
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBadReviewRequest(IllegalArgumentException exc,
                                         RedirectAttributes theRedirectAttributes) {
        theRedirectAttributes.addFlashAttribute("errorMessage", exc.getMessage());
        return "redirect:/dashboard";
    }

    @GetMapping("/customer/{id}")
    public String reviewCustomer(@PathVariable Long id, Model theModel) {
        theModel.addAttribute("review", dashboardService.getReview(id));
        return "customer-review";
    }

    @PostMapping("/customer/{id}/unflag")
    public String unflag(@PathVariable Long id) {
        dashboardService.unflag(id);
        return "redirect:/dashboard";
    }
}
