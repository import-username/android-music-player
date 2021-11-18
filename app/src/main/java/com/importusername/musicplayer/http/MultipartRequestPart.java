package com.importusername.musicplayer.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Object that represents a single part in a multipart request.
 */
public class MultipartRequestPart {
    private final String fieldname;

    private final String contentType;

    private InputStream partData;

    private String stringPartData;

    private final String filename;

    private final boolean includeNewlines;

    /**
     * @param fieldname Content disposition fieldname
     * @param contentType Content type of data to be sent in part
     * @param partData InputStream of bytes to send
     * @param filename Filename of file to create. Should include extension
     * @param includeNewlines Boolean value for if content disposition/content type strings should be returned with newlines.
     */
    public MultipartRequestPart(String fieldname, String contentType, InputStream partData, String filename, boolean includeNewlines) {
        this.fieldname = fieldname;
        this.contentType = contentType;
        this.partData = partData;
        this.filename = filename;
        this.includeNewlines = includeNewlines;
    }

    public MultipartRequestPart(String fieldname, String contentType, String partData, String filename, boolean includeNewlines) {
        this.fieldname = fieldname;
        this.contentType = contentType;
        this.stringPartData = partData;
        this.filename = filename;
        this.includeNewlines = includeNewlines;
    }

    public String getFieldname() {
        return this.fieldname;
    }

    public String getContentType() {
        return this.contentType;
    }

    public InputStream getPartDataStream() {
        if (this.stringPartData != null) {
            return new ByteArrayInputStream(this.stringPartData.getBytes(StandardCharsets.UTF_8));
        }

        return this.partData;
    }

    public String getStringValue() {
        return this.stringPartData;
    }

    public String getFilename() {
        return this.filename;
    }

    /**
     * @return Part content disposition header string.
     */
    public String getContentDisposition() {
        final StringBuilder contentDispositionString = new StringBuilder("Content-Disposition: form-data;name=" + this.fieldname + "");

        if (this.filename != null) {
            contentDispositionString.append(String.format(";filename=%s", this.filename));
        }

        contentDispositionString.append(this.getNewlineString(1));
        
        return contentDispositionString.toString();
    }

    /**
     * @return Part content type header string.
     */
    public String getContentTypeString() {
        return String.format("content-type: %s%s", this.filename != null ? URLConnection.guessContentTypeFromName(this.filename) : "text/plain", this.getNewlineString(2));
    }

    private String getNewlineString(int newlineCount) {
        StringBuilder newlines = new StringBuilder();

        if (this.includeNewlines) {
            for (int i = 0; i < newlineCount; i++) {
                newlines.append("\r\n");
            }
        }

        return newlines.toString();
    }
}
