package com.importusername.musicplayer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.importusername.musicplayer.AppURL;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.util.AppConfig;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent connectionIntent = new Intent(MainActivity.this, ConnectionActivity.class);

        if (AppURL.getAppUrl(this) == null) {
            startActivity(connectionIntent);
        } else {
            // TODO - send request to server, load music player menu if status 200
            startActivity(connectionIntent);
        }
    }
}