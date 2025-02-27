package com.example.mockproject;

import com.example.mockproject.fragment.adapter.MovieAdapter;

public interface OnUpdateStarFavoriteListener {
    void onUpdateStartFavorite(int movieId, MovieAdapter.TYPE type);
}
