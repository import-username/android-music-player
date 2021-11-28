package com.importusername.musicplayer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
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

public class SongItemService extends Service {
    private NotificationManager notificationManager;

    private final IBinder localBinder = new LocalBinder();

    private ExoPlayer exoPlayer;

    private static int NOTIFICATION_ID = 1234;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.localBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // TODO - update notification song name when a new song plays
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentText("Playing (insert song name here)")
                .setSmallIcon(R.drawable.outline_music_note_24);

        this.notificationManager.notify(NOTIFICATION_ID, notification.build());

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

    public ExoPlayer getExoPlayer() {
        return this.exoPlayer;
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
