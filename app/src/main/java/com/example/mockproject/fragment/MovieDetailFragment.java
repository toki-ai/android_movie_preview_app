package com.example.mockproject.fragment;

import static com.example.mockproject.BuildConfig.API_KEY;
import static com.example.mockproject.Constants.SHARE_KEY;
import static com.example.mockproject.Constants.USER_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;
import com.example.mockproject.api.ApiClient;
import com.example.mockproject.api.MovieApiService;
import com.example.mockproject.database.MovieRepository;
import com.example.mockproject.database.ReminderRepository;
import com.example.mockproject.entities.CreditResponse;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.fragment.adapter.CastAdapter;
import com.example.mockproject.fragment.adapter.MovieAdapter;
import com.example.mockproject.receiver.ReminderReceiver;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_MOVIE_ID = "arg_movie_id";
    private static final String ARG_MOVIE_TITLE = "arg_movie_title";

    private int movieId;
    private String movieTitle, movieImage, movieYear;
    private float movieRating;
    private ImageView detailPoster, detailBtnFav, detailAdultIcon;
    private TextView detailReleaseDate, detailRating, detailOverview;
    private androidx.recyclerview.widget.RecyclerView detailCrewList;

    private MovieApiService movieApiService;
    private Calendar selectedTime;
    private Movie currentMovie;
    private int userId;
    private MovieRepository movieRepository;

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
        TextView detailBtnReminder = view.findViewById(R.id.detail_btn_reminder);
        detailBtnFav = view.findViewById(R.id.detail_btn_fav);
        detailReleaseDate = view.findViewById(R.id.detail_releaseDate);
        detailRating = view.findViewById(R.id.detail_rating);
        detailOverview = view.findViewById(R.id.detail_overview);
        detailCrewList = view.findViewById(R.id.detail_crew_list);
        detailAdultIcon = view.findViewById(R.id.detail_adult_icon);

        detailCrewList.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString(USER_ID, "0");
        userId = Integer.parseInt(userIdStr);
        movieRepository = new MovieRepository(getContext());

        fetchMovieDetails(movieId);
        fetchCrewAndCast(movieId);

        detailBtnReminder.setOnClickListener(v -> requestNotificationPermissionIfNeeded());

        detailBtnFav.setOnClickListener(v -> {
            if (currentMovie != null) {
                currentMovie.setFav(!currentMovie.isFav());
                movieRepository.handleClickFavMovie(currentMovie);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateFavoriteListDirectly(currentMovie, null);
                }
                detailBtnFav.setImageResource(
                        currentMovie.isFav() ? R.drawable.icon_movie_star : R.drawable.icon_movie_start_outline
                );
            }
        });
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setAlarmAndShowNotification();
                } else {
                    Toast.makeText(requireContext(),
                            "Notification permission is required for reminders",
                            Toast.LENGTH_SHORT).show();
                }
            });

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            setAlarmAndShowNotification();
        }
    }

    private void setAlarmAndShowNotification() {

        if (userId == 0) {
            Toast.makeText(getContext(), "Need permission to set reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderRepository reminderRepository = new ReminderRepository(getContext());
        List<Reminder> reminders = reminderRepository.getRemindersByUser(userId);
        if (reminders.size() >= 10) {
            Toast.makeText(getContext(), "Only storage maximum 10 reminders", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                selectedTime = Calendar.getInstance();
                                selectedTime.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                long timeInMillis = selectedTime.getTimeInMillis();

                                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                    Toast.makeText(getContext(), "Please turn on exact alarms on Settings", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                                    startActivity(intent);
                                    return;
                                }
                                boolean reminderSuccess = reminderRepository.setOrUpdateReminder(
                                        userId,
                                        movieId,
                                        String.valueOf(timeInMillis),
                                        movieTitle,
                                        movieImage,
                                        movieRating,
                                        movieYear
                                );
                                if (reminderSuccess) {
                                    Intent intent = new Intent(getContext(), ReminderReceiver.class);
                                    intent.putExtra("MOVIE_TITLE", movieTitle);
                                    intent.putExtra("reminder_time", timeInMillis);
                                    intent.putExtra("MOVIE_RATING", detailRating.getText().toString());
                                    String releaseDate = detailReleaseDate.getText().toString();
                                    intent.putExtra("MOVIE_YEAR", releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "N/A");
                                    intent.putExtra("USER_ID", userId);

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                            getContext(),
                                            movieId,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                    );
                                    if (alarmManager != null) {
                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                                    }
                                    Toast.makeText(getContext(), "Reminder set!", Toast.LENGTH_SHORT).show();

                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).reloadDrawerReminders();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Fail to set reminder", Toast.LENGTH_SHORT).show();
                                }
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
    private void fetchMovieDetails(int movieId) {
        movieApiService.getMovieDetail(movieId, API_KEY).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    currentMovie = movie;
                    movieImage = movie.getPosterUrl();
                    movieRating = Float.parseFloat(movie.getRating());
                    movieYear = movie.getReleaseDate();
                    bindMovieData(movie);
                } else {
                    Toast.makeText(getContext(), "Failed to load detail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void bindMovieData(Movie movie) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            Picasso.get().load(movie.getPosterUrl()).into(detailPoster);
        }
        detailReleaseDate.setText(!TextUtils.isEmpty(movie.getReleaseDate())
                ? movie.getReleaseDate()
                : "N/A");
        detailRating.setText(movie.getRating() + "/10.0");
        detailOverview.setText(!TextUtils.isEmpty(movie.getOverview())
                ? movie.getOverview()
                : "No overview available");

        if (movie.getIsAdultMovie()) {
            detailAdultIcon.setVisibility(View.VISIBLE);
            detailAdultIcon.setImageResource(R.drawable.icon_18);
        } else {
            detailAdultIcon.setVisibility(View.GONE);
        }

        boolean isAdded = movieRepository.isMovieAdded(userId, movieId);
        currentMovie.setFav(isAdded);
        detailBtnFav.setImageResource(isAdded ? R.drawable.icon_movie_star: R.drawable.icon_movie_start_outline);
    }

    private void fetchCrewAndCast(int movieId) {
        movieApiService.getMovieCrewAndCast(movieId, API_KEY).enqueue(new Callback<>() {
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
