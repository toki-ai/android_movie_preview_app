package com.example.mockproject.fragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.R;
import com.example.mockproject.entities.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private boolean isGrid;
    public List<Movie> getMovies() {
        return movies;
    }

    public MovieAdapter(List<Movie> movies, boolean isGrid) {
        this.movies = movies;
        this.isGrid = isGrid;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGrid ? R.layout.item_movie_grid : R.layout.item_movie_list;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.title.setText(movie.getTitle());
        Picasso.get().load(movie.getPosterUrl()).into(holder.image);

        if (isGrid) {
            holder.rating.setVisibility(View.GONE);
            holder.overview.setVisibility(View.GONE);
            holder.releaseDate.setVisibility(View.GONE);
            holder.ignoreChildIcon.setVisibility(View.GONE);
        } else {
            holder.rating.setVisibility(View.VISIBLE);
            holder.overview.setVisibility(View.VISIBLE);
            holder.releaseDate.setVisibility(View.VISIBLE);
            holder.ignoreChildIcon.setVisibility(movie.getIsAdultMoview() ? View.VISIBLE : View.GONE);
            holder.rating.setText(movie.getRating() +"/10.0");
            holder.overview.setText(movie.getOverview());
            holder.releaseDate.setText(movie.getReleaseDate());
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, overview, releaseDate;
        ImageView image, ignoreChildIcon;

        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            image = itemView.findViewById(R.id.movieImage);
            rating = itemView.findViewById(R.id.movieRating);
            overview = itemView.findViewById(R.id.movieOverview);
            releaseDate = itemView.findViewById(R.id.movieReleaseDate);
            ignoreChildIcon = itemView.findViewById(R.id.movieIgnoreChild);
        }
    }
}
