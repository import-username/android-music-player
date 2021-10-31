package com.importusername.musicplayer.http;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

/**
 * Class for structuring an http multipart request.
 */
public class MultipartRequestEntity {
    private final ArrayList<MultipartRequestPart> multipartData = new ArrayList<>();

    /**
     * Adds a multipartrequestpart object with provided arguments to multipartData array.
     * @param fieldname Part field name
     * @param value Inputstream of bytes to write to part request
     * @param contentType Mimetype of value argument
     * @param filename Part file name (include file extension)
     * @throws Exception
     */
    public void appendData(String fieldname, InputStream value, String contentType, @Nullable String filename) throws Exception {
        if (isValidArguments(fieldname, value, contentType)) {
            final MultipartRequestPart part = new MultipartRequestPart(fieldname, contentType, value, filename, true);

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
        final String boundary = "920574230592345364354536435r";
        final String newLine = "\r\n";

        for (MultipartRequestPart part : this.multipartData) {
            // Write part data to outputstream
            outputStream.writeBytes("--" + boundary);
            outputStream.writeBytes(part.getContentDisposition());
            outputStream.writeBytes(part.getContentTypeString());
            outputStream.flush();

            this.writeToOutput(part.getPartDataStream(), outputStream);
        }

        outputStream.writeBytes(String.format("--%s--\r\n", boundary));
        outputStream.writeBytes(newLine);

        if (closeWhenDone) {
            outputStream.flush();
            outputStream.close();
        }
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
