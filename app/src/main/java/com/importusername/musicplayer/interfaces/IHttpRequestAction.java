package com.importusername.musicplayer.interfaces;

/**
 * Functional interface used to perform action after request to music player server has been made.
 */
public interface IHttpRequestAction {
    /**
     * @param status Response status code from http request.
     */
    void requestAction(int status, String response);
}
