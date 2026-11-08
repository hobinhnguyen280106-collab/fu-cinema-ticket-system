package com.fu.cinema.desktop.dto;

import java.util.List;

public record DashboardData(long showingMovies, long customers, long pendingBookings, List<RoomCount> todayByRoom) {

}
