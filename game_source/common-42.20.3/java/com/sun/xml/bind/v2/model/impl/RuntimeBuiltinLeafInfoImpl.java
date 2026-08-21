/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.ByteArrayDataSource;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;
import com.sun.xml.bind.v2.util.DataSourceSource;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.MarshalException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;

public abstract class RuntimeBuiltinLeafInfoImpl<T>
extends BuiltinLeafInfoImpl<Type, Class>
implements RuntimeBuiltinLeafInfo,
Transducer<T> {
    private static final Logger logger;
    public static final Map<Type, RuntimeBuiltinLeafInfoImpl<?>> LEAVES;
    public static final RuntimeBuiltinLeafInfoImpl<String> STRING;
    private static final String DATE = "date";
    public static final List<RuntimeBuiltinLeafInfoImpl<?>> builtinBeanInfos;
    public static final String MAP_ANYURI_TO_URI = "mapAnyUriToUri";
    public static final String USE_OLD_GMONTH_MAPPING = "jaxb.ri.useOldGmonthMapping";
    private static final Map<QName, String> xmlGregorianCalendarFormatString;
    private static final Map<QName, Integer> xmlGregorianCalendarFieldRef;

    private RuntimeBuiltinLeafInfoImpl(Class type, QName ... typeNames) {
        super(type, typeNames);
        LEAVES.put(type, this);
    }

    @Override
    public final Class getClazz() {
        return (Class)this.getType();
    }

    public final Transducer getTransducer() {
        return this;
    }

    @Override
    public boolean useNamespace() {
        return false;
    }

    @Override
    public void declareNamespace(T o, XMLSerializer w) throws AccessorException {
    }

    @Override
    public QName getTypeName(T instance) {
        return null;
    }

    private static QName createXS(String typeName) {
        return new QName("http://www.w3.org/2001/XMLSchema", typeName);
    }

    private static byte[] decodeBase64(CharSequence text) {
        if (text instanceof Base64Data) {
            Base64Data base64Data = (Base64Data)text;
            return base64Data.getExact();
        }
        return DatatypeConverterImpl._parseBase64Binary(text.toString());
    }

    private static void checkXmlGregorianCalendarFieldRef(QName type, XMLGregorianCalendar cal) throws MarshalException {
        StringBuilder buf = new StringBuilder();
        int bitField = xmlGregorianCalendarFieldRef.get(type);
        boolean l = true;
        int pos = 0;
        while (bitField != 0) {
            int bit = bitField & 1;
            bitField >>>= 4;
            ++pos;
            if (bit != 1) continue;
            switch (pos) {
                case 1: {
                    if (cal.getSecond() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_SEC);
                    break;
                }
                case 2: {
                    if (cal.getMinute() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_MIN);
                    break;
                }
                case 3: {
                    if (cal.getHour() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_HR);
                    break;
                }
                case 4: {
                    if (cal.getDay() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_DAY);
                    break;
                }
                case 5: {
                    if (cal.getMonth() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_MONTH);
                    break;
                }
                case 6: {
                    if (cal.getYear() != Integer.MIN_VALUE) break;
                    buf.append("  ").append((Object)Messages.XMLGREGORIANCALENDAR_YEAR);
                    break;
                }
            }
        }
        if (buf.length() > 0) {
            throw new MarshalException(Messages.XMLGREGORIANCALENDAR_INVALID.format(type.getLocalPart()) + buf.toString());
        }
    }

    static {
        QName[] qNameArray;
        logger = Util.getClassLogger();
        LEAVES = new HashMap();
        String MAP_ANYURI_TO_URI_VALUE = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return System.getProperty(RuntimeBuiltinLeafInfoImpl.MAP_ANYURI_TO_URI);
            }
        });
        if (MAP_ANYURI_TO_URI_VALUE == null) {
            QName[] qNameArray2 = new QName[10];
            qNameArray2[0] = RuntimeBuiltinLeafInfoImpl.createXS("string");
            qNameArray2[1] = RuntimeBuiltinLeafInfoImpl.createXS("anySimpleType");
            qNameArray2[2] = RuntimeBuiltinLeafInfoImpl.createXS("normalizedString");
            qNameArray2[3] = RuntimeBuiltinLeafInfoImpl.createXS("anyURI");
            qNameArray2[4] = RuntimeBuiltinLeafInfoImpl.createXS("token");
            qNameArray2[5] = RuntimeBuiltinLeafInfoImpl.createXS("language");
            qNameArray2[6] = RuntimeBuiltinLeafInfoImpl.createXS("Name");
            qNameArray2[7] = RuntimeBuiltinLeafInfoImpl.createXS("NCName");
            qNameArray2[8] = RuntimeBuiltinLeafInfoImpl.createXS("NMTOKEN");
            qNameArray = qNameArray2;
            qNameArray2[9] = RuntimeBuiltinLeafInfoImpl.createXS("ENTITY");
        } else {
            QName[] qNameArray3 = new QName[9];
            qNameArray3[0] = RuntimeBuiltinLeafInfoImpl.createXS("string");
            qNameArray3[1] = RuntimeBuiltinLeafInfoImpl.createXS("anySimpleType");
            qNameArray3[2] = RuntimeBuiltinLeafInfoImpl.createXS("normalizedString");
            qNameArray3[3] = RuntimeBuiltinLeafInfoImpl.createXS("token");
            qNameArray3[4] = RuntimeBuiltinLeafInfoImpl.createXS("language");
            qNameArray3[5] = RuntimeBuiltinLeafInfoImpl.createXS("Name");
            qNameArray3[6] = RuntimeBuiltinLeafInfoImpl.createXS("NCName");
            qNameArray3[7] = RuntimeBuiltinLeafInfoImpl.createXS("NMTOKEN");
            qNameArray = qNameArray3;
            qNameArray3[8] = RuntimeBuiltinLeafInfoImpl.createXS("ENTITY");
        }
        QName[] qnames = qNameArray;
        STRING = new StringImplImpl(String.class, qnames);
        ArrayList<RuntimeBuiltinLeafInfoImpl> secondaryList = new ArrayList<RuntimeBuiltinLeafInfoImpl>();
        secondaryList.add(new StringImpl<Character>(Character.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("unsignedShort")}){

            @Override
            public Character parse(CharSequence text) {
                return Character.valueOf((char)DatatypeConverterImpl._parseInt(text));
            }

            @Override
            public String print(Character v) {
                return Integer.toString(v.charValue());
            }
        });
        secondaryList.add(new StringImpl<Calendar>(Calendar.class, new QName[]{DatatypeConstants.DATETIME}){

            @Override
            public Calendar parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString());
            }

            @Override
            public String print(Calendar v) {
                return DatatypeConverterImpl._printDateTime(v);
            }
        });
        secondaryList.add(new StringImpl<GregorianCalendar>(GregorianCalendar.class, new QName[]{DatatypeConstants.DATETIME}){

            @Override
            public GregorianCalendar parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString());
            }

            @Override
            public String print(GregorianCalendar v) {
                return DatatypeConverterImpl._printDateTime(v);
            }
        });
        secondaryList.add(new StringImpl<Date>(Date.class, new QName[]{DatatypeConstants.DATETIME}){

            @Override
            public Date parse(CharSequence text) {
                return DatatypeConverterImpl._parseDateTime(text.toString()).getTime();
            }

            @Override
            public String print(Date v) {
                XMLSerializer xs = XMLSerializer.getInstance();
                QName type = xs.getSchemaType();
                GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
                cal.setTime(v);
                if (type != null && "http://www.w3.org/2001/XMLSchema".equals(type.getNamespaceURI()) && RuntimeBuiltinLeafInfoImpl.DATE.equals(type.getLocalPart())) {
                    return DatatypeConverterImpl._printDate(cal);
                }
                return DatatypeConverterImpl._printDateTime(cal);
            }
        });
        secondaryList.add(new StringImpl<File>(File.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("string")}){

            @Override
            public File parse(CharSequence text) {
                return new File(WhiteSpaceProcessor.trim(text).toString());
            }

            @Override
            public String print(File v) {
                return v.getPath();
            }
        });
        secondaryList.add(new StringImpl<URL>(URL.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("anyURI")}){

            @Override
            public URL parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    return new URL(WhiteSpaceProcessor.trim(text).toString());
                }
                catch (MalformedURLException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            @Override
            public String print(URL v) {
                return v.toExternalForm();
            }
        });
        if (MAP_ANYURI_TO_URI_VALUE == null) {
            secondaryList.add(new StringImpl<URI>(URI.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("string")}){

                @Override
                public URI parse(CharSequence text) throws SAXException {
                    try {
                        return new URI(text.toString());
                    }
                    catch (URISyntaxException e) {
                        UnmarshallingContext.getInstance().handleError(e);
                        return null;
                    }
                }

                @Override
                public String print(URI v) {
                    return v.toString();
                }
            });
        }
        secondaryList.add(new StringImpl<Class>(Class.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("string")}){

            @Override
            public Class parse(CharSequence text) throws SAXException {
                TODO.checkSpec("JSR222 Issue #42");
                try {
                    String name = WhiteSpaceProcessor.trim(text).toString();
                    ClassLoader cl = UnmarshallingContext.getInstance().classLoader;
                    if (cl == null) {
                        cl = Thread.currentThread().getContextClassLoader();
                    }
                    if (cl != null) {
                        return cl.loadClass(name);
                    }
                    return Class.forName(name);
                }
                catch (ClassNotFoundException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            @Override
            public String print(Class v) {
                return v.getName();
            }
        });
        secondaryList.add(new PcdataImpl<Image>(Image.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("base64Binary")}){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public Image parse(CharSequence text) throws SAXException {
                BufferedImage bufferedImage;
                InputStream is = text instanceof Base64Data ? ((Base64Data)text).getInputStream() : new ByteArrayInputStream(RuntimeBuiltinLeafInfoImpl.decodeBase64(text));
                try {
                    bufferedImage = ImageIO.read(is);
                }
                catch (Throwable throwable) {
                    try {
                        is.close();
                        throw throwable;
                    }
                    catch (IOException e) {
                        UnmarshallingContext.getInstance().handleError(e);
                        return null;
                    }
                }
                is.close();
                return bufferedImage;
            }

            private BufferedImage convertToBufferedImage(Image image) throws IOException {
                if (image instanceof BufferedImage) {
                    return (BufferedImage)image;
                }
                MediaTracker tracker = new MediaTracker(new Component(){});
                tracker.addImage(image, 0);
                try {
                    tracker.waitForAll();
                }
                catch (InterruptedException e) {
                    throw new IOException(e.getMessage());
                }
                BufferedImage bufImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
                Graphics2D g = bufImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                return bufImage;
            }

            public Base64Data print(Image v) {
                ByteArrayOutputStreamEx imageData = new ByteArrayOutputStreamEx();
                XMLSerializer xs = XMLSerializer.getInstance();
                String mimeType = xs.getXMIMEContentType();
                if (mimeType == null || mimeType.startsWith("image/*")) {
                    mimeType = "image/png";
                }
                try {
                    Iterator<ImageWriter> itr = ImageIO.getImageWritersByMIMEType(mimeType);
                    if (!itr.hasNext()) {
                        xs.handleEvent(new ValidationEventImpl(1, Messages.NO_IMAGE_WRITER.format(mimeType), xs.getCurrentLocation(null)));
                        throw new RuntimeException("no encoder for MIME type " + mimeType);
                    }
                    ImageWriter w = itr.next();
                    ImageOutputStream os = ImageIO.createImageOutputStream(imageData);
                    w.setOutput(os);
                    w.write(this.convertToBufferedImage(v));
                    os.close();
                    w.dispose();
                }
                catch (IOException e) {
                    xs.handleError(e);
                    throw new RuntimeException(e);
                }
                Base64Data bd = new Base64Data();
                imageData.set(bd, mimeType);
                return bd;
            }
        });
        secondaryList.add(new PcdataImpl<DataHandler>(DataHandler.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("base64Binary")}){

            @Override
            public DataHandler parse(CharSequence text) {
                if (text instanceof Base64Data) {
                    return ((Base64Data)text).getDataHandler();
                }
                return new DataHandler(new ByteArrayDataSource(RuntimeBuiltinLeafInfoImpl.decodeBase64(text), UnmarshallingContext.getInstance().getXMIMEContentType()));
            }

            public Base64Data print(DataHandler v) {
                Base64Data bd = new Base64Data();
                bd.set(v);
                return bd;
            }
        });
        secondaryList.add(new PcdataImpl<Source>(Source.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("base64Binary")}){

            @Override
            public Source parse(CharSequence text) throws SAXException {
                try {
                    if (text instanceof Base64Data) {
                        return new DataSourceSource(((Base64Data)text).getDataHandler());
                    }
                    return new DataSourceSource(new ByteArrayDataSource(RuntimeBuiltinLeafInfoImpl.decodeBase64(text), UnmarshallingContext.getInstance().getXMIMEContentType()));
                }
                catch (MimeTypeParseException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            public Base64Data print(Source v) {
                DataSource ds;
                String dsct;
                XMLSerializer xs = XMLSerializer.getInstance();
                Base64Data bd = new Base64Data();
                String contentType = xs.getXMIMEContentType();
                MimeType mt = null;
                if (contentType != null) {
                    try {
                        mt = new MimeType(contentType);
                    }
                    catch (MimeTypeParseException e) {
                        xs.handleError(e);
                    }
                }
                if (v instanceof DataSourceSource && (dsct = (ds = ((DataSourceSource)v).getDataSource()).getContentType()) != null && (contentType == null || contentType.equals(dsct))) {
                    bd.set(new DataHandler(ds));
                    return bd;
                }
                String charset = null;
                if (mt != null) {
                    charset = mt.getParameter("charset");
                }
                if (charset == null) {
                    charset = "UTF-8";
                }
                try {
                    ByteArrayOutputStreamEx baos = new ByteArrayOutputStreamEx();
                    Transformer tr = xs.getIdentityTransformer();
                    String defaultEncoding = tr.getOutputProperty("encoding");
                    tr.setOutputProperty("encoding", charset);
                    tr.transform(v, new StreamResult(new OutputStreamWriter((OutputStream)baos, charset)));
                    tr.setOutputProperty("encoding", defaultEncoding);
                    baos.set(bd, "application/xml; charset=" + charset);
                    return bd;
                }
                catch (TransformerException e) {
                    xs.handleError(e);
                }
                catch (UnsupportedEncodingException e) {
                    xs.handleError(e);
                }
                bd.set(new byte[0], "application/xml");
                return bd;
            }
        });
        secondaryList.add(new StringImpl<XMLGregorianCalendar>(XMLGregorianCalendar.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("anySimpleType"), DatatypeConstants.DATE, DatatypeConstants.DATETIME, DatatypeConstants.TIME, DatatypeConstants.GMONTH, DatatypeConstants.GDAY, DatatypeConstants.GYEAR, DatatypeConstants.GYEARMONTH, DatatypeConstants.GMONTHDAY}){

            @Override
            public String print(XMLGregorianCalendar cal) {
                XMLSerializer xs = XMLSerializer.getInstance();
                QName type = xs.getSchemaType();
                if (type != null) {
                    try {
                        RuntimeBuiltinLeafInfoImpl.checkXmlGregorianCalendarFieldRef(type, cal);
                        String format = (String)xmlGregorianCalendarFormatString.get(type);
                        if (format != null) {
                            return this.format(format, cal);
                        }
                    }
                    catch (MarshalException e) {
                        xs.handleEvent(new ValidationEventImpl(0, e.getMessage(), xs.getCurrentLocation(null)));
                        return "";
                    }
                }
                return cal.toXMLFormat();
            }

            @Override
            public XMLGregorianCalendar parse(CharSequence lexical) throws SAXException {
                try {
                    return DatatypeConverterImpl.getDatatypeFactory().newXMLGregorianCalendar(lexical.toString().trim());
                }
                catch (Exception e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            private String format(String format, XMLGregorianCalendar value) {
                StringBuilder buf = new StringBuilder();
                int fidx = 0;
                int flen = format.length();
                block9: while (fidx < flen) {
                    char fch;
                    if ((fch = format.charAt(fidx++)) != '%') {
                        buf.append(fch);
                        continue;
                    }
                    switch (format.charAt(fidx++)) {
                        case 'Y': {
                            this.printNumber(buf, value.getEonAndYear(), 4);
                            continue block9;
                        }
                        case 'M': {
                            this.printNumber(buf, value.getMonth(), 2);
                            continue block9;
                        }
                        case 'D': {
                            this.printNumber(buf, value.getDay(), 2);
                            continue block9;
                        }
                        case 'h': {
                            this.printNumber(buf, value.getHour(), 2);
                            continue block9;
                        }
                        case 'm': {
                            this.printNumber(buf, value.getMinute(), 2);
                            continue block9;
                        }
                        case 's': {
                            this.printNumber(buf, value.getSecond(), 2);
                            if (value.getFractionalSecond() == null) continue block9;
                            String frac = value.getFractionalSecond().toPlainString();
                            buf.append(frac.substring(1, frac.length()));
                            continue block9;
                        }
                        case 'z': {
                            int offset = value.getTimezone();
                            if (offset == 0) {
                                buf.append('Z');
                                continue block9;
                            }
                            if (offset == Integer.MIN_VALUE) continue block9;
                            if (offset < 0) {
                                buf.append('-');
                                offset *= -1;
                            } else {
                                buf.append('+');
                            }
                            this.printNumber(buf, offset / 60, 2);
                            buf.append(':');
                            this.printNumber(buf, offset % 60, 2);
                            continue block9;
                        }
                    }
                    throw new InternalError();
                }
                return buf.toString();
            }

            private void printNumber(StringBuilder out, BigInteger number, int nDigits) {
                String s = number.toString();
                for (int i = s.length(); i < nDigits; ++i) {
                    out.append('0');
                }
                out.append(s);
            }

            private void printNumber(StringBuilder out, int number, int nDigits) {
                String s = String.valueOf(number);
                for (int i = s.length(); i < nDigits; ++i) {
                    out.append('0');
                }
                out.append(s);
            }

            @Override
            public QName getTypeName(XMLGregorianCalendar cal) {
                return cal.getXMLSchemaType();
            }
        });
        ArrayList<RuntimeBuiltinLeafInfoImpl<String>> primaryList = new ArrayList<RuntimeBuiltinLeafInfoImpl<String>>();
        primaryList.add(STRING);
        primaryList.add(new StringImpl<Boolean>(Boolean.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("boolean")}){

            @Override
            public Boolean parse(CharSequence text) {
                return DatatypeConverterImpl._parseBoolean(text);
            }

            @Override
            public String print(Boolean v) {
                return v.toString();
            }
        });
        primaryList.add(new PcdataImpl<byte[]>(byte[].class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("base64Binary"), RuntimeBuiltinLeafInfoImpl.createXS("hexBinary")}){

            @Override
            public byte[] parse(CharSequence text) {
                return RuntimeBuiltinLeafInfoImpl.decodeBase64(text);
            }

            public Base64Data print(byte[] v) {
                XMLSerializer w = XMLSerializer.getInstance();
                Base64Data bd = new Base64Data();
                String mimeType = w.getXMIMEContentType();
                bd.set(v, mimeType);
                return bd;
            }
        });
        primaryList.add(new StringImpl<Byte>(Byte.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("byte")}){

            @Override
            public Byte parse(CharSequence text) {
                return DatatypeConverterImpl._parseByte(text);
            }

            @Override
            public String print(Byte v) {
                return DatatypeConverterImpl._printByte(v);
            }
        });
        primaryList.add(new StringImpl<Short>(Short.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("short"), RuntimeBuiltinLeafInfoImpl.createXS("unsignedByte")}){

            @Override
            public Short parse(CharSequence text) {
                return DatatypeConverterImpl._parseShort(text);
            }

            @Override
            public String print(Short v) {
                return DatatypeConverterImpl._printShort(v);
            }
        });
        primaryList.add(new StringImpl<Integer>(Integer.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("int"), RuntimeBuiltinLeafInfoImpl.createXS("unsignedShort")}){

            @Override
            public Integer parse(CharSequence text) {
                return DatatypeConverterImpl._parseInt(text);
            }

            @Override
            public String print(Integer v) {
                return DatatypeConverterImpl._printInt(v);
            }
        });
        primaryList.add(new StringImpl<Long>(Long.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("long"), RuntimeBuiltinLeafInfoImpl.createXS("unsignedInt")}){

            @Override
            public Long parse(CharSequence text) {
                return DatatypeConverterImpl._parseLong(text);
            }

            @Override
            public String print(Long v) {
                return DatatypeConverterImpl._printLong(v);
            }
        });
        primaryList.add(new StringImpl<Float>(Float.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("float")}){

            @Override
            public Float parse(CharSequence text) {
                return Float.valueOf(DatatypeConverterImpl._parseFloat(text.toString()));
            }

            @Override
            public String print(Float v) {
                return DatatypeConverterImpl._printFloat(v.floatValue());
            }
        });
        primaryList.add(new StringImpl<Double>(Double.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("double")}){

            @Override
            public Double parse(CharSequence text) {
                return DatatypeConverterImpl._parseDouble(text);
            }

            @Override
            public String print(Double v) {
                return DatatypeConverterImpl._printDouble(v);
            }
        });
        primaryList.add(new StringImpl<BigInteger>(BigInteger.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("integer"), RuntimeBuiltinLeafInfoImpl.createXS("positiveInteger"), RuntimeBuiltinLeafInfoImpl.createXS("negativeInteger"), RuntimeBuiltinLeafInfoImpl.createXS("nonPositiveInteger"), RuntimeBuiltinLeafInfoImpl.createXS("nonNegativeInteger"), RuntimeBuiltinLeafInfoImpl.createXS("unsignedLong")}){

            @Override
            public BigInteger parse(CharSequence text) {
                return DatatypeConverterImpl._parseInteger(text);
            }

            @Override
            public String print(BigInteger v) {
                return DatatypeConverterImpl._printInteger(v);
            }
        });
        primaryList.add(new StringImpl<BigDecimal>(BigDecimal.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("decimal")}){

            @Override
            public BigDecimal parse(CharSequence text) {
                return DatatypeConverterImpl._parseDecimal(text.toString());
            }

            @Override
            public String print(BigDecimal v) {
                return DatatypeConverterImpl._printDecimal(v);
            }
        });
        primaryList.add(new StringImpl<QName>(QName.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("QName")}){

            @Override
            public QName parse(CharSequence text) throws SAXException {
                try {
                    return DatatypeConverterImpl._parseQName(text.toString(), UnmarshallingContext.getInstance());
                }
                catch (IllegalArgumentException e) {
                    UnmarshallingContext.getInstance().handleError(e);
                    return null;
                }
            }

            @Override
            public String print(QName v) {
                return DatatypeConverterImpl._printQName(v, XMLSerializer.getInstance().getNamespaceContext());
            }

            @Override
            public boolean useNamespace() {
                return true;
            }

            @Override
            public void declareNamespace(QName v, XMLSerializer w) {
                w.getNamespaceContext().declareNamespace(v.getNamespaceURI(), v.getPrefix(), false);
            }
        });
        if (MAP_ANYURI_TO_URI_VALUE != null) {
            primaryList.add(new StringImpl<URI>(URI.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("anyURI")}){

                @Override
                public URI parse(CharSequence text) throws SAXException {
                    try {
                        return new URI(text.toString());
                    }
                    catch (URISyntaxException e) {
                        UnmarshallingContext.getInstance().handleError(e);
                        return null;
                    }
                }

                @Override
                public String print(URI v) {
                    return v.toString();
                }
            });
        }
        primaryList.add(new StringImpl<Duration>(Duration.class, new QName[]{RuntimeBuiltinLeafInfoImpl.createXS("duration")}){

            @Override
            public String print(Duration duration) {
                return duration.toString();
            }

            @Override
            public Duration parse(CharSequence lexical) {
                TODO.checkSpec("JSR222 Issue #42");
                return DatatypeConverterImpl.getDatatypeFactory().newDuration(lexical.toString());
            }
        });
        primaryList.add(new StringImpl<Void>(Void.class, new QName[0]){

            @Override
            public String print(Void value) {
                return "";
            }

            @Override
            public Void parse(CharSequence lexical) {
                return null;
            }
        });
        ArrayList<RuntimeBuiltinLeafInfoImpl<String>> l = new ArrayList<RuntimeBuiltinLeafInfoImpl<String>>(secondaryList.size() + primaryList.size() + 1);
        l.addAll(secondaryList);
        try {
            l.add(new UUIDImpl());
        }
        catch (LinkageError linkageError) {
            // empty catch block
        }
        l.addAll(primaryList);
        builtinBeanInfos = Collections.unmodifiableList(l);
        Map<QName, String> m = xmlGregorianCalendarFormatString = new HashMap<QName, String>();
        m.put(DatatypeConstants.DATETIME, "%Y-%M-%DT%h:%m:%s%z");
        m.put(DatatypeConstants.DATE, "%Y-%M-%D%z");
        m.put(DatatypeConstants.TIME, "%h:%m:%s%z");
        String oldGmonthMappingProperty = AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return System.getProperty(RuntimeBuiltinLeafInfoImpl.USE_OLD_GMONTH_MAPPING);
            }
        });
        if (oldGmonthMappingProperty == null) {
            m.put(DatatypeConstants.GMONTH, "--%M%z");
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Old GMonth mapping used.");
            }
            m.put(DatatypeConstants.GMONTH, "--%M--%z");
        }
        m.put(DatatypeConstants.GDAY, "---%D%z");
        m.put(DatatypeConstants.GYEAR, "%Y%z");
        m.put(DatatypeConstants.GYEARMONTH, "%Y-%M%z");
        m.put(DatatypeConstants.GMONTHDAY, "--%M-%D%z");
        Map<QName, Integer> f = xmlGregorianCalendarFieldRef = new HashMap<QName, Integer>();
        f.put(DatatypeConstants.DATETIME, 0x1111111);
        f.put(DatatypeConstants.DATE, 0x1111000);
        f.put(DatatypeConstants.TIME, 0x1000111);
        f.put(DatatypeConstants.GDAY, 0x1001000);
        f.put(DatatypeConstants.GMONTH, 0x1010000);
        f.put(DatatypeConstants.GYEAR, 0x1100000);
        f.put(DatatypeConstants.GYEARMONTH, 0x1110000);
        f.put(DatatypeConstants.GMONTHDAY, 0x1011000);
    }

    private static class StringImplImpl
    extends StringImpl<String> {
        public StringImplImpl(Class type, QName[] typeNames) {
            super(type, typeNames);
        }

        @Override
        public String parse(CharSequence text) {
            return text.toString();
        }

        @Override
        public String print(String s) {
            return s;
        }

        @Override
        public final void writeText(XMLSerializer w, String o, String fieldName) throws IOException, SAXException, XMLStreamException {
            w.text(o, fieldName);
        }

        @Override
        public final void writeLeafElement(XMLSerializer w, Name tagName, String o, String fieldName) throws IOException, SAXException, XMLStreamException {
            w.leafElement(tagName, o, fieldName);
        }
    }

    private static class UUIDImpl
    extends StringImpl<UUID> {
        public UUIDImpl() {
            super(UUID.class, RuntimeBuiltinLeafInfoImpl.createXS("string"));
        }

        @Override
        public UUID parse(CharSequence text) throws SAXException {
            TODO.checkSpec("JSR222 Issue #42");
            try {
                return UUID.fromString(WhiteSpaceProcessor.trim(text).toString());
            }
            catch (IllegalArgumentException e) {
                UnmarshallingContext.getInstance().handleError(e);
                return null;
            }
        }

        @Override
        public String print(UUID v) {
            return v.toString();
        }
    }

    private static abstract class PcdataImpl<T>
    extends RuntimeBuiltinLeafInfoImpl<T> {
        protected PcdataImpl(Class type, QName ... typeNames) {
            super(type, typeNames);
        }

        public abstract Pcdata print(T var1) throws AccessorException;

        @Override
        public final void writeText(XMLSerializer w, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            w.text((Pcdata)this.print((Object)o), fieldName);
        }

        @Override
        public final void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            w.leafElement(tagName, (Pcdata)this.print((Object)o), fieldName);
        }
    }

    private static abstract class StringImpl<T>
    extends RuntimeBuiltinLeafInfoImpl<T> {
        protected StringImpl(Class type, QName ... typeNames) {
            super(type, typeNames);
        }

        public abstract String print(T var1) throws AccessorException;

        @Override
        public void writeText(XMLSerializer w, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            w.text((String)this.print((Object)o), fieldName);
        }

        @Override
        public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
            w.leafElement(tagName, (String)this.print((Object)o), fieldName);
        }
    }
}

