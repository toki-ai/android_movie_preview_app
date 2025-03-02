package com.example.mockproject.database;

import static com.example.mockproject.Constants.SHARE_KEY;
import static com.example.mockproject.Constants.USER_ID;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mockproject.entities.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MovieRepository {
    private final DatabaseHelper dbDatabaseHelper;
    private final SharedPreferences sharedPreferences;

    public MovieRepository(Context context) {
        dbDatabaseHelper = new DatabaseHelper(context);
        sharedPreferences = context.getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
    }

    public void handleClickFavMovie(Movie movie) {
        String userId = sharedPreferences.getString(USER_ID, "");
        if (userId.isEmpty()) {
            return;
        }
        int uid = Integer.parseInt(userId);

        if (isMovieAdded(uid, movie.getId())) {
            deleteFavMovie(uid, movie.getId());
        } else {
            addNewFavMovie(uid, movie);
        }
    }

    public boolean isMovieAdded(int userId, int movieId) {
        SQLiteDatabase db = dbDatabaseHelper.getReadableDatabase();
        String query = "SELECT 1 FROM " + DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME +
                " WHERE " + DatabaseHelper.FavMoviesUsersEntry.COLUMN_USER_ID + " = ? " +
                " AND " + DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID + " = ? ";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(movieId)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public void addNewFavMovie(int userId, Movie movie) {
        SQLiteDatabase db = dbDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues movieValues = new ContentValues();
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID, movie.getId());
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE, movie.getTitle());
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RATING, movie.getRating());
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_IMAGE, movie.getPosterUrl());
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ADULT, movie.getIsAdultMovie() ? 1 : 0);
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RELEASE, movie.getReleaseDate());
            movieValues.put(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_OVERVIEW, movie.getOverview());

            db.insertWithOnConflict(DatabaseHelper.FavMovieEntry.MOVIE_TABLE_NAME,
                    null,
                    movieValues,
                    SQLiteDatabase.CONFLICT_IGNORE);

            ContentValues userMovieValues = new ContentValues();
            userMovieValues.put(DatabaseHelper.FavMoviesUsersEntry.COLUMN_USER_ID, userId);
            userMovieValues.put(DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID, movie.getId());

            db.insert(DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME, null, userMovieValues);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Movie Repository", Objects.requireNonNull(e.getMessage()));
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteFavMovie(int userId, int movieId) {
        SQLiteDatabase db = dbDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME,
                    DatabaseHelper.FavMoviesUsersEntry.COLUMN_USER_ID + " = ? AND " +
                            DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(movieId)});

            Cursor cursor = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME +
                            " WHERE " + DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID + " = ? LIMIT 1",
                    new String[]{String.valueOf(movieId)});

            if (!cursor.moveToFirst()) {
                db.delete(DatabaseHelper.FavMovieEntry.MOVIE_TABLE_NAME,
                        DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID + " = ?",
                        new String[]{String.valueOf(movieId)});
            }

            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Movie Repository", Objects.requireNonNull(e.getMessage()));
        } finally {
            db.endTransaction();
            db.close();
        }

    }
    public List<Movie> getFavMoviesByUserId(int userId) {
        SQLiteDatabase db = dbDatabaseHelper.getReadableDatabase();
        List<Movie> favMovies = new ArrayList<>();

        String query = "SELECT m.* FROM " + DatabaseHelper.FavMovieEntry.MOVIE_TABLE_NAME + " m " +
                " INNER JOIN " + DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME + " mu " +
                " ON m." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID + " = mu." + DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID +
                " WHERE mu." + DatabaseHelper.FavMoviesUsersEntry.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RATING));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_IMAGE));
                boolean isAdult = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ADULT)) == 1;
                String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RELEASE));
                String overview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_OVERVIEW));

                Movie movie = new Movie(movieId, title, rating, image, isAdult, releaseDate, overview);
                movie.setFav(true);
                favMovies.add(movie);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return favMovies;
    }

    public List<Movie> getFavMoviesByKeyword(int userId, String keyword) {
        List<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = dbDatabaseHelper.getReadableDatabase();
        String sql = "SELECT f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RATING + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ADULT + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_IMAGE + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_OVERVIEW + ", "
                + "f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RELEASE
                + " FROM " + DatabaseHelper.FavMovieEntry.MOVIE_TABLE_NAME + " f"
                + " JOIN " + DatabaseHelper.FavMoviesUsersEntry.TABLE_NAME + " fu"
                + " ON f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID + " = fu." + DatabaseHelper.FavMoviesUsersEntry.COLUMN_MOVIE_ID
                + " WHERE fu." + DatabaseHelper.FavMoviesUsersEntry.COLUMN_USER_ID + " = ?"
                + " AND f." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE + " LIKE ?";

        Cursor cursor = db.rawQuery(sql, new String[] { String.valueOf(userId), "%" + keyword + "%" });
        if (cursor.moveToFirst()) {
            do {
                int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RATING));
                int isAdult = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ADULT));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_IMAGE));
                String overview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_OVERVIEW));
                String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RELEASE));
                Movie movie = new Movie(movieId, title, rating, image, isAdult == 1, releaseDate, overview);
                movie.setFav(true);
                movieList.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return movieList;
    }

}
