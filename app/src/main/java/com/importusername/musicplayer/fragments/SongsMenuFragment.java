package com.importusername.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.ISongItemListener;
import com.importusername.musicplayer.interfaces.OnRefreshComplete;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SongsMenuFragment extends EventFragment implements IBackPressFragment {
    private Context context;

    private SongsMenuListAdapter songsMenuListAdapter;

    private SongItemService service;

    public SongsMenuFragment() {
        super(R.layout.music_player_songs_menu_fragment);
    }

    public SongsMenuFragment(SongItemService service) {
        super(R.layout.music_player_songs_menu_fragment);

        this.service = service;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getActivity().getApplicationContext();
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

        songsMenuListAdapter.setOnAddPlaylistClickListener(this.onAddPlaylistClick());
        songsMenuListAdapter.setOnRefreshComplete(this.onRefreshComplete());
        recyclerView.setAdapter(songsMenuListAdapter);

        ((SwipeRefreshLayout) view.findViewById(R.id.songs_menu_swipe_refresh_layout)).setOnRefreshListener(() -> {
            final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.songs_menu_swipe_refresh_layout);

            swipeRefreshLayout.setRefreshing(true);

            this.songsMenuListAdapter.refreshDataset();
        });

        return view;
    }

    private OnRefreshComplete onRefreshComplete() {
        return () -> {
            final SwipeRefreshLayout swipeRefreshLayout = this.getView().findViewById(R.id.songs_menu_swipe_refresh_layout);

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        };
    }

    private SongsMenuListAdapter.OnAddPlaylistClick onAddPlaylistClick() {
        return (clickedSong) -> {
            final FragmentTransaction fragmentTransaction = SongsMenuFragment.this.getChildFragmentManager().beginTransaction();

            final AddToPlaylistFragment addToPlaylistFragment = new AddToPlaylistFragment(clickedSong);

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            fragmentTransaction
                    .replace(R.id.songs_menu_fragment_container, addToPlaylistFragment, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("Add To Playlist Fragment")
                    .commit();
        };
    }

    private ISongItemListener songItemClickListener() {
        return (SongsMenuItem item) -> {
            final List<SongsMenuItem> songsMenuItemList = new ArrayList<>();

            final BufferSongPlaylistThread bufferSongPlaylistThread = new BufferSongPlaylistThread(
                    AppConfig.getProperty("url", SongsMenuFragment.this.context)
                            + Endpoints.GET_SONGS
                            + "?includeTotal=true",
                    songsMenuItemList,
                    SongsMenuFragment.this.context
            );

            bufferSongPlaylistThread.setTargetSongItem(item);

            final FragmentTransaction fragmentTransaction = SongsMenuFragment.this.getChildFragmentManager().beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            bufferSongPlaylistThread.setOnCompleteListener((boolean complete) -> {
                final SongFragment songFragment = new SongFragment(
                        item,
                        (this.songsMenuListAdapter.getSongItemIndex(item) - 1),
                        songsMenuItemList,
                        this.service,
                        true
                );

                songFragment.setFragmentEventListener("display_bottom_panel", (songsList) -> {
                    this.emitFragmentEvent("display_bottom_panel", songsList);
                });

                songFragment.setFragmentEventListener("close_bottom_panel", (data) -> {
                    SongsMenuFragment.this.emitFragmentEvent("close_bottom_panel", data);
                });

                fragmentTransaction
                        .replace(R.id.songs_menu_fragment_container, songFragment, null)
                        .setReorderingAllowed(true)
                        .addToBackStack("Song Fragment")
                        .commit();
            });

            bufferSongPlaylistThread.start();
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
                this.getActivity().runOnUiThread(() -> {
                    SongsMenuFragment.this.songsMenuListAdapter.refreshDataset();
                });
            });

            createSongMenuFragment.setFragmentEventListener("redirect_to_song", (data) -> {
                SongsMenuFragment.this.songsMenuListAdapter.populateSongsDataset();

                getChildFragmentManager().beginTransaction().remove(createSongMenuFragment).commit();
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
