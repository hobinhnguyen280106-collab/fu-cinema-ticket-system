package com.fu.fu_cinama_ticket_system.controller.customer;

import com.fu.fu_cinama_ticket_system.dto.BookingRequest;
import com.fu.fu_cinama_ticket_system.entity.Booking;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.service.BookingService;
import com.fu.fu_cinama_ticket_system.service.ShowtimeService;
import com.fu.fu_cinama_ticket_system.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final ShowtimeService showtimeService;
    private final BookingService bookingService;

    // TODO 28: Xem sơ đồ ghế của suất chiếu
    @GetMapping("/showtimes/{id}/seats")
    public String seats(@PathVariable Long id, Model model) {
        model.addAttribute("seatMap", showtimeService.getSeatMap(id));
        return "customer/seats";
    }

    // TODO 28: Đặt vé (chọn ghế, kiểm tra tuổi, pessimistic lock)
    @PostMapping("/bookings")
    public String book(@Valid @ModelAttribute BookingRequest request, BindingResult br,
                       HttpSession session, RedirectAttributes ra) {
        String back = "redirect:/customer/showtimes/" + request.getShowtimeId() + "/seats";
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
            return back;
        }
        try {
            Long customerId = SessionUtils.getCurrentCustomerId(session);
            Booking booking = bookingService.create(customerId, request);
            ra.addFlashAttribute("success", "Đặt vé thành công! Mã đơn #" + booking.getBookingId());
            // Điều hướng sang chi tiết đơn (hoặc về trang chủ nếu chưa có TODO 29)
            return "redirect:/customer/bookings/" + booking.getBookingId();
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return back;
        }
    }
}
