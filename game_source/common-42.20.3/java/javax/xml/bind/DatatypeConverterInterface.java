/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public interface DatatypeConverterInterface {
    public String parseString(String var1);

    public BigInteger parseInteger(String var1);

    public int parseInt(String var1);

    public long parseLong(String var1);

    public short parseShort(String var1);

    public BigDecimal parseDecimal(String var1);

    public float parseFloat(String var1);

    public double parseDouble(String var1);

    public boolean parseBoolean(String var1);

    public byte parseByte(String var1);

    public QName parseQName(String var1, NamespaceContext var2);

    public Calendar parseDateTime(String var1);

    public byte[] parseBase64Binary(String var1);

    public byte[] parseHexBinary(String var1);

    public long parseUnsignedInt(String var1);

    public int parseUnsignedShort(String var1);

    public Calendar parseTime(String var1);

    public Calendar parseDate(String var1);

    public String parseAnySimpleType(String var1);

    public String printString(String var1);

    public String printInteger(BigInteger var1);

    public String printInt(int var1);

    public String printLong(long var1);

    public String printShort(short var1);

    public String printDecimal(BigDecimal var1);

    public String printFloat(float var1);

    public String printDouble(double var1);

    public String printBoolean(boolean var1);

    public String printByte(byte var1);

    public String printQName(QName var1, NamespaceContext var2);

    public String printDateTime(Calendar var1);

    public String printBase64Binary(byte[] var1);

    public String printHexBinary(byte[] var1);

    public String printUnsignedInt(long var1);

    public String printUnsignedShort(int var1);

    public String printTime(Calendar var1);

    public String printDate(Calendar var1);

    public String printAnySimpleType(String var1);
}

