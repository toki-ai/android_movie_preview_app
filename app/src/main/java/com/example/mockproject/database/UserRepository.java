package com.example.mockproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mockproject.entities.User;

public class UserRepository {

    private final DatabaseHelper databaseHelper;

    public UserRepository(Context context){
        this.databaseHelper = new DatabaseHelper(context);
    }

    public long login(String name, String birthday, String email, String image, Integer gender) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long userId = -1;

        String query = "SELECT * FROM " + DatabaseHelper.UserEntry.USER_TABLE_NAME +
                " WHERE " + DatabaseHelper.UserEntry.USER_COLUMN_EMAIL + " = ?" +
                " AND " + DatabaseHelper.UserEntry.USER_COLUMN_NAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, name});

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_ID));
        }

        cursor.close();

        if (userId == -1) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_NAME, name);
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_EMAIL, email);

            if (birthday != null) {
                values.put(DatabaseHelper.UserEntry.USER_COLUMN_BIRTHDAY, birthday);
            }

            if (image != null) {
                values.put(DatabaseHelper.UserEntry.USER_COLUMN_IMAGE, image);
            }

            if (gender != null) {
                values.put(DatabaseHelper.UserEntry.USER_COLUMN_GENDER, gender);
            }

            userId = db.insert(DatabaseHelper.UserEntry.USER_TABLE_NAME, null, values);
        }

        db.close();
        return userId;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM " + DatabaseHelper.UserEntry.USER_TABLE_NAME +
                " WHERE " + DatabaseHelper.UserEntry.USER_COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_NAME));
            String birthday = cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_BIRTHDAY))
                    ? null
                    : cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_BIRTHDAY));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_EMAIL));
            boolean gender = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_GENDER)) == 1;
            String image = cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_IMAGE))
                    ? null
                    : cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.UserEntry.USER_COLUMN_IMAGE));
            user = new User(name, image, gender, email, birthday);
        }

        cursor.close();
        db.close();
        return user;
    }

    public int updateUserProfile(int userId, String name, String birthday, String email, String image, Integer gender) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (name != null) {
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_NAME, name);
        }
        if (birthday != null) {
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_BIRTHDAY, birthday);
        }
        if (email != null) {
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_EMAIL, email);
        }
        if (image != null) {
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_IMAGE, image);
        }
        if (gender != null) {
            values.put(DatabaseHelper.UserEntry.USER_COLUMN_GENDER, gender);
        }

        int rowsUpdated = db.update(DatabaseHelper.UserEntry.USER_TABLE_NAME, values,
                DatabaseHelper.UserEntry.USER_COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        db.close();
        return rowsUpdated;
    }
}
