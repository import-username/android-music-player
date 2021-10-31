package com.importusername.musicplayer.http;

import java.io.InputStream;
import java.net.URLConnection;

/**
 * Object that represents a single part in a multipart request.
 */
public class MultipartRequestPart {
    private final String fieldname;

    private final String contentType;

    private final InputStream partData;

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

    public String getFieldname() {
        return this.fieldname;
    }

    public String getContentType() {
        return this.contentType;
    }

    public InputStream getPartDataStream() {
        return partData;
    }

    public String getFilename() {
        return this.filename;
    }

    /**
     * @return Part content disposition header string.
     */
    public String getContentDisposition() {
        return String.format("Content-Disposition: form-data;name=\"%s\";filename=\"%s\"%s", this.fieldname, this.filename, this.getNewlineString(1));
    }

    /**
     * @return Part content type header string.
     */
    public String getContentTypeString() {
        return String.format("content-type: %s%s", URLConnection.guessContentTypeFromName(this.filename), this.getNewlineString(2));
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
