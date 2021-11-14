package com.importusername.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;

public class NetworkErrorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.network_connection_error_layout);

        this.findViewById(R.id.retry_connection_button).setOnClickListener(this.retryButtonListener());
    }

    private View.OnClickListener retryButtonListener() {
        return (View view) -> {
            final String url = AppConfig.getProperty("url", this) + "/authenticate";

            NetworkErrorActivity.this.displayErrorText("Attempting to reconnect...");
            NetworkErrorActivity.this.toggleLoadingCircle(true);

            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(
                    url,
                    RequestMethod.GET,
                    this,
                    true,
                    NetworkErrorActivity.this.networkErrorRetryAction());

            musicPlayerRequestThread.start();
        };
    }

    private IHttpRequestAction networkErrorRetryAction() {
        return (status, response, headers) -> {
            switch (status) {
                case 200:
                    // If /authenticate endpoint responds with status 200
                    final Intent musicPlayerIntent = new Intent(NetworkErrorActivity.this, MusicPlayerActivity.class);

                    this.startActivity(musicPlayerIntent);

                    break;
                case 503:
                    runOnUiThread(() -> {
                        NetworkErrorActivity.this.toggleLoadingCircle(false);
                        NetworkErrorActivity.this.displayErrorText("Failed to connect.");
                    });
                    break;
                default:
                    // If /authenticate endpoint doesn't respond with status 200
                    final Intent loginActivity = new Intent(NetworkErrorActivity.this, AuthFormActivity.class);

                    this.startActivity(loginActivity);

                    break;
            }
        };
    }

    /**
     * Displays error text when user fails to connect to server.
     * @param text Error text to display
     */
    private void displayErrorText(String text) {
        ((TextView) this.findViewById(R.id.reconnection_error_text)).setText(text);

        this.toggleErrorText(true);
    }

    private void toggleErrorText(boolean display) {
        this.findViewById(R.id.reconnection_error_text).setVisibility(display ? View.VISIBLE : View.GONE);
    }

    private void toggleLoadingCircle(boolean display) {
        this.findViewById(R.id.network_reconnect_progress_bar).setVisibility(display ? View.VISIBLE : View.GONE);
    }
}
