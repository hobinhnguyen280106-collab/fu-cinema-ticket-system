package com.fu.cinema.desktop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportBookingRow(Long id, LocalDateTime bookingDate, String customerName, String movieTitle, BigDecimal total) {

}
