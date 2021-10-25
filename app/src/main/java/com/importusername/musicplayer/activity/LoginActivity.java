package com.importusername.musicplayer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.http.HttpBody;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set login button onclick listener.
        findViewById(R.id.music_player_login_button).setOnClickListener(this.loginButtonAction());
    }

    private View.OnClickListener loginButtonAction() {
        return (View view) -> {
            // Get email and password text
            final String emailText = ((EditText) findViewById(R.id.login_email_input)).getText().toString();
            final String passwordText = ((EditText) findViewById(R.id.login_password_input)).getText().toString();

            final HttpBody body = new HttpBody();

            try {
                body.setBody(new JSONObject().put("email", emailText).put("password", passwordText));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Send login request
            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(
                    AppConfig.getProperty("url", LoginActivity.this) + "/login",
                    RequestMethod.POST,
                    LoginActivity.this,
                    false,
                    this.loginRequestAction()
            );
            musicPlayerRequestThread.setBody(body);

            musicPlayerRequestThread.start();
        };
    }

    private IHttpRequestAction loginRequestAction() {
        return (int status) -> {
            System.out.println(status);
        };
    }
}
