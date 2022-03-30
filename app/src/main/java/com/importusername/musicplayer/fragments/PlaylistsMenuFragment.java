package com.importusername.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
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
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistItem;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistMenuAdapter;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.BottomPanelInterface;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppToast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsMenuFragment extends Fragment implements IBackPressFragment, BottomPanelInterface {
    private Context context;

    private OnFragmentLifecycleChange OnlifecycleChangeListener;

    private PlaylistMenuAdapter playlistMenuAdapter;

    private SongItemService songItemService;

    public PlaylistsMenuFragment() {
        super(R.layout.music_player_playlists_menu_fragment);
    }

    public PlaylistsMenuFragment(SongItemService service) {
        super(R.layout.music_player_playlists_menu_fragment);

        this.songItemService = service;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this.getActivity().getApplicationContext();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.music_player_playlists_menu_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.playlist_menu_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        this.playlistMenuAdapter = new PlaylistMenuAdapter(
                new ArrayList<>(),
                this.getActivity(),
                this.addPlaylistListener(),
                this.playlistItemClickListener());

        recyclerView.setAdapter(this.playlistMenuAdapter);

        return view;
    }

    private View.OnClickListener addPlaylistListener() {
        return (view) -> {
            final FragmentManager fragmentManager = getChildFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            final CreatePlaylistFragment playlistFragment = new CreatePlaylistFragment();

            playlistFragment.setFragmentEventListener("refresh_dataset", (data) -> {
                this.getActivity().runOnUiThread(() -> {
                    PlaylistsMenuFragment.this.playlistMenuAdapter.refreshDataset();
                });
            });

            playlistFragment.setFragmentEventListener("redirect_to_playlist", (data) -> {
                PlaylistsMenuFragment.this.playlistMenuAdapter.populatePlaylistDataset();

                getChildFragmentManager().beginTransaction().remove(playlistFragment).commit();
            });

            fragmentTransaction
                    .replace(R.id.playlist_menu_fragment_container, playlistFragment, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("PlaylistMenuFragment")
                    .commit();
        };
    }

    private PlaylistMenuAdapter.OnPlaylistClick playlistItemClickListener() {
        return (PlaylistItem item) -> {
            final String url = AppConfig.getProperty("url", this.getContext())
                    + Endpoints.GET_PLAYLIST_SONGS
                    + "/" + item.getPlaylistId();

            final ArrayList<SongsMenuItem> songsMenuItems = new ArrayList<>();

            final BufferSongPlaylistThread bufferSongPlaylistThread = new BufferSongPlaylistThread(
                    url,
                    songsMenuItems,
                    this.getContext()
            );

            bufferSongPlaylistThread.setLimit(500);

            bufferSongPlaylistThread.setOnCompleteListener((complete) -> {
                if (songsMenuItems.size() > 0) {
                    final FragmentTransaction fragmentTransaction = PlaylistsMenuFragment.this.getChildFragmentManager().beginTransaction();

                    fragmentTransaction.setCustomAnimations(
                            R.anim.slide_in,
                            R.anim.slide_out,
                            R.anim.slide_in,
                            R.anim.slide_out
                    );

                    final PlaylistFragment playlistFragment = new PlaylistFragment(item, songsMenuItems, this.songItemService);

                    playlistFragment.setOnFragmentLifecycleChange(this.OnlifecycleChangeListener);

                    fragmentTransaction
                                .replace(R.id.playlist_menu_fragment_container, playlistFragment, null)
                                .setReorderingAllowed(true)
                                .addToBackStack("Playlist Fragment")
                                .commit();
                } else {
                    AppToast.showToast("Failed to load playlist songs.", this.getActivity());
                }
            });

            bufferSongPlaylistThread.start();
        };
    }

    @Override
    public boolean shouldAllowBackPress() {
        if (getChildFragmentManager().findFragmentById(R.id.playlist_menu_fragment_container) instanceof IBackPressFragment) {
            return ((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.playlist_menu_fragment_container)).shouldAllowBackPress();
        }

        return false;
    }

    @Override
    public void setOnFragmentLifecycleChange(OnFragmentLifecycleChange listener) {
        this.OnlifecycleChangeListener = listener;
    }
}
