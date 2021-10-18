package com.importusername.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import com.importusername.musicplayer.util.AppConfig;

public class AppURL {
    public static void setAppUrl(String url, Context context) {
        final SharedPreferences.Editor prefsEditor = context.getSharedPreferences("app", 0).edit();

        prefsEditor.putString("url", url);

        prefsEditor.apply();
    }

    public static String getAppUrl(Context context) {
        final String property = AppConfig.getProperty("url", context);

        String appUrl;

        if (property == null) {
            final SharedPreferences prefs = context.getSharedPreferences("app", 0);

            appUrl = prefs.getString("url", null);
        } else {
            appUrl = property;
        }

        return appUrl;
    }
}
