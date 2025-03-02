package com.example.mockproject.fragment.adapter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mockproject.MainActivity;
import com.example.mockproject.ReminderActivity;
import com.example.mockproject.R;
import com.example.mockproject.entities.Reminder;
import com.example.mockproject.receiver.ReminderReceiver;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private final Context context;

    public ReminderAdapter(Context context, List<Reminder> reminders) {
        Toast.makeText(context, String.valueOf(reminders.size()), Toast.LENGTH_SHORT).show();
        this.context = context;
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        Toast.makeText(context, "HIII", Toast.LENGTH_SHORT).show();
        if (reminder.getMovie() != null && reminder.getMovie().getPosterUrl() != null) {
            Picasso.get()
                    .load(reminder.getMovie().getPosterUrl())
                    .placeholder(R.drawable.img_slash_bg)
                    .into(holder.reminderImage);
        } else {
            holder.reminderImage.setImageResource(R.drawable.img_slash_bg);
        }

        String title = reminder.getMovie().getTitle();
        if (title.length() > 20) {
            title = title.substring(0, 20) + "...";
        }
        holder.reminderMovieTitle.setText(title);

        if (reminder.getMovie().getReleaseDate() != null && reminder.getMovie().getReleaseDate().length() >= 4) {
            holder.reminderMovieReleaseYear.setText(reminder.getMovie().getReleaseDate().substring(0, 4));
        }
        holder.reminderMovieRating.setText(reminder.getMovie().getRating() + "/10");
        try {
            long timeInMillis = Long.parseLong(reminder.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(timeInMillis));
            holder.reminderTime.setText(formattedTime);
        } catch (NumberFormatException e) {
            holder.reminderTime.setText(reminder.getTime());
        }

        if(context instanceof MainActivity){
            holder.btnDelete.setVisibility(View.GONE);
        }else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                cancelAlarm(reminder.getId(), reminder.getMovie().getId());
                if (context instanceof ReminderActivity) {
                    ((ReminderActivity) context).deleteReminderDirectly(reminder.getId());
                }
            });
        }

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
                context,
                movieId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
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

