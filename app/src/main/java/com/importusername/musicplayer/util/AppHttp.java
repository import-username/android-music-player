package com.importusername.musicplayer.util;

import android.net.Uri;

import java.util.HashMap;

public class AppHttp {
    public static HashMap<String, String> getQueryParams(Uri uri) {
        final HashMap<String, String> queryParams = new HashMap<>();

        final String queryString = uri.getQuery();

        if (queryString != null) {
            for (String keyValuePair : queryString.split("&")) {
                queryParams.put(keyValuePair.split("=")[0], keyValuePair.split("=")[1]);
            }
        }

        return queryParams;
    }
}
