package com.importusername.musicplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
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
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;

import java.util.HashMap;
import java.util.List;

// TODO - add buttons for pausing/playing/stopping music to notification
public class SongItemService extends Service {
    private NotificationManager notificationManager;

    private final IBinder localBinder = new LocalBinder();

    private ExoPlayer exoPlayer;

    private static int NOTIFICATION_ID = 1234;

    private static String NOTIFICATION_CHANNEL_ID = "12345";

    private final String defaultNotifTitle = "Nothing's playing";
    private final String defaultNotifContent = "...";

    private final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.outline_music_note_24);

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
                .setContentTitle(this.defaultNotifTitle)
                .setContentText(this.defaultNotifContent)
                .setSmallIcon(R.drawable.outline_music_note_24);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = NOTIFICATION_CHANNEL_ID;

        final NotificationChannel notificationChannel = new NotificationChannel(
                channelId,
                "Song Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setShowBadge(false);
        notificationManager.createNotificationChannel(notificationChannel);

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

        this.displayNotification(this.defaultNotifTitle, this.defaultNotifContent);

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

    public void addSong(SongsMenuItem songsMenuItem, Context context) {
        final Handler handler = new Handler(this.exoPlayer.getApplicationLooper());

        handler.post(() -> {
            this.exoPlayer.addMediaItem(MediaItem.fromUri(Uri.parse(
                    AppConfig.getProperty("url", context)
                            + Endpoints.SONG
                            + "/"
                            + songsMenuItem.getSongId()
            )));
        });
    }

    public void addSongs(List<SongsMenuItem> items, Context context) {
        final Handler handler = new Handler(this.exoPlayer.getApplicationLooper());

        handler.post(() -> {
            for (SongsMenuItem item : items) {
                this.exoPlayer.addMediaItem(MediaItem.fromUri(Uri.parse(
                        AppConfig.getProperty("url", context)
                                + Endpoints.SONG
                                + "/"
                                + item.getSongId()
                )));
            }
        });
    }

    public void releasePlayer() {
        this.exoPlayer.stop();
        this.exoPlayer.release();
    }

    public void displayNotification(String title, String contentText) {
        Log.i("SONG SERVICE", "Updating notification: " + title);

        this.notificationBuilder.setContentTitle(title).setContentText(contentText);

        Notification notification = notificationBuilder.build();

        this.notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void resetNotification() {
        this.displayNotification(this.defaultNotifTitle, this.defaultNotifContent);
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
