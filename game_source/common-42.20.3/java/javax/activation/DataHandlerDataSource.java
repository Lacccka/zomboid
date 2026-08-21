/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;

class DataHandlerDataSource
implements DataSource {
    DataHandler dataHandler = null;

    public DataHandlerDataSource(DataHandler dh) {
        this.dataHandler = dh;
    }

    public InputStream getInputStream() throws IOException {
        return this.dataHandler.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.dataHandler.getOutputStream();
    }

    public String getContentType() {
        return this.dataHandler.getContentType();
    }

    public String getName() {
        return this.dataHandler.getName();
    }
}

