package com.importusername.musicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppCookie {
    /**
     * Sets authCookie property in application's shared preferences store.
     * @param cookie Value of cookie returned from server in set-cookie or cookie header.
     * @param context Application context
     */
    public static void setAuthCookie(String cookie, Context context) {
        final SharedPreferences.Editor prefsEditor = context.getSharedPreferences("app", 0).edit();

        prefsEditor.putString("authCookie", cookie);

        prefsEditor.apply();
    }

    /**
     * Retrieves authCookie property from application shared preferences store.
     * @param context Application context
     * @return authCookie property value from shared preferences or auth_cookie from properties file.
     */
    public static String getAuthCookie(Context context) {
        // Read auth_cookie property from config file if it exists
        final String property = AppConfig.getProperty("auth_cookie", context);

        String appUrl;

        // If auth_cookie property is not in properties file, search in shared preferences store.
        if (property == null) {
            final SharedPreferences prefs = context.getSharedPreferences("app", 0);

            appUrl = prefs.getString("authCookie", null);
        } else {
            appUrl = property;
        }

        return appUrl;
    }
}
