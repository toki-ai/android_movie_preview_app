package com.example.mockproject.fragment;

import static com.example.mockproject.Constants.KEY_MOVIE_TYPE;
import static com.example.mockproject.Constants.KEY_RATING_FILTER;
import static com.example.mockproject.Constants.KEY_RELEASE_YEAR_FILTER;
import static com.example.mockproject.Constants.KEY_SORT_OPTION;
import static com.example.mockproject.Constants.SHARE_KEY;
import static com.example.mockproject.Constants.SORT_RATING;
import static com.example.mockproject.Constants.SORT_RELEASE_DATE;
import static com.example.mockproject.Constants.TYPE_NOW_PLAYING;
import static com.example.mockproject.Constants.TYPE_POPULAR;
import static com.example.mockproject.Constants.TYPE_TOP_RATED;
import static com.example.mockproject.Constants.TYPE_UPCOMING;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;

public class SettingsFragment extends Fragment {

    private RadioGroup radioGroupFilter, radioGroupSort;
    private SeekBar seekBarRating, seekBarYear;
    private TextView tvRatingValue, tvYearValue;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);

        radioGroupFilter = view.findViewById(R.id.detail_radio_filter_type);
        RadioButton radioPopular = view.findViewById(R.id.radio_popular);
        RadioButton radioTopRated = view.findViewById(R.id.radio_top_rated);
        RadioButton radioUpcoming = view.findViewById(R.id.radio_upcoming);
        RadioButton radioNowPlaying = view.findViewById(R.id.radio_now_playing);

        seekBarRating = view.findViewById(R.id.detail_rating_seekbar);
        tvRatingValue = view.findViewById(R.id.detail_rating_value);

        seekBarYear = view.findViewById(R.id.detail_release_seekbar);
        tvYearValue = view.findViewById(R.id.detail_release_year_value);

        radioGroupSort = view.findViewById(R.id.detail_sort_radio);
        RadioButton radioSortRelease = view.findViewById(R.id.radio_sort_release_date);
        RadioButton radioSortRating = view.findViewById(R.id.radio_sort_rating);

        String savedType = sharedPreferences.getString(KEY_MOVIE_TYPE, TYPE_POPULAR);
        switch (savedType) {
            case TYPE_TOP_RATED:
                radioTopRated.setChecked(true);
                break;
            case TYPE_UPCOMING:
                radioUpcoming.setChecked(true);
                break;
            case TYPE_NOW_PLAYING:
                radioNowPlaying.setChecked(true);
                break;
            case TYPE_POPULAR:
            default:
                radioPopular.setChecked(true);
                break;
        }

        float savedRating = sharedPreferences.getFloat(KEY_RATING_FILTER, 0f);
        int savedRatingProgress = (int) (savedRating * 10);
        seekBarRating.setProgress(savedRatingProgress);
        tvRatingValue.setText(String.valueOf(savedRating));

        int savedYear = sharedPreferences.getInt(KEY_RELEASE_YEAR_FILTER, 1970);
        int progressYear = (savedYear < 1970) ? 0 : savedYear - 1970;
        seekBarYear.setProgress(progressYear);
        tvYearValue.setText(String.valueOf(savedYear));

        String savedSort = sharedPreferences.getString(KEY_SORT_OPTION, SORT_RELEASE_DATE);
        if (SORT_RATING.equals(savedSort)) {
            radioSortRating.setChecked(true);
        } else {
            radioSortRelease.setChecked(true);
        }

        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            String type = TYPE_POPULAR;
            if (checkedId == R.id.radio_top_rated) {
                type = TYPE_TOP_RATED;
            } else if (checkedId == R.id.radio_upcoming) {
                type = TYPE_UPCOMING;
            } else if (checkedId == R.id.radio_now_playing) {
                type = TYPE_NOW_PLAYING;
            }
            sharedPreferences.edit().putString(KEY_MOVIE_TYPE, type).apply();
        });

        seekBarRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rating = progress / 10f;
                tvRatingValue.setText(String.valueOf(rating));
                sharedPreferences.edit().putFloat(KEY_RATING_FILTER, rating).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarYear.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actualYear = 1970 + progress;
                tvYearValue.setText(String.valueOf(actualYear));
                sharedPreferences.edit().putInt(KEY_RELEASE_YEAR_FILTER, actualYear).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            String sort = SORT_RELEASE_DATE;
            if (checkedId == R.id.radio_sort_rating) {
                sort = SORT_RATING;
            }
            sharedPreferences.edit().putString(KEY_SORT_OPTION, sort).apply();
        });

        return view;
    }
}
