package com.importusername.musicplayer.threads;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryUri;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This thread will be used to fetch song items from database in intervals of 50, placing them into the provided array,
 * and continuing until the song item with the provided id has been received from query, or until response contains no
 * items if target song is not provided.
 */
public class BufferSongPlaylistThread extends Thread {
    private boolean started = false;

    private final String url;

    private SongsMenuItem targetItem;

    private final List<SongsMenuItem> songsMenuItemList;

    private final Context context;

    private OnCompleteListener onCompleteListener;

    private int skip = 0;

    private int limit = 50;

    private int queryLimit = -1;

    private int totalAvailableRows = -1;

    public BufferSongPlaylistThread(String url, List<SongsMenuItem> songsMenuItemList, Context context) {
        this.url = url;
        this.context = context;
        this.songsMenuItemList = songsMenuItemList;
    }

    /**
     * Once the thread gets a http response with a song item with the same id as the provided song item object,
     * further requests will cease.
     * Must be called before {@link BufferSongPlaylistThread#start()}
     * @param songsMenuItem The song item which should determine request loop's termination.
     */
    public void setTargetSongItem(SongsMenuItem songsMenuItem) {
        if (!this.started)
            this.targetItem = songsMenuItem;
    }

    /**
     * Sets how many items should be skipped over in query request.
     * @param skip Number of items to skip.
     * Must be called before {@link BufferSongPlaylistThread#start()}
     */
    public void setSkip(int skip) {
        if (!this.started)
            this.skip = skip;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Sets the maximum number of queries to perform before stopping.
     * Can not be called if {@link BufferSongPlaylistThread#setTargetSongItem(SongsMenuItem)} has been called.
     * Must be called before {@link BufferSongPlaylistThread#start()}
     * @param queryLimit Number of queries to perform.
     */
    public void setQueryLimit(int queryLimit) {
        if (!this.started && this.targetItem == null)
            this.queryLimit = queryLimit;
    }

    @Override
    public synchronized void start() {
        super.start();

        this.started = true;
    }

    @Override
    public void run() {
        AtomicBoolean cont = new AtomicBoolean(true);

        if (this.targetItem != null) {
            // Loop should continue until a response contains the target item.
            while (!this.includesSongItem() && cont.get()) {
                this.bufferArray(cont);
            }
        } else if (this.queryLimit != -1) {
            // Loop should continue until n number of queries has been made.
            int queries = 0;
            while ((queries < this.queryLimit) && cont.get()) {
                this.bufferArray(cont);

                queries++;
            }
        } else {
            // Loop should continue until responses contain 0 items.
            while (cont.get()) {
                this.bufferArray(cont);
            }
        }

        if (this.onCompleteListener != null) {
            // TODO - i think cont is always false here, it's useless
            this.onCompleteListener.onComplete(cont.get());
        }
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    /**
     * Sends get-songs request and adds to array.
     * @param continueLoop Used to stop enclosing while loop.
     */
    private void bufferArray(AtomicBoolean continueLoop) {
        if ((this.totalAvailableRows == -1) || (this.songsMenuItemList.size() < this.totalAvailableRows)) {
            try {
                final SongsQueryUri songsQueryUri = new SongsQueryUri();
                songsQueryUri.setSongQueryHost(Uri.parse(this.url));
                songsQueryUri.addQueryParam("skip", this.skip + "");
                songsQueryUri.addQueryParam("limit", this.limit + "");

                MusicPlayerRequest musicPlayerRequest = new MusicPlayerRequest(
                        songsQueryUri.getSongQueryUrl(),
                        true,
                        this.context
                );

                musicPlayerRequest.getRequest();

                if (musicPlayerRequest.getStatus() > 199 && musicPlayerRequest.getStatus() < 300) {
                    final String response = musicPlayerRequest.getResponse();
                    final JSONObject jsonResponse = new JSONObject(response);
                    final JSONArray rowsArray = jsonResponse.getJSONArray("rows");

                    if (rowsArray.length() < 1) {
                        continueLoop.set(false);

                        return;
                    }

                    // TODO - do something with totoalRows variable
                    int totalRows = -1;

                    try {
                        totalRows = jsonResponse.getInt("total");
                    } finally {
                        this.skip += rowsArray.length();

                        for (int i = 0; i < rowsArray.length(); i++) {
                            this.songsMenuItemList.add(new SongsMenuItem(rowsArray.getJSONObject(i)));
                        }
                    }
                } else {
                    // TODO - call a function here for error handling
                    continueLoop.set(false);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            continueLoop.set(false);
        }
    }

    private boolean includesSongItem() {
        for (SongsMenuItem item : this.songsMenuItemList) {
            if (item.getSongId().equals(this.targetItem.getSongId())) {
                return true;
            }
        }

        return false;
    }

    public interface OnCompleteListener {
        void onComplete(boolean complete);
    }
}
