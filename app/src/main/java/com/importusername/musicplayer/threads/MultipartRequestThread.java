package com.importusername.musicplayer.threads;

import android.content.Context;
import android.util.Log;
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;

import java.io.IOException;
import java.io.InputStream;

public class MultipartRequestThread extends Thread {
    private final String url;

    private final boolean authenticate;

    private final Context context;

    private final MultipartRequestEntity multipartRequestEntity;

    private final IHttpRequestAction requestAction;

    public MultipartRequestThread(String url, boolean authenticate, Context context, MultipartRequestEntity multipartRequestEntity, IHttpRequestAction requestAction) {
        this.url = url;
        this.authenticate = authenticate;
        this.context = context;
        this.multipartRequestEntity = multipartRequestEntity;
        this.requestAction = requestAction;
    }

    @Override
    public void run() {
        final MusicPlayerRequest musicPlayerRequest = new MusicPlayerRequest(this.url, this.authenticate, this.context);

        try {
            musicPlayerRequest.multipartRequest(this.multipartRequestEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
