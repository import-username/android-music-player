package com.importusername.musicplayer.threads;

import android.content.Context;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;

import java.io.IOException;

/**
 * Thread for sending http request to music player server.
 */
public class MusicPlayerRequestThread extends Thread {
    private final String url;

    private Context applicationContext;

    private boolean authenticate;

    private final IHttpRequestAction requestAction;

    /**
     * @param url Url to send request to.
     * @param action
     */
    public MusicPlayerRequestThread(String url, IHttpRequestAction action) {
        this.url = url;
        this.requestAction = action;
    }

    public MusicPlayerRequestThread(String url, Context applicationContext, boolean authenticate, IHttpRequestAction action) {
        this.url = url;
        this.applicationContext = applicationContext;
        this.authenticate = authenticate;
        this.requestAction = action;
    }

    @Override
    public void run() {
        final MusicPlayerRequest musicPlayerRequest;

        if (this.authenticate) {
             musicPlayerRequest = new MusicPlayerRequest(this.url, true, this.applicationContext);
        } else {
            musicPlayerRequest = new MusicPlayerRequest(this.url);
        }

        try {
            musicPlayerRequest.get();

            this.requestAction.requestAction(musicPlayerRequest.getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
