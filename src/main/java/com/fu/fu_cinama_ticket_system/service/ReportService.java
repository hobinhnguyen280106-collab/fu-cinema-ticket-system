package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.ReportResult;

import java.time.LocalDate;

public interface ReportService {

    ReportResult generate(LocalDate startDate, LocalDate endDate);
}
