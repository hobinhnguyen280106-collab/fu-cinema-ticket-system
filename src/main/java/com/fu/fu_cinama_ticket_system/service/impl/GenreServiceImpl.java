package com.fu.fu_cinama_ticket_system.service.impl;

import com.fu.fu_cinama_ticket_system.dto.GenreForm;
import com.fu.fu_cinama_ticket_system.entity.Genre;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.exception.ResourceNotFoundException;
import com.fu.fu_cinama_ticket_system.repository.GenreRepository;
import com.fu.fu_cinama_ticket_system.repository.MovieRepository;
import com.fu.fu_cinama_ticket_system.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAllByOrderByGenreNameAsc();
    }

    @Override
    public GenreForm getForm(Long genreId) {
        Genre g = find(genreId);
        GenreForm form = new GenreForm();
        form.setGenreId(g.getGenreId());
        form.setGenreName(g.getGenreName());
        form.setDescription(g.getDescription());
        return form;
    }

    @Override
    @Transactional
    public void create(GenreForm form) {
        if (genreRepository.existsByGenreNameIgnoreCase(form.getGenreName().trim())) {
            throw new BusinessException("Tên thể loại đã tồn tại");
        }
        genreRepository.save(new Genre(form.getGenreName().trim(), form.getDescription().trim()));
    }

    @Override
    @Transactional
    public void update(Long genreId, GenreForm form) {
        Genre genre = find(genreId);
        if (genreRepository.existsByGenreNameIgnoreCaseAndGenreIdNot(form.getGenreName().trim(), genreId)) {
            throw new BusinessException("Tên thể loại đã tồn tại");
        }
        genre.setGenreName(form.getGenreName().trim());
        genre.setDescription(form.getDescription().trim());
    }

    @Override
    @Transactional
    public void delete(Long genreId) {
        Genre genre = find(genreId);
        if (movieRepository.existsByGenre_GenreId(genreId)) {                     // BR-05
            throw new BusinessException("Không thể xoá: vẫn còn phim thuộc thể loại \"" + genre.getGenreName() + "\"");
        }
        genreRepository.delete(genre);
    }

    private Genre find(Long id) {
        return genreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thể loại", id));
    }
}
