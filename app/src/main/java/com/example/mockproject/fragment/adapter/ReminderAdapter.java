package com.example.mockproject.fragment.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.callback.OnReminderDeleteListener;
import com.example.mockproject.R;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.receiver.ReminderReceiver;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private Context context;
    private OnReminderDeleteListener onReminderDeleteListener;

    public ReminderAdapter(Context context, List<Reminder> reminders, OnReminderDeleteListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.onReminderDeleteListener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        if (reminder.getMovie() != null && reminder.getMovie().getPosterUrl() != null) {
            Picasso.get()
                    .load(reminder.getMovie().getPosterUrl())
                    .placeholder(R.drawable.img_slash_bg)
                    .into(holder.reminderImage);
        } else {
            holder.reminderImage.setImageResource(R.drawable.img_slash_bg);
        }

        holder.reminderMovieTitle.setText(reminder.getMovie().getTitle());

        if (reminder.getMovie().getReleaseDate() != null && reminder.getMovie().getReleaseDate().length() >= 4) {
            holder.reminderMovieReleaseYear.setText(reminder.getMovie().getReleaseDate().substring(0, 4));
        }
        holder.reminderMovieRating.setText(reminder.getMovie().getRating() + "/10");
        holder.reminderTime.setText(reminder.getTime());

        holder.btnDelete.setOnClickListener(v -> {
            if (onReminderDeleteListener != null) {
                cancelAlarm(reminder.getId(), reminder.getMovie().getId());
                onReminderDeleteListener.onReminderDeleted(reminder.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).openDetailFragment(reminder.getMovie().getId(), reminder.getMovie().getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private void cancelAlarm(int reminderId, int movieId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, movieId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public void updateReminders(List<Reminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ImageView reminderImage, btnDelete;
        TextView reminderMovieTitle, reminderMovieReleaseYear, reminderMovieRating, reminderTime;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderImage = itemView.findViewById(R.id.reminder_image);
            btnDelete = itemView.findViewById(R.id.reminder_btn_delete);
            reminderMovieTitle = itemView.findViewById(R.id.reminder_movie_title);
            reminderMovieReleaseYear = itemView.findViewById(R.id.reminder_movie_release_year);
            reminderMovieRating = itemView.findViewById(R.id.reminder_movie_rating);
            reminderTime = itemView.findViewById(R.id.reminder_time);
        }
    }
}

