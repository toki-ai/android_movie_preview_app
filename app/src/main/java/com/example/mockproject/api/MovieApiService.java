package com.example.mockproject.api;

import com.example.mockproject.entities.CreditResponse;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("movie/{category}")
    Call<MovieResponse> getMoviesByCategory(
            @Path("category") String category,
            @Query("api_key") String apiKey,
            @Query("page") int page
    );

    @GET("movie/{id}")
    Call<Movie> getMovieDetail(
            @Path("id") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("movie/{id}/credits")
    Call<CreditResponse> getMovieCrewAndCast(
            @Path("id") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
        @Query("api_key") String apiKey,
        @Query("page") int page,
        @Query("query") String query
    );
}
