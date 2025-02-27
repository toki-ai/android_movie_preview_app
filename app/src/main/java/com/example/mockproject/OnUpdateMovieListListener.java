package com.example.mockproject;

public interface OnUpdateMovieListListener {
    void onToolbarIconClick();
    void onToolbarOpsClick(String type);
    void onUpdateItemStarFav(int movieId);
}

