package com.example.mockproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.callback.OnReminderDeleteListener;
import com.example.mockproject.database.ReminderRepository;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.fragment.adapter.ReminderAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity implements OnReminderDeleteListener {

    private ReminderAdapter reminderAdapter;
    private List<Reminder> reminderList;
    private ReminderRepository reminderRepository;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARE_KEY, Context.MODE_PRIVATE);
        userId = Integer.parseInt(sharedPreferences.getString(MainActivity.USER_ID, "0"));
        reminderRepository = new ReminderRepository(this);

        RecyclerView recyclerView = findViewById(R.id.reminder_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        reminderList = reminderRepository.getRemindersByUser(userId);
        if (reminderList == null) {
            reminderList = new ArrayList<>();
        }

        reminderAdapter = new ReminderAdapter(this, reminderList, ReminderActivity.this);

        recyclerView.setAdapter(reminderAdapter);
    }

    private void reloadReminders() {
        reminderList = reminderRepository.getRemindersByUser(userId);
        if (reminderList == null) {
            reminderList = new ArrayList<>();
        }
        reminderAdapter.updateReminders(reminderList);
    }

    @Override
    public void onReminderDeleted(int reminderId) {
        int rowsDeleted = reminderRepository.deleteReminder(reminderId);
        if (rowsDeleted > 0) {
            Toast.makeText(ReminderActivity.this, "Delete reminder successfully", Toast.LENGTH_SHORT).show();
            reloadReminders();
        } else {
            Toast.makeText(ReminderActivity.this, "Delete reminder failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
