package com.honeywell.net.core.multipart.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import com.honeywell.net.core.multipart.Args;
import com.honeywell.net.core.multipart.Consts;
import com.honeywell.net.core.multipart.ContentType;

public class StringBody extends AbstractContentBody {

    public static final String ENC_8BIT = "8bit";

    private final byte[] content;

    public StringBody(final String text) throws UnsupportedEncodingException {
        this(text, "text/plain", Consts.UTF_8);
    }

    public StringBody(final String text, final String mimeType,
                      final Charset charset) throws UnsupportedEncodingException {
        this(text, ContentType.create(mimeType, charset));
    }

    public StringBody(final String text, final ContentType contentType) {
        super(contentType);
        final Charset charset = contentType.getCharset();
        final String csname = charset != null ? charset.name()
                : Consts.ASCII.name();
        try {
            this.content = text.getBytes(csname);
        } catch (final UnsupportedEncodingException ex) {
            // Should never happen
            throw new UnsupportedCharsetException(csname);
        }
    }

    public void writeTo(final OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        final InputStream in = new ByteArrayInputStream(this.content);
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = in.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }
        out.flush();
    }

    public String getTransferEncoding() {
        return ENC_8BIT;
    }

    public long getContentLength() {
        return this.content.length;
    }

    public String getFilename() {
        return null;
    }

}
