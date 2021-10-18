package com.importusername.musicplayer.http;

import androidx.annotation.Nullable;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for sending requests to music player server.
 */
public class MusicPlayerRequest {
    private final String url;

    private int responseStatus;

    private String response;

    private Map<String, List<String>> responseHeaders;

    public MusicPlayerRequest(String url) {
        this.url = url;
    }

    /**
     * Sends GET request to server.
     * @param headers HashMap object representing headers to put on request.
     * @throws IOException
     */
    public void get(Map<String, String> headers) throws IOException {
        // Create URL object from url field
        final URL url = new URL(this.url);

        // Open http connection, allowing input from server to client and setting method as GET
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");

        // Add headers to request
        for (String header : headers.keySet()) {
            urlConnection.setRequestProperty(header, headers.get(header));
        }

        // Send request
        urlConnection.connect();

        // Get response status code
        this.responseStatus = urlConnection.getResponseCode();

        // Set response headers field.
        this.responseHeaders = urlConnection.getHeaderFields();

        // Read response message
        this.readHttpResponseBody(urlConnection.getInputStream());

        // Close connection
        urlConnection.disconnect();
    }

    /**
     * Sends GET request to server.
     * @throws IOException
     */
    public void get() throws IOException {
        // Create URL object from url field
        final URL url = new URL(this.url);

        // Open http connection, allowing input from server to client and setting method as GET
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");

        // Send request
        urlConnection.connect();

        // Get response status code
        this.responseStatus = urlConnection.getResponseCode();

        // Set response headers field.
        this.responseHeaders = urlConnection.getHeaderFields();

        // Read response message
        this.readHttpResponseBody(urlConnection.getInputStream());

        // Close connection
        urlConnection.disconnect();
    }

    /**
     * Reads request response body into response field.
     * @param responseStream InputStream from HttpURLConnection object..
     * @throws IOException
     */
    private void readHttpResponseBody(InputStream responseStream) throws IOException {
        final BufferedInputStream bufferedResponseStream = new BufferedInputStream(responseStream);

        final StringBuilder responseString = new StringBuilder();

        int result;

        while ((result = bufferedResponseStream.read()) != -1) {
            responseString.append((char) result);
        }

        this.response = responseString.toString();
    }

    /**
     * Returns request status code.
     * @return int status
     */
    public int getStatus() {
        return this.responseStatus;
    }

    /**
     * Returns request response string.
     * @return response string
     */
    public String getResponse() {
        return this.response;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.responseHeaders;
    }
}
