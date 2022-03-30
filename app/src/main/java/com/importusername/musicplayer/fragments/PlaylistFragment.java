package com.importusername.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.SongListener;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistAdapter;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.BottomPanelInterface;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.OnRefreshComplete;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistFragment extends Fragment implements IBackPressFragment, BottomPanelInterface {
    private BottomPanelInterface.OnFragmentLifecycleChange onFragmentLifecycleChange;

    private PlaylistAdapter playlistAdapter;

    private final PlaylistItem playlistItem;

    private final SongItemService songItemService;

    private final List<SongsMenuItem> songsMenuItemList;

    private SongListener songListener;

    private String songsRequestUrl;

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

        this.songsRequestUrl = AppConfig.getProperty("url", view.getContext())
                + Endpoints.GET_PLAYLIST_SONGS
                + this.playlistItem.getPlaylistId();

        this.onFragmentLifecycleChange.displayBottomPanel(false, null, this.songsRequestUrl);

        this.songItemService.stopPlayer();

        this.songListener = new SongListener(
                AppConfig.getProperty("url", view.getContext())
                        + Endpoints.GET_PLAYLIST_SONGS
                        + "/" + this.playlistItem.getPlaylistId(),
                this.songsMenuItemList,
                this.songItemService,
                view.getContext()
        );

        this.songListener.setGetNewSongs(false);
        this.songListener.setOnSongChangeListener(this.onSongChangeListener());

        this.songItemService.getExoPlayer().addListener(this.songListener);

        RecyclerView recyclerView = view.findViewById(R.id.playlist_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));

        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            private int currentYPos = 0;

            private boolean headerVisible = false;

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                this.currentYPos += oldScrollY;

                final ConstraintLayout stickyHeader = view.findViewById(R.id.playlist_sticky_header);

                if (this.currentYPos < -830) {
                    if (!this.headerVisible) {
                        this.headerVisible = true;

                        PlaylistFragment.this.updateStickyHeaderData();

                        stickyHeader.setTranslationY(-300);

                        stickyHeader.setVisibility(View.VISIBLE);

                        stickyHeader.animate().setInterpolator(null);
                        stickyHeader.animate().setDuration(100);
                        stickyHeader.animate().translationY(0);
                    }
                } else {
                    if (this.headerVisible) {
                        this.headerVisible = false;

                        stickyHeader.animate().translationY(-300);

                        stickyHeader.animate().withEndAction(() -> {
                            stickyHeader.setVisibility(View.GONE);

                            PlaylistFragment.this.updateHeaderData();
                        });
                    }
                }
            }
        });

        this.playlistAdapter = new PlaylistAdapter(this.playlistItem, this.getActivity());
        this.playlistAdapter.setOnItemClickListener((songItem, index) -> {
            this.songItemService.getExoPlayer().seekTo(index, 0);
        });
        this.playlistAdapter.setExoplayer(this.songItemService.getExoPlayer());
        this.playlistAdapter.setOnMenuRefresh(this.onRefreshComplete());

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
            final RecyclerView headerRecyclerview = view.findViewById(R.id.playlist_recyclerview);
            final PlaylistAdapter.PlaylistHeader header = (PlaylistAdapter.PlaylistHeader) headerRecyclerview.findViewHolderForAdapterPosition(0);

            if (header != null) {
                header.setPlayingTitle(this.songsMenuItemList.get(0).getSongName());
            }

            this.songItemService.getExoPlayer().prepare();

            this.songItemService.getExoPlayer().setVolume(1f);
            this.songItemService.getExoPlayer().setPlayWhenReady(true);
        }

        ((SwipeRefreshLayout) view.findViewById(R.id.playlist_menu_refresh_layout)).setOnRefreshListener(() -> {
            final SwipeRefreshLayout swipeRefreshLayout = this.getView().findViewById(R.id.playlist_menu_refresh_layout);

            swipeRefreshLayout.setRefreshing(true);

            this.playlistAdapter.refreshDataset();
        });

        this.setStickyHeaderListeners(view);

        return view;
    }

    private OnRefreshComplete onRefreshComplete() {
        return () -> {
            final SwipeRefreshLayout swipeRefreshLayout = this.getView().findViewById(R.id.playlist_menu_refresh_layout);

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        };
    }

    private SongListener.OnSongChangeListener onSongChangeListener() {
        return (songItem) -> {
            final RecyclerView recyclerView = this.getView().findViewById(R.id.playlist_recyclerview);
            final PlaylistAdapter.PlaylistHeader header = (PlaylistAdapter.PlaylistHeader) recyclerView.findViewHolderForAdapterPosition(0);

            if (header != null) {
                header.setPlayingTitle(songItem.getSongName());
            }

            this.updateStickyHeaderData();
        };
    }

    private void updateHeaderData() {
        final RecyclerView recyclerView = this.getView().findViewById(R.id.playlist_recyclerview);
        final PlaylistAdapter.PlaylistHeader header = (PlaylistAdapter.PlaylistHeader) recyclerView.findViewHolderForAdapterPosition(0);

        final SongsMenuItem songItem = this.playlistAdapter.getPlaylistSongsList().get(
                this.songItemService.getExoPlayer().getCurrentMediaItemIndex() + 1
        );

        if (header != null) {
            header.setPlayingTitle(songItem.getSongName());
        }
    }

    private void updateStickyHeaderData() {
        final RecyclerView recyclerView = this.getView().findViewById(R.id.playlist_recyclerview);
        final ConstraintLayout stickyHeader = this.getView().findViewById(R.id.playlist_sticky_header);
        final SongsMenuItem songItem = this.playlistAdapter.getPlaylistSongsList().get(
                this.songItemService.getExoPlayer().getCurrentMediaItemIndex() + 1
        );
        final ExoPlayer player = this.songItemService.getExoPlayer();

        final ImageView stickyHeaderThumbnail = stickyHeader.findViewById(R.id.playlist_sticky_header_thumbnail);

        ((TextView) stickyHeader.findViewById(R.id.sticky_header_title)).setText(songItem.getSongName());

        if (player.isPlaying()) {
            stickyHeader.findViewById(R.id.sticky_header_pause).setVisibility(View.VISIBLE);
            stickyHeader.findViewById(R.id.sticky_header_play).setVisibility(View.GONE);
        } else {
            stickyHeader.findViewById(R.id.sticky_header_pause).setVisibility(View.GONE);
            stickyHeader.findViewById(R.id.sticky_header_play).setVisibility(View.VISIBLE);
        }

        final PlaylistAdapter.PlaylistSongItem playlistSongItem = ((PlaylistAdapter.PlaylistSongItem) recyclerView.findViewHolderForAdapterPosition(
                this.songItemService.getExoPlayer().getCurrentMediaItemIndex() + 1
        ));

        if (playlistSongItem != null) {
            stickyHeaderThumbnail.setImageDrawable(playlistSongItem.getThumbnail());
        }
    }

    private void setStickyHeaderListeners(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.playlist_recyclerview);
        final ConstraintLayout stickyHeader = view.findViewById(R.id.playlist_sticky_header);

        if (stickyHeader != null) {
            stickyHeader.setOnClickListener((v) -> {
                recyclerView.smoothScrollToPosition(0);
            });

            stickyHeader.findViewById(R.id.sticky_header_play).setOnClickListener((v) -> {
                this.songItemService.playAudio();
            });

            stickyHeader.findViewById(R.id.sticky_header_pause).setOnClickListener((v) -> {
                this.songItemService.pauseAudio();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.onFragmentLifecycleChange.displayBottomPanel(false, null, this.songsRequestUrl);
    }

    @Override
    public void onStop() {
        super.onStop();

        this.songItemService.removePlayerListener(this.songListener);

        this.onFragmentLifecycleChange.displayBottomPanel(true, this.songsMenuItemList, this.songsRequestUrl);
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }

    @Override
    public void setOnFragmentLifecycleChange(OnFragmentLifecycleChange listener) {
        this.onFragmentLifecycleChange = listener;
    }
}
