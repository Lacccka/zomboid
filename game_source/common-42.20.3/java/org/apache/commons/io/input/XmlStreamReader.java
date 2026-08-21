/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.XmlStreamReaderException;

public class XmlStreamReader
extends Reader {
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final String US_ASCII = StandardCharsets.US_ASCII.name();
    private static final String UTF_16BE = StandardCharsets.UTF_16BE.name();
    private static final String UTF_16LE = StandardCharsets.UTF_16LE.name();
    private static final String UTF_32BE = "UTF-32BE";
    private static final String UTF_32LE = "UTF-32LE";
    private static final String UTF_16 = StandardCharsets.UTF_16.name();
    private static final String UTF_32 = "UTF-32";
    private static final String EBCDIC = "CP1047";
    private static final ByteOrderMark[] BOMS = new ByteOrderMark[]{ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE};
    private static final ByteOrderMark[] XML_GUESS_BYTES = new ByteOrderMark[]{new ByteOrderMark(UTF_8, 60, 63, 120, 109), new ByteOrderMark(UTF_16BE, 0, 60, 0, 63), new ByteOrderMark(UTF_16LE, 60, 0, 63, 0), new ByteOrderMark("UTF-32BE", 0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109), new ByteOrderMark("UTF-32LE", 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0), new ByteOrderMark("CP1047", 76, 111, 167, 148)};
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=[\"']?([.[^; \"']]*)[\"']?");
    public static final Pattern ENCODING_PATTERN = Pattern.compile("^<\\?xml\\s+(?:version\\s*=\\s*(?:(?:\"1\\.[0-9]+\")|(?:'1.[0-9]+'))\\s+)??encoding\\s*=\\s*((?:\"[A-Za-z0-9][A-Za-z0-9._+:-]*\")|(?:'[A-Za-z0-9][A-Za-z0-9._+:-]*'))", 8);
    private static final String RAW_EX_1 = "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch";
    private static final String RAW_EX_2 = "Illegal encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM";
    private static final String HTTP_EX_1 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be null";
    private static final String HTTP_EX_2 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch";
    private static final String HTTP_EX_3 = "Illegal encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Illegal MIME";
    private final Reader reader;
    private final String encoding;
    private final String defaultEncoding;

    public static Builder builder() {
        return new Builder();
    }

    static String getContentTypeEncoding(String httpContentType) {
        int i;
        String encoding = null;
        if (httpContentType != null && (i = httpContentType.indexOf(";")) > -1) {
            String postMime = httpContentType.substring(i + 1);
            Matcher m = CHARSET_PATTERN.matcher(postMime);
            encoding = m.find() ? m.group(1) : null;
            encoding = encoding != null ? encoding.toUpperCase(Locale.ROOT) : null;
        }
        return encoding;
    }

    static String getContentTypeMime(String httpContentType) {
        String mime = null;
        if (httpContentType != null) {
            int i = httpContentType.indexOf(";");
            mime = i >= 0 ? httpContentType.substring(0, i) : httpContentType;
            mime = mime.trim();
        }
        return mime;
    }

    private static String getXmlProlog(InputStream inputStream2, String guessedEnc) throws IOException {
        String encoding = null;
        if (guessedEnc != null) {
            byte[] bytes = IOUtils.byteArray();
            inputStream2.mark(8192);
            int offset = 0;
            int max = 8192;
            int c = inputStream2.read(bytes, offset, max);
            int firstGT = -1;
            String xmlProlog = "";
            while (c != -1 && firstGT == -1 && offset < 8192) {
                c = inputStream2.read(bytes, offset += c, max -= c);
                xmlProlog = new String(bytes, 0, offset, guessedEnc);
                firstGT = xmlProlog.indexOf(62);
            }
            if (firstGT == -1) {
                if (c == -1) {
                    throw new IOException("Unexpected end of XML stream");
                }
                throw new IOException("XML prolog or ROOT element not found on first " + offset + " bytes");
            }
            int bytesRead = offset;
            if (bytesRead > 0) {
                inputStream2.reset();
                BufferedReader bReader = new BufferedReader(new StringReader(xmlProlog.substring(0, firstGT + 1)));
                StringBuilder prolog = new StringBuilder();
                IOConsumer.forEach(bReader.lines(), l -> prolog.append((String)l).append(' '));
                Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find()) {
                    encoding = m.group(1).toUpperCase(Locale.ROOT);
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }

    static boolean isAppXml(String mime) {
        return mime != null && (mime.equals("application/xml") || mime.equals("application/xml-dtd") || mime.equals("application/xml-external-parsed-entity") || mime.startsWith("application/") && mime.endsWith("+xml"));
    }

    static boolean isTextXml(String mime) {
        return mime != null && (mime.equals("text/xml") || mime.equals("text/xml-external-parsed-entity") || mime.startsWith("text/") && mime.endsWith("+xml"));
    }

    @Deprecated
    public XmlStreamReader(File file) throws IOException {
        this(Objects.requireNonNull(file, "file").toPath());
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2) throws IOException {
        this(inputStream2, true);
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2, boolean lenient) throws IOException {
        this(inputStream2, lenient, null);
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2, boolean lenient, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        BOMInputStream bom = new BOMInputStream((InputStream)new BufferedInputStream(Objects.requireNonNull(inputStream2, "inputStream"), 8192), false, BOMS);
        BOMInputStream pis = new BOMInputStream((InputStream)bom, true, XML_GUESS_BYTES);
        this.encoding = this.processHttpStream(bom, pis, lenient);
        this.reader = new InputStreamReader((InputStream)pis, this.encoding);
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2, String httpContentType) throws IOException {
        this(inputStream2, httpContentType, true);
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2, String httpContentType, boolean lenient) throws IOException {
        this(inputStream2, httpContentType, lenient, null);
    }

    @Deprecated
    public XmlStreamReader(InputStream inputStream2, String httpContentType, boolean lenient, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        BOMInputStream bom = new BOMInputStream((InputStream)new BufferedInputStream(Objects.requireNonNull(inputStream2, "inputStream"), 8192), false, BOMS);
        BOMInputStream pis = new BOMInputStream((InputStream)bom, true, XML_GUESS_BYTES);
        this.encoding = this.processHttpStream(bom, pis, lenient, httpContentType);
        this.reader = new InputStreamReader((InputStream)pis, this.encoding);
    }

    @Deprecated
    public XmlStreamReader(Path file) throws IOException {
        this(Files.newInputStream(Objects.requireNonNull(file, "file"), new OpenOption[0]));
    }

    public XmlStreamReader(URL url) throws IOException {
        this(Objects.requireNonNull(url, "url").openConnection(), null);
    }

    public XmlStreamReader(URLConnection urlConnection, String defaultEncoding) throws IOException {
        Objects.requireNonNull(urlConnection, "urlConnection");
        this.defaultEncoding = defaultEncoding;
        boolean lenient = true;
        String contentType = urlConnection.getContentType();
        InputStream inputStream2 = urlConnection.getInputStream();
        BOMInputStream bomInput = ((BOMInputStream.Builder)BOMInputStream.builder().setInputStream(new BufferedInputStream(inputStream2, 8192))).setInclude(false).setByteOrderMarks(BOMS).get();
        BOMInputStream piInput = ((BOMInputStream.Builder)BOMInputStream.builder().setInputStream(new BufferedInputStream(bomInput, 8192))).setInclude(true).setByteOrderMarks(XML_GUESS_BYTES).get();
        this.encoding = urlConnection instanceof HttpURLConnection || contentType != null ? this.processHttpStream(bomInput, piInput, true, contentType) : this.processHttpStream(bomInput, piInput, true);
        this.reader = new InputStreamReader((InputStream)piInput, this.encoding);
    }

    String calculateHttpEncoding(String bomEnc, String xmlGuessEnc, String xmlEnc, boolean lenient, String httpContentType) throws IOException {
        if (lenient && xmlEnc != null) {
            return xmlEnc;
        }
        String cTMime = XmlStreamReader.getContentTypeMime(httpContentType);
        String cTEnc = XmlStreamReader.getContentTypeEncoding(httpContentType);
        boolean appXml = XmlStreamReader.isAppXml(cTMime);
        boolean textXml = XmlStreamReader.isTextXml(cTMime);
        if (!appXml && !textXml) {
            String msg = MessageFormat.format(HTTP_EX_3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }
        if (cTEnc == null) {
            if (appXml) {
                return this.calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
            }
            return this.defaultEncoding == null ? US_ASCII : this.defaultEncoding;
        }
        if (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE)) {
            if (bomEnc != null) {
                String msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return cTEnc;
        }
        if (cTEnc.equals(UTF_16)) {
            if (bomEnc != null && bomEnc.startsWith(UTF_16)) {
                return bomEnc;
            }
            String msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }
        if (cTEnc.equals(UTF_32BE) || cTEnc.equals(UTF_32LE)) {
            if (bomEnc != null) {
                String msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return cTEnc;
        }
        if (cTEnc.equals(UTF_32)) {
            if (bomEnc != null && bomEnc.startsWith(UTF_32)) {
                return bomEnc;
            }
            String msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }
        return cTEnc;
    }

    String calculateRawEncoding(String bomEnc, String xmlGuessEnc, String xmlEnc) throws IOException {
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                return this.defaultEncoding == null ? UTF_8 : this.defaultEncoding;
            }
            if (xmlEnc.equals(UTF_16) && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc.equals(UTF_16LE))) {
                return xmlGuessEnc;
            }
            return xmlEnc;
        }
        if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }
        if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_16) && !xmlEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }
        if (bomEnc.equals(UTF_32BE) || bomEnc.equals(UTF_32LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_32) && !xmlEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }
        String msg = MessageFormat.format(RAW_EX_2, bomEnc, xmlGuessEnc, xmlEnc);
        throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    private String doLenientDetection(String httpContentType, XmlStreamReaderException ex) throws IOException {
        String encoding;
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            httpContentType = httpContentType.substring("text/html".length());
            httpContentType = "text/xml" + httpContentType;
            try {
                return this.calculateHttpEncoding(ex.getBomEncoding(), ex.getXmlGuessEncoding(), ex.getXmlEncoding(), true, httpContentType);
            }
            catch (XmlStreamReaderException ex2) {
                ex = ex2;
            }
        }
        if ((encoding = ex.getXmlEncoding()) == null) {
            encoding = ex.getContentTypeEncoding();
        }
        if (encoding == null) {
            encoding = this.defaultEncoding == null ? UTF_8 : this.defaultEncoding;
        }
        return encoding;
    }

    public String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    private String processHttpStream(BOMInputStream bomInput, BOMInputStream piInput, boolean lenient) throws IOException {
        String bomEnc = bomInput.getBOMCharsetName();
        String xmlGuessEnc = piInput.getBOMCharsetName();
        String xmlEnc = XmlStreamReader.getXmlProlog(piInput, xmlGuessEnc);
        try {
            return this.calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
        }
        catch (XmlStreamReaderException ex) {
            if (lenient) {
                return this.doLenientDetection(null, ex);
            }
            throw ex;
        }
    }

    private String processHttpStream(BOMInputStream bomInput, BOMInputStream piInput, boolean lenient, String httpContentType) throws IOException {
        String bomEnc = bomInput.getBOMCharsetName();
        String xmlGuessEnc = piInput.getBOMCharsetName();
        String xmlEnc = XmlStreamReader.getXmlProlog(piInput, xmlGuessEnc);
        try {
            return this.calculateHttpEncoding(bomEnc, xmlGuessEnc, xmlEnc, lenient, httpContentType);
        }
        catch (XmlStreamReaderException ex) {
            if (lenient) {
                return this.doLenientDetection(httpContentType, ex);
            }
            throw ex;
        }
    }

    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        return this.reader.read(buf, offset, len);
    }

    public static class Builder
    extends AbstractStreamBuilder<XmlStreamReader, Builder> {
        private boolean nullCharset = true;
        private boolean lenient = true;
        private String httpContentType;

        @Override
        public XmlStreamReader get() throws IOException {
            String defaultEncoding = this.nullCharset ? null : this.getCharset().name();
            return this.httpContentType == null ? new XmlStreamReader(this.getInputStream(), this.lenient, defaultEncoding) : new XmlStreamReader(this.getInputStream(), this.httpContentType, this.lenient, defaultEncoding);
        }

        @Override
        public Builder setCharset(Charset charset) {
            this.nullCharset = charset == null;
            return (Builder)super.setCharset(charset);
        }

        @Override
        public Builder setCharset(String charset) {
            this.nullCharset = charset == null;
            return (Builder)super.setCharset(Charsets.toCharset(charset, this.getCharsetDefault()));
        }

        public Builder setHttpContentType(String httpContentType) {
            this.httpContentType = httpContentType;
            return this;
        }

        public Builder setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }
    }
}

