/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ResultParser;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VCardResultParser
extends ResultParser {
    private static final Pattern BEGIN_VCARD = Pattern.compile("BEGIN:VCARD", 2);
    private static final Pattern VCARD_LIKE_DATE = Pattern.compile("\\d{4}-?\\d{2}-?\\d{2}");
    private static final Pattern CR_LF_SPACE_TAB = Pattern.compile("\r\n[ \t]");
    private static final Pattern NEWLINE_ESCAPE = Pattern.compile("\\\\[nN]");
    private static final Pattern VCARD_ESCAPES = Pattern.compile("\\\\([,;\\\\])");
    private static final Pattern EQUALS = Pattern.compile("=");
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final Pattern UNESCAPED_SEMICOLONS = Pattern.compile("(?<!\\\\);+");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern SEMICOLON_OR_COMMA = Pattern.compile("[;,]");

    @Override
    public AddressBookParsedResult parse(Result result) {
        String[] geo;
        List<String> nicknameString;
        String rawText = VCardResultParser.getMassagedText(result);
        Matcher m = BEGIN_VCARD.matcher(rawText);
        if (!m.find() || m.start() != 0) {
            return null;
        }
        List<List<String>> names = VCardResultParser.matchVCardPrefixedField("FN", rawText, true, false);
        if (names == null) {
            names = VCardResultParser.matchVCardPrefixedField("N", rawText, true, false);
            VCardResultParser.formatNames(names);
        }
        String[] nicknames = (nicknameString = VCardResultParser.matchSingleVCardPrefixedField("NICKNAME", rawText, true, false)) == null ? null : COMMA.split(nicknameString.get(0));
        List<List<String>> phoneNumbers = VCardResultParser.matchVCardPrefixedField("TEL", rawText, true, false);
        List<List<String>> emails = VCardResultParser.matchVCardPrefixedField("EMAIL", rawText, true, false);
        List<String> note = VCardResultParser.matchSingleVCardPrefixedField("NOTE", rawText, false, false);
        List<List<String>> addresses = VCardResultParser.matchVCardPrefixedField("ADR", rawText, true, true);
        List<String> org = VCardResultParser.matchSingleVCardPrefixedField("ORG", rawText, true, true);
        List<String> birthday = VCardResultParser.matchSingleVCardPrefixedField("BDAY", rawText, true, false);
        if (birthday != null && !VCardResultParser.isLikeVCardDate(birthday.get(0))) {
            birthday = null;
        }
        List<String> title = VCardResultParser.matchSingleVCardPrefixedField("TITLE", rawText, true, false);
        List<List<String>> urls2 = VCardResultParser.matchVCardPrefixedField("URL", rawText, true, false);
        List<String> instantMessenger = VCardResultParser.matchSingleVCardPrefixedField("IMPP", rawText, true, false);
        List<String> geoString = VCardResultParser.matchSingleVCardPrefixedField("GEO", rawText, true, false);
        String[] stringArray = geo = geoString == null ? null : SEMICOLON_OR_COMMA.split(geoString.get(0));
        if (geo != null && geo.length != 2) {
            geo = null;
        }
        return new AddressBookParsedResult(VCardResultParser.toPrimaryValues(names), nicknames, null, VCardResultParser.toPrimaryValues(phoneNumbers), VCardResultParser.toTypes(phoneNumbers), VCardResultParser.toPrimaryValues(emails), VCardResultParser.toTypes(emails), VCardResultParser.toPrimaryValue(instantMessenger), VCardResultParser.toPrimaryValue(note), VCardResultParser.toPrimaryValues(addresses), VCardResultParser.toTypes(addresses), VCardResultParser.toPrimaryValue(org), VCardResultParser.toPrimaryValue(birthday), VCardResultParser.toPrimaryValue(title), VCardResultParser.toPrimaryValues(urls2), geo);
    }

    static List<List<String>> matchVCardPrefixedField(CharSequence prefix, String rawText, boolean trim, boolean parseFieldDivider) {
        ArrayList matches = null;
        int i = 0;
        int max = rawText.length();
        while (i < max) {
            Matcher matcher = Pattern.compile("(?:^|\n)" + prefix + "(?:;([^:]*))?:", 2).matcher(rawText);
            if (i > 0) {
                --i;
            }
            if (!matcher.find(i)) break;
            i = matcher.end(0);
            String metadataString = matcher.group(1);
            ArrayList<String> metadata = null;
            boolean quotedPrintable = false;
            String quotedPrintableCharset = null;
            if (metadataString != null) {
                for (String metadatum : SEMICOLON.split(metadataString)) {
                    if (metadata == null) {
                        metadata = new ArrayList<String>(1);
                    }
                    metadata.add(metadatum);
                    String[] metadatumTokens = EQUALS.split(metadatum, 2);
                    if (metadatumTokens.length <= 1) continue;
                    String key = metadatumTokens[0];
                    String value = metadatumTokens[1];
                    if ("ENCODING".equalsIgnoreCase(key) && "QUOTED-PRINTABLE".equalsIgnoreCase(value)) {
                        quotedPrintable = true;
                        continue;
                    }
                    if (!"CHARSET".equalsIgnoreCase(key)) continue;
                    quotedPrintableCharset = value;
                }
            }
            int matchStart = i;
            while ((i = rawText.indexOf(10, i)) >= 0) {
                if (i < rawText.length() - 1 && (rawText.charAt(i + 1) == ' ' || rawText.charAt(i + 1) == '\t')) {
                    i += 2;
                    continue;
                }
                if (!quotedPrintable || (i < 1 || rawText.charAt(i - 1) != '=') && (i < 2 || rawText.charAt(i - 2) != '=')) break;
                ++i;
            }
            if (i < 0) {
                i = max;
                continue;
            }
            if (i > matchStart) {
                if (matches == null) {
                    matches = new ArrayList(1);
                }
                if (i >= 1 && rawText.charAt(i - 1) == '\r') {
                    --i;
                }
                String element = rawText.substring(matchStart, i);
                if (trim) {
                    element = element.trim();
                }
                if (quotedPrintable) {
                    element = VCardResultParser.decodeQuotedPrintable(element, quotedPrintableCharset);
                    if (parseFieldDivider) {
                        element = UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
                    }
                } else {
                    if (parseFieldDivider) {
                        element = UNESCAPED_SEMICOLONS.matcher(element).replaceAll("\n").trim();
                    }
                    element = CR_LF_SPACE_TAB.matcher(element).replaceAll("");
                    element = NEWLINE_ESCAPE.matcher(element).replaceAll("\n");
                    element = VCARD_ESCAPES.matcher(element).replaceAll("$1");
                }
                if (metadata == null) {
                    ArrayList<String> match = new ArrayList<String>(1);
                    match.add(element);
                    matches.add(match);
                } else {
                    metadata.add(0, element);
                    matches.add(metadata);
                }
                ++i;
                continue;
            }
            ++i;
        }
        return matches;
    }

    private static String decodeQuotedPrintable(CharSequence value, String charset) {
        int length = value.length();
        StringBuilder result = new StringBuilder(length);
        ByteArrayOutputStream fragmentBuffer = new ByteArrayOutputStream();
        block4: for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '\n': 
                case '\r': {
                    continue block4;
                }
                case '=': {
                    char nextChar;
                    if (i >= length - 2 || (nextChar = value.charAt(i + 1)) == '\r' || nextChar == '\n') continue block4;
                    char nextNextChar = value.charAt(i + 2);
                    int firstDigit = VCardResultParser.parseHexDigit(nextChar);
                    int secondDigit = VCardResultParser.parseHexDigit(nextNextChar);
                    if (firstDigit >= 0 && secondDigit >= 0) {
                        fragmentBuffer.write((firstDigit << 4) + secondDigit);
                    }
                    i += 2;
                    continue block4;
                }
                default: {
                    VCardResultParser.maybeAppendFragment(fragmentBuffer, charset, result);
                    result.append(c);
                }
            }
        }
        VCardResultParser.maybeAppendFragment(fragmentBuffer, charset, result);
        return result.toString();
    }

    private static void maybeAppendFragment(ByteArrayOutputStream fragmentBuffer, String charset, StringBuilder result) {
        if (fragmentBuffer.size() > 0) {
            String fragment;
            byte[] fragmentBytes = fragmentBuffer.toByteArray();
            if (charset == null) {
                fragment = new String(fragmentBytes, Charset.forName("UTF-8"));
            } else {
                try {
                    fragment = new String(fragmentBytes, charset);
                }
                catch (UnsupportedEncodingException e) {
                    fragment = new String(fragmentBytes, Charset.forName("UTF-8"));
                }
            }
            fragmentBuffer.reset();
            result.append(fragment);
        }
    }

    static List<String> matchSingleVCardPrefixedField(CharSequence prefix, String rawText, boolean trim, boolean parseFieldDivider) {
        List<List<String>> values2 = VCardResultParser.matchVCardPrefixedField(prefix, rawText, trim, parseFieldDivider);
        return values2 == null || values2.isEmpty() ? null : values2.get(0);
    }

    private static String toPrimaryValue(List<String> list) {
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private static String[] toPrimaryValues(Collection<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>(lists.size());
        for (List<String> list : lists) {
            String value = list.get(0);
            if (value == null || value.isEmpty()) continue;
            result.add(value);
        }
        return result.toArray(new String[lists.size()]);
    }

    private static String[] toTypes(Collection<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>(lists.size());
        for (List<String> list : lists) {
            String type = null;
            for (int i = 1; i < list.size(); ++i) {
                String metadatum = list.get(i);
                int equals = metadatum.indexOf(61);
                if (equals < 0) {
                    type = metadatum;
                    break;
                }
                if (!"TYPE".equalsIgnoreCase(metadatum.substring(0, equals))) continue;
                type = metadatum.substring(equals + 1);
                break;
            }
            result.add(type);
        }
        return result.toArray(new String[lists.size()]);
    }

    private static boolean isLikeVCardDate(CharSequence value) {
        return value == null || VCARD_LIKE_DATE.matcher(value).matches();
    }

    private static void formatNames(Iterable<List<String>> names) {
        if (names != null) {
            for (List<String> list : names) {
                int end;
                String name = list.get(0);
                String[] components = new String[5];
                int start = 0;
                for (int componentIndex = 0; componentIndex < components.length - 1 && (end = name.indexOf(59, start)) >= 0; ++componentIndex) {
                    components[componentIndex] = name.substring(start, end);
                    start = end + 1;
                }
                components[componentIndex] = name.substring(start);
                StringBuilder newName = new StringBuilder(100);
                VCardResultParser.maybeAppendComponent(components, 3, newName);
                VCardResultParser.maybeAppendComponent(components, 1, newName);
                VCardResultParser.maybeAppendComponent(components, 2, newName);
                VCardResultParser.maybeAppendComponent(components, 0, newName);
                VCardResultParser.maybeAppendComponent(components, 4, newName);
                list.set(0, newName.toString().trim());
            }
        }
    }

    private static void maybeAppendComponent(String[] components, int i, StringBuilder newName) {
        if (components[i] != null && !components[i].isEmpty()) {
            if (newName.length() > 0) {
                newName.append(' ');
            }
            newName.append(components[i]);
        }
    }
}

