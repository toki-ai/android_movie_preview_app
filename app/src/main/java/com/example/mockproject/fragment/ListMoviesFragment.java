package com.example.mockproject.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.example.mockproject.MainActivity;
import com.example.mockproject.OnUpdateMovieListListener;
import com.example.mockproject.R;
import com.example.mockproject.api.ApiClient;
import com.example.mockproject.api.MovieApiService;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.MovieResponse;
import com.example.mockproject.fragment.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMoviesFragment extends Fragment implements OnUpdateMovieListListener {
    private static final String API_KEY = "e7631ffcb8e766993e5ec0c1f4245f93";
    public static final String TYPE_POPULAR = "POPULAR";
    public static final String TYPE_UPCOMING = "UPCOMING";
    public static final String TYPE_TOP_RATED = "TOP_RATED";
    public static final String TYPE_NOW_PLAYING = "NOW_PLAYING";
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private boolean isGrid = false;
    private MovieApiService movieApiService;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentFetchType = TYPE_POPULAR;
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
        fetchMovies(currentPage); //currentFetchType
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
        prefs = requireContext().getSharedPreferences(MainActivity.SHARE_KEY, Context.MODE_PRIVATE);

        prefListener = (sharedPreferences, key) -> {
            if (key.equals(SettingsFragment.KEY_MOVIE_TYPE) || key.equals(SettingsFragment.KEY_RATING_FILTER) || key.equals(SettingsFragment.KEY_RELEASE_YEAR_FILTER) || key.equals(SettingsFragment.KEY_SORT_OPTION)) {
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
        fetchMovies(currentPage); //currentFetchType
    }

    private void updateLayoutManager() {
        recyclerView.setLayoutManager(isGrid ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext()));
    }

    private void fetchMovies(int page) {
        isLoading = true;
        movieAdapter.addLoadingFooter();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(MainActivity.SHARE_KEY, Context.MODE_PRIVATE);

        String movieType = sharedPreferences.getString(SettingsFragment.KEY_MOVIE_TYPE, "Popular Movies");
        Call<MovieResponse> call;
        switch (movieType) {
            case "Top Rated Movies":
                call = movieApiService.getTopRatedMovies(API_KEY, page);
                break;
            case "Upcoming Movies":
                call = movieApiService.getUpcomingMovies(API_KEY, page);
                break;
            case "Now Playing Movies":
                call = movieApiService.getNowPlayingMovies(API_KEY, page);
                break;
            case "Popular Movies":
            default:
                Log.d("TAGTAG", "HIIII");
                call = movieApiService.getPopularMovies(API_KEY, page);
                break;
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                movieAdapter.removeLoadingFooter();
                Log.d("TAGTAG", "BYEE");
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> newMovies = response.body().getMovies();

                    float minRating = sharedPreferences.getFloat(SettingsFragment.KEY_RATING_FILTER, 0f);
                    if (minRating > 0f) {
                        newMovies = newMovies.stream()
                                .filter(m -> Float.parseFloat(m.getRating()) >= minRating)
                                .collect(Collectors.toList());
                    }

                    int minYear = sharedPreferences.getInt(SettingsFragment.KEY_RELEASE_YEAR_FILTER, 1970);
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

                    String sortOption = sharedPreferences.getString(SettingsFragment.KEY_SORT_OPTION, "Release Date Descending");
                    if ("Rating Descending".equals(sortOption)) {
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

    @Override
    public void onToolbarIconClick() {
        isGrid = !isGrid;
        updateLayoutManager();
        movieAdapter = new MovieAdapter(movieAdapter.getMovies(), isGrid, getContext(), MovieAdapter.TYPE.LIST);
        recyclerView.setAdapter(movieAdapter);
    }

    @Override
    public void onToolbarOpsClick(String type) {
        currentFetchType = type;
        currentPage = 1;
        movieAdapter.updateMovies(new ArrayList<>());
        fetchMovies(currentPage); //currentFetchType
    }

    @Override
    public void onUpdateItemStarFav(int movieId) {
        movieAdapter.updateItemFavStar(movieId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
    }
}
