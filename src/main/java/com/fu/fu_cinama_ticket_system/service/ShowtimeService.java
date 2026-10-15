package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.RoomShowtimeCount;
import com.fu.fu_cinama_ticket_system.dto.SeatMap;
import com.fu.fu_cinama_ticket_system.dto.ShowtimeForm;
import com.fu.fu_cinama_ticket_system.entity.Showtime;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {

    Page<Showtime> search(Long movieId, Long roomId, LocalDate date, int page, int size);

    ShowtimeForm getForm(Long showtimeId);

    void create(ShowtimeForm form);

    void update(Long showtimeId, ShowtimeForm form);

    /** @return true nếu đã xoá hẳn, false nếu chỉ huỷ suất chiếu (BR-09) */
    boolean delete(Long showtimeId);

    List<Showtime> findUpcomingByMovie(Long movieId);

    SeatMap getSeatMap(Long showtimeId);

    List<RoomShowtimeCount> countTodayByRoom();
}
