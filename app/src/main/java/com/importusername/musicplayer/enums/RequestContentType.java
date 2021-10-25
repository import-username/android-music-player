package com.importusername.musicplayer.enums;

/**
 * Content type constants for http requests.
 */
public enum RequestContentType {
    TEXT("text/plain"),
    JSON("application/json");

    private final String contentType;

    RequestContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns content type value to be used in http header Content-Type.
     * @return contentType string field.
     */
    public String getContentType() {
        return this.contentType;
    }
}
