package com.importusername.musicplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.enums.AppSettings;
import com.importusername.musicplayer.fragments.*;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.views.MusicPlayerBottomPanel;

import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {
    private SongItemService service;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerActivity.this.service = ((SongItemService.LocalBinder) service).getService();

            ((MusicPlayerBottomPanel) MusicPlayerActivity.this.findViewById(R.id.music_player_bottom_panel))
                    .setExoplayerService(MusicPlayerActivity.this.service);

            ((MusicPlayerBottomPanel) MusicPlayerActivity.this.findViewById(R.id.music_player_bottom_panel))
                    .setOnCloseListener(() -> {
                        MusicPlayerActivity.this.service.stopPlayer();
                        MusicPlayerActivity.this.service.displayNotification("Nothing's playing", "...");
                    });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MusicPlayerActivity.this.service = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_music_player_menu);

        final BottomNavigationView navigationView = findViewById(R.id.music_player_navbar);

        navigationView.setOnItemSelectedListener(this.musicPlayerNavigationListener());

        final Intent songItemService = new Intent(this, SongItemService.class);

        startService(songItemService);
        this.bindSongItemService(songItemService);
    }

    private NavigationBarView.OnItemSelectedListener musicPlayerNavigationListener() {
        return (item) -> {
            final FragmentManager fragmentManager = MusicPlayerActivity.this.getSupportFragmentManager();

            final MusicPlayerBottomPanel panel = this.findViewById(R.id.music_player_bottom_panel);

            switch(item.getTitle().toString()) {
                case "Home":
                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, HomeMenuFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("HomeFragment")
                            .commit();
                    break;
                case "Songs":
                    final SongsMenuFragment songsMenuFragment = new SongsMenuFragment(this.service);

                    songsMenuFragment.setFragmentEventListener("display_bottom_panel", (songsList) -> {
                        if (songsList instanceof List) {
                            panel.setVisibility(View.VISIBLE);

                            panel.setSongitemsList((List<SongsMenuItem>) songsList);
                        }
                    });

                    songsMenuFragment.setFragmentEventListener("close_bottom_panel", (data) -> {
                        panel.stopBottomPanel();
                    });

                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, songsMenuFragment, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("SongsFragment")
                            .commit();
                    break;
                case "Playlists":
                    final PlaylistsMenuFragment playlistsMenuFragment = new PlaylistsMenuFragment(this.service);

                    playlistsMenuFragment.setOnFragmentLifecycleChange((display, songsList) -> {
                        if (display) {
                            panel.setVisibility(View.VISIBLE);

                            panel.setSongitemsList(songsList);
                        } else {
                            panel.stopBottomPanel();
                        }
                    });

                    fragmentManager.beginTransaction()
                            .replace(R.id.music_player_fragment_view, playlistsMenuFragment, null)
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

    /**
     * Bind a songitemservice instance to the fragment's enclosing activity component.
     */
    private void bindSongItemService(Intent serviceIntent) {
        this.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind service from the fragment's enclosing activity.
     */
    private void unbindSongItemService() {
        // TODO - stop exoplayer audio if user settings states not to continue playing
        if (this.service.getExoPlayer() != null) {
            this.service.releasePlayer();
        }

        if (this.service != null) {
            this.unbindService(this.serviceConnection);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        final boolean playInBackground = this.getSharedPreferences("app", 0).getBoolean(
                AppSettings.PLAY_IN_BACKGROUND.getSettingName(), false
        );

        if (!playInBackground) {
            this.service.getExoPlayer().stop();
            this.service.cancelNotification();
            MusicPlayerActivity.this.findViewById(R.id.music_player_bottom_panel).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.unbindSongItemService();
        this.service.stopSelf();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
