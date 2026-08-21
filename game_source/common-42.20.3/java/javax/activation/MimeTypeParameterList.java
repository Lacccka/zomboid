/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import javax.activation.MimeTypeParseException;

public class MimeTypeParameterList {
    private Hashtable parameters = new Hashtable();
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";

    public MimeTypeParameterList() {
    }

    public MimeTypeParameterList(String parameterList) throws MimeTypeParseException {
        this.parse(parameterList);
    }

    protected void parse(String parameterList) throws MimeTypeParseException {
        char c;
        if (parameterList == null) {
            return;
        }
        int length = parameterList.length();
        if (length <= 0) {
            return;
        }
        int i = MimeTypeParameterList.skipWhiteSpace(parameterList, 0);
        while (i < length && (c = parameterList.charAt(i)) == ';') {
            String value;
            ++i;
            if ((i = MimeTypeParameterList.skipWhiteSpace(parameterList, i)) >= length) {
                return;
            }
            int lastIndex = i;
            while (i < length && MimeTypeParameterList.isTokenChar(parameterList.charAt(i))) {
                ++i;
            }
            String name = parameterList.substring(lastIndex, i).toLowerCase(Locale.ENGLISH);
            if ((i = MimeTypeParameterList.skipWhiteSpace(parameterList, i)) >= length || parameterList.charAt(i) != '=') {
                throw new MimeTypeParseException("Couldn't find the '=' that separates a parameter name from its value.");
            }
            ++i;
            if ((i = MimeTypeParameterList.skipWhiteSpace(parameterList, i)) >= length) {
                throw new MimeTypeParseException("Couldn't find a value for parameter named " + name);
            }
            c = parameterList.charAt(i);
            if (c == '\"') {
                if (++i >= length) {
                    throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                }
                lastIndex = i;
                while (i < length && (c = parameterList.charAt(i)) != '\"') {
                    if (c == '\\') {
                        ++i;
                    }
                    ++i;
                }
                if (c != '\"') {
                    throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                }
                value = MimeTypeParameterList.unquote(parameterList.substring(lastIndex, i));
                ++i;
            } else if (MimeTypeParameterList.isTokenChar(c)) {
                lastIndex = i;
                while (i < length && MimeTypeParameterList.isTokenChar(parameterList.charAt(i))) {
                    ++i;
                }
                value = parameterList.substring(lastIndex, i);
            } else {
                throw new MimeTypeParseException("Unexpected character encountered at index " + i);
            }
            this.parameters.put(name, value);
            i = MimeTypeParameterList.skipWhiteSpace(parameterList, i);
        }
        if (i < length) {
            throw new MimeTypeParseException("More characters encountered in input than expected.");
        }
    }

    public int size() {
        return this.parameters.size();
    }

    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }

    public String get(String name) {
        return (String)this.parameters.get(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public void set(String name, String value) {
        this.parameters.put(name.trim().toLowerCase(Locale.ENGLISH), value);
    }

    public void remove(String name) {
        this.parameters.remove(name.trim().toLowerCase(Locale.ENGLISH));
    }

    public Enumeration getNames() {
        return this.parameters.keys();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.ensureCapacity(this.parameters.size() * 16);
        Enumeration keys2 = this.parameters.keys();
        while (keys2.hasMoreElements()) {
            String key = (String)keys2.nextElement();
            buffer.append("; ");
            buffer.append(key);
            buffer.append('=');
            buffer.append(MimeTypeParameterList.quote((String)this.parameters.get(key)));
        }
        return buffer.toString();
    }

    private static boolean isTokenChar(char c) {
        return c > ' ' && c < '\u007f' && TSPECIALS.indexOf(c) < 0;
    }

    private static int skipWhiteSpace(String rawdata, int i) {
        int length = rawdata.length();
        while (i < length && Character.isWhitespace(rawdata.charAt(i))) {
            ++i;
        }
        return i;
    }

    private static String quote(String value) {
        boolean needsQuotes = false;
        int length = value.length();
        for (int i = 0; i < length && !needsQuotes; ++i) {
            needsQuotes = !MimeTypeParameterList.isTokenChar(value.charAt(i));
        }
        if (needsQuotes) {
            StringBuffer buffer = new StringBuffer();
            buffer.ensureCapacity((int)((double)length * 1.5));
            buffer.append('\"');
            for (int i = 0; i < length; ++i) {
                char c = value.charAt(i);
                if (c == '\\' || c == '\"') {
                    buffer.append('\\');
                }
                buffer.append(c);
            }
            buffer.append('\"');
            return buffer.toString();
        }
        return value;
    }

    private static String unquote(String value) {
        int valueLength = value.length();
        StringBuffer buffer = new StringBuffer();
        buffer.ensureCapacity(valueLength);
        boolean escaped = false;
        for (int i = 0; i < valueLength; ++i) {
            char currentChar = value.charAt(i);
            if (!escaped && currentChar != '\\') {
                buffer.append(currentChar);
                continue;
            }
            if (escaped) {
                buffer.append(currentChar);
                escaped = false;
                continue;
            }
            escaped = true;
        }
        return buffer.toString();
    }
}

