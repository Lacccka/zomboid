/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CollapsedStringAdapter
extends XmlAdapter<String, String> {
    @Override
    public String unmarshal(String text) {
        int s;
        if (text == null) {
            return null;
        }
        int len = text.length();
        for (s = 0; s < len && !CollapsedStringAdapter.isWhiteSpace(text.charAt(s)); ++s) {
        }
        if (s == len) {
            return text;
        }
        StringBuilder result = new StringBuilder(len);
        if (s != 0) {
            for (int i = 0; i < s; ++i) {
                result.append(text.charAt(i));
            }
            result.append(' ');
        }
        boolean inStripMode = true;
        for (int i = s + 1; i < len; ++i) {
            char ch = text.charAt(i);
            boolean b = CollapsedStringAdapter.isWhiteSpace(ch);
            if (inStripMode && b) continue;
            inStripMode = b;
            if (inStripMode) {
                result.append(' ');
                continue;
            }
            result.append(ch);
        }
        len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ') {
            result.setLength(len - 1);
        }
        return result.toString();
    }

    @Override
    public String marshal(String s) {
        return s;
    }

    protected static boolean isWhiteSpace(char ch) {
        if (ch > ' ') {
            return false;
        }
        return ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ';
    }
}

