package com.importusername.musicplayer.activity;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
// TODO - add focusable, clickable properties to every xml layout file
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading_screen_layout);

        if (AppCookie.getAuthCookie(this) == null) {
            startActivity(new Intent(MainActivity.this, AuthFormActivity.class));
        } else {
            final String url = AppConfig.getProperty("url", this) + "/authenticate";
            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(
                    url,
                    RequestMethod.GET,
                    this,
                    true,
                    this.musicPlayerRequestAction());

            musicPlayerRequestThread.start();
        }
    }

    private IHttpRequestAction musicPlayerRequestAction() {
        return (status, response, headers) -> {
            switch (status) {
                case 200:
                    // If /authenticate endpoint responds with status 200
                    final Intent musicPlayerIntent = new Intent(MainActivity.this, MusicPlayerActivity.class);

                    this.startActivity(musicPlayerIntent);

                    break;
                case 503:
                    final Intent networkErrorIntent = new Intent(MainActivity.this, NetworkErrorActivity.class);

                    this.startActivity(networkErrorIntent);

                    break;
                default:
                    // If /authenticate endpoint doesn't respond with status 200
                    final Intent loginActivity = new Intent(MainActivity.this, AuthFormActivity.class);

                    this.startActivity(loginActivity);

                    break;
            }
        };
    }
}