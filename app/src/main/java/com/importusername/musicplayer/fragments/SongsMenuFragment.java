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

    public SongsMenuFragment() {
        super(R.layout.music_player_songs_menu_fragment);
    }

    public SongsMenuFragment(SongItemService service) {
        super(R.layout.music_player_songs_menu_fragment);

        this.service = service;
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

                                final SongFragment songFragment = new SongFragment(
                                        item,
                                        songItems,
                                        this.service,
                                        true
                                );

//                                songFragment.setFragmentEventListener("stopped_fragment", (listener) -> {
//                                    // TODO - check user setting pref to determine if music should continue playing through bottom panel
////                                    SongsMenuFragment.this.service.stopPlayer((Player.Listener) listener);
//                                });

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

    @Override
    public boolean shouldAllowBackPress() {
        if (getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container) instanceof IBackPressFragment) {
            return ((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container)).shouldAllowBackPress();
        }

        return false;
    }
}
