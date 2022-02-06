package com.importusername.musicplayer.adapters.songsmenu;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Data structure representing a song item. Intended to be used with the songs menu list adapter.
 */
public class SongsMenuItem {
    private String songId;

    private String songName;

    private String songFileId;

    private String songThumbnailId;

    private String songDescription;

    private String author;

    private boolean favorite = false;

    public SongsMenuItem(String songName) {
        this.songName = songName;
    }

    /**
     * Constructor accepting a json object representing a song item.
     * Automatically populates all available fields.
     * @param songJsonObject Song json object
     * @throws JSONException If json parsing encounters an issue.
     */
    public SongsMenuItem(JSONObject songJsonObject) throws JSONException {
        this.populateFieldsWithJson(songJsonObject);
    }

    private void populateFieldsWithJson(JSONObject songObject) throws JSONException {
        this.setSongName(songObject.getString("song_title"));
        this.setAuthor(songObject.getString("song_author"));
        this.setSongFileId(songObject.getString("song_file_path"));
        this.setSongThumbnailId(songObject.getString("song_thumbnail_path"));
        this.setSongDescription(songObject.getString("song_description"));
        this.setFavorite(songObject.getString("song_favorite"));
        this.setSongId(songObject.getString("id"));
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setSongFileId(String songFileId) {
        this.songFileId = songFileId;
    }

    public void setSongThumbnailId(String songThumbnailId) {
        this.songThumbnailId = songThumbnailId;
    }

    public void setSongDescription(String songDescription) {
        this.songDescription = songDescription;
    }

    public void setFavorite(String songFavorite) {
        if (songFavorite.equals("true") || songFavorite.equals("t") || songFavorite.equals("TRUE")) {
            this.favorite = true;
        } else if (songFavorite.equals("false") || songFavorite.equals("f") || songFavorite.equals("FALSE")) {
            this.favorite = false;
        }
    }

    public String getSongId() {
        return this.getNullableValue(this.songId);
    }

    public String getSongName() {
        return this.getNullableValue(this.songName);
    }

    public String getSongFileId() {
        return this.getNullableValue(this.songFileId);
    }

    public String getSongThumbnailId() {
        return this.getNullableValue(this.songThumbnailId);
    }

    public String getSongDescription() {
        return this.getNullableValue(this.songDescription);
    }

    public String getAuthor() {
        return this.getNullableValue(this.author);
    }

    public boolean getFavorite() {
        return this.favorite;
    }

    private String getNullableValue(String value) {
        if (value == null || value.equals("NULL") || value.equals("null")) {
            return null;
        }

        return value;
    }
//    private void getInitializedFields() {
//        Field[] fields = this.getClass().getDeclaredFields();
//        for (int i = 0; i < fields.length; i++) {
//            try {
//                Log.d("SongsMenuItem", fields[i].get(this) + "");
//            } catch (IllegalAccessException exc) {
//                exc.printStackTrace();
//            }
//        }
//    }
}
