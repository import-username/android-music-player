package com.importusername.musicplayer.http;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

/**
 * Class for structuring an http multipart request.
 */
public class MultipartRequestEntity {
    private final String boundary = "----" + ((int) System.currentTimeMillis()) + "----";

    private final ArrayList<MultipartRequestPart> multipartData = new ArrayList<>();

    /**
     * Adds a multipartrequestpart object with provided arguments to multipartData array.
     * @param fieldname Part field name
     * @param value Inputstream of bytes to write to part request
     * @param contentType Mimetype of value argument
     * @param filename Part file name (include file extension)
     * @throws Exception
     */
    public void appendData(String fieldname, InputStream value, String contentType, String filename) throws Exception {
        final String multipartContentType = contentType == null ? "text/plain" : contentType;

        if (isValidArguments(fieldname, value, multipartContentType)) {
            final MultipartRequestPart part = new MultipartRequestPart(fieldname, multipartContentType, value, filename, true);

            this.multipartData.add(part);
        } else {
            throw new Exception("Invalid parameters");
        }
    }

    /**
     * Returns if appendData method arguments are valid
     */
    private boolean isValidArguments(String param1, Object param2, String param3) {
        return (param1 != null && param2 != null && param3 != null);
    }

    /**
     * Writes multipart data to outputstream.
     * @param outputStream Outputstream object
     */
    public void writeMultipartData(DataOutputStream outputStream, boolean closeWhenDone) throws IOException {
        final String newLine = "\r\n";

        for (MultipartRequestPart part : this.multipartData) {
            // Write part data to outputstream
            outputStream.writeBytes("--" + this.boundary);
            outputStream.writeBytes(part.getContentDisposition());
            outputStream.writeBytes(part.getContentTypeString());
            outputStream.flush();

            this.writeToOutput(part.getPartDataStream(), outputStream);
        }

        outputStream.writeBytes(String.format("--%s--\r\n", this.boundary));
        outputStream.writeBytes(newLine);

        if (closeWhenDone) {
            outputStream.flush();
            outputStream.close();
        }
    }

    public String getBoundary() {
        return this.boundary;
    }

    /**
     * Writes bytes from inputstream to outputstream;
     * @param inputStream Inputstream object.
     * @param outputStream Outputstream object.
     */
    private void writeToOutput(InputStream inputStream, DataOutputStream outputStream) throws IOException {
        int result = inputStream.read();

        while (result != -1) {
            outputStream.write(result);

            result = inputStream.read();
        }

        inputStream.close();

        outputStream.writeBytes("\r\n");
    }
}
