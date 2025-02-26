package com.example.mockproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class UserRepository {

    private DatabaseHelper databaseHelper;

    public UserRepository(Context context){
        this.databaseHelper = new DatabaseHelper(context);
    }

    public long addUser(String name, String birthday, String email, int gender) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.UserEntry.USER_TABLE_NAME, name);
        values.put(DatabaseHelper.UserEntry.USER_COLUMN_BIRTHDAY, birthday);
        values.put(DatabaseHelper.UserEntry.USER_COLUMN_EMAIL, email);
        values.put(DatabaseHelper.UserEntry.USER_COLUMN_GENDER, gender);

        long userId = db.insert(DatabaseHelper.UserEntry.USER_TABLE_NAME, null, values);
        db.close();
        return userId;
    }

    public int deleteUser(int userId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.UserEntry.USER_TABLE_NAME,
                DatabaseHelper.UserEntry.USER_COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rowsDeleted;
    }

}
