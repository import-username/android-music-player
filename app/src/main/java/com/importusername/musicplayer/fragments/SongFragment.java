package com.importusername.musicplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
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
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SongFragment extends Fragment implements IBackPressFragment {
    private SongsMenuItem songsMenuItem;

    private List<SongsMenuItem> songsMenuItemList;

    private SongItemService service;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongFragment.this.service = ((SongItemService.LocalBinder) service).getService();

            final PlayerControlView controlView = getView().findViewById(R.id.player_view);

            controlView.setShowNextButton(true);
            controlView.setShowPreviousButton(true);
            controlView.setShowShuffleButton(true);

            // TODO - add both types of infinitely repeating toggle mode
            controlView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL);

            controlView.setShowTimeoutMs(0);

            controlView.setPlayer(SongFragment.this.service.getExoPlayer());

            SongFragment.this.addSongItems(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            SongFragment.this.service = null;
        }
    };

    public SongFragment() {
        super(R.layout.song_menu_layout);
    }

    public SongFragment(SongsMenuItem songsMenuItem) {
        super(R.layout.song_menu_layout);

        this.songsMenuItem = songsMenuItem;
    }

    /**
     *
     * @param startingItem The songmenuitem which should be played with initial priority.
     * @param songsMenuItemList List of songmenuitem objects to pass into exoplayer playlist.
     */
    public SongFragment(SongsMenuItem startingItem, List<SongsMenuItem> songsMenuItemList) {
        super(R.layout.song_menu_layout);

        this.songsMenuItemList = songsMenuItemList;
        this.songsMenuItem = startingItem;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.song_menu_layout, container, false);

        ((TextView) view.findViewById(R.id.song_menu_title_view)).setText(this.songsMenuItem.getSongName());

        if (this.songsMenuItem.getSongThumbnailId() != null) {
            final String url = AppConfig.getProperty("url", view.getContext())
                    + Endpoints.GET_THUMBNAIL + "/"
                    + this.songsMenuItem.getSongThumbnailId().split("/")[2];

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

        if (this.songsMenuItem.getAuthor() != null) {
            ((TextView) view.findViewById(R.id.song_menu_author_view)).setText(this.songsMenuItem.getAuthor());
        }

        this.bindSongItemService();

        return view;
    }

    /**
     * Adds single song item/iteratively add from list depending on value passed in constructor.
     * @param autoPlayAudio Boolean value to determine if audio should be played when all media items have been added.
     */
    private void addSongItems(boolean autoPlayAudio) {
        final ExoPlayer exoPlayer = this.service.getExoPlayer();

        // TODO - handle non 2xx status codes
        if (this.songsMenuItemList == null) {
            final Uri uri = Uri.parse(AppConfig.getProperty("url", this.getContext()) + "/song/" + this.songsMenuItem.getSongId());

            MediaItem songItem = MediaItem.fromUri(uri);

            exoPlayer.setMediaItem(songItem);
        } else {
            for (SongsMenuItem item : this.songsMenuItemList) {
                if (item != null) {
                    final Uri uri = Uri.parse(AppConfig.getProperty("url", this.getContext()) + "/song/" + item.getSongId());

                    exoPlayer.addMediaItem(MediaItem.fromUri(uri));

                    if (item.equals(this.songsMenuItem)) {
                        exoPlayer.seekTo(this.songsMenuItemList.indexOf(item), 0);
                    }
                }
            }
        }

        if (autoPlayAudio) {
            this.playAudio();
        }
    }

    /**
     * Prepares exoplayer object and plays when ready.
     */
    private void playAudio() {
        final ExoPlayer exoPlayer = this.service.getExoPlayer();

        if (!exoPlayer.isPlaying()) {
            exoPlayer.prepare();

            exoPlayer.setPlaybackSpeed(1);
            exoPlayer.setVolume(1f);
            exoPlayer.setPlayWhenReady(true);
        }
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
        return true;
    }
}
