package com.fu.fu_cinama_ticket_system.controller.admin;

import com.fu.fu_cinama_ticket_system.enums.BookingStatus;
import com.fu.fu_cinama_ticket_system.service.BookingService;
import com.fu.fu_cinama_ticket_system.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    // TODO 24: Danh sách đơn đặt vé + lọc đa tiêu chí
    @GetMapping
    public String list(@RequestParam(required = false) BookingStatus status,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("bookings", bookingService.search(status, from, to, kw, page, PageUtils.DEFAULT_SIZE));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("kw", kw);
        model.addAttribute("pageUrl", PageUtils.currentUrlWithoutPage());
        return "admin/bookings/list";
    }

    // TODO 24: Chi tiết đơn đặt vé
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("booking", bookingService.getDetail(id));
        return "admin/bookings/detail";
    }

    // TODO 25: Xác nhận đơn (PENDING -> CONFIRMED theo BR-13)
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.confirm(id);
        ra.addFlashAttribute("success", "Đã xác nhận đơn #" + id);
        return "redirect:/admin/bookings/" + id;
    }

    // TODO 25: Hủy đơn (-> CANCELLED theo BR-13)
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.cancelByAdmin(id);
        ra.addFlashAttribute("success", "Đã huỷ đơn #" + id);
        return "redirect:/admin/bookings/" + id;
    }
}
