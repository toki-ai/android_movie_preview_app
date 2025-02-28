package com.example.mockproject.callback;

public interface OnUpdateMovieListListener {
    void onToolbarIconClick();
    void onToolbarOpsClick(String type);
    void onUpdateItemStarFav(int movieId);
}

