package com.example.mockproject.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.R;
import com.example.mockproject.api.ApiClient;
import com.example.mockproject.api.MovieApiService;
import com.example.mockproject.entities.CreditResponse;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.fragment.adapter.CastAdapter;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE_ID = "arg_movie_id";
    private static final String ARG_MOVIE_TITLE = "arg_movie_title";
    private static final String API_KEY = "e7631ffcb8e766993e5ec0c1f4245f93";

    private int movieId;
    private String movieTitle;
    private ImageView detailPoster, detailBtnFav;
    private TextView detailReleaseDate, detailRating, detailOverview;
    private TextView detailBtnReminder;
    private RecyclerView detailCrewList;

    private MovieApiService movieApiService;

    public static MovieDetailFragment newInstance(int movieId, String movieTitle) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        args.putString(ARG_MOVIE_TITLE, movieTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID);
            movieTitle = getArguments().getString(ARG_MOVIE_TITLE);
        }
        movieApiService = ApiClient.getClient().create(MovieApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailPoster = view.findViewById(R.id.detail_poster);
        detailBtnReminder = view.findViewById(R.id.detail_btn_reminder);
        detailBtnFav = view.findViewById(R.id.detail_btn_fav);
        detailReleaseDate = view.findViewById(R.id.detail_releaseDate);
        detailRating = view.findViewById(R.id.detail_rating);
        detailOverview = view.findViewById(R.id.detail_overview);
        detailCrewList = view.findViewById(R.id.detail_crew_list);

        detailCrewList.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        fetchMovieDetails(movieId);
        fetchCrewAndCast(movieId);

        detailBtnReminder.setOnClickListener(v -> {
            Toast.makeText(getContext(),
                    "Reminder set for " + movieTitle,
                    Toast.LENGTH_SHORT
            ).show();
        });

        detailBtnFav.setOnClickListener(v -> {
            detailBtnFav.setImageResource(R.drawable.icon_movie_star);
        });
    }

    private void fetchMovieDetails(int movieId) {
        movieApiService.getMovieDetail(movieId, API_KEY).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call,
                                   @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    bindMovieData(movie);
                } else {
                    Toast.makeText(getContext(), "Failed to load detail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMovieData(Movie movie) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            Picasso.get().load(movie.getPosterUrl()).into(detailPoster);
        }
        detailReleaseDate.setText(!TextUtils.isEmpty(movie.getReleaseDate())
                ? movie.getReleaseDate()
                : "N/A");
        detailRating.setText(String.valueOf(movie.getRating()) + "/10.0");
        detailOverview.setText(!TextUtils.isEmpty(movie.getOverview())
                ? movie.getOverview()
                : "No overview available");
    }

    private void fetchCrewAndCast(int movieId) {
        movieApiService.getMovieCrewAndCast(movieId, API_KEY).enqueue(new Callback<CreditResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreditResponse> call,
                                   @NonNull Response<CreditResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CreditResponse creditResponse = response.body();

                    if (creditResponse.getCast() != null && !creditResponse.getCast().isEmpty()) {
                        CastAdapter castAdapter = new CastAdapter(creditResponse.getCast());
                        detailCrewList.setAdapter(castAdapter);
                    }
                } else {
                    Toast.makeText(getContext(), "No Cast/Crew data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreditResponse> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error loading credits", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
