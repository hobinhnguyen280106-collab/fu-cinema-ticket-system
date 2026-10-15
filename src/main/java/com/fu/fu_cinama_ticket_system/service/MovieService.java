package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.MovieForm;
import com.fu.fu_cinama_ticket_system.entity.Movie;
import com.fu.fu_cinama_ticket_system.enums.MovieStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {

    Page<Movie> searchForAdmin(String keyword, Long genreId, MovieStatus status, int page, int size, String sort);

    Page<Movie> searchForCustomer(String keyword, Long genreId, int page, int size);

    Movie getById(Long movieId);

    MovieForm getForm(Long movieId);

    void create(MovieForm form);

    void update(Long movieId, MovieForm form);

    /**
     * Xoá phim theo quy tắc BR-06:
     * @return true nếu đã xoá hẳn khỏi database, false nếu chỉ chuyển trạng thái sang STOPPED
     */
    boolean delete(Long movieId);

    List<Movie> findSchedulable();

    long countShowing();
}