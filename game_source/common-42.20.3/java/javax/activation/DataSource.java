/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataSource {
    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public String getContentType();

    public String getName();
}

