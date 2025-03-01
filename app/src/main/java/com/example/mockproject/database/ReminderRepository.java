package com.example.mockproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mockproject.entities.Movie;
import com.example.mockproject.entities.Reminder;

import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {
    private final DatabaseHelper databaseHelper;

    public ReminderRepository(Context context){
        this.databaseHelper = new DatabaseHelper(context);
    }

    public boolean setOrUpdateReminder(int userId, int movieId, String time) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String query = "SELECT " + DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID +
                " FROM " + DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME +
                " WHERE user_id = ? AND movie_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(movieId)});
        boolean success;
        if (cursor.moveToFirst()) {
            int reminderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID));
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME, time);
            int rowsUpdated = db.update(DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME,
                    values,
                    DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID + " = ?",
                    new String[]{String.valueOf(reminderId)});
            success = rowsUpdated > 0;
        } else {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME, time);
            values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_USER_ID, userId);
            values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_ID, movieId);
            long id = db.insert(DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME, null, values);
            success = id > 0;
        }
        cursor.close();
        db.close();
        return success;
    }

    public int deleteReminder(int reminderId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME,
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID + " = ?",
                new String[]{String.valueOf(reminderId)});
        db.close();
        return rowsDeleted;
    }

    public List<Reminder> getRemindersByUser(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT * " +
                " FROM " + DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME + " r " +
                " JOIN " + DatabaseHelper.FavMovieEntry.MOVIE_TABLE_NAME + " m " +
                " ON r.movie_id = m." + DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID +
                " WHERE r.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int reminderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME));

                int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_TITLE));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RATING));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_IMAGE));
                boolean isAdult = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_ADULT)) == 1;
                String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_RELEASE));
                String overview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FavMovieEntry.MOVIE_COLUMN_OVERVIEW));

                Movie movie = new Movie(movieId, title, rating, image, isAdult, releaseDate, overview);

                reminders.add(new Reminder(reminderId, time, movie));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminders;
    }
}
