package com.fu.fu_cinama_ticket_system.repository;

import com.fu.fu_cinama_ticket_system.entity.Review;
import com.fu.fu_cinama_ticket_system.entity.ReviewId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {

    // ===== Derived query =====
    @EntityGraph(attributePaths = "customer")
    List<Review> findByMovie_MovieIdOrderByReviewDateDesc(Long movieId);

    // (custom query: xem TODO 10)
}
