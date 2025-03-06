package com.example.mockproject.fragment;


import static com.example.mockproject.BuildConfig.API_KEY;
import static com.example.mockproject.utils.Constants.KEY_MOVIE_TYPE;
import static com.example.mockproject.utils.Constants.KEY_RATING_FILTER;
import static com.example.mockproject.utils.Constants.KEY_RELEASE_YEAR_FILTER;
import static com.example.mockproject.utils.Constants.KEY_SORT_OPTION;
import static com.example.mockproject.utils.Constants.SHARE_KEY;
import static com.example.mockproject.utils.Constants.TYPE_NOW_PLAYING;
import static com.example.mockproject.utils.Constants.TYPE_POPULAR;
import static com.example.mockproject.utils.Constants.TYPE_TOP_RATED;
import static com.example.mockproject.utils.Constants.TYPE_UPCOMING;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.R;
import com.example.mockproject.api.ApiClient;
import com.example.mockproject.api.MovieApiService;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.MovieResponse;
import com.example.mockproject.adapter.MovieAdapter;
import com.example.mockproject.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMoviesFragment extends Fragment  {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    public boolean isGrid = false;
    private MovieApiService movieApiService;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPages = 1;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_movies, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        movieAdapter = new MovieAdapter(new ArrayList<>(), isGrid, getContext(), MovieAdapter.TYPE.LIST);
        recyclerView.setAdapter(movieAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        updateLayoutManager();

        movieApiService = ApiClient.getClient().create(MovieApiService.class);
        fetchMovies(currentPage);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (!rv.canScrollVertically(1)) {
                    if (!isLoading && currentPage < totalPages) {
                        loadNextPage();
                    }
                }
            }
        });

        prefs = requireContext().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        prefListener = (sharedPreferences, key) -> {
            if (key != null &&
                    (key.equals(KEY_MOVIE_TYPE)
                            || key.equals(KEY_RATING_FILTER)
                            || key.equals(KEY_RELEASE_YEAR_FILTER)
                            || key.equals(KEY_SORT_OPTION))
            ) {
                currentPage = 1;
                movieAdapter.updateMovies(new ArrayList<>());
                fetchMovies(currentPage);
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        return view;
    }
    private void loadNextPage() {
        currentPage++;
        fetchMovies(currentPage);
    }

    private void updateLayoutManager() {
        recyclerView.setLayoutManager(isGrid ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext()));
    }
    public void refreshMovies() {
        currentPage = 1;
        movieAdapter.updateMovies(new ArrayList<>());
        fetchMovies(currentPage);
    }
    public void toggleLayout() {
        isGrid = !isGrid;
        updateLayoutManager();
        movieAdapter = new MovieAdapter(movieAdapter.getMovies(), isGrid, getContext(), MovieAdapter.TYPE.LIST);
        recyclerView.setAdapter(movieAdapter);
    }

    private void fetchMovies(int page) {
        isLoading = true;
        movieAdapter.addLoadingFooter();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        String movieType = sharedPreferences.getString(KEY_MOVIE_TYPE, "Popular Movies");
        Call<MovieResponse> call;
        switch (movieType) {
            case TYPE_TOP_RATED:
                call = movieApiService.getTopRatedMovies(API_KEY, page);
                break;
            case TYPE_UPCOMING:
                call = movieApiService.getUpcomingMovies(API_KEY, page);
                break;
            case TYPE_NOW_PLAYING:
                call = movieApiService.getNowPlayingMovies(API_KEY, page);
                break;
            case TYPE_POPULAR:
            default:
                call = movieApiService.getPopularMovies(API_KEY, page);
                break;
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                movieAdapter.removeLoadingFooter();
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> newMovies = response.body().getMovies();

                    float minRating = sharedPreferences.getFloat(KEY_RATING_FILTER, 0f);
                    if (minRating > 0f) {
                        newMovies = newMovies.stream()
                                .filter(m -> Float.parseFloat(m.getRating()) >= minRating)
                                .collect(Collectors.toList());
                    }

                    int minYear = sharedPreferences.getInt(KEY_RELEASE_YEAR_FILTER, 1970);
                    newMovies = newMovies.stream()
                            .filter(m -> {
                                try {
                                    int movieYear = Integer.parseInt(m.getReleaseDate().substring(0, 4));
                                    return movieYear >= minYear;
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());

                    String sortOption = sharedPreferences.getString(KEY_SORT_OPTION, Constants.SORT_RELEASE_DATE);
                    if (Constants.SORT_RATING.equals(sortOption)) {
                        newMovies.sort((m1, m2) -> Float.compare(Float.parseFloat(m2.getRating()), Float.parseFloat(m1.getRating())));
                    } else {
                        newMovies.sort((m1, m2) -> m2.getReleaseDate().compareTo(m1.getReleaseDate()));
                    }

                    if (page == 1) {
                        movieAdapter.updateMovies(newMovies);
                    } else {
                        movieAdapter.addMovies(newMovies, page);
                    }
                    totalPages = response.body().getTotalPages();
                } else {
                    Toast.makeText(requireContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                movieAdapter.removeLoadingFooter();
                isLoading = false;
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateItemStarFav(int movieId) {
        movieAdapter.updateItemFavStar(movieId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs != null && prefListener != null) {
            prefs.registerOnSharedPreferenceChangeListener(prefListener);
        }
    }
}
