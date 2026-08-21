/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.CloseableURLConnection;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.StandardLineSeparator;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.function.IOTriFunction;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.AppendableWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.ThresholdingOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

public class IOUtils {
    public static final int CR = 13;
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final char DIR_SEPARATOR = File.separatorChar;
    public static final char DIR_SEPARATOR_UNIX = '/';
    public static final char DIR_SEPARATOR_WINDOWS = '\\';
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final int EOF = -1;
    public static final int LF = 10;
    @Deprecated
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String LINE_SEPARATOR_UNIX = StandardLineSeparator.LF.getString();
    public static final String LINE_SEPARATOR_WINDOWS = StandardLineSeparator.CRLF.getString();
    private static final ThreadLocal<byte[]> SCRATCH_BYTE_BUFFER_RW = ThreadLocal.withInitial(IOUtils::byteArray);
    private static final byte[] SCRATCH_BYTE_BUFFER_WO = IOUtils.byteArray();
    private static final ThreadLocal<char[]> SCRATCH_CHAR_BUFFER_RW = ThreadLocal.withInitial(IOUtils::charArray);
    private static final char[] SCRATCH_CHAR_BUFFER_WO = IOUtils.charArray();

    public static BufferedInputStream buffer(InputStream inputStream2) {
        Objects.requireNonNull(inputStream2, "inputStream");
        return inputStream2 instanceof BufferedInputStream ? (BufferedInputStream)inputStream2 : new BufferedInputStream(inputStream2);
    }

    public static BufferedInputStream buffer(InputStream inputStream2, int size) {
        Objects.requireNonNull(inputStream2, "inputStream");
        return inputStream2 instanceof BufferedInputStream ? (BufferedInputStream)inputStream2 : new BufferedInputStream(inputStream2, size);
    }

    public static BufferedOutputStream buffer(OutputStream outputStream2) {
        Objects.requireNonNull(outputStream2, "outputStream");
        return outputStream2 instanceof BufferedOutputStream ? (BufferedOutputStream)outputStream2 : new BufferedOutputStream(outputStream2);
    }

    public static BufferedOutputStream buffer(OutputStream outputStream2, int size) {
        Objects.requireNonNull(outputStream2, "outputStream");
        return outputStream2 instanceof BufferedOutputStream ? (BufferedOutputStream)outputStream2 : new BufferedOutputStream(outputStream2, size);
    }

    public static BufferedReader buffer(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
    }

    public static BufferedReader buffer(Reader reader, int size) {
        return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader, size);
    }

    public static BufferedWriter buffer(Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer);
    }

    public static BufferedWriter buffer(Writer writer, int size) {
        return writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer, size);
    }

    public static byte[] byteArray() {
        return IOUtils.byteArray(8192);
    }

    public static byte[] byteArray(int size) {
        return new byte[size];
    }

    private static char[] charArray() {
        return IOUtils.charArray(8192);
    }

    private static char[] charArray(int size) {
        return new char[size];
    }

    static void clear() {
        SCRATCH_BYTE_BUFFER_RW.remove();
        SCRATCH_CHAR_BUFFER_RW.remove();
        Arrays.fill(SCRATCH_BYTE_BUFFER_WO, (byte)0);
        Arrays.fill(SCRATCH_CHAR_BUFFER_WO, '\u0000');
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static void close(Closeable ... closeables) throws IOExceptionList {
        IOConsumer.forAll(IOUtils::close, closeables);
    }

    public static void close(Closeable closeable, IOConsumer<IOException> consumer) throws IOException {
        block3: {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (IOException e) {
                    if (consumer == null) break block3;
                    consumer.accept(e);
                }
            }
        }
    }

    public static void close(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection)conn).disconnect();
        }
    }

    private static void closeQ(Closeable closeable) {
        IOUtils.closeQuietly(closeable, null);
    }

    public static void closeQuietly(Closeable closeable) {
        IOUtils.closeQuietly(closeable, null);
    }

    public static void closeQuietly(Closeable ... closeables) {
        if (closeables != null) {
            IOUtils.closeQuietly(Arrays.stream(closeables));
        }
    }

    public static void closeQuietly(Closeable closeable, Consumer<IOException> consumer) {
        block3: {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (IOException e) {
                    if (consumer == null) break block3;
                    consumer.accept(e);
                }
            }
        }
    }

    public static void closeQuietly(InputStream input) {
        IOUtils.closeQ(input);
    }

    public static void closeQuietly(Iterable<Closeable> closeables) {
        if (closeables != null) {
            closeables.forEach(IOUtils::closeQuietly);
        }
    }

    public static void closeQuietly(OutputStream output) {
        IOUtils.closeQ(output);
    }

    public static void closeQuietly(Reader reader) {
        IOUtils.closeQ(reader);
    }

    public static void closeQuietly(Selector selector) {
        IOUtils.closeQ(selector);
    }

    public static void closeQuietly(ServerSocket serverSocket) {
        IOUtils.closeQ(serverSocket);
    }

    public static void closeQuietly(Socket socket) {
        IOUtils.closeQ(socket);
    }

    public static void closeQuietly(Stream<Closeable> closeables) {
        if (closeables != null) {
            closeables.forEach(IOUtils::closeQuietly);
        }
    }

    public static void closeQuietly(Writer writer) {
        IOUtils.closeQ(writer);
    }

    public static long consume(InputStream input) throws IOException {
        return IOUtils.copyLarge(input, NullOutputStream.INSTANCE);
    }

    public static long consume(Reader input) throws IOException {
        return IOUtils.copyLarge(input, NullWriter.INSTANCE);
    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        byte[] array1 = IOUtils.getScratchByteArray();
        byte[] array2 = IOUtils.byteArray();
        block0: while (true) {
            int pos1 = 0;
            int pos2 = 0;
            int index = 0;
            while (true) {
                if (index >= 8192) continue block0;
                if (pos1 == index) {
                    int count1;
                    while ((count1 = input1.read(array1, pos1, 8192 - pos1)) == 0) {
                    }
                    if (count1 == -1) {
                        return pos2 == index && input2.read() == -1;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    int count2;
                    while ((count2 = input2.read(array2, pos2, 8192 - pos2)) == 0) {
                    }
                    if (count2 == -1) {
                        return pos1 == index && input1.read() == -1;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
                ++index;
            }
            break;
        }
    }

    private static boolean contentEquals(Iterator<?> iterator1, Iterator<?> iterator2) {
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return false;
            }
            if (Objects.equals(iterator1.next(), iterator2.next())) continue;
            return false;
        }
        return !iterator2.hasNext();
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        char[] array1 = IOUtils.getScratchCharArray();
        char[] array2 = IOUtils.charArray();
        block0: while (true) {
            int pos1 = 0;
            int pos2 = 0;
            int index = 0;
            while (true) {
                if (index >= 8192) continue block0;
                if (pos1 == index) {
                    int count1;
                    while ((count1 = input1.read(array1, pos1, 8192 - pos1)) == 0) {
                    }
                    if (count1 == -1) {
                        return pos2 == index && input2.read() == -1;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    int count2;
                    while ((count2 = input2.read(array2, pos2, 8192 - pos2)) == 0) {
                    }
                    if (count2 == -1) {
                        return pos1 == index && input1.read() == -1;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
                ++index;
            }
            break;
        }
    }

    private static boolean contentEquals(Stream<?> stream1, Stream<?> stream2) {
        if (stream1 == stream2) {
            return true;
        }
        if (stream1 == null || stream2 == null) {
            return false;
        }
        return IOUtils.contentEquals(stream1.iterator(), stream2.iterator());
    }

    private static boolean contentEqualsIgnoreEOL(BufferedReader reader1, BufferedReader reader2) {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null || reader2 == null) {
            return false;
        }
        return IOUtils.contentEquals(reader1.lines(), reader2.lines());
    }

    public static boolean contentEqualsIgnoreEOL(Reader reader1, Reader reader2) throws UncheckedIOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null || reader2 == null) {
            return false;
        }
        return IOUtils.contentEqualsIgnoreEOL(IOUtils.toBufferedReader(reader1), IOUtils.toBufferedReader(reader2));
    }

    public static int copy(InputStream inputStream2, OutputStream outputStream2) throws IOException {
        long count = IOUtils.copyLarge(inputStream2, outputStream2);
        return count > Integer.MAX_VALUE ? -1 : (int)count;
    }

    public static long copy(InputStream inputStream2, OutputStream outputStream2, int bufferSize) throws IOException {
        return IOUtils.copyLarge(inputStream2, outputStream2, IOUtils.byteArray(bufferSize));
    }

    @Deprecated
    public static void copy(InputStream input, Writer writer) throws IOException {
        IOUtils.copy(input, writer, Charset.defaultCharset());
    }

    public static void copy(InputStream input, Writer writer, Charset inputCharset) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(inputCharset));
        IOUtils.copy((Reader)reader, writer);
    }

    public static void copy(InputStream input, Writer writer, String inputCharsetName) throws IOException {
        IOUtils.copy(input, writer, Charsets.toCharset(inputCharsetName));
    }

    public static QueueInputStream copy(java.io.ByteArrayOutputStream outputStream2) throws IOException {
        Objects.requireNonNull(outputStream2, "outputStream");
        QueueInputStream in = new QueueInputStream();
        outputStream2.writeTo(in.newQueueOutputStream());
        return in;
    }

    public static long copy(Reader reader, Appendable output) throws IOException {
        return IOUtils.copy(reader, output, CharBuffer.allocate(8192));
    }

    public static long copy(Reader reader, Appendable output, CharBuffer buffer) throws IOException {
        int n;
        long count = 0L;
        while (-1 != (n = reader.read(buffer))) {
            buffer.flip();
            output.append(buffer, 0, n);
            count += (long)n;
        }
        return count;
    }

    @Deprecated
    public static void copy(Reader reader, OutputStream output) throws IOException {
        IOUtils.copy(reader, output, Charset.defaultCharset());
    }

    public static void copy(Reader reader, OutputStream output, Charset outputCharset) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.toCharset(outputCharset));
        IOUtils.copy(reader, (Writer)writer);
        writer.flush();
    }

    public static void copy(Reader reader, OutputStream output, String outputCharsetName) throws IOException {
        IOUtils.copy(reader, output, Charsets.toCharset(outputCharsetName));
    }

    public static int copy(Reader reader, Writer writer) throws IOException {
        long count = IOUtils.copyLarge(reader, writer);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int)count;
    }

    public static long copy(URL url, File file) throws IOException {
        try (OutputStream outputStream2 = Files.newOutputStream(Objects.requireNonNull(file, "file").toPath(), new OpenOption[0]);){
            long l = IOUtils.copy(url, outputStream2);
            return l;
        }
    }

    public static long copy(URL url, OutputStream outputStream2) throws IOException {
        try (InputStream inputStream2 = Objects.requireNonNull(url, "url").openStream();){
            long l = IOUtils.copyLarge(inputStream2, outputStream2);
            return l;
        }
    }

    public static long copyLarge(InputStream inputStream2, OutputStream outputStream2) throws IOException {
        return IOUtils.copy(inputStream2, outputStream2, 8192);
    }

    public static long copyLarge(InputStream inputStream2, OutputStream outputStream2, byte[] buffer) throws IOException {
        int n;
        Objects.requireNonNull(inputStream2, "inputStream");
        Objects.requireNonNull(outputStream2, "outputStream");
        long count = 0L;
        while (-1 != (n = inputStream2.read(buffer))) {
            outputStream2.write(buffer, 0, n);
            count += (long)n;
        }
        return count;
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length) throws IOException {
        return IOUtils.copyLarge(input, output, inputOffset, length, IOUtils.getScratchByteArray());
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length, byte[] buffer) throws IOException {
        int read;
        int bufferLength;
        if (inputOffset > 0L) {
            IOUtils.skipFully(input, inputOffset);
        }
        if (length == 0L) {
            return 0L;
        }
        int bytesToRead = bufferLength = buffer.length;
        if (length > 0L && length < (long)bufferLength) {
            bytesToRead = (int)length;
        }
        long totalRead = 0L;
        while (bytesToRead > 0 && -1 != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += (long)read;
            if (length <= 0L) continue;
            bytesToRead = (int)Math.min(length - totalRead, (long)bufferLength);
        }
        return totalRead;
    }

    public static long copyLarge(Reader reader, Writer writer) throws IOException {
        return IOUtils.copyLarge(reader, writer, IOUtils.getScratchCharArray());
    }

    public static long copyLarge(Reader reader, Writer writer, char[] buffer) throws IOException {
        int n;
        long count = 0L;
        while (-1 != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
            count += (long)n;
        }
        return count;
    }

    public static long copyLarge(Reader reader, Writer writer, long inputOffset, long length) throws IOException {
        return IOUtils.copyLarge(reader, writer, inputOffset, length, IOUtils.getScratchCharArray());
    }

    public static long copyLarge(Reader reader, Writer writer, long inputOffset, long length, char[] buffer) throws IOException {
        int read;
        if (inputOffset > 0L) {
            IOUtils.skipFully(reader, inputOffset);
        }
        if (length == 0L) {
            return 0L;
        }
        int bytesToRead = buffer.length;
        if (length > 0L && length < (long)buffer.length) {
            bytesToRead = (int)length;
        }
        long totalRead = 0L;
        while (bytesToRead > 0 && -1 != (read = reader.read(buffer, 0, bytesToRead))) {
            writer.write(buffer, 0, read);
            totalRead += (long)read;
            if (length <= 0L) continue;
            bytesToRead = (int)Math.min(length - totalRead, (long)buffer.length);
        }
        return totalRead;
    }

    private static byte[] fill0(byte[] arr) {
        Arrays.fill(arr, (byte)0);
        return arr;
    }

    private static char[] fill0(char[] arr) {
        Arrays.fill(arr, '\u0000');
        return arr;
    }

    static byte[] getScratchByteArray() {
        return IOUtils.fill0(SCRATCH_BYTE_BUFFER_RW.get());
    }

    static byte[] getScratchByteArrayWriteOnly() {
        return IOUtils.fill0(SCRATCH_BYTE_BUFFER_WO);
    }

    static char[] getScratchCharArray() {
        return IOUtils.fill0(SCRATCH_CHAR_BUFFER_RW.get());
    }

    static char[] getScratchCharArrayWriteOnly() {
        return IOUtils.fill0(SCRATCH_CHAR_BUFFER_WO);
    }

    public static int length(byte[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(char[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(CharSequence csq) {
        return csq == null ? 0 : csq.length();
    }

    public static int length(Object[] array) {
        return array == null ? 0 : array.length;
    }

    public static LineIterator lineIterator(InputStream input, Charset charset) {
        return new LineIterator(new InputStreamReader(input, Charsets.toCharset(charset)));
    }

    public static LineIterator lineIterator(InputStream input, String charsetName) {
        return IOUtils.lineIterator(input, Charsets.toCharset(charsetName));
    }

    public static LineIterator lineIterator(Reader reader) {
        return new LineIterator(reader);
    }

    public static int read(InputStream input, byte[] buffer) throws IOException {
        return IOUtils.read(input, buffer, 0, buffer.length);
    }

    public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        return IOUtils.read(input::read, buffer, offset, length);
    }

    static int read(IOTriFunction<byte[], Integer, Integer, Integer> input, byte[] buffer, int offset, int length) throws IOException {
        int location;
        int remaining;
        int count;
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        for (remaining = length; remaining > 0 && -1 != (count = input.apply(buffer, offset + (location = length - remaining), remaining).intValue()); remaining -= count) {
        }
        return length - remaining;
    }

    public static int read(ReadableByteChannel input, ByteBuffer buffer) throws IOException {
        int count;
        int length = buffer.remaining();
        while (buffer.remaining() > 0 && -1 != (count = input.read(buffer))) {
        }
        return length - buffer.remaining();
    }

    public static int read(Reader reader, char[] buffer) throws IOException {
        return IOUtils.read(reader, buffer, 0, buffer.length);
    }

    public static int read(Reader reader, char[] buffer, int offset, int length) throws IOException {
        int location;
        int remaining;
        int count;
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        for (remaining = length; remaining > 0 && -1 != (count = reader.read(buffer, offset + (location = length - remaining), remaining)); remaining -= count) {
        }
        return length - remaining;
    }

    public static void readFully(InputStream input, byte[] buffer) throws IOException {
        IOUtils.readFully(input, buffer, 0, buffer.length);
    }

    public static void readFully(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        int actual = IOUtils.read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    public static byte[] readFully(InputStream input, int length) throws IOException {
        byte[] buffer = IOUtils.byteArray(length);
        IOUtils.readFully(input, buffer, 0, buffer.length);
        return buffer;
    }

    public static void readFully(ReadableByteChannel input, ByteBuffer buffer) throws IOException {
        int expected = buffer.remaining();
        int actual = IOUtils.read(input, buffer);
        if (actual != expected) {
            throw new EOFException("Length to read: " + expected + " actual: " + actual);
        }
    }

    public static void readFully(Reader reader, char[] buffer) throws IOException {
        IOUtils.readFully(reader, buffer, 0, buffer.length);
    }

    public static void readFully(Reader reader, char[] buffer, int offset, int length) throws IOException {
        int actual = IOUtils.read(reader, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    @Deprecated
    public static List<String> readLines(InputStream input) throws UncheckedIOException {
        return IOUtils.readLines(input, Charset.defaultCharset());
    }

    public static List<String> readLines(InputStream input, Charset charset) throws UncheckedIOException {
        return IOUtils.readLines(new InputStreamReader(input, Charsets.toCharset(charset)));
    }

    public static List<String> readLines(InputStream input, String charsetName) throws UncheckedIOException {
        return IOUtils.readLines(input, Charsets.toCharset(charsetName));
    }

    public static List<String> readLines(Reader reader) throws UncheckedIOException {
        return IOUtils.toBufferedReader(reader).lines().collect(Collectors.toList());
    }

    public static byte[] resourceToByteArray(String name) throws IOException {
        return IOUtils.resourceToByteArray(name, null);
    }

    public static byte[] resourceToByteArray(String name, ClassLoader classLoader) throws IOException {
        return IOUtils.toByteArray(IOUtils.resourceToURL(name, classLoader));
    }

    public static String resourceToString(String name, Charset charset) throws IOException {
        return IOUtils.resourceToString(name, charset, null);
    }

    public static String resourceToString(String name, Charset charset, ClassLoader classLoader) throws IOException {
        return IOUtils.toString(IOUtils.resourceToURL(name, classLoader), charset);
    }

    public static URL resourceToURL(String name) throws IOException {
        return IOUtils.resourceToURL(name, null);
    }

    public static URL resourceToURL(String name, ClassLoader classLoader) throws IOException {
        URL resource;
        URL uRL = resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }
        return resource;
    }

    public static long skip(InputStream input, long toSkip) throws IOException {
        return IOUtils.skip(input, toSkip, IOUtils::getScratchByteArrayWriteOnly);
    }

    public static long skip(InputStream input, long toSkip, Supplier<byte[]> skipBufferSupplier) throws IOException {
        byte[] skipBuffer;
        long remain;
        long n;
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        for (remain = toSkip; remain > 0L && (n = (long)input.read(skipBuffer = skipBufferSupplier.get(), 0, (int)Math.min(remain, (long)skipBuffer.length))) >= 0L; remain -= n) {
        }
        return toSkip - remain;
    }

    public static long skip(ReadableByteChannel input, long toSkip) throws IOException {
        long remain;
        int n;
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        ByteBuffer skipByteBuffer = ByteBuffer.allocate((int)Math.min(toSkip, 8192L));
        for (remain = toSkip; remain > 0L; remain -= (long)n) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int)Math.min(remain, 8192L));
            n = input.read(skipByteBuffer);
            if (n == -1) break;
        }
        return toSkip - remain;
    }

    public static long skip(Reader reader, long toSkip) throws IOException {
        char[] charArray;
        long remain;
        long n;
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        for (remain = toSkip; remain > 0L && (n = (long)reader.read(charArray = IOUtils.getScratchCharArrayWriteOnly(), 0, (int)Math.min(remain, (long)charArray.length))) >= 0L; remain -= n) {
        }
        return toSkip - remain;
    }

    public static void skipFully(InputStream input, long toSkip) throws IOException {
        long skipped = IOUtils.skip(input, toSkip, IOUtils::getScratchByteArrayWriteOnly);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void skipFully(InputStream input, long toSkip, Supplier<byte[]> skipBufferSupplier) throws IOException {
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        long skipped = IOUtils.skip(input, toSkip, skipBufferSupplier);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void skipFully(ReadableByteChannel input, long toSkip) throws IOException {
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        long skipped = IOUtils.skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void skipFully(Reader reader, long toSkip) throws IOException {
        long skipped = IOUtils.skip(reader, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static InputStream toBufferedInputStream(InputStream input) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input);
    }

    public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input, size);
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
    }

    public static BufferedReader toBufferedReader(Reader reader, int size) {
        return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader, size);
    }

    public static byte[] toByteArray(InputStream inputStream2) throws IOException {
        try (UnsynchronizedByteArrayOutputStream ubaOutput = UnsynchronizedByteArrayOutputStream.builder().get();){
            byte[] byArray;
            try (ThresholdingOutputStream thresholdOutput = new ThresholdingOutputStream(Integer.MAX_VALUE, os -> {
                throw new IllegalArgumentException(String.format("Cannot read more than %,d into a byte array", Integer.MAX_VALUE));
            }, os -> ubaOutput);){
                IOUtils.copy(inputStream2, (OutputStream)thresholdOutput);
                byArray = ubaOutput.toByteArray();
            }
            return byArray;
        }
    }

    public static byte[] toByteArray(InputStream input, int size) throws IOException {
        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        return IOUtils.toByteArray(Objects.requireNonNull(input, "input")::read, size);
    }

    public static byte[] toByteArray(InputStream input, long size) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }
        return IOUtils.toByteArray(input, (int)size);
    }

    static byte[] toByteArray(IOTriFunction<byte[], Integer, Integer, Integer> input, int size) throws IOException {
        int offset;
        int read;
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }
        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] data = IOUtils.byteArray(size);
        for (offset = 0; offset < size && (read = input.apply(data, offset, size - offset).intValue()) != -1; offset += read) {
        }
        if (offset != size) {
            throw new IOException("Unexpected read size, current: " + offset + ", expected: " + size);
        }
        return data;
    }

    @Deprecated
    public static byte[] toByteArray(Reader reader) throws IOException {
        return IOUtils.toByteArray(reader, Charset.defaultCharset());
    }

    public static byte[] toByteArray(Reader reader, Charset charset) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();){
            IOUtils.copy(reader, (OutputStream)output, charset);
            byte[] byArray = output.toByteArray();
            return byArray;
        }
    }

    public static byte[] toByteArray(Reader reader, String charsetName) throws IOException {
        return IOUtils.toByteArray(reader, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static byte[] toByteArray(String input) {
        return input.getBytes(Charset.defaultCharset());
    }

    public static byte[] toByteArray(URI uri) throws IOException {
        return IOUtils.toByteArray(uri.toURL());
    }

    public static byte[] toByteArray(URL url) throws IOException {
        try (CloseableURLConnection urlConnection = CloseableURLConnection.open(url);){
            byte[] byArray = IOUtils.toByteArray(urlConnection);
            return byArray;
        }
    }

    public static byte[] toByteArray(URLConnection urlConnection) throws IOException {
        try (InputStream inputStream2 = urlConnection.getInputStream();){
            byte[] byArray = IOUtils.toByteArray(inputStream2);
            return byArray;
        }
    }

    @Deprecated
    public static char[] toCharArray(InputStream inputStream2) throws IOException {
        return IOUtils.toCharArray(inputStream2, Charset.defaultCharset());
    }

    public static char[] toCharArray(InputStream inputStream2, Charset charset) throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        IOUtils.copy(inputStream2, (Writer)writer, charset);
        return writer.toCharArray();
    }

    public static char[] toCharArray(InputStream inputStream2, String charsetName) throws IOException {
        return IOUtils.toCharArray(inputStream2, Charsets.toCharset(charsetName));
    }

    public static char[] toCharArray(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        IOUtils.copy(reader, (Writer)sw);
        return sw.toCharArray();
    }

    @Deprecated
    public static InputStream toInputStream(CharSequence input) {
        return IOUtils.toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(CharSequence input, Charset charset) {
        return IOUtils.toInputStream(input.toString(), charset);
    }

    public static InputStream toInputStream(CharSequence input, String charsetName) {
        return IOUtils.toInputStream(input, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static InputStream toInputStream(String input) {
        return IOUtils.toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(String input, Charset charset) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charset)));
    }

    public static InputStream toInputStream(String input, String charsetName) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charsetName)));
    }

    @Deprecated
    public static String toString(byte[] input) {
        return new String(input, Charset.defaultCharset());
    }

    public static String toString(byte[] input, String charsetName) {
        return new String(input, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static String toString(InputStream input) throws IOException {
        return IOUtils.toString(input, Charset.defaultCharset());
    }

    public static String toString(InputStream input, Charset charset) throws IOException {
        try (StringBuilderWriter sw = new StringBuilderWriter();){
            IOUtils.copy(input, (Writer)sw, charset);
            String string = sw.toString();
            return string;
        }
    }

    public static String toString(InputStream input, String charsetName) throws IOException {
        return IOUtils.toString(input, Charsets.toCharset(charsetName));
    }

    public static String toString(IOSupplier<InputStream> input, Charset charset) throws IOException {
        return IOUtils.toString(input, charset, () -> {
            throw new NullPointerException("input");
        });
    }

    public static String toString(IOSupplier<InputStream> input, Charset charset, IOSupplier<String> defaultString) throws IOException {
        if (input == null) {
            return defaultString.get();
        }
        try (InputStream inputStream2 = input.get();){
            String string = inputStream2 != null ? IOUtils.toString(inputStream2, charset) : defaultString.get();
            return string;
        }
    }

    public static String toString(Reader reader) throws IOException {
        try (StringBuilderWriter sw = new StringBuilderWriter();){
            IOUtils.copy(reader, (Writer)sw);
            String string = sw.toString();
            return string;
        }
    }

    @Deprecated
    public static String toString(URI uri) throws IOException {
        return IOUtils.toString(uri, Charset.defaultCharset());
    }

    public static String toString(URI uri, Charset encoding) throws IOException {
        return IOUtils.toString(uri.toURL(), Charsets.toCharset(encoding));
    }

    public static String toString(URI uri, String charsetName) throws IOException {
        return IOUtils.toString(uri, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static String toString(URL url) throws IOException {
        return IOUtils.toString(url, Charset.defaultCharset());
    }

    public static String toString(URL url, Charset encoding) throws IOException {
        return IOUtils.toString(url::openStream, encoding);
    }

    public static String toString(URL url, String charsetName) throws IOException {
        return IOUtils.toString(url, Charsets.toCharset(charsetName));
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    @Deprecated
    public static void write(byte[] data, Writer writer) throws IOException {
        IOUtils.write(data, writer, Charset.defaultCharset());
    }

    public static void write(byte[] data, Writer writer, Charset charset) throws IOException {
        if (data != null) {
            writer.write(new String(data, Charsets.toCharset(charset)));
        }
    }

    public static void write(byte[] data, Writer writer, String charsetName) throws IOException {
        IOUtils.write(data, writer, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static void write(char[] data, OutputStream output) throws IOException {
        IOUtils.write(data, output, Charset.defaultCharset());
    }

    public static void write(char[] data, OutputStream output, Charset charset) throws IOException {
        if (data != null) {
            IOUtils.write(new String(data), output, charset);
        }
    }

    public static void write(char[] data, OutputStream output, String charsetName) throws IOException {
        IOUtils.write(data, output, Charsets.toCharset(charsetName));
    }

    public static void write(char[] data, Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    @Deprecated
    public static void write(CharSequence data, OutputStream output) throws IOException {
        IOUtils.write(data, output, Charset.defaultCharset());
    }

    public static void write(CharSequence data, OutputStream output, Charset charset) throws IOException {
        if (data != null) {
            IOUtils.write(data.toString(), output, charset);
        }
    }

    public static void write(CharSequence data, OutputStream output, String charsetName) throws IOException {
        IOUtils.write(data, output, Charsets.toCharset(charsetName));
    }

    public static void write(CharSequence data, Writer writer) throws IOException {
        if (data != null) {
            IOUtils.write(data.toString(), writer);
        }
    }

    @Deprecated
    public static void write(String data, OutputStream output) throws IOException {
        IOUtils.write(data, output, Charset.defaultCharset());
    }

    public static void write(String data, OutputStream output, Charset charset) throws IOException {
        if (data != null) {
            Channels.newChannel(output).write(Charsets.toCharset(charset).encode(data));
        }
    }

    public static void write(String data, OutputStream output, String charsetName) throws IOException {
        IOUtils.write(data, output, Charsets.toCharset(charsetName));
    }

    public static void write(String data, Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    @Deprecated
    public static void write(StringBuffer data, OutputStream output) throws IOException {
        IOUtils.write(data, output, (String)null);
    }

    @Deprecated
    public static void write(StringBuffer data, OutputStream output, String charsetName) throws IOException {
        if (data != null) {
            IOUtils.write(data.toString(), output, Charsets.toCharset(charsetName));
        }
    }

    @Deprecated
    public static void write(StringBuffer data, Writer writer) throws IOException {
        if (data != null) {
            writer.write(data.toString());
        }
    }

    public static void writeChunked(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                int chunk = Math.min(bytes, 8192);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    public static void writeChunked(char[] data, Writer writer) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                int chunk = Math.min(bytes, 8192);
                writer.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    @Deprecated
    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output) throws IOException {
        IOUtils.writeLines(lines, lineEnding, output, Charset.defaultCharset());
    }

    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, Charset charset) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        if (StandardCharsets.UTF_16.equals(charset)) {
            charset = StandardCharsets.UTF_16BE;
        }
        byte[] eolBytes = lineEnding.getBytes(charset);
        for (Object line : lines) {
            if (line != null) {
                IOUtils.write(line.toString(), output, charset);
            }
            output.write(eolBytes);
        }
    }

    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, String charsetName) throws IOException {
        IOUtils.writeLines(lines, lineEnding, output, Charsets.toCharset(charsetName));
    }

    public static void writeLines(Collection<?> lines, String lineEnding, Writer writer) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        for (Object line : lines) {
            if (line != null) {
                writer.write(line.toString());
            }
            writer.write(lineEnding);
        }
    }

    public static Writer writer(Appendable appendable) {
        Objects.requireNonNull(appendable, "appendable");
        if (appendable instanceof Writer) {
            return (Writer)appendable;
        }
        if (appendable instanceof StringBuilder) {
            return new StringBuilderWriter((StringBuilder)appendable);
        }
        return new AppendableWriter<Appendable>(appendable);
    }

    @Deprecated
    public IOUtils() {
    }
}

