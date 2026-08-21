/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.util.Collections;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamProvider;
import org.apache.commons.compress.archivers.StreamingNotSupportedException;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.Sets;

public class ArchiveStreamFactory
implements ArchiveStreamProvider {
    private static final int TAR_HEADER_SIZE = 512;
    private static final int TAR_TEST_ENTRY_COUNT = 10;
    private static final int DUMP_SIGNATURE_SIZE = 32;
    private static final int SIGNATURE_SIZE = 12;
    public static final ArchiveStreamFactory DEFAULT = new ArchiveStreamFactory();
    public static final String APK = "apk";
    public static final String XAPK = "xapk";
    public static final String APKS = "apks";
    public static final String APKM = "apkm";
    public static final String AR = "ar";
    public static final String ARJ = "arj";
    public static final String CPIO = "cpio";
    public static final String DUMP = "dump";
    public static final String JAR = "jar";
    public static final String TAR = "tar";
    public static final String ZIP = "zip";
    public static final String SEVEN_Z = "7z";
    private volatile String entryEncoding;
    private SortedMap<String, ArchiveStreamProvider> archiveInputStreamProviders;
    private SortedMap<String, ArchiveStreamProvider> archiveOutputStreamProviders;

    private static Iterable<ArchiveStreamProvider> archiveStreamProviderIterable() {
        return ServiceLoader.load(ArchiveStreamProvider.class, ClassLoader.getSystemClassLoader());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String detect(InputStream in) throws ArchiveException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Mark is not supported.");
        }
        byte[] signature = new byte[12];
        in.mark(signature.length);
        int signatureLength = -1;
        try {
            signatureLength = IOUtils.readFully(in, signature);
            in.reset();
        }
        catch (IOException e) {
            throw new ArchiveException("IOException while reading signature.", e);
        }
        if (ZipArchiveInputStream.matches(signature, signatureLength)) {
            return ZIP;
        }
        if (JarArchiveInputStream.matches(signature, signatureLength)) {
            return JAR;
        }
        if (ArArchiveInputStream.matches(signature, signatureLength)) {
            return AR;
        }
        if (CpioArchiveInputStream.matches(signature, signatureLength)) {
            return CPIO;
        }
        if (ArjArchiveInputStream.matches(signature, signatureLength)) {
            return ARJ;
        }
        if (SevenZFile.matches(signature, signatureLength)) {
            return SEVEN_Z;
        }
        byte[] dumpsig = new byte[32];
        in.mark(dumpsig.length);
        try {
            signatureLength = IOUtils.readFully(in, dumpsig);
            in.reset();
        }
        catch (IOException e) {
            throw new ArchiveException("IOException while reading dump signature", e);
        }
        if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
            return DUMP;
        }
        byte[] tarHeader = new byte[512];
        in.mark(tarHeader.length);
        try {
            signatureLength = IOUtils.readFully(in, tarHeader);
            in.reset();
        }
        catch (IOException e) {
            throw new ArchiveException("IOException while reading tar signature", e);
        }
        if (TarArchiveInputStream.matches(tarHeader, signatureLength)) {
            return TAR;
        }
        if (signatureLength < 512) throw new ArchiveException("No Archiver found for the stream signature");
        try (TarArchiveInputStream inputStream2 = new TarArchiveInputStream(new ByteArrayInputStream(tarHeader));){
            TarArchiveEntry entry = inputStream2.getNextEntry();
            int count = 0;
            while (entry != null && entry.isDirectory() && entry.isCheckSumOK() && count++ < 10) {
                entry = inputStream2.getNextEntry();
            }
            if (entry == null || !entry.isCheckSumOK() || entry.isDirectory() || entry.getSize() <= 0L) {
                if (count <= 0) throw new ArchiveException("No Archiver found for the stream signature");
            }
            String string = TAR;
            return string;
        }
        catch (Exception exception) {
            // empty catch block
        }
        throw new ArchiveException("No Archiver found for the stream signature");
    }

    public static SortedMap<String, ArchiveStreamProvider> findAvailableArchiveInputStreamProviders() {
        return AccessController.doPrivileged(() -> {
            TreeMap<String, ArchiveStreamProvider> map = new TreeMap<String, ArchiveStreamProvider>();
            ArchiveStreamFactory.putAll(DEFAULT.getInputStreamArchiveNames(), DEFAULT, map);
            ArchiveStreamFactory.archiveStreamProviderIterable().forEach(provider -> ArchiveStreamFactory.putAll(provider.getInputStreamArchiveNames(), provider, map));
            return map;
        });
    }

    public static SortedMap<String, ArchiveStreamProvider> findAvailableArchiveOutputStreamProviders() {
        return AccessController.doPrivileged(() -> {
            TreeMap<String, ArchiveStreamProvider> map = new TreeMap<String, ArchiveStreamProvider>();
            ArchiveStreamFactory.putAll(DEFAULT.getOutputStreamArchiveNames(), DEFAULT, map);
            ArchiveStreamFactory.archiveStreamProviderIterable().forEach(provider -> ArchiveStreamFactory.putAll(provider.getOutputStreamArchiveNames(), provider, map));
            return map;
        });
    }

    static void putAll(Set<String> names, ArchiveStreamProvider provider, TreeMap<String, ArchiveStreamProvider> map) {
        names.forEach(name -> map.put(ArchiveStreamFactory.toKey(name), provider));
    }

    private static String toKey(String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    public ArchiveStreamFactory() {
        this(null);
    }

    public ArchiveStreamFactory(String entryEncoding) {
        this.entryEncoding = entryEncoding;
    }

    public <I extends ArchiveInputStream<? extends ArchiveEntry>> I createArchiveInputStream(InputStream in) throws ArchiveException {
        return this.createArchiveInputStream(ArchiveStreamFactory.detect(in), in);
    }

    public <I extends ArchiveInputStream<? extends ArchiveEntry>> I createArchiveInputStream(String archiverName, InputStream in) throws ArchiveException {
        return this.createArchiveInputStream(archiverName, in, this.entryEncoding);
    }

    @Override
    public <I extends ArchiveInputStream<? extends ArchiveEntry>> I createArchiveInputStream(String archiverName, InputStream in, String actualEncoding) throws ArchiveException {
        if (archiverName == null) {
            throw new IllegalArgumentException("Archiver name must not be null.");
        }
        if (in == null) {
            throw new IllegalArgumentException("InputStream must not be null.");
        }
        if (AR.equalsIgnoreCase(archiverName)) {
            return (I)new ArArchiveInputStream(in);
        }
        if (ARJ.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new ArjArchiveInputStream(in, actualEncoding);
            }
            return (I)new ArjArchiveInputStream(in);
        }
        if (ZIP.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new ZipArchiveInputStream(in, actualEncoding);
            }
            return (I)new ZipArchiveInputStream(in);
        }
        if (TAR.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new TarArchiveInputStream(in, actualEncoding);
            }
            return (I)new TarArchiveInputStream(in);
        }
        if (JAR.equalsIgnoreCase(archiverName) || APK.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new JarArchiveInputStream(in, actualEncoding);
            }
            return (I)new JarArchiveInputStream(in);
        }
        if (CPIO.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new CpioArchiveInputStream(in, actualEncoding);
            }
            return (I)new CpioArchiveInputStream(in);
        }
        if (DUMP.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (I)new DumpArchiveInputStream(in, actualEncoding);
            }
            return (I)new DumpArchiveInputStream(in);
        }
        if (SEVEN_Z.equalsIgnoreCase(archiverName)) {
            throw new StreamingNotSupportedException(SEVEN_Z);
        }
        ArchiveStreamProvider archiveStreamProvider = (ArchiveStreamProvider)this.getArchiveInputStreamProviders().get(ArchiveStreamFactory.toKey(archiverName));
        if (archiveStreamProvider != null) {
            return archiveStreamProvider.createArchiveInputStream(archiverName, in, actualEncoding);
        }
        throw new ArchiveException("Archiver: " + archiverName + " not found.");
    }

    public <O extends ArchiveOutputStream<? extends ArchiveEntry>> O createArchiveOutputStream(String archiverName, OutputStream out) throws ArchiveException {
        return this.createArchiveOutputStream(archiverName, out, this.entryEncoding);
    }

    @Override
    public <O extends ArchiveOutputStream<? extends ArchiveEntry>> O createArchiveOutputStream(String archiverName, OutputStream out, String actualEncoding) throws ArchiveException {
        if (archiverName == null) {
            throw new IllegalArgumentException("Archiver name must not be null.");
        }
        if (out == null) {
            throw new IllegalArgumentException("OutputStream must not be null.");
        }
        if (AR.equalsIgnoreCase(archiverName)) {
            return (O)new ArArchiveOutputStream(out);
        }
        if (ZIP.equalsIgnoreCase(archiverName)) {
            ZipArchiveOutputStream zip2 = new ZipArchiveOutputStream(out);
            if (actualEncoding != null) {
                zip2.setEncoding(actualEncoding);
            }
            return (O)zip2;
        }
        if (TAR.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (O)new TarArchiveOutputStream(out, actualEncoding);
            }
            return (O)new TarArchiveOutputStream(out);
        }
        if (JAR.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (O)new JarArchiveOutputStream(out, actualEncoding);
            }
            return (O)new JarArchiveOutputStream(out);
        }
        if (CPIO.equalsIgnoreCase(archiverName)) {
            if (actualEncoding != null) {
                return (O)new CpioArchiveOutputStream(out, actualEncoding);
            }
            return (O)new CpioArchiveOutputStream(out);
        }
        if (SEVEN_Z.equalsIgnoreCase(archiverName)) {
            throw new StreamingNotSupportedException(SEVEN_Z);
        }
        ArchiveStreamProvider archiveStreamProvider = (ArchiveStreamProvider)this.getArchiveOutputStreamProviders().get(ArchiveStreamFactory.toKey(archiverName));
        if (archiveStreamProvider != null) {
            return archiveStreamProvider.createArchiveOutputStream(archiverName, out, actualEncoding);
        }
        throw new ArchiveException("Archiver: " + archiverName + " not found.");
    }

    public SortedMap<String, ArchiveStreamProvider> getArchiveInputStreamProviders() {
        if (this.archiveInputStreamProviders == null) {
            this.archiveInputStreamProviders = Collections.unmodifiableSortedMap(ArchiveStreamFactory.findAvailableArchiveInputStreamProviders());
        }
        return this.archiveInputStreamProviders;
    }

    public SortedMap<String, ArchiveStreamProvider> getArchiveOutputStreamProviders() {
        if (this.archiveOutputStreamProviders == null) {
            this.archiveOutputStreamProviders = Collections.unmodifiableSortedMap(ArchiveStreamFactory.findAvailableArchiveOutputStreamProviders());
        }
        return this.archiveOutputStreamProviders;
    }

    public String getEntryEncoding() {
        return this.entryEncoding;
    }

    @Override
    public Set<String> getInputStreamArchiveNames() {
        return Sets.newHashSet(AR, ARJ, ZIP, TAR, JAR, CPIO, DUMP, SEVEN_Z);
    }

    @Override
    public Set<String> getOutputStreamArchiveNames() {
        return Sets.newHashSet(AR, ZIP, TAR, JAR, CPIO, SEVEN_Z);
    }

    @Deprecated
    public void setEntryEncoding(String entryEncoding) {
        this.entryEncoding = entryEncoding;
    }
}

