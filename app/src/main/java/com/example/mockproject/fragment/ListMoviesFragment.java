package com.example.mockproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;
import com.example.mockproject.api.ApiClient;
import com.example.mockproject.api.MovieApiService;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.MovieResponse;
import com.example.mockproject.fragment.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMoviesFragment extends Fragment {
    private static final String API_KEY = "e7631ffcb8e766993e5ec0c1f4245f93";
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private boolean isGrid = false;
    private MovieApiService movieApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_movies, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        movieAdapter = new MovieAdapter(new ArrayList<>(), isGrid);
        recyclerView.setAdapter(movieAdapter);
        updateLayoutManager();

        movieApiService = ApiClient.getClient().create(MovieApiService.class);
        fetchMovies(1);

        ((MainActivity) requireActivity()).setToolbarIconClickListener(() -> {
            isGrid = !isGrid;
            updateLayoutManager();
            movieAdapter = new MovieAdapter(movieAdapter.getMovies(), isGrid);
            recyclerView.setAdapter(movieAdapter);
        });

        return view;
    }

    private void updateLayoutManager() {
        recyclerView.setLayoutManager(isGrid ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext()));
    }

    private void fetchMovies(int page) {
        movieApiService.getPopularMovies(API_KEY, page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovies();
                    movieAdapter.updateMovies(movies);
                } else {
                    Toast.makeText(getContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
