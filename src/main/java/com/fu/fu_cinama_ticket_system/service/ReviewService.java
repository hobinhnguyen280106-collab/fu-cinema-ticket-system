package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.RatingSummary;
import com.fu.fu_cinama_ticket_system.dto.ReviewForm;
import com.fu.fu_cinama_ticket_system.entity.Review;

import java.util.List;

public interface ReviewService {

    List<Review> findByMovie(Long movieId);

    RatingSummary summarize(Long movieId);

    boolean canReview(Long customerId, Long movieId);

    ReviewForm getForm(Long customerId, Long movieId);

    void save(Long customerId, Long movieId, ReviewForm form);
}
