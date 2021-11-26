package com.importusername.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SongFragment extends Fragment implements IBackPressFragment {
    private ExoPlayer exoPlayer;

    private SongsMenuItem songsMenuItem;

    public SongFragment() {
        super(R.layout.song_menu_layout);
    }

    public SongFragment(SongsMenuItem songsMenuItem) {
        super(R.layout.song_menu_layout);

        this.songsMenuItem = songsMenuItem;
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

        if (exoPlayer == null) {
            final HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", AppCookie.getAuthCookie(this.getContext()));

            final DefaultHttpDataSource.Factory defaultHttpDataSource = new DefaultHttpDataSource.Factory();
            defaultHttpDataSource.setDefaultRequestProperties(headers);

            final DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this.getContext());
            defaultRenderersFactory.setEnableAudioTrackPlaybackParams(true);

            final DefaultLoadControl loadControl = new DefaultLoadControl();
            loadControl.onPrepared();

            exoPlayer = new ExoPlayer.Builder(this.getContext())
                .setMediaSourceFactory(new DefaultMediaSourceFactory(defaultHttpDataSource))
                .build();

            final PlayerControlView controlView = ((PlayerControlView) view.findViewById(R.id.player_view));

            controlView.setShowNextButton(true);
            controlView.setShowPreviousButton(true);
            controlView.setShowShuffleButton(true);
//            controlView.setRepeatToggleModes();

            controlView.setShowTimeoutMs(0);

            ((PlayerControlView) view.findViewById(R.id.player_view)).setPlayer(exoPlayer);
        }

        this.playAudio();

        return view;
    }

    private void playAudio() {
        // TODO - handle non 2xx status codes
        final Uri uri = Uri.parse(AppConfig.getProperty("url", this.getContext()) + "/song/" + this.songsMenuItem.getSongId());

        MediaItem songItem = MediaItem.fromUri(uri);

        exoPlayer.setMediaItem(songItem);

        if (!this.exoPlayer.isPlaying()) {
            exoPlayer.prepare();

            exoPlayer.setPlaybackSpeed(1);
            exoPlayer.setVolume(0.4f);
            exoPlayer.play();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.exoPlayer != null) {
            this.exoPlayer.stop();
            this.exoPlayer.release();
        }
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
