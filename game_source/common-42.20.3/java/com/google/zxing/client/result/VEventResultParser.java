/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.CalendarParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.VCardResultParser;
import java.util.List;

public final class VEventResultParser
extends ResultParser {
    @Override
    public CalendarParsedResult parse(Result result) {
        double longitude;
        double latitude;
        String rawText = VEventResultParser.getMassagedText(result);
        int vEventStart = rawText.indexOf("BEGIN:VEVENT");
        if (vEventStart < 0) {
            return null;
        }
        String summary = VEventResultParser.matchSingleVCardPrefixedField("SUMMARY", rawText, true);
        String start = VEventResultParser.matchSingleVCardPrefixedField("DTSTART", rawText, true);
        if (start == null) {
            return null;
        }
        String end = VEventResultParser.matchSingleVCardPrefixedField("DTEND", rawText, true);
        String duration = VEventResultParser.matchSingleVCardPrefixedField("DURATION", rawText, true);
        String location = VEventResultParser.matchSingleVCardPrefixedField("LOCATION", rawText, true);
        String organizer = VEventResultParser.stripMailto(VEventResultParser.matchSingleVCardPrefixedField("ORGANIZER", rawText, true));
        String[] attendees = VEventResultParser.matchVCardPrefixedField("ATTENDEE", rawText, true);
        if (attendees != null) {
            for (int i = 0; i < attendees.length; ++i) {
                attendees[i] = VEventResultParser.stripMailto(attendees[i]);
            }
        }
        String description = VEventResultParser.matchSingleVCardPrefixedField("DESCRIPTION", rawText, true);
        String geoString = VEventResultParser.matchSingleVCardPrefixedField("GEO", rawText, true);
        if (geoString == null) {
            latitude = Double.NaN;
            longitude = Double.NaN;
        } else {
            int semicolon = geoString.indexOf(59);
            if (semicolon < 0) {
                return null;
            }
            try {
                latitude = Double.parseDouble(geoString.substring(0, semicolon));
                longitude = Double.parseDouble(geoString.substring(semicolon + 1));
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        try {
            return new CalendarParsedResult(summary, start, end, duration, location, organizer, attendees, description, latitude, longitude);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String matchSingleVCardPrefixedField(CharSequence prefix, String rawText, boolean trim) {
        List<String> values2 = VCardResultParser.matchSingleVCardPrefixedField(prefix, rawText, trim, false);
        return values2 == null || values2.isEmpty() ? null : values2.get(0);
    }

    private static String[] matchVCardPrefixedField(CharSequence prefix, String rawText, boolean trim) {
        List<List<String>> values2 = VCardResultParser.matchVCardPrefixedField(prefix, rawText, trim, false);
        if (values2 == null || values2.isEmpty()) {
            return null;
        }
        int size = values2.size();
        String[] result = new String[size];
        for (int i = 0; i < size; ++i) {
            result[i] = values2.get(i).get(0);
        }
        return result;
    }

    private static String stripMailto(String s) {
        if (s != null && (s.startsWith("mailto:") || s.startsWith("MAILTO:"))) {
            s = s.substring(7);
        }
        return s;
    }
}

