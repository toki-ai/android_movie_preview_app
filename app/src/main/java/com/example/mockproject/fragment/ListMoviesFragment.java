package com.example.mockproject.fragment;

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

import com.example.mockproject.OnUpdateMovieListListener;
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
        fetchMovies(currentPage, currentFetchType);
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
        return view;
    }
    private void loadNextPage() {
        currentPage++;
        fetchMovies(currentPage, currentFetchType);
    }

    private void updateLayoutManager() {
        recyclerView.setLayoutManager(isGrid ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext()));
    }

    private void fetchMovies(int page, String fetchType) {
        isLoading = true;
        movieAdapter.addLoadingFooter();
        Call<MovieResponse> call;
        switch(fetchType) {
            case TYPE_POPULAR:
                call = movieApiService.getPopularMovies(API_KEY, page);
                break;
            case TYPE_UPCOMING:
                call = movieApiService.getUpcomingMovies(API_KEY, page);
                break;
            case TYPE_NOW_PLAYING:
                call = movieApiService.getNowPlayingMovies(API_KEY, page);
                break;
            case TYPE_TOP_RATED:
                call = movieApiService.getTopRatedMovies(API_KEY, page);
                break;
            default:
                call = null;
        }
        if (call == null) {
            return;
        }
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                movieAdapter.removeLoadingFooter();
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    List<Movie> newMovies = movieResponse.getMovies();
                    if (page == 1) {
                        movieAdapter.updateMovies(newMovies);
                    } else {
                        movieAdapter.addMovies(newMovies, page);
                    }
                    totalPages = movieResponse.getTotalPages();
                } else {
                    Toast.makeText(requireContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call,
                                  @NonNull Throwable t) {
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
        fetchMovies(currentPage, currentFetchType);
    }

    @Override
    public void onUpdateItemStarFav(int movieId) {
        movieAdapter.updateItemFavStar(movieId);
    }
}
