/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataContentHandler;
import javax.activation.DataContentHandlerFactory;
import javax.activation.DataHandlerDataSource;
import javax.activation.DataSource;
import javax.activation.DataSourceDataContentHandler;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.activation.ObjectDataContentHandler;
import javax.activation.SecuritySupport;
import javax.activation.URLDataSource;
import javax.activation.UnsupportedDataTypeException;

public class DataHandler
implements Transferable {
    private DataSource dataSource = null;
    private DataSource objDataSource = null;
    private Object object = null;
    private String objectMimeType = null;
    private CommandMap currentCommandMap = null;
    private static final DataFlavor[] emptyFlavors = new DataFlavor[0];
    private DataFlavor[] transferFlavors = emptyFlavors;
    private DataContentHandler dataContentHandler = null;
    private DataContentHandler factoryDCH = null;
    private static DataContentHandlerFactory factory = null;
    private DataContentHandlerFactory oldFactory = null;
    private String shortType = null;

    public DataHandler(DataSource ds) {
        this.dataSource = ds;
        this.oldFactory = factory;
    }

    public DataHandler(Object obj, String mimeType) {
        this.object = obj;
        this.objectMimeType = mimeType;
        this.oldFactory = factory;
    }

    public DataHandler(URL url) {
        this.dataSource = new URLDataSource(url);
        this.oldFactory = factory;
    }

    private synchronized CommandMap getCommandMap() {
        if (this.currentCommandMap != null) {
            return this.currentCommandMap;
        }
        return CommandMap.getDefaultCommandMap();
    }

    public DataSource getDataSource() {
        if (this.dataSource == null) {
            if (this.objDataSource == null) {
                this.objDataSource = new DataHandlerDataSource(this);
            }
            return this.objDataSource;
        }
        return this.dataSource;
    }

    public String getName() {
        if (this.dataSource != null) {
            return this.dataSource.getName();
        }
        return null;
    }

    public String getContentType() {
        if (this.dataSource != null) {
            return this.dataSource.getContentType();
        }
        return this.objectMimeType;
    }

    public InputStream getInputStream() throws IOException {
        InputStream ins = null;
        if (this.dataSource != null) {
            ins = this.dataSource.getInputStream();
        } else {
            DataContentHandler dch = this.getDataContentHandler();
            if (dch == null) {
                throw new UnsupportedDataTypeException("no DCH for MIME type " + this.getBaseType());
            }
            if (dch instanceof ObjectDataContentHandler && ((ObjectDataContentHandler)dch).getDCH() == null) {
                throw new UnsupportedDataTypeException("no object DCH for MIME type " + this.getBaseType());
            }
            final DataContentHandler fdch = dch;
            final PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pin = new PipedInputStream(pos);
            new Thread(new Runnable(){

                public void run() {
                    try {
                        fdch.writeTo(DataHandler.this.object, DataHandler.this.objectMimeType, pos);
                    }
                    catch (IOException iOException) {
                    }
                    finally {
                        try {
                            pos.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
            }, "DataHandler.getInputStream").start();
            ins = pin;
        }
        return ins;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(OutputStream os) throws IOException {
        if (this.dataSource != null) {
            InputStream is = null;
            byte[] data = new byte[8192];
            is = this.dataSource.getInputStream();
            try {
                int bytes_read;
                while ((bytes_read = is.read(data)) > 0) {
                    os.write(data, 0, bytes_read);
                }
            }
            finally {
                is.close();
                is = null;
            }
        } else {
            DataContentHandler dch = this.getDataContentHandler();
            dch.writeTo(this.object, this.objectMimeType, os);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (this.dataSource != null) {
            return this.dataSource.getOutputStream();
        }
        return null;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        if (factory != this.oldFactory) {
            this.transferFlavors = emptyFlavors;
        }
        if (this.transferFlavors == emptyFlavors) {
            this.transferFlavors = this.getDataContentHandler().getTransferDataFlavors();
        }
        if (this.transferFlavors == emptyFlavors) {
            return this.transferFlavors;
        }
        return (DataFlavor[])this.transferFlavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor[] lFlavors = this.getTransferDataFlavors();
        for (int i = 0; i < lFlavors.length; ++i) {
            if (!lFlavors[i].equals(flavor)) continue;
            return true;
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return this.getDataContentHandler().getTransferData(flavor, this.dataSource);
    }

    public synchronized void setCommandMap(CommandMap commandMap) {
        if (commandMap != this.currentCommandMap || commandMap == null) {
            this.transferFlavors = emptyFlavors;
            this.dataContentHandler = null;
            this.currentCommandMap = commandMap;
        }
    }

    public CommandInfo[] getPreferredCommands() {
        if (this.dataSource != null) {
            return this.getCommandMap().getPreferredCommands(this.getBaseType(), this.dataSource);
        }
        return this.getCommandMap().getPreferredCommands(this.getBaseType());
    }

    public CommandInfo[] getAllCommands() {
        if (this.dataSource != null) {
            return this.getCommandMap().getAllCommands(this.getBaseType(), this.dataSource);
        }
        return this.getCommandMap().getAllCommands(this.getBaseType());
    }

    public CommandInfo getCommand(String cmdName) {
        if (this.dataSource != null) {
            return this.getCommandMap().getCommand(this.getBaseType(), cmdName, this.dataSource);
        }
        return this.getCommandMap().getCommand(this.getBaseType(), cmdName);
    }

    public Object getContent() throws IOException {
        if (this.object != null) {
            return this.object;
        }
        return this.getDataContentHandler().getContent(this.getDataSource());
    }

    public Object getBean(CommandInfo cmdinfo) {
        Object bean = null;
        try {
            ClassLoader cld = null;
            cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = this.getClass().getClassLoader();
            }
            bean = cmdinfo.getCommandObject(this, cld);
        }
        catch (IOException iOException) {
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        return bean;
    }

    private synchronized DataContentHandler getDataContentHandler() {
        if (factory != this.oldFactory) {
            this.oldFactory = factory;
            this.factoryDCH = null;
            this.dataContentHandler = null;
            this.transferFlavors = emptyFlavors;
        }
        if (this.dataContentHandler != null) {
            return this.dataContentHandler;
        }
        String simpleMT = this.getBaseType();
        if (this.factoryDCH == null && factory != null) {
            this.factoryDCH = factory.createDataContentHandler(simpleMT);
        }
        if (this.factoryDCH != null) {
            this.dataContentHandler = this.factoryDCH;
        }
        if (this.dataContentHandler == null) {
            this.dataContentHandler = this.dataSource != null ? this.getCommandMap().createDataContentHandler(simpleMT, this.dataSource) : this.getCommandMap().createDataContentHandler(simpleMT);
        }
        this.dataContentHandler = this.dataSource != null ? new DataSourceDataContentHandler(this.dataContentHandler, this.dataSource) : new ObjectDataContentHandler(this.dataContentHandler, this.object, this.objectMimeType);
        return this.dataContentHandler;
    }

    private synchronized String getBaseType() {
        if (this.shortType == null) {
            String ct = this.getContentType();
            try {
                MimeType mt = new MimeType(ct);
                this.shortType = mt.getBaseType();
            }
            catch (MimeTypeParseException e) {
                this.shortType = ct;
            }
        }
        return this.shortType;
    }

    public static synchronized void setDataContentHandlerFactory(DataContentHandlerFactory newFactory) {
        block4: {
            if (factory != null) {
                throw new Error("DataContentHandlerFactory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkSetFactory();
                }
                catch (SecurityException ex) {
                    if (DataHandler.class.getClassLoader() == newFactory.getClass().getClassLoader()) break block4;
                    throw ex;
                }
            }
        }
        factory = newFactory;
    }
}

