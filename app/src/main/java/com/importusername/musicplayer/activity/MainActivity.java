package com.importusername.musicplayer.activity;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.util.AppConfig;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        try {
            System.out.println(AppConfig.getProperty("url;", getApplicationContext()) == null);
            Log.d(null, AppConfig.getProperty("url;", getApplicationContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}