package com.importusername.musicplayer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.importusername.musicplayer.AppURL;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Activity for connecting to music player server.
 */
public class ConnectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection);

        final AppCompatButton connectBtn = (AppCompatButton) findViewById(R.id.connection_menu_button);
        connectBtn.setOnClickListener(this.sendConnectionRequest());
    }

    /**
     * Sends get request to /verify-music-player endpoint.
     */
    public View.OnClickListener sendConnectionRequest() {
        return (view) -> {
            EditText urlInput = findViewById(R.id.connection_menu_input);

            if (urlInput != null) {
                String urlString = "";

                try {
                    urlString = this.getUrlDomain(urlInput.getText().toString(), true, false);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                final MusicPlayerRequestThread connectionRequestThread = new MusicPlayerRequestThread("http://" + urlString + "/verify-music-player", this.connectionAction());

                connectionRequestThread.start();
            }
        };
    }

    private String getUrlDomain(String urlString, boolean includePort, boolean includeProtocol) throws MalformedURLException {
        final URL url = new URL(urlString);

        String parsedUrl = "";

        if (includeProtocol && url.getProtocol().length() > 0) {
            parsedUrl += url.getProtocol();
        }

        parsedUrl = url.getHost();

        if (includePort && url.getPort() > 0) {
            parsedUrl += ":" + url.getPort();
        }

        return parsedUrl;
    }

    private IHttpRequestAction connectionAction() {
        return new IHttpRequestAction() {
            @Override
            public void requestAction(int status) {
                if (status == 200) {
                    final EditText urlInput = findViewById(R.id.connection_menu_input);
                    String urlString = "";

                    try {
                        urlString = "http://" + ConnectionActivity.this.getUrlDomain(urlInput.getText().toString(), true, false);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    AppURL.setAppUrl(urlString, ConnectionActivity.this);
                }
            }
        };
    }
}
