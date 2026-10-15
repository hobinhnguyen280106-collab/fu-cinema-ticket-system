package com.fu.fu_cinama_ticket_system.repository;

import com.fu.fu_cinama_ticket_system.entity.Movie;
import com.fu.fu_cinama_ticket_system.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // ===== Derived query =====
    boolean existsByGenre_GenreId(Long genreId);

    long countByStatus(MovieStatus status);

    List<Movie> findByStatusNotOrderByTitleAsc(MovieStatus status);      // dropdown tạo suất chiếu

    // (custom query: xem TODO 10)
}
