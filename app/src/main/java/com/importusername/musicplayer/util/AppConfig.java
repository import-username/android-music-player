package com.importusername.musicplayer.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for getting properties from config.properties asset file.
 */
public class AppConfig {
    /**
     * Loads config.properties inputstream from asset manager object's open method.
     * @param key The property key to get from config.properties.
     * @param ctx Application context from activity.
     * @return Property value or null if it doesn't exist.
     * @throws IOException
     */
    public static String getProperty(String key, Context ctx) throws IOException {
        final Properties properties = new Properties();

        final AssetManager assetManager = ctx.getAssets();

        properties.load(assetManager.open("config.properties"));

        return properties.getProperty(key);
    }
}
