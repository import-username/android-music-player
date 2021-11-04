package com.importusername.musicplayer.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.fragments.HomeMenuFragment;
import com.importusername.musicplayer.fragments.PlaylistsMenuFragment;
import com.importusername.musicplayer.fragments.SettingsMenuFragment;
import com.importusername.musicplayer.fragments.SongsMenuFragment;

// TODO - disable bottom nav view moving up when keyboard is displayed
public class MusicPlayerActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = this.getSupportFragmentManager();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_music_player_menu);

        final BottomNavigationView navigationView = findViewById(R.id.music_player_navbar);

        navigationView.setOnItemSelectedListener(this.musicPlayerNavigationListener());
    }

    private NavigationBarView.OnItemSelectedListener musicPlayerNavigationListener() {
        return (item) -> {
            switch(item.getTitle().toString()) {
                case "Home":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, HomeMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
                case "Songs":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, SongsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
                case "Playlists":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, PlaylistsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
                case "Settings":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, SettingsMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .commit();
                    break;
            }

            return true;
        };
    }

    // TODO - override onbackpressed method to disable back button
}
