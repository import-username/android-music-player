package com.importusername.musicplayer.adapters.songsmenu;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.OnSongQueryListener;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SongsQueryEntity {
    private int skip = 0;

    private int limit = 12;

    /**
     * Resets the skip and limit counts to original value.
     */
    public void reset() {
        this.skip = 0;
        this.limit = 12;
    }

    public void queryNextSong(Context context, SongsQueryUri songsQueryUri, OnSongQueryListener onSongQueryListener) {
        songsQueryUri.addQueryParam("skip", this.skip + "");
        songsQueryUri.addQueryParam("limit", this.limit + "");

        MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                songsQueryUri.getSongQueryUrl(),
                RequestMethod.GET,
                context,
                true,
                (status, response, headers) -> {
                    try {
                        if (status > 199 && status < 300) {
                            final JSONObject responseObject = new JSONObject(response);
                            final JSONArray rowsArray = responseObject.getJSONArray("rows");

                            int totalRows = -1;

                            try {
                                 totalRows = responseObject.getInt("total");
                            } finally {
                                this.skip += rowsArray.length();

                                onSongQueryListener.onSongQuery(rowsArray, totalRows);
                            }
                        } else {
                            onSongQueryListener.onSongQuery(new JSONArray(), -1);
                        }
                    } catch (JSONException exc) {
                        exc.printStackTrace();

                        onSongQueryListener.onSongQuery(null, -1);
                    }
                }
        );

        requestThread.start();
    }
}
