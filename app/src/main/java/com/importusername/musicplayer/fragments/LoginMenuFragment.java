package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.activity.MusicPlayerActivity;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.http.HttpBody;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class LoginMenuFragment extends Fragment {
    public LoginMenuFragment() {
        super(R.layout.login_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_menu_fragment, container, false);

        view.findViewById(R.id.music_player_login_button).setOnClickListener(this.loginButtonAction());

        return view;
    }

    private View.OnClickListener loginButtonAction() {
        return (View view) -> {
            // Get email and password text
            final String emailText = ((EditText) getView().findViewById(R.id.login_email_input)).getText().toString();
            final String passwordText = ((EditText) getView().findViewById(R.id.login_password_input)).getText().toString();

            final HttpBody body = new HttpBody();

            try {
                body.setBody(new JSONObject().put("email", emailText).put("password", passwordText));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Send login request
            final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(
                    AppConfig.getProperty("url", LoginMenuFragment.this.getContext()) + "/login",
                    RequestMethod.POST,
                    LoginMenuFragment.this.getContext(),
                    false,
                    this.loginRequestAction()
            );
            musicPlayerRequestThread.setBody(body);

            musicPlayerRequestThread.start();
        };
    }

    private IHttpRequestAction loginRequestAction() {
        return (int status, String response, Map<String, List<String>> headers) -> {
            if (status == 200) {
                if (((CheckBox) LoginMenuFragment.this.getView().findViewById(R.id.stay_logged_in_checkbox)).isChecked()) {
                    AppCookie.setAuthCookie(headers.get("Set-Cookie").get(0), LoginMenuFragment.this.getContext());
                }

                final Intent musicPlayerIntent = new Intent(LoginMenuFragment.this.getContext(), MusicPlayerActivity.class);

                this.startActivity(musicPlayerIntent);
            } else {
                LoginMenuFragment.this.displayRequestErrorMessage(response);
            }
        };
    }

    /**
     * Displays login error message on failed login.
     * @param errorMessage Error message to display.
     */
    private void displayRequestErrorMessage(String errorMessage) {
        LoginMenuFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView errorTextView = ((TextView) LoginMenuFragment.this.getView().findViewById(R.id.login_error_message_text));

                try {
                    errorTextView.setText(new JSONObject(errorMessage).getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}
