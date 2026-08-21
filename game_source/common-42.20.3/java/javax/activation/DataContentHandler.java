/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.DataSource;

public interface DataContentHandler {
    public DataFlavor[] getTransferDataFlavors();

    public Object getTransferData(DataFlavor var1, DataSource var2) throws UnsupportedFlavorException, IOException;

    public Object getContent(DataSource var1) throws IOException;

    public void writeTo(Object var1, String var2, OutputStream var3) throws IOException;
}

