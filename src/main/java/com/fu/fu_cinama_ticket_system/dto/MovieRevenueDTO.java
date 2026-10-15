package com.fu.fu_cinama_ticket_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// Dùng trong constructor expression của JPQL: kiểu tham số khớp kết quả (COUNT → Long, SUM(BigDecimal) → BigDecimal)
@Getter
@AllArgsConstructor
public class MovieRevenueDTO {

    private final String title;
    private final Long ticketCount;
    private final BigDecimal revenue;
}