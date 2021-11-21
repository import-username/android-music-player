package com.importusername.musicplayer.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import androidx.fragment.app.FragmentActivity;
import com.importusername.musicplayer.interfaces.IThumbnailRequestAction;
import com.importusername.musicplayer.util.AppCookie;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ThumbnailRequestThread extends Thread {
    private final String url;

    private final Context context;

    private final IThumbnailRequestAction requestAction;

    public ThumbnailRequestThread(String url, Context context, IThumbnailRequestAction requestAction) {
        this.url = url;
        this.context = context;
        this.requestAction = requestAction;
    }

    @Override
    public void run() {
        try {
            final URL url = new URL(this.url);

            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Cookie", AppCookie.getAuthCookie(this.context));

            BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int i = 0;
            while (i != -1) {
                i = inputStream.read();

                outputStream.write(i);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

            this.requestAction.requestAction(byteArrayInputStream);

            urlConnection.disconnect();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}
