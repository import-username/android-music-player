package com.importusername.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.activity.PlaylistAdapter;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppToast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment implements IBackPressFragment {
    private PlaylistAdapter playlistAdapter;

    private final PlaylistItem playlistItem;

    private final SongItemService songItemService;

    private final List<SongsMenuItem> songsMenuItemList;

    public PlaylistFragment(PlaylistItem playlistItem, List<SongsMenuItem> songsMenuItemList, SongItemService service) {
        super(R.layout.playlist_menu);

        this.playlistItem = playlistItem;
        this.songsMenuItemList = songsMenuItemList;
        this.songItemService = service;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.playlist_menu, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.playlist_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));

        this.playlistAdapter = new PlaylistAdapter(this.playlistItem, this.getActivity());
        this.playlistAdapter.setOnItemClickListener((songItem, index) -> {
            this.songItemService.getExoPlayer().seekTo(index, 0);
        });

        recyclerView.setAdapter(this.playlistAdapter);

        for (SongsMenuItem item : this.songsMenuItemList) {
            this.songItemService.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                    AppConfig.getProperty("url", this.getContext())
                            + Endpoints.SONG
                            + "/"
                            + item.getSongId()
            )));
        }

        if (!this.songItemService.getExoPlayer().isPlaying()) {
            this.songItemService.getExoPlayer().prepare();

            this.songItemService.getExoPlayer().setVolume(1f);
            this.songItemService.getExoPlayer().setPlayWhenReady(true);
        }

        return view;
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
