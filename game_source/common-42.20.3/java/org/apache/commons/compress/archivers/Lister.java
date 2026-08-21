/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public final class Lister {
    private static final ArchiveStreamFactory FACTORY = ArchiveStreamFactory.DEFAULT;
    private final boolean quiet;
    private final String[] args;

    private static <T extends ArchiveInputStream<? extends E>, E extends ArchiveEntry> T createArchiveInputStream(String[] args2, InputStream inputStream2) throws ArchiveException {
        if (args2.length > 1) {
            return (T)FACTORY.createArchiveInputStream(args2[1], inputStream2);
        }
        return (T)FACTORY.createArchiveInputStream(inputStream2);
    }

    private static String detectFormat(Path file) throws ArchiveException, IOException {
        try (BufferedInputStream inputStream2 = new BufferedInputStream(Files.newInputStream(file, new OpenOption[0]));){
            String string = ArchiveStreamFactory.detect(inputStream2);
            return string;
        }
    }

    public static void main(String ... args2) throws ArchiveException, IOException {
        if (args2 == null || args2.length == 0) {
            Lister.usage();
            return;
        }
        new Lister(false, args2).go();
    }

    private static void usage() {
        System.err.println("Parameters: archive-name [archive-type]\n");
        System.err.println("The magic archive-type 'zipfile' prefers ZipFile over ZipArchiveInputStream");
        System.err.println("The magic archive-type 'tarfile' prefers TarFile over TarArchiveInputStream");
    }

    @Deprecated
    public Lister() {
        this(false, "");
    }

    Lister(boolean quiet, String ... args2) {
        this.quiet = quiet;
        this.args = (String[])args2.clone();
        Objects.requireNonNull(args2[0], "args[0]");
    }

    void go() throws ArchiveException, IOException {
        this.list(Paths.get(this.args[0], new String[0]), this.args);
    }

    private void list(Path file, String ... args2) throws ArchiveException, IOException {
        this.println("Analyzing " + file);
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            System.err.println(file + " doesn't exist or is a directory");
        }
        String format = (args2.length > 1 ? args2[1] : Lister.detectFormat(file)).toLowerCase(Locale.ROOT);
        this.println("Detected format " + format);
        switch (format) {
            case "7z": {
                this.list7z(file);
                break;
            }
            case "zip": {
                this.listZipUsingZipFile(file);
                break;
            }
            case "tar": {
                this.listZipUsingTarFile(file);
                break;
            }
            default: {
                this.listStream(file, args2);
            }
        }
    }

    private void list7z(Path file) throws IOException {
        try (SevenZFile sevenZFile = ((SevenZFile.Builder)SevenZFile.builder().setPath(file)).get();){
            SevenZArchiveEntry entry;
            this.println("Created " + sevenZFile);
            while ((entry = sevenZFile.getNextEntry()) != null) {
                this.println(entry.getName() == null ? sevenZFile.getDefaultName() + " (entry name was null)" : entry.getName());
            }
        }
    }

    private void listStream(Path file, String[] args2) throws ArchiveException, IOException {
        try (BufferedInputStream inputStream2 = new BufferedInputStream(Files.newInputStream(file, new OpenOption[0]));
             Object archiveInputStream = Lister.createArchiveInputStream(args2, inputStream2);){
            this.println("Created " + archiveInputStream.toString());
            ((ArchiveInputStream)archiveInputStream).forEach(this::println);
        }
    }

    private void listZipUsingTarFile(Path file) throws IOException {
        try (TarFile tarFile = new TarFile(file);){
            this.println("Created " + tarFile);
            tarFile.getEntries().forEach(this::println);
        }
    }

    private void listZipUsingZipFile(Path file) throws IOException {
        try (ZipFile zipFile = ((ZipFile.Builder)ZipFile.builder().setPath(file)).get();){
            this.println("Created " + zipFile);
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            while (en.hasMoreElements()) {
                this.println(en.nextElement());
            }
        }
    }

    private void println(ArchiveEntry entry) {
        this.println(entry.getName());
    }

    private void println(String line) {
        if (!this.quiet) {
            System.out.println(line);
        }
    }
}

