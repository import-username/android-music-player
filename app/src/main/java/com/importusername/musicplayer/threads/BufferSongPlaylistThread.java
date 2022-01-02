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
 * and continuing until the song item with the provided id has been received from query.
 */
public class BufferSongPlaylistThread extends Thread {
    private final String url;

    private final SongsMenuItem targetItem;

    private final List<SongsMenuItem> songsMenuItemList;

    private final Context context;

    private OnCompleteListener onCompleteListener;

    private int skip = 0;

    private int limit = 50;

    private int totalAvailableRows = -1;

    public BufferSongPlaylistThread(String url, SongsMenuItem targetItem, List<SongsMenuItem> songsMenuItemList, Context context) {
        this.url = url;
        this.targetItem = targetItem;
        this.context = context;
        this.songsMenuItemList = songsMenuItemList;
    }

    @Override
    public void run() {
        AtomicBoolean cont = new AtomicBoolean(true);

        while (!this.includesSongItem() && cont.get()) {
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
                            cont.set(false);

                            return;
                        }

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
                        cont.set(false);

                        return;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                cont.set(false);

                return;
            }
        }

        if (this.onCompleteListener != null) {
            this.onCompleteListener.onComplete(cont.get());
        }
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
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
