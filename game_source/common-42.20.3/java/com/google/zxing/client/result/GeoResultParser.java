/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.ResultParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeoResultParser
extends ResultParser {
    private static final Pattern GEO_URL_PATTERN = Pattern.compile("geo:([\\-0-9.]+),([\\-0-9.]+)(?:,([\\-0-9.]+))?(?:\\?(.*))?", 2);

    @Override
    public GeoParsedResult parse(Result result) {
        double altitude;
        double longitude;
        double latitude;
        String rawText = GeoResultParser.getMassagedText(result);
        Matcher matcher = GEO_URL_PATTERN.matcher(rawText);
        if (!matcher.matches()) {
            return null;
        }
        String query = matcher.group(4);
        try {
            latitude = Double.parseDouble(matcher.group(1));
            if (latitude > 90.0 || latitude < -90.0) {
                return null;
            }
            longitude = Double.parseDouble(matcher.group(2));
            if (longitude > 180.0 || longitude < -180.0) {
                return null;
            }
            if (matcher.group(3) == null) {
                altitude = 0.0;
            } else {
                altitude = Double.parseDouble(matcher.group(3));
                if (altitude < 0.0) {
                    return null;
                }
            }
        }
        catch (NumberFormatException ignored) {
            return null;
        }
        return new GeoParsedResult(latitude, longitude, altitude, query);
    }
}

