package com.example.mockproject.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.mockproject.MainActivity;
import com.example.mockproject.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "movie_reminder_channel";
    private static final String CHANNEL_NAME = "Movie Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String movieTitle = intent.getStringExtra("MOVIE_TITLE");
        String releaseYear = intent.getStringExtra("MOVIE_YEAR");
        String rating = intent.getStringExtra("MOVIE_RATING");

        String content = "";
        if (releaseYear != null && rating != null) {
            content = "Year: " + releaseYear + "   Rate: " + rating + "/10";
        }

        createNotificationChannel(context);

        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_slash_bg);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_film)
                .setLargeIcon(largeIcon)
                .setContentTitle(movieTitle != null ? movieTitle : "Movie Reminder")
                .setContentText("abcdef")
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}
