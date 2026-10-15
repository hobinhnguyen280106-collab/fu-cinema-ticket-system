package com.fu.fu_cinama_ticket_system;

import com.fu.fu_cinama_ticket_system.dto.MovieRevenueDTO;
import com.fu.fu_cinama_ticket_system.dto.RatingSummary;
import com.fu.fu_cinama_ticket_system.dto.RoomShowtimeCount;
import com.fu.fu_cinama_ticket_system.entity.Showtime;
import com.fu.fu_cinama_ticket_system.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("local") // Chạy trên database SQL Server đã có dữ liệu seed
@Transactional // Tự động rollback, không làm sai lệch database
class QueryCheckTests {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void checkQueries() {
        LocalDate today = LocalDate.now();

        // 1. Check Q3: Kiểm tra trùng giờ phòng Room 01 lúc N+1 10:00 (trùng suất 09:00 - 11:15)
        Long room01Id = roomRepository.findAll().stream()
                .filter(r -> r.getRoomName().equalsIgnoreCase("Room 01"))
                .findFirst().map(r -> r.getRoomId()).orElse(1L);

        LocalDateTime checkStart = today.plusDays(1).atTime(10, 0);
        LocalDateTime checkEnd = today.plusDays(1).atTime(11, 45);
        List<Showtime> overlaps = showtimeRepository.findOverlapping(room01Id, checkStart, checkEnd, null);
        System.out.println("=== [TEST Q3 - Chồng giờ] ===");
        System.out.println("Số suất bị trùng: " + overlaps.size());
        overlaps.forEach(s -> System.out.println("-> Trùng với suất: " + s.getMovie().getTitle() + " [" + s.getStartTime() + " - " + s.getEndTime() + "]"));

        // 2. Check Q4: Ghế đã bán của suất N+1 09:00
        Showtime s1 = showtimeRepository.findAll().stream()
                .filter(s -> s.getStartTime().toLocalDate().isEqual(today.plusDays(1)) && s.getStartTime().getHour() == 9)
                .findFirst().orElse(null);
        if (s1 != null) {
            List<String> soldSeats = ticketRepository.findSoldSeatCodes(s1.getShowtimeId());
            System.out.println("=== [TEST Q4 - Ghế đã bán suất 09:00] ===");
            System.out.println("Ghế đã bán (loại trừ đơn huỷ): " + soldSeats);
        }

        // 3. Check Q7: Báo cáo doanh thu theo phim N-7 -> N
        LocalDateTime from = today.minusDays(7).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        List<MovieRevenueDTO> revenues = bookingRepository.revenueByMovie(from, to);
        System.out.println("=== [TEST Q7 - Doanh thu theo phim N-7 đến N] ===");
        revenues.forEach(r -> System.out.println("-> Phim: " + r.getTitle() + " | Số vé: " + r.getTicketCount() + " | Doanh thu: " + r.getRevenue()));

        // 4. Check Q10: Thống kê đánh giá của phim chưa có review
        RatingSummary summary = reviewRepository.summarize(9999L);
        System.out.println("=== [TEST Q10 - Điểm đánh giá phim rỗng] ===");
        System.out.println("Điểm TB: " + (summary != null ? summary.getAverage() : null) + " | Lượt đánh giá: " + (summary != null ? summary.getTotal() : 0));

        // 5. Check Q11: Native query số suất hôm nay theo phòng
        List<RoomShowtimeCount> roomCounts = showtimeRepository.countTodayShowtimesByRoom();
        System.out.println("=== [TEST Q11 - Suất hôm nay theo phòng (Native Query)] ===");
        roomCounts.forEach(rc -> System.out.println("-> Phòng: " + rc.getRoomName() + " | Số suất hôm nay: " + rc.getShowtimeCount()));
    }
}