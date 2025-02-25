package com.example.mockproject.entities;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;

    @SerializedName("adult")
    private boolean isAdultMovie;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String overview;

    @SerializedName("vote_average")
    private float rating;

    @SerializedName("release_date")
    private String releaseDate;

    public Movie(int id, String title, float rating, String image, boolean isAdult, String releaseDate, String overview) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        String baseUrl = "https://image.tmdb.org/t/p/w500";
        String imagePath = image.replace(baseUrl, "");
        this.posterPath = imagePath;
        this.isAdultMovie = isAdult;
        this.releaseDate = releaseDate;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }
    public String getOverview() {
        return overview;
    }
    public boolean getIsAdultMovie() {
        return isAdultMovie;
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
