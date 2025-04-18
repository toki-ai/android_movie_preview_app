package com.example.mockproject.receiver;

import static com.example.mockproject.utils.Constants.RECEIVER_MOVIE_RATING;
import static com.example.mockproject.utils.Constants.RECEIVER_MOVIE_TITLE;
import static com.example.mockproject.utils.Constants.RECEIVER_MOVIE_YEAR;
import static com.example.mockproject.utils.Constants.RECEIVER_TIME;
import static com.example.mockproject.utils.Constants.RECEIVER_USER_ID;
import static com.example.mockproject.utils.Constants.SHARE_KEY;
import static com.example.mockproject.utils.Constants.USER_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;
import com.example.mockproject.database.ReminderRepository;
import com.example.mockproject.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "movie_reminder_channel";
    private static final String CHANNEL_NAME = "Movie Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        int currentUserId = Integer.parseInt(prefs.getString(USER_ID, "0"));
        int reminderUserId = intent.getIntExtra(RECEIVER_USER_ID, 0);

        if (currentUserId != reminderUserId) {
            return;
        }

        String movieTitle = intent.getStringExtra(RECEIVER_MOVIE_TITLE);
        int movieId = intent.getIntExtra("MOVIE_ID", -1);
        long reminderTime = intent.getLongExtra(RECEIVER_TIME, 0);
        String movieRating = intent.getStringExtra(RECEIVER_MOVIE_RATING);
        String movieYear = intent.getStringExtra(RECEIVER_MOVIE_YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(reminderTime));

        String content = "Time: " + formattedTime;
        if (movieYear != null && movieRating != null) {
            content += " | Year: " + movieYear + " | Rate: " + movieRating;
        }

        createNotificationChannel(context);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra("MOVIE_ID", movieId);
        activityIntent.putExtra("MOVIE_TITLE", movieTitle);
        activityIntent.putExtra("FROM_REMINDER", true);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                movieId,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_slash_bg);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_film)
                .setLargeIcon(largeIcon)
                .setContentTitle(movieTitle != null ? movieTitle : "Movie Reminder")
                .setContentText(content)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        ReminderRepository reminderRepository = new ReminderRepository(context);
        reminderRepository.deleteReminderByMovieId(currentUserId, movieId);

        Intent updateUIIntent = new Intent(Constants.ACTION_UPDATE_REMINDER_LIST);
        context.sendBroadcast(updateUIIntent);
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Channel for Movie Reminder Notifications");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
