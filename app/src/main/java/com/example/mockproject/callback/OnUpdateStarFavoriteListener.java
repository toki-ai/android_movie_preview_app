package com.example.mockproject.callback;

import com.example.mockproject.entities.Movie;
import com.example.mockproject.fragment.adapter.MovieAdapter;

public interface OnUpdateStarFavoriteListener {
    void onUpdateStartFavorite(Movie movie, MovieAdapter.TYPE type);
}
