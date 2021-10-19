package com.importusername.musicplayer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.importusername.musicplayer.AppURL;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent connectionIntent = new Intent(MainActivity.this, ConnectionActivity.class);

        if (AppURL.getAppUrl(this) == null) {
            startActivity(connectionIntent);
        } else {
            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(AppURL.getAppUrl(this) + "/verify-music-player", this.musicPlayerRequestAction());

            musicPlayerRequestThread.start();
        }
    }

    private IHttpRequestAction musicPlayerRequestAction() {
        return (status) -> {
            if (status == 200) {
                final Intent musicPlayerIntent = new Intent(MainActivity.this, MusicPlayerActivity.class);

                this.startActivity(musicPlayerIntent);
            } else {
                final Intent connectionIntent = new Intent(MainActivity.this, ConnectionActivity.class);

                this.startActivity(connectionIntent);
            }
        };
    }
}