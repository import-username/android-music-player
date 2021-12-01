package com.importusername.musicplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.Player;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.ISongItemListener;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SongsMenuFragment extends Fragment implements IBackPressFragment {
    private SongsMenuListAdapter songsMenuListAdapter;

    private SongItemService service;

    private Handler handler;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongsMenuFragment.this.service = ((SongItemService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            SongsMenuFragment.this.service = null;
        }
    };

    public SongsMenuFragment() {
        super(R.layout.music_player_songs_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_player_songs_menu_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.songs_menu_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        songsMenuListAdapter = new SongsMenuListAdapter(
                new ArrayList<>(),
                this.getActivity(),
                this.addSongClickListener(),
                this.songItemClickListener(),
                true);

        recyclerView.setAdapter(songsMenuListAdapter);

        handler = new Handler(Looper.getMainLooper());

        this.bindSongItemService();

        return view;
    }

    private ISongItemListener songItemClickListener() {
        return (SongsMenuItem item) -> {
            final String url = AppConfig.getProperty("url", this.getContext())
                    + Endpoints.GET_SONGS;

            final FragmentTransaction fragmentTransaction = SongsMenuFragment.this.getChildFragmentManager().beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            final MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                    url,
                    RequestMethod.GET,
                    this.getContext(),
                    true,
                    (status, response, headers) -> {
                        if (status > 199 && status < 300) {
                            try {
                                List<SongsMenuItem> songItems = new ArrayList<>();

                                final JSONArray rows = new JSONObject(response).getJSONArray("rows");

                                for (int i = 0; i < rows.length(); i++) {
                                    songItems.add(new SongsMenuItem(rows.getJSONObject(i)));
                                }

                                // TODO - get setting pref to determine if all songs should be played
                                final SongFragment songFragment = new SongFragment(
                                        this.service.getExoPlayer(),
                                        item,
                                        songItems,
                                        handler,
                                        true
                                );

                                songFragment.setFragmentEventListener("stopped_fragment", (listener) -> {
                                    this.service.getExoPlayer().stop();
                                    this.service.getExoPlayer().clearMediaItems();
                                    
                                    if (listener instanceof Player.Listener) {
                                        this.service.getExoPlayer().removeListener((Player.Listener) listener);
                                    }
                                });

                                fragmentTransaction
                                        .replace(R.id.songs_menu_fragment_container, songFragment, null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("Song Fragment")
                                        .commit();
                            } catch (JSONException exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
            );

            requestThread.start();
        };
    }

    /**
     * Listener function for displaying fragment which allows user to add an audio file and create a song item.
     */
    private View.OnClickListener addSongClickListener() {
        return (View view) -> {
            final FragmentManager fragmentManager = getChildFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            final CreateSongMenuFragment createSongMenuFragment = new CreateSongMenuFragment();

            // Repopulate song list adapter's dataset when child fragment emits refresh_dataset event.
            createSongMenuFragment.setFragmentEventListener("refresh_dataset", (data) -> {
                SongsMenuFragment.this.songsMenuListAdapter.populateSongsDataset();
            });

            fragmentTransaction
                    .replace(R.id.songs_menu_fragment_container, createSongMenuFragment, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("SongsMenuFragment")
                    .commit();
        };
    }

    /**
     * Bind a songitemservice instance to the fragment's enclosing activity component.
     */
    private void bindSongItemService() {
        final Intent songItemService = new Intent(getContext(), SongItemService.class);

        getContext().bindService(songItemService, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind service from the fragment's enclosing activity.
     */
    private void unbindSongItemService() {
        // TODO - stop exoplayer audio if user settings states not to continue playing
        if (this.service.getExoPlayer() != null) {
            this.service.getExoPlayer().stop();
            this.service.getExoPlayer().release();
        }

        this.getContext().unbindService(this.serviceConnection);
    }

    @Override
    public void onStop() {
        super.onStop();

        this.unbindSongItemService();
    }

    @Override
    public boolean shouldAllowBackPress() {
        if (getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container) instanceof IBackPressFragment) {
            return ((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container)).shouldAllowBackPress();
        }

        return false;
    }
}
