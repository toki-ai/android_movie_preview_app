package com.example.mockproject;

import static com.example.mockproject.utils.Constants.SHARE_KEY;
import static com.example.mockproject.utils.Constants.USER_ID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.database.ReminderRepository;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.adapter.ReminderAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity  {

    private ReminderAdapter reminderAdapter;
    private List<Reminder> reminderList;
    private ReminderRepository reminderRepository;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        userId = Integer.parseInt(sharedPreferences.getString(USER_ID, "0"));
        reminderRepository = new ReminderRepository(this);
        ImageButton btnBack = findViewById(R.id.reminder_toolbar_icon_back);
        RecyclerView recyclerView = findViewById(R.id.reminder_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ReminderActivity.this, DividerItemDecoration.VERTICAL));
        btnBack.setOnClickListener(v -> finish());

        reminderList = reminderRepository.getRemindersByUser(userId);
        if (reminderList == null) {
            reminderList = new ArrayList<>();
        }

        reminderAdapter = new ReminderAdapter(this, reminderList);

        recyclerView.setAdapter(reminderAdapter);
    }

    private void reloadReminders() {
        reminderList = reminderRepository.getRemindersByUser(userId);
        if (reminderList == null) {
            reminderList = new ArrayList<>();
        }
        reminderAdapter.updateReminders(reminderList);
    }

    public void deleteReminderDirectly(int reminderId) {
        int rowsDeleted = reminderRepository.deleteReminder(reminderId);
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Delete reminder successfully", Toast.LENGTH_SHORT).show();
            reloadReminders();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "Delete reminder failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
