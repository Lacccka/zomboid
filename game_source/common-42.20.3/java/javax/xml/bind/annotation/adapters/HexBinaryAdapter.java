/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation.adapters;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class HexBinaryAdapter
extends XmlAdapter<String, byte[]> {
    @Override
    public byte[] unmarshal(String s) {
        if (s == null) {
            return null;
        }
        return DatatypeConverter.parseHexBinary(s);
    }

    @Override
    public String marshal(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return DatatypeConverter.printHexBinary(bytes);
    }
}

