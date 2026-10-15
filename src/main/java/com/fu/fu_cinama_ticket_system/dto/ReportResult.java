package com.fu.fu_cinama_ticket_system.dto;

import com.fu.fu_cinama_ticket_system.entity.Booking;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
public class ReportResult {

    LocalDate startDate;
    LocalDate endDate;
    List<MovieRevenueDTO> revenueByMovie;
    List<Booking> bookings;
    long totalBookings;
    long totalTickets;
    BigDecimal totalRevenue;

    public boolean isEmpty() {
        return bookings.isEmpty();
    }
}
