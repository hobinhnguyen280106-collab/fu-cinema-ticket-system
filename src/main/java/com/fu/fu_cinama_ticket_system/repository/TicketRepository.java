package com.fu.fu_cinama_ticket_system.repository;

import com.fu.fu_cinama_ticket_system.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ===== Q4: ghế đã bán của một suất chiếu (không tính đơn đã huỷ) =====
    @Query("""
            SELECT t.seatCode FROM Ticket t
            WHERE t.showtime.showtimeId = :showtimeId
              AND t.booking.status <> com.fu.fu_cinama_ticket_system.enums.BookingStatus.CANCELLED
            """)
    List<String> findSoldSeatCodes(@Param("showtimeId") Long showtimeId);
}
