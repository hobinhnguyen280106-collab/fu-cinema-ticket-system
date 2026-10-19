package com.fu.cinema.service;

import com.fu.cinema.entity.Movie;

import java.util.List;

public interface MovieService {

    List<Movie> findSchedulable();
}
