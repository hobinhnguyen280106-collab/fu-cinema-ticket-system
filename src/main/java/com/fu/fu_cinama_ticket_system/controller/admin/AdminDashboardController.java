package com.fu.fu_cinama_ticket_system.controller.admin;

import com.fu.fu_cinama_ticket_system.dto.DashboardStats;
import com.fu.fu_cinama_ticket_system.service.BookingService;
import com.fu.fu_cinama_ticket_system.service.CustomerService;
import com.fu.fu_cinama_ticket_system.service.MovieService;
import com.fu.fu_cinama_ticket_system.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MovieService movieService;
    private final CustomerService customerService;
    private final BookingService bookingService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", new DashboardStats(
                movieService.countShowing(),
                customerService.count(),
                bookingService.countPending(),
                showtimeService.countTodayByRoom()));
        return "admin/dashboard";
    }
}