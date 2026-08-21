/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.VINParsedResult;
import java.util.regex.Pattern;

public final class VINResultParser
extends ResultParser {
    private static final Pattern IOQ = Pattern.compile("[IOQ]");
    private static final Pattern AZ09 = Pattern.compile("[A-Z0-9]{17}");

    @Override
    public VINParsedResult parse(Result result) {
        if (result.getBarcodeFormat() != BarcodeFormat.CODE_39) {
            return null;
        }
        String rawText = result.getText();
        if (!AZ09.matcher(rawText = IOQ.matcher(rawText).replaceAll("").trim()).matches()) {
            return null;
        }
        try {
            if (!VINResultParser.checkChecksum(rawText)) {
                return null;
            }
            String wmi = rawText.substring(0, 3);
            return new VINParsedResult(rawText, wmi, rawText.substring(3, 9), rawText.substring(9, 17), VINResultParser.countryCode(wmi), rawText.substring(3, 8), VINResultParser.modelYear(rawText.charAt(9)), rawText.charAt(10), rawText.substring(11));
        }
        catch (IllegalArgumentException iae) {
            return null;
        }
    }

    private static boolean checkChecksum(CharSequence vin) {
        char expectedCheckChar;
        int sum = 0;
        for (int i = 0; i < vin.length(); ++i) {
            sum += VINResultParser.vinPositionWeight(i + 1) * VINResultParser.vinCharValue(vin.charAt(i));
        }
        char checkChar = vin.charAt(8);
        return checkChar == (expectedCheckChar = VINResultParser.checkChar(sum % 11));
    }

    private static int vinCharValue(char c) {
        if (c >= 'A' && c <= 'I') {
            return c - 65 + 1;
        }
        if (c >= 'J' && c <= 'R') {
            return c - 74 + 1;
        }
        if (c >= 'S' && c <= 'Z') {
            return c - 83 + 2;
        }
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        throw new IllegalArgumentException();
    }

    private static int vinPositionWeight(int position) {
        if (position >= 1 && position <= 7) {
            return 9 - position;
        }
        if (position == 8) {
            return 10;
        }
        if (position == 9) {
            return 0;
        }
        if (position >= 10 && position <= 17) {
            return 19 - position;
        }
        throw new IllegalArgumentException();
    }

    private static char checkChar(int remainder) {
        if (remainder < 10) {
            return (char)(48 + remainder);
        }
        if (remainder == 10) {
            return 'X';
        }
        throw new IllegalArgumentException();
    }

    private static int modelYear(char c) {
        if (c >= 'E' && c <= 'H') {
            return c - 69 + 1984;
        }
        if (c >= 'J' && c <= 'N') {
            return c - 74 + 1988;
        }
        if (c == 'P') {
            return 1993;
        }
        if (c >= 'R' && c <= 'T') {
            return c - 82 + 1994;
        }
        if (c >= 'V' && c <= 'Y') {
            return c - 86 + 1997;
        }
        if (c >= '1' && c <= '9') {
            return c - 49 + 2001;
        }
        if (c >= 'A' && c <= 'D') {
            return c - 65 + 2010;
        }
        throw new IllegalArgumentException();
    }

    private static String countryCode(CharSequence wmi) {
        char c1 = wmi.charAt(0);
        char c2 = wmi.charAt(1);
        switch (c1) {
            case '1': 
            case '4': 
            case '5': {
                return "US";
            }
            case '2': {
                return "CA";
            }
            case '3': {
                if (c2 < 'A' || c2 > 'W') break;
                return "MX";
            }
            case '9': {
                if ((c2 < 'A' || c2 > 'E') && (c2 < '3' || c2 > '9')) break;
                return "BR";
            }
            case 'J': {
                if (c2 < 'A' || c2 > 'T') break;
                return "JP";
            }
            case 'K': {
                if (c2 < 'L' || c2 > 'R') break;
                return "KO";
            }
            case 'L': {
                return "CN";
            }
            case 'M': {
                if (c2 < 'A' || c2 > 'E') break;
                return "IN";
            }
            case 'S': {
                if (c2 >= 'A' && c2 <= 'M') {
                    return "UK";
                }
                if (c2 < 'N' || c2 > 'T') break;
                return "DE";
            }
            case 'V': {
                if (c2 >= 'F' && c2 <= 'R') {
                    return "FR";
                }
                if (c2 < 'S' || c2 > 'W') break;
                return "ES";
            }
            case 'W': {
                return "DE";
            }
            case 'X': {
                if (c2 != '0' && (c2 < '3' || c2 > '9')) break;
                return "RU";
            }
            case 'Z': {
                if (c2 < 'A' || c2 > 'R') break;
                return "IT";
            }
        }
        return null;
    }
}

