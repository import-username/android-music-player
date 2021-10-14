package com.importusername.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import com.importusername.musicplayer.activity.MainActivity;
import com.importusername.musicplayer.util.AppConfig;

public class AppURL {
    private static String appUrl;

    public static void loadUrl(Context context) {
        final String property = AppConfig.getProperty("url", context);

        if (property == null) {
            final SharedPreferences prefs = context.getSharedPreferences("app", 0);

            appUrl = prefs.getString("url", null);;
        } else {
            appUrl = property;
        }
    }

    public static String getAppUrl() {
        return appUrl;
    }
}
