package com.fu.fu_cinama_ticket_system.dto;

import com.fu.fu_cinama_ticket_system.entity.Showtime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMap {
    private Showtime showtime;
    private List<String> rows;
    private Integer seatsPerRow;
    private Set<String> soldSeats;
}
