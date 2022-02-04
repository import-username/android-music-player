package com.importusername.musicplayer.adapters.playlistmenu;

import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaylistItem {
    private String playlistId;

    private String playlistName;

    private String playlistThumbnailId;

    private final List<SongsMenuItem> playlistItems = new ArrayList<>();

    public PlaylistItem(String playlistName) {
        this.playlistName = playlistName;
    }

    public PlaylistItem(JSONObject playlistJsonObject) throws JSONException {
        this.populateFieldsWithJson(playlistJsonObject);
    }

    private void populateFieldsWithJson(JSONObject jsonObject) throws JSONException {
        this.setPlaylistName(jsonObject.getString("playlist_title"));
        this.setPlaylistId(jsonObject.getString("id"));
        this.setPlaylistThumbnailId(jsonObject.getString("playlist_thumbnail_path"));
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public void setPlaylistThumbnailId(String thumbnailId) {
        this.playlistThumbnailId = thumbnailId;
    }

    public String getPlaylistId() {
        return this.playlistId;
    }

    public String getPlaylistName() {
        return this.playlistName;
    }

    public String getPlaylistThumbnailId() {
        return this.playlistThumbnailId;
    }

    public void fetchItems() {

    }

    public void addItem(SongsMenuItem songsMenuItem) {
        if (songsMenuItem != null) {
            this.playlistItems.add(songsMenuItem);
        }
    }

    public SongsMenuItem getItemById() {
        return null;
    }

    public List<SongsMenuItem> getPlaylistItems() {
        return this.playlistItems;
    }
}