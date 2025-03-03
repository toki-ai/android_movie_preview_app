package com.example.mockproject.fragment;

import static com.example.mockproject.Constants.SHARE_KEY;
import static com.example.mockproject.Constants.USER_ID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mockproject.callback.OnLoginRequestListener;
import com.example.mockproject.callback.OnUpdateFavoriteListListener;
import com.example.mockproject.R;
import com.example.mockproject.database.MovieRepository;
import com.example.mockproject.entities.Movie;
import com.example.mockproject.fragment.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements OnUpdateFavoriteListListener {
    private OnLoginRequestListener loginRequestListener;
    private MovieRepository movieRepository;
    private SharedPreferences sharedPreferences;
    private MovieAdapter favMovieAdapter;
    private ScrollView scrollView;
    private TextView btnLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context instanceof OnLoginRequestListener) {
            loginRequestListener = (OnLoginRequestListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        movieRepository = new MovieRepository(getContext());
        sharedPreferences = requireContext().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(USER_ID, "");

        RecyclerView recyclerView = view.findViewById(R.id.fav_recyclerView);
        scrollView = view.findViewById(R.id.fav_scroll_view);
        btnLogin = view.findViewById(R.id.fav_btn_login);

        favMovieAdapter = new MovieAdapter(new ArrayList<>(), false, getContext(), MovieAdapter.TYPE.FAV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(favMovieAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        if(userId.isEmpty()){
            onUpdateFavoriteUILogin();
        }else{
            onUpdateFavoriteList();
        }
        return  view;
    }

    public void setLoginMode(boolean isLogin){
        btnLogin.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(isLogin ? View.GONE : View.VISIBLE);
    }

    public void showSearchResults(List<Movie> searchResults) {
        favMovieAdapter.updateMovies(searchResults);
    }

    @Override
    public void onUpdateFavoriteList() {
        setLoginMode(false);
        String userId = sharedPreferences.getString(USER_ID, "");
        if (userId.isEmpty()) {
            return;
        }
        List<Movie> favMovies = movieRepository.getFavMoviesByUserId(Integer.parseInt(userId));
        favMovieAdapter.updateMovies(favMovies);
    }

    @Override
    public void onUpdateFavoriteUILogin() {
        setLoginMode(true);
        btnLogin.setOnClickListener(v -> {
            if (loginRequestListener != null) {
                loginRequestListener.onLoginRequested();
            }
        });
    }
}