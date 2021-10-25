package com.importusername.musicplayer.http;

import com.importusername.musicplayer.enums.RequestContentType;
import org.json.JSONObject;

/**
 * Class for constructing an http body object.
 * Intended to be used in MusicPlayerRequest class for http requests.
 */
public class HttpBody {
    // Request body content type for content-type header
    private RequestContentType contentType;

    private Object body;

    /**
     * Sets body field to jsonobject.
     * @param jsonBody Json object.
     */
    public void setBody(JSONObject jsonBody) {
        this.contentType = RequestContentType.JSON;

        this.body = jsonBody;
    }

    /**
     * Sets body field to plain text.
     * @param plainTextBody Plain text string.
     */
    public void setBody(String plainTextBody) {
        this.contentType = RequestContentType.TEXT;

        this.body = plainTextBody;
    }

    /**
     * @return Body object.
     */
    public Object getBody() {
        return this.body;
    }

    /**
     * @return HttpBody content type.
     */
    public RequestContentType getContentType() {
        return this.contentType;
    }
}
