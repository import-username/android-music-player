package com.importusername.musicplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;

import java.util.HashMap;

// TODO - add buttons for pausing/playing/stopping music to notification
public class SongItemService extends Service {
    private NotificationManager notificationManager;

    private final IBinder localBinder = new LocalBinder();

    private ExoPlayer exoPlayer;

    private static int NOTIFICATION_ID = 1234;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String channelId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = this.createNotificationChannel();
        } else {
            channelId = "";
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setContentTitle("title")
                .setContentText("contentText")
                .setSmallIcon(R.drawable.outline_music_note_24);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "12345";

        notificationManager.createNotificationChannel(new NotificationChannel(
                channelId,
                "SongItemService Channel",
                NotificationManager.IMPORTANCE_DEFAULT));

        return channelId;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.localBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        this.displayNotification("Nothing's playing", "...");

        if (this.exoPlayer == null) {
            final HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", AppCookie.getAuthCookie(this));

            final DefaultHttpDataSource.Factory defaultHttpDataSource = new DefaultHttpDataSource.Factory();
            defaultHttpDataSource.setDefaultRequestProperties(headers);

            this.exoPlayer = new ExoPlayer.Builder(this)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(defaultHttpDataSource))
                    .build();
        }
    }

    public void removePlayerListener(Player.Listener listener) {
        this.exoPlayer.removeListener(listener);
    }

    public ExoPlayer getExoPlayer() {
        return this.exoPlayer;
    }

    public void playAudio() {
        if (!this.exoPlayer.isPlaying()) {
            this.exoPlayer.prepare();

            this.exoPlayer.setVolume(1f);
            this.exoPlayer.setPlayWhenReady(true);
        }
    }

    public void pauseAudio() {
        if (this.exoPlayer.isPlaying()) {
            this.exoPlayer.pause();
        }
    }

    public void resumeAudio() {
        if (!this.exoPlayer.isPlaying()) {
            this.exoPlayer.play();
        }
    }

    public void stopPlayer() {
        this.exoPlayer.stop();
        this.exoPlayer.clearMediaItems();
    }

    public void stopPlayer(Player.Listener listenerToRemove) {
        this.exoPlayer.stop();
        this.exoPlayer.clearMediaItems();

        this.exoPlayer.removeListener(listenerToRemove);

    }

    public void releasePlayer() {
        this.exoPlayer.stop();
        this.exoPlayer.release();
    }

    public void displayNotification(String title, String contentText) {
        // TODO - update notification song name when a new song plays
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.outline_music_note_24);

        Notification notification = notificationBuilder.build();

        this.notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void cancelNotification() {
        this.notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.notificationManager.cancel(NOTIFICATION_ID);
    }

    public class LocalBinder extends Binder {
        public SongItemService getService() {
            return SongItemService.this;
        }
    }
}
