package com.importusername.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.AppSettings;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SongFragment extends EventFragment implements IBackPressFragment {
    private SongsMenuItem initialSongItem;

    private int initialSkip;

    private List<SongsMenuItem> songMenuItemList;

    private boolean playAllSongs = false;

    private SongItemService service;

    private Player.Listener playerListener;

    public SongFragment() {
        super(R.layout.song_menu_layout);
    }

    /**
     * Performs the same as {@link SongFragment#SongFragment(SongsMenuItem, int, List, SongItemService, boolean)}
     * but does not include a list of items to be played after the initial song item.
     */
    public SongFragment(SongsMenuItem initialSongItem, int initialSkip, SongItemService service) {
        super(R.layout.song_menu_layout);

        this.initialSongItem = initialSongItem;
        this.initialSkip = initialSkip;
        this.service = service;
    }

    /**
     * @param initialSongItem The song item that the player should initially start playing.
     * @param initialSkip The number of song queries that was skipped in initial get-songs request.
     * @param songMenuItemList List of song items to play. Initial song item should be included in this list.
     * @param playAllSongs Boolean value to determine if all songs in the list should be added to exoplayer playlist.
     */
    public SongFragment(SongsMenuItem initialSongItem, int initialSkip, List<SongsMenuItem> songMenuItemList, SongItemService service, boolean playAllSongs) {
        super(R.layout.song_menu_layout);

        this.initialSongItem = initialSongItem;
        this.initialSkip = initialSkip;
        this.songMenuItemList = songMenuItemList;
        this.playAllSongs = playAllSongs;
        this.service = service;
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

        controlView.setPlayer(this.service.getExoPlayer());

        // TODO - change service notification title/content when song changes
        this.playerListener = new Player.Listener() {
            // Used to avoid requesting more songs with the same skip query param twice.
            private int skippedMediaIndex;

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                                Player.PositionInfo newPosition,
                                                int reason) {
                if (newPosition.mediaItemIndex != oldPosition.mediaItemIndex) {
                    // Add more songs if the end of playlist is being reached
                    if (newPosition.mediaItemIndex != skippedMediaIndex && ((SongFragment.this.songMenuItemList.size() - newPosition.mediaItemIndex) < 11)) {
                        skippedMediaIndex = newPosition.mediaItemIndex;

                        final String url = AppConfig.getProperty("url", SongFragment.this.getContext())
                                + Endpoints.GET_SONGS
                                + "?skip=" + (SongFragment.this.songMenuItemList.size() + SongFragment.this.initialSkip);

                        final MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                                url,
                                RequestMethod.GET,
                                SongFragment.this.getContext(),
                                true,
                                (status, response, headers) -> {
                                    if (status > 199 && status < 300) {
                                        final Handler handler = new Handler (
                                                SongFragment.this.service.getExoPlayer().getApplicationLooper()
                                        );

                                        handler.post(() -> {
                                            try {
                                                final JSONArray rows = new JSONObject(response).getJSONArray("rows");

                                                for (int i = 0; i < rows.length(); i++) {
                                                    SongFragment.this.songMenuItemList.add(new SongsMenuItem(rows.getJSONObject(i)));

                                                    SongFragment.this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                                                            AppConfig.getProperty("url", SongFragment.this.getContext())
                                                                    + Endpoints.SONG
                                                                    + "/"
                                                                    + new SongsMenuItem(rows.getJSONObject(i)).getSongId()
                                                    )));
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                }
                        );

                        requestThread.start();
                    }

                    // If media item was changed, change layout data to that of the corresponding song
                    SongFragment.this.changeSongLayoutInfo(SongFragment.this.songMenuItemList.get(newPosition.mediaItemIndex));
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    SongFragment.this.displayNotification(
                            SongFragment.this.songMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
                    );
                } else {
                    SongFragment.this.service.displayNotification("Nothing's playing...", "...");
                }
            }
        };

        this.service.getExoPlayer().addListener(this.playerListener);

        this.addSongItems();

        return view;
    }

    public List<SongsMenuItem> getSongMenuItemList() {
        return this.songMenuItemList;
    }

    /**
     * Adds single song item/iteratively add from list depending on value passed in constructor.
     */
    private void addSongItems() {
        this.service.getExoPlayer().stop();
        this.service.getExoPlayer().clearMediaItems();

        // TODO - handle non 2xx status codes
        if (this.isValidSongItemList() && this.playAllSongs) {
            for (SongsMenuItem item : this.songMenuItemList) {
                this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                        AppConfig.getProperty("url", this.getContext())
                                + Endpoints.SONG
                                + "/"
                                + item.getSongId()
                )));

                if (item.getSongId().equals(this.initialSongItem.getSongId())) {
                    this.service.getExoPlayer().seekTo(this.songMenuItemList.indexOf(item), 0);
                }

                this.playAudio();
            }
        } else {
            this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                    AppConfig.getProperty("url", this.getContext())
                    + Endpoints.SONG
                    + "/"
                    + this.initialSongItem.getSongId()
            )));

            this.service.playAudio();
        }

        this.displayNotification(this.initialSongItem.getSongName());
    }

    /**
     * If player is not playing, prepares exoplayer and begins playback when ready.
     */
    public void playAudio() {
        if (!this.service.getExoPlayer().isPlaying()) {
            this.service.getExoPlayer().prepare();

            this.service.getExoPlayer().setVolume(1f);
            this.service.getExoPlayer().setPlayWhenReady(true);
        }
    }

    /**
     * Changes the appropriate views to detail the provided song item.
     * @param item SongsMenuItem object with data that should be displayed.
     */
    private void changeSongLayoutInfo(SongsMenuItem item) {
        if (this.getView() != null) {
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
    }

    private void displayNotification(String name) {
        final int notifNameLimit = 30;

        String songNotifName = name;

        if (songNotifName.length() > notifNameLimit) {
            songNotifName = songNotifName.substring(0, notifNameLimit) + "...";
        }

        this.service.displayNotification("Now playing", songNotifName);
    }

    /**
     * Checks whether the constructor's song item list's contents are valid.
     * @return True/false
     */
    private boolean isValidSongItemList() {
        return this.songMenuItemList != null && this.songMenuItemList.size() > 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.emitFragmentEvent("close_bottom_panel", null);
    }

    @Override
    public void onStop() {
        super.onStop();

        final boolean continuePlayingThroughPanel = this.getContext().getSharedPreferences("app", 0).getBoolean(
                AppSettings.CONTINUE_BOTTOM_PANEL_PLAYING.getSettingName(), false
        );

        if (!continuePlayingThroughPanel) {
            this.service.stopPlayer();
        } else {
            this.emitFragmentEvent("display_bottom_panel", this.songMenuItemList);
        }
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
