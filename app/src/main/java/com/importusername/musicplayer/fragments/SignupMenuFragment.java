package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.os.Bundle;
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

public class SignupMenuFragment extends Fragment {
    public SignupMenuFragment() {
        super(R.layout.signup_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_menu_fragment, container, false);

        view.findViewById(R.id.music_player_signup_button).setOnClickListener(this.signupButtonAction());

        return view;
    }

    private View.OnClickListener signupButtonAction() {
        return (View view) -> {
            // Get email and password text
            final String emailText = ((EditText) getView().findViewById(R.id.signup_email_input)).getText().toString();
            final String passwordText = ((EditText) getView().findViewById(R.id.signup_password_input)).getText().toString();
            final String retypePasswordText = ((EditText) getView().findViewById(R.id.signup_retype_password_input)).getText().toString();

            if (passwordText.equals(retypePasswordText)) {
                final HttpBody body = new HttpBody();

                try {
                    body.setBody(new JSONObject().put("email", emailText).put("password", passwordText));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send login request
                final MusicPlayerRequestThread musicPlayerRequestThread = new MusicPlayerRequestThread(
                        AppConfig.getProperty("url", SignupMenuFragment.this.getContext()) + "/signup",
                        RequestMethod.POST,
                        SignupMenuFragment.this.getContext(),
                        false,
                        this.signupRequestAction()
                );
                musicPlayerRequestThread.setBody(body);

                musicPlayerRequestThread.start();
            } else {
                SignupMenuFragment.this.displayRequestErrorMessage("Passwords do not match.");
            }

        };
    }

    private IHttpRequestAction signupRequestAction() {
        return (int status, String response, Map<String, List<String>> headers) -> {
            if (status == 200) {
                // TODO - change to login activity or automatically login
            } else {
                SignupMenuFragment.this.displayRequestErrorMessage(new JSONObject(response).getString("message"));
            }
        };
    }

    /**
     * Displays signup error message on failed signup.
     * @param errorMessage Error message to display.
     */
    private void displayRequestErrorMessage(String errorMessage) {
        SignupMenuFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView errorTextView = ((TextView) SignupMenuFragment.this.getView().findViewById(R.id.signup_error_message_text));

                errorTextView.setText(errorMessage);

                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}
