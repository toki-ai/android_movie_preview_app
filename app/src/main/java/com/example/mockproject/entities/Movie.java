package com.example.mockproject.entities;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String overview;

    @SerializedName("vote_average")
    private float rating;

    @SerializedName("release_date")
    private String releaseDate;

    public String getOverview() {
        return overview;
    }

    public String getRating() {
        return String.format("%.1f", rating);
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }
}
