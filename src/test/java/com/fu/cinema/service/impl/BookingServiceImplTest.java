package com.fu.cinema.service.impl;

import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Room;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import com.fu.cinema.enums.ShowtimeStatus;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.BookingRepository;
import com.fu.cinema.repository.CustomerRepository;
import com.fu.cinema.repository.ShowtimeRepository;
import com.fu.cinema.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Unit test (không cần database): kiểm tra BR-10, BR-11, BR-12 của BookingServiceImpl.create */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Showtime showtime;
    private Customer customer;

    @BeforeEach
    void setUp() {
        Room room = new Room("Room 01", RoomType.TWO_D, 8, 12, RoomStatus.ACTIVE);   // hàng A..H, ghế 1..12

        Movie movie = new Movie();
        movie.setTitle("Phim test");
        movie.setDuration(120);
        movie.setAgeRating(AgeRating.T18);

        showtime = new Showtime();
        showtime.setShowtimeId(1L);
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setEndTime(showtime.getStartTime().plusMinutes(135));
        showtime.setTicketPrice(BigDecimal.valueOf(80000));
        showtime.setStatus(ShowtimeStatus.OPEN);

        customer = new Customer();
        customer.setCustomerId(10L);
        customer.setBirthday(LocalDate.now().minusYears(25));
    }

    private BookingRequest request(String... seats) {
        BookingRequest r = new BookingRequest();
        r.setShowtimeId(1L);
        r.setSeatCodes(List.of(seats));
        return r;
    }

    @Test
    void create_success_totalIsCalculatedOnServer() {
        when(showtimeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(showtime));
        when(ticketRepository.findSoldSeatCodes(1L)).thenReturn(List.of("A1"));
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking booking = bookingService.create(10L, request("b2", "B3"));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getTickets()).hasSize(2);
        assertThat(booking.getTickets().get(0).getSeatCode()).isEqualTo("B2");
        assertThat(booking.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(160000));
    }

    @Test
    void create_moreThan8Seats_throws() {
        assertThatThrownBy(() -> bookingService.create(10L,
                request("A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tối đa");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_showtimeNotOpen_throws() {
        showtime.setStatus(ShowtimeStatus.CANCELLED);
        when(showtimeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(showtime));

        assertThatThrownBy(() -> bookingService.create(10L, request("A1")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không còn mở bán");
    }

    @Test
    void create_seatOutsideRoom_throws() {
        when(showtimeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(showtime));

        assertThatThrownBy(() -> bookingService.create(10L, request("A13")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không tồn tại");
    }

    @Test
    void create_seatAlreadySold_throws() {
        when(showtimeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(showtime));
        when(ticketRepository.findSoldSeatCodes(1L)).thenReturn(List.of("A1"));

        assertThatThrownBy(() -> bookingService.create(10L, request("A1", "A2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("A1");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_customerTooYoungForT18_throws() {
        customer.setBirthday(LocalDate.now().minusYears(15));
        when(showtimeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(showtime));
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> bookingService.create(10L, request("C5")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("18 tuổi");
        verify(bookingRepository, never()).save(any());
    }
}
