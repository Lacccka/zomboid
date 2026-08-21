/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.harmony.pack200.Pack200Adapter;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.Archive;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.reflect.FieldUtils;

public class Pack200UnpackerAdapter
extends Pack200Adapter
implements Pack200.Unpacker {
    static BoundedInputStream newBoundedInputStream(File file) throws IOException {
        return Pack200UnpackerAdapter.newBoundedInputStream(file.toPath());
    }

    private static BoundedInputStream newBoundedInputStream(FileInputStream fileInputStream) throws IOException {
        return Pack200UnpackerAdapter.newBoundedInputStream(Pack200UnpackerAdapter.readPathString(fileInputStream), new String[0]);
    }

    static BoundedInputStream newBoundedInputStream(InputStream inputStream2) throws IOException {
        if (inputStream2 instanceof BoundedInputStream) {
            return (BoundedInputStream)inputStream2;
        }
        if (inputStream2 instanceof CloseShieldInputStream) {
            return Pack200UnpackerAdapter.newBoundedInputStream(((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).get());
        }
        if (inputStream2 instanceof FilterInputStream) {
            return Pack200UnpackerAdapter.newBoundedInputStream(Pack200UnpackerAdapter.unwrap((FilterInputStream)inputStream2));
        }
        if (inputStream2 instanceof FileInputStream) {
            return Pack200UnpackerAdapter.newBoundedInputStream((FileInputStream)inputStream2);
        }
        return Pack200UnpackerAdapter.newBoundedInputStream(((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).get());
    }

    static BoundedInputStream newBoundedInputStream(Path path) throws IOException {
        return ((BoundedInputStream.Builder)((BoundedInputStream.Builder)((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(new BufferedInputStream(Files.newInputStream(path, new OpenOption[0])))).setMaxCount(Files.size(path))).setPropagateClose(false)).get();
    }

    static BoundedInputStream newBoundedInputStream(String first, String ... more) throws IOException {
        return Pack200UnpackerAdapter.newBoundedInputStream(Paths.get(first, more));
    }

    static BoundedInputStream newBoundedInputStream(URL url) throws IOException, URISyntaxException {
        return Pack200UnpackerAdapter.newBoundedInputStream(Paths.get(url.toURI()));
    }

    private static <T> T readField(Object object, String fieldName) {
        try {
            return (T)FieldUtils.readField(object, fieldName, true);
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    static String readPathString(FileInputStream fis) {
        return (String)Pack200UnpackerAdapter.readField(fis, "path");
    }

    static InputStream unwrap(FilterInputStream filterInputStream) {
        return (InputStream)Pack200UnpackerAdapter.readField(filterInputStream, "in");
    }

    static InputStream unwrap(InputStream inputStream2) {
        return inputStream2 instanceof FilterInputStream ? Pack200UnpackerAdapter.unwrap((FilterInputStream)inputStream2) : inputStream2;
    }

    @Override
    public void unpack(File file, JarOutputStream out) throws IOException {
        if (file == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        long size = file.length();
        int bufferSize = size > 0L && size < 8192L ? (int)size : 8192;
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file.toPath(), new OpenOption[0]), bufferSize);){
            this.unpack(in, out);
        }
    }

    @Override
    public void unpack(InputStream in, JarOutputStream out) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        this.completed(0.0);
        try {
            new Archive(in, out).unpack();
        }
        catch (Pack200Exception e) {
            throw new IOException("Failed to unpack Jar:" + e);
        }
        this.completed(1.0);
    }
}

