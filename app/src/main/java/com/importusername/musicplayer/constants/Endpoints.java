package com.importusername.musicplayer.constants;

/**
 * Class of constant strings representing server endpoints.
 */
public class Endpoints {
    public static final String GET_SONGS = "/song/get-songs";

    public static final String GET_SONG = "/song/get-song";

    public static final String GET_THUMBNAIL = "/song/get-thumbnail";

    public static final String SONG = "/song";

    public static final String DELETE_SONG = "/song/delete";

    public static final String GET_PLAYLIST_THUMBNAIL = "/playlist/get-thumbnail";

    public static final String GET_PLAYLISTS = "/playlist/get-playlists";

    public static final String CREATE_PLAYLIST = "/playlist/create-playlist";

    public static final String DELETE_PLAYLIST = "/playlist/delete-playlist";

    public static final String ADD_SONG_TO_PLAYLIST = "/song/add-to-playlist";

    // Requires query param of playlist id
    public static final String GET_PLAYLIST_SONGS = "/playlist/get-songs";

    public static final String REMOVE_SONG_FROM_PLAYLIST = "/song/remove-from-playlist";
}
