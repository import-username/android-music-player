package com.importusername.musicplayer.http;

import android.content.Context;
import android.util.Log;
import com.importusername.musicplayer.enums.RequestContentType;
import com.importusername.musicplayer.util.AppCookie;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for sending requests to music player server.
 */
public class MusicPlayerRequest {
    private final String url;
    private final boolean authenticate;
    private final Context applicationContext;

    private final Map<String, String> requestHeaders;

    private Object requestBody;

    private RequestContentType contentType;

    private int responseStatus;

    private String response;

    private Map<String, List<String>> responseHeaders;

    private int connectionTimeout = 10000;

    /**
     * Initializes instance fields and calls setAuthCookie method.
     * @param url Url to send request to. Should include protocol, port (if necessary), endpoint, url param, and query params.
     * @param authenticate Boolean value for whether cookie header should be sent with request.
     * @param applicationContext Android app context object.
     */
    public MusicPlayerRequest(String url, boolean authenticate, Context applicationContext) {
        this.url = url;
        this.authenticate = authenticate;
        this.applicationContext = applicationContext;
        this.requestHeaders = new HashMap<>();
        this.setAuthCookie(applicationContext);
    }

    /**
     * Sets auth cookie header in headers hashmap if authenticate is true.
     */
    private void setAuthCookie(Context context) {
        if (this.authenticate) {
            this.requestHeaders.put("Cookie", AppCookie.getAuthCookie(context));
        }
    }

    /**
     * Iteratively adds headers to requestHeaders map.
     * @param headers Map of key value pairs representing http headers.
     */
    public void setHeaders(Map<String, String> headers) {
        for (String header : headers.keySet()) {
            // Do not allow Cookie header to be added from headers map.
            // Instead, instantiate class with authenticate boolean = true.
            if (!header.equals("Cookie")) {
                this.requestHeaders.put(header, headers.get(header));
            }
        }
    }

    /**
     * Adds a single header.
     * @param name Header name
     * @param value Header value
     * @return Object which method was called on for method chaining.
     */
    public MusicPlayerRequest setHeader(String name, String value) {
        if (!name.equals("Cookie")) {
            this.requestHeaders.put(name, value);
        }

        return this;
    }

    /**
     * Sets duration before timeout for http request.
     * @param timeout Timeout duration in milliseconds.
     */
    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
    }

    /**
     * Sets httpBody field.
     * @param httpBody HttpBody object.
     */
    public void setRequestBody(HttpBody httpBody) {
        if (httpBody.getBody() != null && httpBody.getContentType() != null) {
            this.contentType = httpBody.getContentType();

            this.requestHeaders.put("Content-Type", this.contentType.getContentType());

            this.requestBody = httpBody.getBody();
        }
    }

    /**
     * Sends GET request to server.
     * @throws IOException
     */
    public void getRequest() throws IOException {
        // Create URL object from url field
        URL url = new URL(this.url);

        // Open http connection, allowing input from server to client and setting method as GET
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(this.connectionTimeout);

        this.addRequestHeaders(urlConnection);

        // Send request
        urlConnection.connect();

        // Get response status code
        this.responseStatus = urlConnection.getResponseCode();

        // Set response headers field.
        this.responseHeaders = urlConnection.getHeaderFields();

        // Read response message
        if (urlConnection.getResponseCode() != 200) {
            this.readHttpResponseBody(urlConnection.getErrorStream());
        } else {
            this.readHttpResponseBody(urlConnection.getInputStream());
        }

        // Close connection
        urlConnection.disconnect();
    }

    /**
     * Sends post request to server.
     * @throws IOException
     */
    public void postRequest() throws IOException {
        URL url = new URL(this.url);

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setConnectTimeout(this.connectionTimeout);

        urlConnection.setRequestMethod("POST");

        this.addRequestHeaders(urlConnection);
        this.addRequestBody(urlConnection, true);

        urlConnection.connect();

        this.responseStatus = urlConnection.getResponseCode();
        this.responseHeaders = urlConnection.getHeaderFields();
        if (urlConnection.getResponseCode() != 200) {
            this.readHttpResponseBody(urlConnection.getErrorStream());
        } else {
            this.readHttpResponseBody(urlConnection.getInputStream());
        }
        urlConnection.disconnect();
    }

    public void deleteRequest() throws IOException {
        URL url = new URL(this.url);

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setConnectTimeout(this.connectionTimeout);

        urlConnection.setRequestMethod("DELETE");

        this.addRequestHeaders(urlConnection);
        this.addRequestBody(urlConnection, true);

        urlConnection.connect();

        this.responseStatus = urlConnection.getResponseCode();
        this.responseHeaders = urlConnection.getHeaderFields();
        if (urlConnection.getResponseCode() != 200) {
            this.readHttpResponseBody(urlConnection.getErrorStream());
        } else {
            this.readHttpResponseBody(urlConnection.getInputStream());
        }
        urlConnection.disconnect();
    }

    public void patchRequest() throws IOException {
        URL url = new URL(this.url);

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setConnectTimeout(this.connectionTimeout);

        urlConnection.setRequestMethod("PATCH");

        this.addRequestHeaders(urlConnection);
        this.addRequestBody(urlConnection, true);

        urlConnection.connect();

        this.responseStatus = urlConnection.getResponseCode();
        this.responseHeaders = urlConnection.getHeaderFields();
        if (urlConnection.getResponseCode() != 200) {
            this.readHttpResponseBody(urlConnection.getErrorStream());
        } else {
            this.readHttpResponseBody(urlConnection.getInputStream());
        }
        urlConnection.disconnect();
    }

    /**
     * Sends a post request with content-type multipart.
     * @param multipartRequestEntity Multipart request entity object to read data from.
     * @throws IOException
     */
    public void multipartRequest(MultipartRequestEntity multipartRequestEntity) throws IOException {
        final URL url = new URL(this.url);

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", multipartRequestEntity.getBoundary()));
        urlConnection.setRequestProperty("Connection", "Keep-Alive");

        urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");

        this.addRequestHeaders(urlConnection);

        urlConnection.connect();

        final DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());

        multipartRequestEntity.writeMultipartData(outputStream, true);

        this.responseStatus = urlConnection.getResponseCode();
        this.responseHeaders = urlConnection.getHeaderFields();

        if (urlConnection.getResponseCode() != 200) {
            this.readHttpResponseBody(urlConnection.getErrorStream());
        } else {
            this.readHttpResponseBody(urlConnection.getInputStream());
        }

        urlConnection.disconnect();
    }

    /**
     * Adds request property to HttpURLConnection object for every header in map object.
     * @param connection HttpURLConnection object.
     */
    private void addRequestHeaders(HttpURLConnection connection) {
        if (this.requestHeaders != null && this.requestHeaders.size() > 0) {
            for (String header : this.requestHeaders.keySet()) {
                connection.setRequestProperty(header, this.requestHeaders.get(header));
            }
        }
    }

    /**
     * Writes http body to HttpURLConnection object's outputstream.
     * @param connection HttpURLConnection object.
     * @param closeStreamWhenDone Boolean value to close stream when finished writing.
     * @throws IOException
     */
    private void addRequestBody(HttpURLConnection connection, boolean closeStreamWhenDone) throws IOException {
        if (this.requestBody != null) {
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());

            switch (this.contentType.name()) {
                case "JSON":
                    outputStream.write(((JSONObject) this.requestBody).toString().getBytes());
                    outputStream.flush();
                    break;
                case "TEXT":
                    outputStream.write(this.requestBody.toString().getBytes());
                    break;
            }

            if (closeStreamWhenDone) {
                outputStream.close();
            }
        }
    }

    /**
     * Reads request response body into response field.
     * @param responseStream InputStream from HttpURLConnection object.
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
