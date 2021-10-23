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
import com.importusername.musicplayer.util.AppCookie;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppCookie.getAuthCookie(this) == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(AppConfig.getProperty("url", this) + "/authenticate", this, true, this.musicPlayerRequestAction());

            musicPlayerRequestThread.start();
        }
    }

    private IHttpRequestAction musicPlayerRequestAction() {
        return (status) -> {
            if (status == 200) {
                // If /authenticate endpoint responds with status 200
                final Intent musicPlayerIntent = new Intent(MainActivity.this, MusicPlayerActivity.class);

                this.startActivity(musicPlayerIntent);
            } else {
                // If /authenticate endpoint doesn't respond with status 200
                final Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);

                this.startActivity(loginActivity);
            }
        };
    }
}