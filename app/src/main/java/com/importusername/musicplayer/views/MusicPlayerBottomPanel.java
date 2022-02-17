package com.importusername.musicplayer.views;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.activity.MusicPlayerActivity;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.fragments.SongFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// TODO - fix issue where pause/play button not in correct state when displaying from song menu
// TODO - url that layout uses to query more songs from backend should be changeable
public class MusicPlayerBottomPanel extends ConstraintLayout {
    private SongItemService service;

    private List<SongsMenuItem> songsMenuItemList;

    private Player.Listener bottomPanelListener;

    private OnCloseListener onCloseListener;

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context) {
        super(context);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initializeLayout();
    }

    /**
     * This should be called in the onCreate or onCreateView of enclosing activity/fragment/etc before user interaction.
     * @param service Service with exoplayer object.
     */
    public void setExoplayerService(SongItemService service) {
        this.service = service;
    }

    public void setSongitemsList(List<SongsMenuItem> list) {
        this.songsMenuItemList = list;

        // This conditional is to buffer playlist when bottom panel is displayed within the last 10 songs.
        if ((this.songsMenuItemList.size() - this.service.getExoPlayer().getCurrentMediaItemIndex()) < 11) {
            this.bufferPlaylist();
        }

        this.findViewById(R.id.bottom_panel_skip_left).setOnClickListener((v) -> {
            MusicPlayerBottomPanel.this.service.getExoPlayer().seekToPreviousMediaItem();
        });

        this.findViewById(R.id.bottom_panel_skip_right).setOnClickListener((v) -> {
            MusicPlayerBottomPanel.this.service.getExoPlayer().seekToNextMediaItem();
        });

        ((TextView) this.findViewById(R.id.music_player_bottom_panel_title)).setText(
                this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
        );

        ((TextView) this.findViewById(R.id.music_player_bottom_panel_author)).setText(
                this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getAuthor() != null
                        ? "by " + this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getAuthor()
                        : "by ..."
        );

        /*
         * When song fragment stops, its player listener is removed.
         * This will prevent player object from sending requests to get more available songs.
         * This listener object will be used to avoid that issue.
         */
        this.bottomPanelListener = new Player.Listener() {
            // Used to avoid requesting more songs with the same skip query param twice.
            private int skippedMediaIndex;

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                                Player.PositionInfo newPosition,
                                                int reason) {
                if (newPosition.mediaItemIndex != oldPosition.mediaItemIndex) {
                    // Add more songs if the end of playlist is being reached
                    if (newPosition.mediaItemIndex != skippedMediaIndex && ((MusicPlayerBottomPanel.this.songsMenuItemList.size() - newPosition.mediaItemIndex) < 11)) {
                        skippedMediaIndex = newPosition.mediaItemIndex;

                        MusicPlayerBottomPanel.this.bufferPlaylist();
                    }

                    ((TextView) MusicPlayerBottomPanel.this.findViewById(R.id.music_player_bottom_panel_title))
                            .setText(MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex)
                                    .getSongName());

                    ((TextView) MusicPlayerBottomPanel.this.findViewById(R.id.music_player_bottom_panel_author))
                            .setText(
                                    MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex).getAuthor() != null
                                            ? "by " + MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex).getAuthor() : "by ..."
                            );

                    MusicPlayerBottomPanel.this.service.displayNotification(
                            "Now playing",
                            MusicPlayerBottomPanel.this.songsMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
                    );
                }

            }
        };

        this.service.getExoPlayer().addListener(this.bottomPanelListener);
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    /**
     * Displays bottom panel, continues playing songs with provided list, and continues buffering song list
     * for a fluent transition from playlist/song menus to another menu to avoid stopping/restarting audio.
     * @param songRequestsUrl Url that should be used to request more songs if available.
     * @param songsList The current array of song items used to play media in exoplayer.
     */
    public void displayBottomPanel(String songRequestsUrl, List<SongsMenuItem> songsList) {
        // TODO
    }

    public void stopBottomPanel() {
        if (this.bottomPanelListener != null) {
            this.service.getExoPlayer().removeListener(this.bottomPanelListener);
        }

        this.setVisibility(View.GONE);
    }

    private void initializeLayout() {
        MusicPlayerBottomPanel.inflate(this.getContext(), R.layout.music_player_bottom_panel, this);

        this.findViewById(R.id.close_bottom_panel_button).setOnClickListener((v) -> {
            this.setVisibility(View.GONE);

            if (MusicPlayerBottomPanel.this.onCloseListener != null) {
                MusicPlayerBottomPanel.this.onCloseListener.onPanelClosed();
            }
        });

        final ImageView pauseBtn = this.findViewById(R.id.pause_button);
        final ImageView playBtn = this.findViewById(R.id.play_button);

        this.findViewById(R.id.pause_button).setOnClickListener((v) -> {
            pauseBtn.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.VISIBLE);

            if (this.service != null) {
                this.service.pauseAudio();
            }
        });

        this.findViewById(R.id.play_button).setOnClickListener((v) -> {
            pauseBtn.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.INVISIBLE);

            if (this.service != null) {
                this.service.resumeAudio();
            }
        });
    }

    private void bufferPlaylist() {
        int sizeBeforeRequest = MusicPlayerBottomPanel.this.songsMenuItemList.size();

        final String url = AppConfig.getProperty("url", MusicPlayerBottomPanel.this.getContext())
                + Endpoints.GET_SONGS;

        final BufferSongPlaylistThread bufferSongPlaylistThread = new BufferSongPlaylistThread(
                url,
                MusicPlayerBottomPanel.this.songsMenuItemList,
                MusicPlayerBottomPanel.this.getContext()
        );

        bufferSongPlaylistThread.setQueryLimit(1);

        bufferSongPlaylistThread.setSkip(MusicPlayerBottomPanel.this.songsMenuItemList.size());

        bufferSongPlaylistThread.setOnCompleteListener((boolean complete) -> {
            for (int i = sizeBeforeRequest; i < MusicPlayerBottomPanel.this.songsMenuItemList.size(); i++) {
                final Handler handler = new Handler (
                        MusicPlayerBottomPanel.this.service.getExoPlayer().getApplicationLooper()
                );

                int finalI = i;

                handler.post(() -> {
                    MusicPlayerBottomPanel.this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                            AppConfig.getProperty("url", MusicPlayerBottomPanel.this.getContext())
                                    + Endpoints.SONG
                                    + "/"
                                    + MusicPlayerBottomPanel.this.songsMenuItemList.get(finalI).getSongId()
                    )));
                });
            }
        });

        bufferSongPlaylistThread.start();
    }

    public interface OnCloseListener {
        void onPanelClosed();
    }
}
