package com.honeywell.net.core.multipart.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.honeywell.net.core.multipart.Args;
import com.honeywell.net.core.multipart.ContentType;

public class InputStreamBody extends AbstractContentBody {

    public static final String ENC_BINARY = "binary";

    private final InputStream in;
    private final String filename;

    public InputStreamBody(final InputStream in, final String filename) {
        this(in, ContentType.DEFAULT_BINARY, filename); //ContentType.create("image/" + getFormat(filename))
    }

    public InputStreamBody(final InputStream in, final ContentType contentType,
                           final String filename) {
        super(contentType);
        Args.notNull(in, "Input stream");
        this.in = in;
        this.filename = filename;
    }

    public void writeTo(final OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        try {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = this.in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.flush();
        } finally {
            this.in.close();
        }
    }

    public String getTransferEncoding() {
        return ENC_BINARY;
    }

    public long getContentLength() {
        return -1;
    }

    public String getFilename() {
        return this.filename;
    }

}
