package com.importusername.musicplayer.interfaces;

import org.json.JSONArray;

/**
 * Function to be called when song query request in
 * {@link com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity} is completed.
 */
public interface OnSongQueryListener {
    /**
     * @param jsonArray Array of json objects representing individual song objects/rows.
     * @param total Total number of songs available with provided query string. -1 if null.
     */
    void onSongQuery(JSONArray jsonArray, int total);
}
