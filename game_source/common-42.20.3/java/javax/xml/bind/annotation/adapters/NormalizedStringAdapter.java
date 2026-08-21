/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class NormalizedStringAdapter
extends XmlAdapter<String, String> {
    @Override
    public String unmarshal(String text) {
        int i;
        if (text == null) {
            return null;
        }
        for (i = text.length() - 1; i >= 0 && !NormalizedStringAdapter.isWhiteSpaceExceptSpace(text.charAt(i)); --i) {
        }
        if (i < 0) {
            return text;
        }
        char[] buf = text.toCharArray();
        buf[i--] = 32;
        while (i >= 0) {
            if (NormalizedStringAdapter.isWhiteSpaceExceptSpace(buf[i])) {
                buf[i] = 32;
            }
            --i;
        }
        return new String(buf);
    }

    @Override
    public String marshal(String s) {
        return s;
    }

    protected static boolean isWhiteSpaceExceptSpace(char ch) {
        if (ch >= ' ') {
            return false;
        }
        return ch == '\t' || ch == '\n' || ch == '\r';
    }
}

