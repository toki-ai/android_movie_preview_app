package com.example.mockproject.adapter;

import static com.example.mockproject.utils.Constants.SHARE_KEY;
import static com.example.mockproject.utils.Constants.USER_ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;
import com.example.mockproject.callback.OnLoginRequestListener;
import com.example.mockproject.database.MovieRepository;
import com.example.mockproject.entities.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Optional;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Movie> movies;
    private final boolean isGrid;
    private final MovieRepository movieRepository;
    private final SharedPreferences sharedPreferences;
    private boolean isLoadingAdded = false;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final int PAGE_SIZE = 20;
    private final TYPE type;
    private final Context context;

    public enum TYPE {
        LIST, FAV
    }

    public MovieAdapter(List<Movie> movies, boolean isGrid, Context context, TYPE type) {
        this.movies = movies;
        this.isGrid = isGrid;
        this.movieRepository = new MovieRepository(context);
        this.sharedPreferences = context.getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        this.context = context;
        this.type = type;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public void updateItemFavStar(int movieId) {
        Optional<Movie> movieOptional = movies.stream()
                .filter(movie -> movie.getId() == movieId)
                .findFirst();
        movieOptional.ifPresent(movie -> notifyItemChanged(movies.indexOf(movie)));
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movies.size() && isLoadingAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            int layoutId = isGrid ? R.layout.item_movie_grid : R.layout.item_movie_list;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new MovieViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            MovieViewHolder movieHolder = (MovieViewHolder) holder;
            Movie movie = movies.get(position);

            movieHolder.title.setText(movie.getTitle());
            Picasso.get().load(movie.getPosterUrl()).into(movieHolder.image);

            String userId = sharedPreferences.getString(USER_ID, "");

            if (isGrid) {
                movieHolder.rating.setVisibility(View.GONE);
                movieHolder.overview.setVisibility(View.GONE);
                movieHolder.releaseDate.setVisibility(View.GONE);
                movieHolder.ignoreChildIcon.setVisibility(View.GONE);
                //movieHolder.btnMovieFavorite.setVisibility(View.GONE);
            } else {
                movieHolder.rating.setVisibility(View.VISIBLE);
                movieHolder.overview.setVisibility(View.VISIBLE);
                movieHolder.releaseDate.setVisibility(View.VISIBLE);
                movieHolder.btnMovieFavorite.setVisibility(View.VISIBLE);

                movieHolder.ignoreChildIcon.setVisibility(movie.getIsAdultMovie() ? View.VISIBLE : View.GONE);
                movieHolder.rating.setText(movie.getRating() + "/10.0");
                movieHolder.overview.setText(movie.getOverview());
                movieHolder.releaseDate.setText(movie.getReleaseDate());

                if (userId.isEmpty()) {
                    movie.setFav(false);
                    movieHolder.btnMovieFavorite.setImageResource(
                            R.drawable.icon_movie_start_outline
                    );
                } else {
                    boolean isFav = movieRepository.isMovieAdded(Integer.parseInt(userId), movie.getId());
                    movie.setFav(isFav);
                    movieHolder.btnMovieFavorite.setImageResource(
                            isFav ? R.drawable.icon_movie_star
                                    : R.drawable.icon_movie_start_outline
                    );
                }

                movieHolder.btnMovieFavorite.setOnClickListener(v -> {
                    if (userId.isEmpty()) {
                        Toast.makeText(context, "You need to login to add favorites", Toast.LENGTH_SHORT).show();
                        if (context instanceof OnLoginRequestListener) {
                            ((OnLoginRequestListener) context).onLoginRequested();
                        }
                        return;
                    }
                    movie.setFav(!movie.isFav());

                    movieHolder.btnMovieFavorite.setImageResource(
                            movie.isFav() ? R.drawable.icon_movie_star : R.drawable.icon_movie_start_outline
                    );
                    notifyItemChanged(position);

                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateFavoriteListDirectly(movie, type);
                    }
                });

            }
            movieHolder.itemView.setOnClickListener(v -> {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openDetailFragment(movie.getId(), movie.getTitle());
                }
            });
        } else {
            LoadingViewHolder loadingHolder = (LoadingViewHolder) holder;
            loadingHolder.progressBar.setIndeterminate(true);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addMovies(List<Movie> newMovies, int pageNumber) {
        if (pageNumber == 1) {
            movies.clear();
        }
        for (int i = 0; i < newMovies.size(); i++) {
            Movie newMovie = newMovies.get(i);
            newMovie.setStableId((long) (pageNumber - 1) *PAGE_SIZE + i );

            if (!movies.contains(newMovie)) {
                movies.add(newMovie);
            }
        }
        notifyDataSetChanged();
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        notifyItemInserted(movies.size());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
        notifyItemRemoved(movies.size());
    }

    public List<Movie> getMovies() {
        return movies;
    }

    @Override
    public int getItemCount() {
        return isLoadingAdded ? movies.size() + 1 : movies.size();
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

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
