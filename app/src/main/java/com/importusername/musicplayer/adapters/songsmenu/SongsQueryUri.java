package com.importusername.musicplayer.adapters.songsmenu;

import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SongsQueryUri {
    private String songQueryHost;

    private final Map<String, String> queryParams = new HashMap<>();

    /**
     * Sets the query url's host. Should include protocol/scheme.
     * @param uri Uri object of url to be queried for song data.
     */
    public void setSongQueryHost(Uri uri) {
        String protocol = "";
        String host = "";

        if (uri.getScheme() != null) {
            protocol = uri.getScheme() + "://";
        }

        if (uri.getHost() != null) {
            host = uri.getHost();

            if (uri.getPort() != -1) {
                host += (":" + uri.getPort());
            }
        }

        if (uri.getPath() != null) {
            host += uri.getPath();
        }

        if (uri.getQuery() != null) {
            for (String pairString : uri.getQuery().split("&")) {
                final String[] queryPair = pairString.split("=");
                this.addQueryParam(queryPair[0], queryPair[1]);
            }
        }

        this.songQueryHost = protocol + host;
    }

    public SongsQueryUri addQueryParam(String key, String value) {
        key = key.replace(" ", "%20");

        value = value.replace(" ", "%20");

        this.queryParams.put(key, value);

        return this;
    }

    public String getSongQueryString() {
        StringBuilder queryStringBuilder = new StringBuilder();
        String queryString = "";

        if (this.queryParams.size() > 0) {
            queryStringBuilder.append("?");

            for (String key : this.queryParams.keySet()) {
                queryStringBuilder.append(key).append("=").append(this.queryParams.get(key)).append("&");
            }

            queryString = queryStringBuilder.substring(0, queryStringBuilder.length() - 1);
        }

        return queryString;
    }

    public String getSongQueryUrl() {
        return this.songQueryHost + this.getSongQueryString();
    }
}
