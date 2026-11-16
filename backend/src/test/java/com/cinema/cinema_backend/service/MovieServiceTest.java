package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.CreateMovieRequest;
import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Test
    void createsMovieWithTrimmedRequiredFieldsAndOptionalMetadata() {
        MovieService movieService = new MovieService(movieRepository);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movie movie = movieService.createMovie(new CreateMovieRequest(
                "  New Movie  ",
                "  Drama ",
                " PG-13 ",
                "  A new story. ",
                " ",
                null,
                "COMING_SOON",
                " Director ",
                " Cast ",
                " Producer "
        ));

        assertEquals("New Movie", movie.getTitle());
        assertEquals("Drama", movie.getGenre());
        assertEquals("A new story.", movie.getDescription());
        assertEquals("PG-13", movie.getRating());
        assertEquals("COMING_SOON", movie.getStatus());
        assertNull(movie.getPosterUrl());
        assertEquals("Director", movie.getDirector());
        verify(movieRepository).save(any(Movie.class));
    }
}
