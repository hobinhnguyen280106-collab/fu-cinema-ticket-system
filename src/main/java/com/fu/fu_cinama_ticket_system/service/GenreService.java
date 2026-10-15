package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.GenreForm;
import com.fu.fu_cinama_ticket_system.entity.Genre;

import java.util.List;

public interface GenreService {

    List<Genre> findAll();

    GenreForm getForm(Long genreId);

    void create(GenreForm form);

    void update(Long genreId, GenreForm form);

    void delete(Long genreId);
}
