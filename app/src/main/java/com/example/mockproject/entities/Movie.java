package com.example.mockproject.entities;

import android.annotation.SuppressLint;

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
    private long stableId;

    private boolean isFav;

    public Movie(int id, String title, float rating, String image, boolean isAdult, String releaseDate, String overview) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        String baseUrl = "https://image.tmdb.org/t/p/w500";
        this.posterPath = image.replace(baseUrl, "");
        this.isAdultMovie = isAdult;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.isFav = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        Movie other = (Movie) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
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

    @SuppressLint("DefaultLocale")
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

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public boolean isFav() {
        return isFav;
    }

    public long getStableId() {
        return stableId;
    }

    public void setStableId(long stableId) {
        this.stableId = stableId;
    }
}
