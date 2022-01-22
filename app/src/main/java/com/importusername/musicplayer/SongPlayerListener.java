package com.importusername.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;

import java.util.List;

public class SongPlayerListener implements Player.Listener {
    private final List<SongsMenuItem> songPlaylist;

    private final Context context;

    private final SongItemService service;

    private final View songView;

    private int skippedMediaIndex; // Used to avoid requesting more songs with the same skip query param twice.

    private TextView titleView;
    private TextView authorView;
    private ImageView thumbnailView;
    private ImageView defaultThumbnailView;

    public SongPlayerListener(List<SongsMenuItem> songPlaylist, Context context, SongItemService service, View songView) {
        this.songPlaylist = songPlaylist;
        this.context = context;
        this.service = service;
        this.songView = songView;
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                        Player.PositionInfo newPosition,
                                        int reason) {
        if (newPosition.mediaItemIndex != oldPosition.mediaItemIndex) {
            // Add more songs if the end of playlist is being reached
            if (newPosition.mediaItemIndex != skippedMediaIndex && ((this.songPlaylist.size() - newPosition.mediaItemIndex) < 11)) {
                int sizeBeforeRequest = this.songPlaylist.size();

                skippedMediaIndex = newPosition.mediaItemIndex;

                final String url = AppConfig.getProperty("url", this.context)
                        + Endpoints.GET_SONGS;

                final BufferSongPlaylistThread bufferSongPlaylistThread = new BufferSongPlaylistThread(
                        url,
                        this.songPlaylist,
                        this.context
                );

                bufferSongPlaylistThread.setQueryLimit(1);

                bufferSongPlaylistThread.setSkip(this.songPlaylist.size());

                bufferSongPlaylistThread.setOnCompleteListener((boolean complete) -> {
                    for (int i = sizeBeforeRequest; i < this.songPlaylist.size(); i++) {
                        final Handler handler = new Handler (
                                this.service.getExoPlayer().getApplicationLooper()
                        );

                        int finalI = i;

                        handler.post(() -> {
                            this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                                    AppConfig.getProperty("url", this.context)
                                            + Endpoints.SONG
                                            + "/"
                                            + this.songPlaylist.get(finalI).getSongId()
                            )));
                        });
                    }
                });

                bufferSongPlaylistThread.start();
            }

            // If media item was changed, change layout data to that of the corresponding song
            this.changeSongLayoutData(this.songPlaylist.get(newPosition.mediaItemIndex));
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
//                if (isPlaying) {
//                    SongFragment.this.displayNotification(
//                            SongFragment.this.songMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
//                    );
//                } else {
//                    SongFragment.this.service.displayNotification("Nothing's playing...", "...");
//                }
    }

    public void setSongLayoutViews(TextView titleView, TextView authorView, ImageView thumbnailView, ImageView defaultThumbnailView) {
        this.titleView = titleView;
        this.authorView = authorView;
        this.thumbnailView = thumbnailView;
        this.defaultThumbnailView = defaultThumbnailView;
    }

    private void changeSongLayoutData(SongsMenuItem item) {
        if (this.songView != null) {
            if (titleView != null) {
                titleView.setText(item.getSongName());
            }

            if (item.getSongThumbnailId() != null) {
                final String url = AppConfig.getProperty("url", this.context)
                        + Endpoints.GET_THUMBNAIL + "/"
                        + item.getSongThumbnailId().split("/")[2];

                final GlideUrl glideUrl = new GlideUrl(
                        url,
                        new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.context)).build()
                );

                if (defaultThumbnailView != null) {
                    defaultThumbnailView.setVisibility(View.GONE);
                }

                if (thumbnailView != null) {
                    thumbnailView.setVisibility(View.VISIBLE);

                    Glide.with(this.context)
                            .load(glideUrl)
                            .into(thumbnailView);
                }
            } else {
                if (thumbnailView != null) {
                    Glide.with(this.context)
                        .clear(thumbnailView);

                    thumbnailView.setVisibility(View.GONE);
                }

                if (defaultThumbnailView != null) {
                    defaultThumbnailView.setVisibility(View.VISIBLE);
                }
            }

            if (item.getAuthor() != null && authorView != null) {
                authorView.setText(item.getAuthor());
            }
        }
    }

    private void bufferPlaylist() {}
}
