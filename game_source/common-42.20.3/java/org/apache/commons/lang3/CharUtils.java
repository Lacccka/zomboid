/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class CharUtils {
    private static final String[] CHAR_STRING_ARRAY = new String[128];
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final char LF = '\n';
    public static final char CR = '\r';
    public static final char NUL = '\u0000';

    public static int compare(char x, char y) {
        return x - y;
    }

    public static boolean isAscii(char ch) {
        return ch < '\u0080';
    }

    public static boolean isAsciiAlpha(char ch) {
        return CharUtils.isAsciiAlphaUpper(ch) || CharUtils.isAsciiAlphaLower(ch);
    }

    public static boolean isAsciiAlphaLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiAlphanumeric(char ch) {
        return CharUtils.isAsciiAlpha(ch) || CharUtils.isAsciiNumeric(ch);
    }

    public static boolean isAsciiAlphaUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isAsciiControl(char ch) {
        return ch < ' ' || ch == '\u007f';
    }

    public static boolean isAsciiNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAsciiPrintable(char ch) {
        return ch >= ' ' && ch < '\u007f';
    }

    public static char toChar(Character ch) {
        return Objects.requireNonNull(ch, "ch").charValue();
    }

    public static char toChar(Character ch, char defaultValue) {
        return ch != null ? ch.charValue() : defaultValue;
    }

    public static char toChar(String str) {
        Validate.notEmpty(str, "The String must not be empty", new Object[0]);
        return str.charAt(0);
    }

    public static char toChar(String str, char defaultValue) {
        return StringUtils.isEmpty(str) ? defaultValue : str.charAt(0);
    }

    @Deprecated
    public static Character toCharacterObject(char c) {
        return Character.valueOf(c);
    }

    public static Character toCharacterObject(String str) {
        return StringUtils.isEmpty(str) ? null : Character.valueOf(str.charAt(0));
    }

    public static int toIntValue(char ch) {
        if (!CharUtils.isAsciiNumeric(ch)) {
            throw new IllegalArgumentException("The character " + ch + " is not in the range '0' - '9'");
        }
        return ch - 48;
    }

    public static int toIntValue(char ch, int defaultValue) {
        return CharUtils.isAsciiNumeric(ch) ? ch - 48 : defaultValue;
    }

    public static int toIntValue(Character ch) {
        return CharUtils.toIntValue(CharUtils.toChar(ch));
    }

    public static int toIntValue(Character ch, int defaultValue) {
        return ch != null ? CharUtils.toIntValue(ch.charValue(), defaultValue) : defaultValue;
    }

    public static String toString(char ch) {
        if (ch < CHAR_STRING_ARRAY.length) {
            return CHAR_STRING_ARRAY[ch];
        }
        return String.valueOf(ch);
    }

    public static String toString(Character ch) {
        return ch != null ? CharUtils.toString(ch.charValue()) : null;
    }

    public static String unicodeEscaped(char ch) {
        return "\\u" + HEX_DIGITS[ch >> 12 & 0xF] + HEX_DIGITS[ch >> 8 & 0xF] + HEX_DIGITS[ch >> 4 & 0xF] + HEX_DIGITS[ch & 0xF];
    }

    public static String unicodeEscaped(Character ch) {
        return ch != null ? CharUtils.unicodeEscaped(ch.charValue()) : null;
    }

    @Deprecated
    public CharUtils() {
    }

    static {
        ArrayUtils.setAll(CHAR_STRING_ARRAY, i -> String.valueOf((char)i));
    }
}

