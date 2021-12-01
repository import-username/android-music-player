package com.importusername.musicplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SongFragment extends EventFragment implements IBackPressFragment {
    private SongsMenuItem initialSongItem;

    private List<SongsMenuItem> songMenuItemList;

    private ExoPlayer exoPlayer;

    private Handler handler;

    private boolean playAllSongs = false;

    private Player.Listener playerListener;

    public SongFragment() {
        super(R.layout.song_menu_layout);
    }

    /**
     * Performs the same as {@link SongFragment#SongFragment(ExoPlayer, SongsMenuItem, List, Handler, boolean)}
     * but does not include a list of items to be played after the initial song item.
     */
    public SongFragment(ExoPlayer exoPlayer, SongsMenuItem initialSongItem, Handler handler) {
        super(R.layout.song_menu_layout);

        this.exoPlayer = exoPlayer;
        this.initialSongItem = initialSongItem;
        this.handler = handler;
    }

    /**
     * @param exoPlayer Exoplayer object
     * @param initialSongItem The song item that the player should initially start playing.
     * @param songMenuItemList List of song items to play. Initial song item should be included in this list.
     * @param handler A handler object from the exoplayer's enclosing thread. Used to interact with exoplayer.
     * @param playAllSongs Boolean value to determine if all songs in the list should be added to exoplayer playlist.
     */
    public SongFragment(ExoPlayer exoPlayer, SongsMenuItem initialSongItem, List<SongsMenuItem> songMenuItemList, Handler handler, boolean playAllSongs) {
        super(R.layout.song_menu_layout);

        this.exoPlayer = exoPlayer;
        this.initialSongItem = initialSongItem;
        this.songMenuItemList = songMenuItemList;
        this.handler = handler;
        this.playAllSongs = playAllSongs;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.song_menu_layout, container, false);

        ((TextView) view.findViewById(R.id.song_menu_title_view)).setText(this.initialSongItem.getSongName());

        if (this.initialSongItem.getSongThumbnailId() != null) {
            final String url = AppConfig.getProperty("url", view.getContext())
                    + Endpoints.GET_THUMBNAIL + "/"
                    + this.initialSongItem.getSongThumbnailId().split("/")[2];

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.getActivity())).build()
            );

            final ImageView thumbnail = view.findViewById(R.id.song_menu_image_custom);
            final ImageView defaultThumbnail = view.findViewById(R.id.song_menu_image_default);

            defaultThumbnail.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);

            Glide.with(this.getActivity())
                    .load(glideUrl)
                    .into(thumbnail);
        }

        if (this.initialSongItem.getAuthor() != null) {
            ((TextView) view.findViewById(R.id.song_menu_author_view)).setText(this.initialSongItem.getAuthor());
        }

        final PlayerControlView controlView = view.findViewById(R.id.player_view);

        controlView.setShowNextButton(true);
        controlView.setShowPreviousButton(true);
        controlView.setShowShuffleButton(true);

        // TODO - add both types of infinitely repeating toggle mode
        controlView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL);

        controlView.setShowTimeoutMs(0);

        controlView.setPlayer(SongFragment.this.exoPlayer);

        this.playerListener = new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                                Player.PositionInfo newPosition,
                                                int reason) {
                if (newPosition.mediaItemIndex != oldPosition.mediaItemIndex) {
                    SongFragment.this.changeSongLayoutInfo(SongFragment.this.songMenuItemList.get(newPosition.mediaItemIndex));
                }
            }
        };

        exoPlayer.addListener(this.playerListener);

        this.addSongItems();

        return view;
    }

    /**
     * Adds single song item/iteratively add from list depending on value passed in constructor.
     */
    private void addSongItems() {
        // TODO - handle non 2xx status codes
        if (this.isValidSongItemList() && this.playAllSongs) {
            for (SongsMenuItem item : this.songMenuItemList) {
                this.handler.post(() -> {
                    this.exoPlayer.addMediaItem(MediaItem.fromUri(Uri.parse(
                            AppConfig.getProperty("url", this.getContext())
                                    + Endpoints.SONG
                                    + "/"
                                    + item.getSongId()
                    )));

                    if (item.getSongId().equals(this.initialSongItem.getSongId())) {
                        this.exoPlayer.seekTo(this.songMenuItemList.indexOf(item), 0);
                    }

                    this.playAudio();
                });
            }
        } else {
            this.handler.post(() -> {
                this.exoPlayer.addMediaItem(MediaItem.fromUri(Uri.parse(
                        AppConfig.getProperty("url", this.getContext())
                        + Endpoints.SONG
                        + "/"
                        + this.initialSongItem.getSongId()
                )));

                this.playAudio();
            });
        }
    }

    /**
     * If player is not playing, prepares exoplayer and begins playback when ready.
     */
    public void playAudio() {
        if (!this.exoPlayer.isPlaying()) {
            this.exoPlayer.prepare();

            this.exoPlayer.setVolume(1f);
            this.exoPlayer.setPlayWhenReady(true);
        }
    }

    /**
     * Changes the appropriate views to detail the provided song item.
     * @param item SongsMenuItem object with data that should be displayed.
     */
    private void changeSongLayoutInfo(SongsMenuItem item) {
        ((TextView) getView().findViewById(R.id.song_menu_title_view)).setText(item.getSongName());

        if (item.getSongThumbnailId() != null) {
            final String url = AppConfig.getProperty("url", getView().getContext())
                    + Endpoints.GET_THUMBNAIL + "/"
                    + item.getSongThumbnailId().split("/")[2];

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.getActivity())).build()
            );

            final ImageView thumbnail = getView().findViewById(R.id.song_menu_image_custom);
            final ImageView defaultThumbnail = getView().findViewById(R.id.song_menu_image_default);

            defaultThumbnail.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);

            Glide.with(this.getActivity())
                    .load(glideUrl)
                    .into(thumbnail);
        } else {
            final ImageView thumbnail = getView().findViewById(R.id.song_menu_image_custom);
            final ImageView defaultThumbnail = getView().findViewById(R.id.song_menu_image_default);

            Glide.with(this.getActivity())
                    .clear(thumbnail);

            defaultThumbnail.setVisibility(View.VISIBLE);
            thumbnail.setVisibility(View.GONE);

        }

        if (item.getAuthor() != null) {
            ((TextView) getView().findViewById(R.id.song_menu_author_view)).setText(item.getAuthor());
        }
    }

    /**
     * Checks whether the constructor's song item list's contents are valid.
     * @return True/false
     */
    private boolean isValidSongItemList() {
        return this.songMenuItemList != null && this.songMenuItemList.size() > 0;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Emit a stopped_fragment event to notify parent fragment that player should be stopped.
        this.emitFragmentEvent("stopped_fragment", this.playerListener);
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
