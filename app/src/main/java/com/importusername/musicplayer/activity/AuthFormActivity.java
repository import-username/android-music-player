package com.importusername.musicplayer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.fragments.LoginMenuFragment;
import com.importusername.musicplayer.fragments.SignupMenuFragment;

public class AuthFormActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth_form);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.auth_form_fragment_container, LoginMenuFragment.class, null)
                .commit();

        // Set login/signup button onclick listener.
        findViewById(R.id.music_player_signup_title_button).setOnClickListener(this.swapAuthFragmentListener());
        findViewById(R.id.music_player_login_title_button).setOnClickListener(this.swapAuthFragmentListener());
    }

    private View.OnClickListener swapAuthFragmentListener() {
        return (View view) -> {
            FragmentManager fragmentManager = getSupportFragmentManager();

            switch (((AppCompatButton) view).getText().toString()) {
                case "LOG IN":
                    ((TextView) findViewById(R.id.music_player_auth_form_title)).setText(R.string.music_player_login_title);
                    fragmentManager.beginTransaction()
                            .replace(R.id.auth_form_fragment_container, LoginMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
                case "SIGN UP":
                    ((TextView) findViewById(R.id.music_player_auth_form_title)).setText(R.string.music_player_signup_title);
                    fragmentManager.beginTransaction()
                            .replace(R.id.auth_form_fragment_container, SignupMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
            }
        };
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentManager = getSupportFragmentManager().findFragmentById(R.id.auth_form_fragment_container);

        if (!(fragmentManager instanceof LoginMenuFragment || fragmentManager instanceof SignupMenuFragment)) {
            super.onBackPressed();
        }
    }
}
