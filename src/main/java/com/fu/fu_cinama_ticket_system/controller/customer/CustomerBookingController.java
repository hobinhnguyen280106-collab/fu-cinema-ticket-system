package com.fu.fu_cinama_ticket_system.controller.customer;

import com.fu.fu_cinama_ticket_system.dto.BookingRequest;
import com.fu.fu_cinama_ticket_system.entity.Booking;
import com.fu.fu_cinama_ticket_system.enums.BookingStatus;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.service.BookingService;
import com.fu.fu_cinama_ticket_system.service.ShowtimeService;
import com.fu.fu_cinama_ticket_system.util.PageUtils;
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
            return "redirect:/customer/bookings/" + booking.getBookingId();
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return back;
        }
    }

    // TODO 29: Lịch sử đặt vé của khách hàng
    @GetMapping("/bookings")
    public String history(@RequestParam(required = false) BookingStatus status,
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session, Model model) {
        model.addAttribute("bookings", bookingService.findByCustomer(
                SessionUtils.getCurrentCustomerId(session), status, page, PageUtils.DEFAULT_SIZE));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("pageUrl", PageUtils.currentUrlWithoutPage());
        return "customer/bookings";
    }

    // TODO 29: Chi tiết vé khách hàng (chống lỗi IDOR)
    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("booking", bookingService.getDetailForCustomer(id, SessionUtils.getCurrentCustomerId(session)));
        return "customer/booking-detail";
    }

    // TODO 29: Khách hàng hủy vé trước giờ chiếu ít nhất 2 tiếng (BR-14)
    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            bookingService.cancelByCustomer(id, SessionUtils.getCurrentCustomerId(session));
            ra.addFlashAttribute("success", "Đã huỷ đơn #" + id);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }
}
