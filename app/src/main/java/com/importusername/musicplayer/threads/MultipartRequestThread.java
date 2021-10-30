package com.importusername.musicplayer.threads;

import android.content.Context;
import android.util.Log;
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.http.MusicPlayerRequest;

import java.io.IOException;
import java.io.InputStream;

public class MultipartRequestThread extends Thread {
    private final String url;

    private final boolean authenticate;

    private final Context context;

    private final InputStream contentStream;

    private final String fieldname;

    private final String filename;

    public MultipartRequestThread(String url, boolean authenticate, Context context, InputStream contentStream, String fieldname, String filename) {
        this.url = url;
        this.authenticate = authenticate;
        this.context = context;
        this.contentStream = contentStream;
        this.fieldname = fieldname;
        this.filename = filename;
    }

    @Override
    public void run() {
        final MusicPlayerRequest musicPlayerRequest = new MusicPlayerRequest(this.url, this.authenticate, this.context);

        try {
            musicPlayerRequest.multipartRequest(this.contentStream, this.fieldname, this.filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
