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
                final MusicPlayerRequestThread connectionRequestThread = new MusicPlayerRequestThread(urlInput.getText().toString(), this.connectionAction());

                connectionRequestThread.start();
            }
        };
    }

    private IHttpRequestAction connectionAction() {
        return new IHttpRequestAction() {
            @Override
            public void requestAction(int status) {
                if (status == 200) {
                    final EditText urlInput = findViewById(R.id.connection_menu_input);

                    System.out.println(status);
                    AppURL.setAppUrl(urlInput.getText().toString(), ConnectionActivity.this);
                }
            }
        };
    }
}
