package com.honeywell.net.core.multipart.content;

import java.io.IOException;
import java.io.OutputStream;

import com.honeywell.net.core.multipart.Args;
import com.honeywell.net.core.multipart.ContentType;

public class ByteArrayBody extends AbstractContentBody {

    public static final String ENC_BINARY = "binary";

    private final byte[] data;

    private final String filename;

    public ByteArrayBody(final byte[] data, final ContentType contentType,
            final String filename) {
        super(contentType);
        Args.notNull(data, "byte[]");
        this.data = data;
        this.filename = filename;
    }

    /**
     * Creates a new ByteArrayBody.
     *
     * @param data The contents of the file contained in this part.
     * @param filename The name of the file contained in this part.
     */
    public ByteArrayBody(final byte[] data, final String filename) {
        this(data, ContentType.APPLICATION_OCTET_STREAM, filename); //ContentType.create("image/" + getFormat(filename))
    }

    public String getFilename() {
        return filename;
    }

    public void writeTo(final OutputStream out) throws IOException {
        out.write(data);
    }

    @Override
    public String getCharset() {
        return null;
    }

    public String getTransferEncoding() {
        return ENC_BINARY;
    }

    public long getContentLength() {
        return data.length;
    }

}
