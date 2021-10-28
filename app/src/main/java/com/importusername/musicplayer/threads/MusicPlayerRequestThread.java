package com.importusername.musicplayer.threads;

import android.content.Context;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.http.HttpBody;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Thread for sending http request to music player server.
 */
public class MusicPlayerRequestThread extends Thread {
    private final String url;

    private Context applicationContext;

    private boolean authenticate;

    private RequestMethod requestMethod;

    private final IHttpRequestAction requestAction;

    private HttpBody body;

    private Map<String, String> headers;

    /**
     * @param url Url to send request to.
     * @param requestMethod Request method enum
     * @param applicationContext Application context object.
     * @param authenticate Boolean value for if request should be authenticated.
     * @param action Method to execute when request is finished.
     */
    public MusicPlayerRequestThread(String url, RequestMethod requestMethod, Context applicationContext, boolean authenticate, IHttpRequestAction action) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.applicationContext = applicationContext;
        this.authenticate = authenticate;
        this.requestAction = action;
    }

    /**
     * Calls MusicPlayerRequest method corresponding to the request method.
     * @param request MusicPlayerRequest object.
     * @throws IOException
     */
    private void sendRequest(MusicPlayerRequest request) throws IOException {
        // TODO - add corresponding request method calls
        switch (this.requestMethod) {
            case GET:
                request.getRequest();
                break;
            case POST:
                request.postRequest();
                break;
            case PATCH:
                break;
            case DELETE:
                break;
        }
    }

    public void setBody(HttpBody body) {
        this.body = body;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void run() {
        final MusicPlayerRequest musicPlayerRequest = new MusicPlayerRequest(this.url, this.authenticate, this.applicationContext);

        if (this.body != null) {
            musicPlayerRequest.setRequestBody(this.body);
        }

        if (this.headers != null) {
            musicPlayerRequest.setHeaders(this.headers);
        }

        try {
            this.sendRequest(musicPlayerRequest);

            this.requestAction.requestAction(musicPlayerRequest.getStatus(), musicPlayerRequest.getResponse(), musicPlayerRequest.getResponseHeaders());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
