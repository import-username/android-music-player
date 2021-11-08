package com.importusername.musicplayer.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.fragments.*;
import com.importusername.musicplayer.interfaces.IBackPressFragment;

public class MusicPlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_music_player_menu);

        final BottomNavigationView navigationView = findViewById(R.id.music_player_navbar);

        navigationView.setOnItemSelectedListener(this.musicPlayerNavigationListener());
    }

    private NavigationBarView.OnItemSelectedListener musicPlayerNavigationListener() {
        return (item) -> {
            final FragmentManager fragmentManager = MusicPlayerActivity.this.getSupportFragmentManager();

            switch(item.getTitle().toString()) {
                case "Home":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, HomeMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("HomeFragment")
                            .commit();
                    break;
                case "Songs":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, SongsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("SongsFragment")
                            .commit();
                    break;
                case "Playlists":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, PlaylistsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("PlaylistsFragment")
                            .commit();
                    break;
                case "Settings":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, SettingsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("SettingsFragment")
                            .commit();
                    break;
            }

            return true;
        };
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.music_player_fragment_view);

        if (fragment instanceof IBackPressFragment && ((IBackPressFragment) fragment).shouldAllowBackPress()) {
            if (fragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                fragment.getChildFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }
}
