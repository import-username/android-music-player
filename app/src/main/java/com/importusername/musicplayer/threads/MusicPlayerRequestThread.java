package com.importusername.musicplayer.threads;

import android.content.Context;
import com.importusername.musicplayer.enums.RequestMethod;
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

    private RequestMethod requestMethod;

    private final IHttpRequestAction requestAction;

    /**
     * @param url Url to send request to.
     * @param action
     */
    public MusicPlayerRequestThread(String url, RequestMethod requestMethod, IHttpRequestAction action) {
        this.url = url;
        this.requestAction = action;
        this.requestMethod = requestMethod;
    }

    public MusicPlayerRequestThread(String url, RequestMethod requestMethod, Context applicationContext, boolean authenticate, IHttpRequestAction action) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.applicationContext = applicationContext;
        this.authenticate = authenticate;
        this.requestAction = action;
    }

    private void sendRequest(MusicPlayerRequest request) throws IOException {
        // TODO - add corresponding request method calls
        switch (this.requestMethod) {
            case GET:
                request.get();
                break;
            case POST:
                break;
            case PATCH:
                break;
            case DELETE:
                break;
        }
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
            this.sendRequest(musicPlayerRequest);

            this.requestAction.requestAction(musicPlayerRequest.getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
