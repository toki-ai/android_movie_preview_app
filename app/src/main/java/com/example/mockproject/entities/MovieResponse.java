package com.example.mockproject.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<Movie> movies;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public int getPage() { return page; }
    public List<Movie> getMovies() { return movies; }
    public int getTotalPages() { return totalPages; }
    public int getTotalResults() { return totalResults; }
}
