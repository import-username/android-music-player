package com.importusername.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.services.SongItemService;
import com.importusername.musicplayer.threads.BufferSongPlaylistThread;
import com.importusername.musicplayer.util.AppConfig;

import java.util.List;

public class SongListener implements Player.Listener {
    private final List<SongsMenuItem> songsMenuItemList;

    private final SongItemService service;

    private final Context context;

    private final String songsQueryUrl;

    private OnSongChangeListener onSongChangeListener;

    private boolean autoUpdateNotification = true;

    private boolean getNewSongs = true;

    // Used to avoid requesting more songs with the same skip query param twice.
    private int skippedMediaIndex;

    public SongListener(String songsQueryUrl, List<SongsMenuItem> songsMenuItemList, SongItemService service, Context context) {
        this.songsQueryUrl = songsQueryUrl;
        this.songsMenuItemList = songsMenuItemList;
        this.service = service;
        this.context = context;
    }

    public void setOnSongChangeListener(OnSongChangeListener onSongChangeListener) {
        this.onSongChangeListener = onSongChangeListener;
    }

    /**
     * Should notification be changed automatically when song changes.
     * Default: true
     * @param autoChangeNotification
     */
    public void setAutoChangeNotification(boolean autoChangeNotification) {
        this.autoUpdateNotification = autoChangeNotification;
    }

    /**
     * Should {@link SongListener#songsMenuItemList} be rebuffered with more songs (if available) when
     * end of playlist is being reached. Default: true
     * @param getNewSongs true/false
     */
    public void setGetNewSongs(boolean getNewSongs) {
        this.getNewSongs = getNewSongs;
    }

    private void bufferPlaylist() {
        int sizeBeforeRequest = this.songsMenuItemList.size();

        final BufferSongPlaylistThread bufferSongPlaylistThread = new BufferSongPlaylistThread(
                this.songsQueryUrl,
                this.songsMenuItemList,
                this.context
        );

        bufferSongPlaylistThread.setQueryLimit(1);

        bufferSongPlaylistThread.setSkip(this.songsMenuItemList.size());

        bufferSongPlaylistThread.setOnCompleteListener((boolean complete) -> {
            for (int i = sizeBeforeRequest; i < this.songsMenuItemList.size(); i++) {
                final Handler handler = new Handler (
                        this.service.getExoPlayer().getApplicationLooper()
                );

                int finalI = i;

                handler.post(() -> {
                    this.service.getExoPlayer().addMediaItem(MediaItem.fromUri(Uri.parse(
                            AppConfig.getProperty("url", this.context)
                                    + Endpoints.SONG
                                    + "/"
                                    + this.songsMenuItemList.get(finalI).getSongId()
                    )));
                });
            }
        });

        bufferSongPlaylistThread.start();
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                        Player.PositionInfo newPosition,
                                        int reason) {
        if (newPosition.mediaItemIndex != oldPosition.mediaItemIndex) {
            // Add more songs if the end of playlist is being reached
            if (newPosition.mediaItemIndex != skippedMediaIndex && ((this.songsMenuItemList.size() - newPosition.mediaItemIndex) < 11)) {
                skippedMediaIndex = newPosition.mediaItemIndex;

                if (this.getNewSongs) {
                    this.bufferPlaylist();
                }
            }

            if (this.onSongChangeListener != null) {
                this.onSongChangeListener.change(this.songsMenuItemList.get(newPosition.mediaItemIndex));

                if (this.autoUpdateNotification) {
                    Log.i("SONG LISTENER", "Changing notification for song: " + this.songsMenuItemList.get(newPosition.mediaItemIndex).getSongName());

                    this.service.displayNotification(
                            "Now playing: ",
                            this.songsMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
                    );
                }
            }
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        this.service.displayNotification(
                "Now playing: ",
                this.songsMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
        );

        this.onSongChangeListener.change(this.songsMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()));
    }

    /**
     * Function that will be called every time the song changes.
     * Might be used for changing menu layout, updating notification, etc.
     */
    public interface OnSongChangeListener {
        void change(SongsMenuItem songsMenuItem);
    }
}
