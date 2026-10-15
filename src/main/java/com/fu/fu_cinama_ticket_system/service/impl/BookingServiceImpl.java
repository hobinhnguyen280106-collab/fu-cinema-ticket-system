package com.fu.fu_cinama_ticket_system.service.impl;

import com.fu.fu_cinama_ticket_system.dto.BookingRequest;
import com.fu.fu_cinama_ticket_system.entity.Booking;
import com.fu.fu_cinama_ticket_system.enums.BookingStatus;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.exception.ResourceNotFoundException;
import com.fu.fu_cinama_ticket_system.repository.BookingRepository;
import com.fu.fu_cinama_ticket_system.service.BookingService;
import com.fu.fu_cinama_ticket_system.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    // ================= Admin (TODO 24: Danh sách & Chi tiết đơn) =================

    @Override
    public Page<Booking> search(BookingStatus status, LocalDate fromDate, LocalDate toDate, String keyword, int page, int size) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("Từ ngày phải trước hoặc bằng đến ngày");
        }
        return bookingRepository.search(status,
                fromDate == null ? null : fromDate.atStartOfDay(),
                toDate == null ? null : toDate.plusDays(1).atStartOfDay(),
                ValidationUtils.blankToNull(keyword),
                PageRequest.of(Math.max(page, 0), size, Sort.by("bookingDate").descending()));
    }

    @Override
    public Booking getDetail(Long bookingId) {
        return bookingRepository.findDetailById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt vé", bookingId));
    }

    @Override
    public long countPending() {
        return bookingRepository.countByStatus(BookingStatus.PENDING);
    }

    // ================= Admin (TODO 25: Xác nhận / Hủy đơn - Chưa làm) =================

    @Override
    @Transactional
    public void confirm(Long bookingId) {
        throw new UnsupportedOperationException("Chức năng duyệt đơn thuộc TODO 25");
    }

    @Override
    @Transactional
    public void cancelByAdmin(Long bookingId) {
        throw new UnsupportedOperationException("Chức năng hủy đơn thuộc TODO 25");
    }

    // ================= Customer (TODO 28 & 29: Đặt vé & Lịch sử - Chưa làm) =================

    @Override
    @Transactional
    public Booking create(Long customerId, BookingRequest request) {
        throw new UnsupportedOperationException("Chức năng đặt vé thuộc TODO 28");
    }

    @Override
    public Page<Booking> findByCustomer(Long customerId, BookingStatus status, int page, int size) {
        throw new UnsupportedOperationException("Lịch sử đặt vé thuộc TODO 29");
    }

    @Override
    public Booking getDetailForCustomer(Long bookingId, Long customerId) {
        throw new UnsupportedOperationException("Chi tiết vé khách hàng thuộc TODO 29");
    }

    @Override
    @Transactional
    public void cancelByCustomer(Long bookingId, Long customerId) {
        throw new UnsupportedOperationException("Hủy vé khách hàng thuộc TODO 29");
    }
}
