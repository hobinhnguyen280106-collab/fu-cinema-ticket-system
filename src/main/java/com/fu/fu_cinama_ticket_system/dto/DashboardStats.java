package com.fu.fu_cinama_ticket_system.dto;

import lombok.Value;

import java.util.List;

@Value
public class DashboardStats {

    long showingMovies;
    long customers;
    long pendingBookings;
    List<RoomShowtimeCount> todayShowtimesByRoom;
}