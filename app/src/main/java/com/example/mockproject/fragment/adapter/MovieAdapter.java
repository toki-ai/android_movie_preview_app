package com.example.mockproject.fragment.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.OnUpdateStarFavoriteListener;
import com.example.mockproject.R;
import com.example.mockproject.database.MovieRepository;
import com.example.mockproject.entities.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Optional;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private boolean isGrid;
    private MovieRepository movieRepository;
    private SharedPreferences sharedPreferences;
    private Context context;
    private OnUpdateStarFavoriteListener onUpdateStarFavoriteListener;
    public List<Movie> getMovies() {
        return movies;
    }
    private TYPE type;
    public enum TYPE{
        LIST, FAV
    }

    public MovieAdapter(List<Movie> movies, boolean isGrid, Context context, TYPE type ) {
        this.movies = movies;
        this.isGrid = isGrid;
        this.context = context;
        this.movieRepository = new MovieRepository(context);
        this.sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_KEY, Context.MODE_PRIVATE);
        this.onUpdateStarFavoriteListener = (OnUpdateStarFavoriteListener) context;
        this.type = type;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public void updateItemFavStar(int movieId) {
        Optional<Movie> movieOptional = movies.stream()
                .filter(movie -> movie.getId() == movieId)
                .findFirst();

        movieOptional.ifPresent(movie -> {
            notifyItemChanged(movies.indexOf(movie));
        });
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
        String userId = sharedPreferences.getString(MainActivity.USER_ID, "");
        if (isGrid) {
            holder.rating.setVisibility(View.GONE);
            holder.overview.setVisibility(View.GONE);
            holder.releaseDate.setVisibility(View.GONE);
            holder.ignoreChildIcon.setVisibility(View.GONE);
        } else {
            holder.rating.setVisibility(View.VISIBLE);
            holder.overview.setVisibility(View.VISIBLE);
            holder.releaseDate.setVisibility(View.VISIBLE);
            holder.ignoreChildIcon.setVisibility(movie.getIsAdultMovie() ? View.VISIBLE : View.GONE);
            holder.rating.setText(movie.getRating() +"/10.0");
            holder.overview.setText(movie.getOverview());
            holder.releaseDate.setText(movie.getReleaseDate());
            if(!userId.isEmpty()) {
                movie.setFav(movieRepository.isMovieAdded(Integer.parseInt(userId), movie.getId()));
                holder.btnMovieFavorite.setImageResource(!movie.isFav() ? R.drawable.icon_movie_start_outline : R.drawable.icon_movie_star);
            }
            holder.btnMovieFavorite.setOnClickListener(v -> {
                if(type.equals(TYPE.LIST)) {
                    movie.setFav(!movie.isFav());
                    holder.btnMovieFavorite.setImageResource(!movie.isFav() ? R.drawable.icon_movie_start_outline : R.drawable.icon_movie_star);
                } else if (type.equals(TYPE.FAV)) {
                    movies.remove(position);
                    notifyDataSetChanged();
                }
                movieRepository.handleClickFavMovie(movie);
                onUpdateStarFavoriteListener.onUpdateStartFavorite(movie.getId(), type);
            });
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, overview, releaseDate;
        ImageView image, ignoreChildIcon, btnMovieFavorite;

        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            image = itemView.findViewById(R.id.movieImage);
            rating = itemView.findViewById(R.id.movieRating);
            overview = itemView.findViewById(R.id.movieOverview);
            releaseDate = itemView.findViewById(R.id.movieReleaseDate);
            ignoreChildIcon = itemView.findViewById(R.id.movieIgnoreChild);
            btnMovieFavorite = itemView.findViewById(R.id.btn_movieFavorite);
        }
    }
}
