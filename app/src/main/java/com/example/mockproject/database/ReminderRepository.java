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

    public ReminderRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    public boolean setOrUpdateReminder(int userId, int movieId, String time,
                                       String movieTitle, String movieImage,
                                       float movieRating, String movieYear) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String query = "SELECT " + DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID +
                " FROM " + DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME +
                " WHERE " + DatabaseHelper.ReminderEntry.REMINDER_COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(movieId)});
        boolean success;
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME, time);
        values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_TITLE, movieTitle);
        values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_IMAGE, movieImage);
        values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_RATING, movieRating);
        values.put(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_YEAR, movieYear);
        if (cursor.moveToFirst()) {
            int reminderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID));
            int rowsUpdated = db.update(DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME,
                    values,
                    DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID + " = ?",
                    new String[]{String.valueOf(reminderId)});
            success = rowsUpdated > 0;
        } else {
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
        String query = "SELECT " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID + " AS reminder_id, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME + " AS reminder_time, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_ID + " AS movie_id, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_TITLE + " AS movie_title, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_RATING + " AS movie_rating, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_IMAGE + " AS movie_image, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_YEAR + " AS movie_year " +
                "FROM " + DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME + " " +
                "WHERE " + DatabaseHelper.ReminderEntry.REMINDER_COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                int reminderId = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_id"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("reminder_time"));
                int movieId = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("movie_title"));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("movie_rating"));
                String image = cursor.getString(cursor.getColumnIndexOrThrow("movie_image"));
                String movieYear = cursor.getString(cursor.getColumnIndexOrThrow("movie_year"));
                Movie movie = new Movie(movieId, title, rating, image, false, movieYear, "");
                reminders.add(new Reminder(reminderId, time, movie));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        for (Reminder r : reminders) {
            System.out.println("REMINDER: " + r.getTime());
        }
        return reminders;
    }

    public Reminder getReminderForUserAndMovie(int userId, int movieId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql = "SELECT " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_ID + " AS reminder_id, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_TIME + " AS reminder_time, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_ID + " AS movie_id, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_TITLE + " AS movie_title, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_RATING + " AS movie_rating, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_IMAGE + " AS movie_image, " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_YEAR + " AS movie_year " +
                "FROM " + DatabaseHelper.ReminderEntry.REMINDER_TABLE_NAME + " " +
                "WHERE " + DatabaseHelper.ReminderEntry.REMINDER_COLUMN_USER_ID + "=? AND " +
                DatabaseHelper.ReminderEntry.REMINDER_COLUMN_MOVIE_ID + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId), String.valueOf(movieId) });
        Reminder reminder = null;
        if (cursor != null && cursor.moveToFirst()) {
            int reminderId = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_id"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("reminder_time"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("movie_title"));
            float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("movie_rating"));
            String image = cursor.getString(cursor.getColumnIndexOrThrow("movie_image"));
            String movieYear = cursor.getString(cursor.getColumnIndexOrThrow("movie_year"));
            Movie movie = new Movie(movieId, title, rating, image, false, movieYear, "");
            reminder = new Reminder(reminderId, time, movie);
        }
        if (cursor != null) cursor.close();
        db.close();
        return reminder;
    }
}
