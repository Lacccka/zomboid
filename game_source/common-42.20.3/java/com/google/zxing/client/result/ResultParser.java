/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookAUResultParser;
import com.google.zxing.client.result.AddressBookDoCoMoResultParser;
import com.google.zxing.client.result.BizcardResultParser;
import com.google.zxing.client.result.BookmarkDoCoMoResultParser;
import com.google.zxing.client.result.EmailAddressResultParser;
import com.google.zxing.client.result.EmailDoCoMoResultParser;
import com.google.zxing.client.result.ExpandedProductResultParser;
import com.google.zxing.client.result.GeoResultParser;
import com.google.zxing.client.result.ISBNResultParser;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ProductResultParser;
import com.google.zxing.client.result.SMSMMSResultParser;
import com.google.zxing.client.result.SMSTOMMSTOResultParser;
import com.google.zxing.client.result.SMTPResultParser;
import com.google.zxing.client.result.TelResultParser;
import com.google.zxing.client.result.TextParsedResult;
import com.google.zxing.client.result.URIResultParser;
import com.google.zxing.client.result.URLTOResultParser;
import com.google.zxing.client.result.VCardResultParser;
import com.google.zxing.client.result.VEventResultParser;
import com.google.zxing.client.result.VINResultParser;
import com.google.zxing.client.result.WifiResultParser;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ResultParser {
    private static final ResultParser[] PARSERS = new ResultParser[]{new BookmarkDoCoMoResultParser(), new AddressBookDoCoMoResultParser(), new EmailDoCoMoResultParser(), new AddressBookAUResultParser(), new VCardResultParser(), new BizcardResultParser(), new VEventResultParser(), new EmailAddressResultParser(), new SMTPResultParser(), new TelResultParser(), new SMSMMSResultParser(), new SMSTOMMSTOResultParser(), new GeoResultParser(), new WifiResultParser(), new URLTOResultParser(), new URIResultParser(), new ISBNResultParser(), new ProductResultParser(), new ExpandedProductResultParser(), new VINResultParser()};
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern AMPERSAND = Pattern.compile("&");
    private static final Pattern EQUALS = Pattern.compile("=");
    private static final String BYTE_ORDER_MARK = "\ufeff";

    public abstract ParsedResult parse(Result var1);

    protected static String getMassagedText(Result result) {
        String text = result.getText();
        if (text.startsWith(BYTE_ORDER_MARK)) {
            text = text.substring(1);
        }
        return text;
    }

    public static ParsedResult parseResult(Result theResult) {
        for (ResultParser parser : PARSERS) {
            ParsedResult result = parser.parse(theResult);
            if (result == null) continue;
            return result;
        }
        return new TextParsedResult(theResult.getText(), null);
    }

    protected static void maybeAppend(String value, StringBuilder result) {
        if (value != null) {
            result.append('\n');
            result.append(value);
        }
    }

    protected static void maybeAppend(String[] value, StringBuilder result) {
        if (value != null) {
            for (String s : value) {
                result.append('\n');
                result.append(s);
            }
        }
    }

    protected static String[] maybeWrap(String value) {
        String[] stringArray;
        if (value == null) {
            stringArray = null;
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = value;
        }
        return stringArray;
    }

    protected static String unescapeBackslash(String escaped) {
        int backslash = escaped.indexOf(92);
        if (backslash < 0) {
            return escaped;
        }
        int max = escaped.length();
        StringBuilder unescaped = new StringBuilder(max - 1);
        unescaped.append(escaped.toCharArray(), 0, backslash);
        boolean nextIsEscaped = false;
        for (int i = backslash; i < max; ++i) {
            char c = escaped.charAt(i);
            if (nextIsEscaped || c != '\\') {
                unescaped.append(c);
                nextIsEscaped = false;
                continue;
            }
            nextIsEscaped = true;
        }
        return unescaped.toString();
    }

    protected static int parseHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 97);
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 65);
        }
        return -1;
    }

    protected static boolean isStringOfDigits(CharSequence value, int length) {
        return value != null && length > 0 && length == value.length() && DIGITS.matcher(value).matches();
    }

    protected static boolean isSubstringOfDigits(CharSequence value, int offset, int length) {
        if (value == null || length <= 0) {
            return false;
        }
        int max = offset + length;
        return value.length() >= max && DIGITS.matcher(value.subSequence(offset, max)).matches();
    }

    static Map<String, String> parseNameValuePairs(String uri) {
        int paramStart = uri.indexOf(63);
        if (paramStart < 0) {
            return null;
        }
        HashMap<String, String> result = new HashMap<String, String>(3);
        for (String keyValue : AMPERSAND.split(uri.substring(paramStart + 1))) {
            ResultParser.appendKeyValue(keyValue, result);
        }
        return result;
    }

    private static void appendKeyValue(CharSequence keyValue, Map<String, String> result) {
        String[] keyValueTokens = EQUALS.split(keyValue, 2);
        if (keyValueTokens.length == 2) {
            String key = keyValueTokens[0];
            String value = keyValueTokens[1];
            try {
                value = ResultParser.urlDecode(value);
                result.put(key, value);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
    }

    static String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee);
        }
    }

    static String[] matchPrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        ArrayList<String> matches = null;
        int i = 0;
        int max = rawText.length();
        while (i < max && (i = rawText.indexOf(prefix, i)) >= 0) {
            int start = i += prefix.length();
            boolean more = true;
            while (more) {
                if ((i = rawText.indexOf(endChar, i)) < 0) {
                    i = rawText.length();
                    more = false;
                    continue;
                }
                if (ResultParser.countPrecedingBackslashes(rawText, i) % 2 != 0) {
                    ++i;
                    continue;
                }
                if (matches == null) {
                    matches = new ArrayList<String>(3);
                }
                String element = ResultParser.unescapeBackslash(rawText.substring(start, i));
                if (trim) {
                    element = element.trim();
                }
                if (!element.isEmpty()) {
                    matches.add(element);
                }
                ++i;
                more = false;
            }
        }
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return matches.toArray(new String[matches.size()]);
    }

    private static int countPrecedingBackslashes(CharSequence s, int pos) {
        int count = 0;
        for (int i = pos - 1; i >= 0 && s.charAt(i) == '\\'; --i) {
            ++count;
        }
        return count;
    }

    static String matchSinglePrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        String[] matches = ResultParser.matchPrefixedField(prefix, rawText, endChar, trim);
        return matches == null ? null : matches[0];
    }
}

