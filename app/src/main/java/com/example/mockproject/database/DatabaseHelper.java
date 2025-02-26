package com.example.mockproject.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "MockProject.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static class FavMovieEntry implements BaseColumns {
        public static final String MOVIE_TABLE_NAME = "favMovies";
        public static final String MOVIE_COLUMN_ID = "id";
        public static final String MOVIE_COLUMN_TITLE = "title";
        public static final String MOVIE_COLUMN_RATING = "rating";
        public static final String MOVIE_COLUMN_ADULT = "isAdult";
        public static final String MOVIE_COLUMN_IMAGE = "image";
        public static final String MOVIE_COLUMN_OVERVIEW = "overview";
        public static final String MOVIE_COLUMN_RELEASE = "releaseDate";
    }

    public static class UserEntry implements BaseColumns {
        public static final String USER_TABLE_NAME = "users";
        public static final String USER_COLUMN_ID = "id";
        public static final String USER_COLUMN_NAME = "name";
        public static final String USER_COLUMN_IMAGE = "image";
        public static final String USER_COLUMN_BIRTHDAY = "birthday";
        public static final String USER_COLUMN_EMAIL = "email";
        public static final String USER_COLUMN_GENDER = "gender";
    }

    public static class FavMoviesUsersEntry implements BaseColumns {
        public static final String TABLE_NAME = "favMoviesUsers";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_MOVIE_ID = "movieId";
    }

    public static class ReminderEntry implements BaseColumns {
        public static final String REMINDER_TABLE_NAME = "reminders";
        public static final String REMINDER_COLUMN_ID = "id";
        public static final String REMINDER_COLUMN_TIME = "time";
        public static final String REMINDER_COLUMN_USER_ID = "user_id";
        public static final String REMINDER_COLUMN_MOVIE_ID = "movie_id";
    }

    private static final String QUERY_CREATE_MOVIE_TABLE =
            "CREATE TABLE " + FavMovieEntry.MOVIE_TABLE_NAME + " ("
                    + FavMovieEntry.MOVIE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + FavMovieEntry.MOVIE_COLUMN_TITLE + " TEXT NOT NULL, "
                    + FavMovieEntry.MOVIE_COLUMN_RATING + " REAL NOT NULL, "
                    + FavMovieEntry.MOVIE_COLUMN_IMAGE + " TEXT NOT NULL, "
                    + FavMovieEntry.MOVIE_COLUMN_ADULT + " INTEGER NOT NULL, "
                    + FavMovieEntry.MOVIE_COLUMN_RELEASE + " TEXT NOT NULL, "
                    + FavMovieEntry.MOVIE_COLUMN_OVERVIEW + " TEXT NOT NULL)";

    private static final String QUERY_DROP_MOVIE_TABLE =
            "DROP TABLE IF EXISTS " + FavMovieEntry.MOVIE_TABLE_NAME;

    private static final String QUERY_CREATE_USER_TABLE =
            "CREATE TABLE " + UserEntry.USER_TABLE_NAME + " ("
                    + UserEntry.USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + UserEntry.USER_COLUMN_NAME + " TEXT NOT NULL, "
                    + UserEntry.USER_COLUMN_BIRTHDAY + " TEXT, "
                    + UserEntry.USER_COLUMN_EMAIL + " TEXT NOT NULL, "
                    + UserEntry.USER_COLUMN_IMAGE + " TEXT, "
                    + UserEntry.USER_COLUMN_GENDER + " INTEGER)";

    private static final String QUERY_DROP_USER_TABLE =
            "DROP TABLE IF EXISTS " + UserEntry.USER_TABLE_NAME;

    private static final String QUERY_CREATE_MOVIE_USER_TABLE =
            "CREATE TABLE " + FavMoviesUsersEntry.TABLE_NAME + " ("
                    + FavMoviesUsersEntry.COLUMN_USER_ID + " INTEGER NOT NULL, "
                    + FavMoviesUsersEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + FavMoviesUsersEntry.COLUMN_USER_ID + ") REFERENCES "
                    + UserEntry.USER_TABLE_NAME + "(" + UserEntry.USER_COLUMN_ID + "), "
                    + "FOREIGN KEY(" + FavMoviesUsersEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                    + FavMovieEntry.MOVIE_TABLE_NAME + "(" + FavMovieEntry.MOVIE_COLUMN_ID + "), "
                    + "PRIMARY KEY (" + FavMoviesUsersEntry.COLUMN_USER_ID + ", "
                    + FavMoviesUsersEntry.COLUMN_MOVIE_ID + "))";

    private static final String QUERY_DROP_MOVIE_USER_TABLE =
            "DROP TABLE IF EXISTS " + FavMoviesUsersEntry.TABLE_NAME;

    private static final String QUERY_CREATE_REMINDER_TABLE =
            "CREATE TABLE " + ReminderEntry.REMINDER_TABLE_NAME + " ("
                    + ReminderEntry.REMINDER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ReminderEntry.REMINDER_COLUMN_TIME + " TEXT NOT NULL, "
                    + ReminderEntry.REMINDER_COLUMN_USER_ID + " INTEGER NOT NULL, "
                    + ReminderEntry.REMINDER_COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + ReminderEntry.REMINDER_COLUMN_USER_ID + ") REFERENCES "
                    + UserEntry.USER_TABLE_NAME + "(" + UserEntry.USER_COLUMN_ID + "), "
                    + "FOREIGN KEY(" + ReminderEntry.REMINDER_COLUMN_MOVIE_ID + ") REFERENCES "
                    + FavMovieEntry.MOVIE_TABLE_NAME + "(" + FavMovieEntry.MOVIE_COLUMN_ID + "))";

    private static final String QUERY_DROP_REMINDER_TABLE =
            "DROP TABLE IF EXISTS " + ReminderEntry.REMINDER_TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_MOVIE_TABLE);
        db.execSQL(QUERY_CREATE_USER_TABLE);
        db.execSQL(QUERY_CREATE_MOVIE_USER_TABLE);
        db.execSQL(QUERY_CREATE_REMINDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(QUERY_DROP_MOVIE_TABLE);
        db.execSQL(QUERY_DROP_USER_TABLE);
        db.execSQL(QUERY_DROP_MOVIE_USER_TABLE);
        db.execSQL(QUERY_DROP_REMINDER_TABLE);
        onCreate(db);
    }
}
