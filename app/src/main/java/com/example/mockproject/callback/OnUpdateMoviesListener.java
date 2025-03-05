package com.example.mockproject.callback;

import com.example.mockproject.entities.Movie;

public interface OnUpdateMoviesListener {
    void onUpdateMoviesFromFavorite(Movie movie);
    void onUpdateMoviesFromList(Movie movie);
    void onUpdateMoviesFromDetail(Movie movie);
}
