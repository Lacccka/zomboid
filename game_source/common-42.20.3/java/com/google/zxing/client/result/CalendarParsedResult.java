/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CalendarParsedResult
extends ParsedResult {
    private static final Pattern RFC2445_DURATION = Pattern.compile("P(?:(\\d+)W)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?");
    private static final long[] RFC2445_DURATION_FIELD_UNITS = new long[]{604800000L, 86400000L, 3600000L, 60000L, 1000L};
    private static final Pattern DATE_TIME = Pattern.compile("[0-9]{8}(T[0-9]{6}Z?)?");
    private final String summary;
    private final Date start;
    private final boolean startAllDay;
    private final Date end;
    private final boolean endAllDay;
    private final String location;
    private final String organizer;
    private final String[] attendees;
    private final String description;
    private final double latitude;
    private final double longitude;

    public CalendarParsedResult(String summary, String startString, String endString, String durationString, String location, String organizer, String[] attendees, String description, double latitude, double longitude) {
        super(ParsedResultType.CALENDAR);
        this.summary = summary;
        try {
            this.start = CalendarParsedResult.parseDate(startString);
        }
        catch (ParseException pe) {
            throw new IllegalArgumentException(pe.toString());
        }
        if (endString == null) {
            long durationMS = CalendarParsedResult.parseDurationMS(durationString);
            this.end = durationMS < 0L ? null : new Date(this.start.getTime() + durationMS);
        } else {
            try {
                this.end = CalendarParsedResult.parseDate(endString);
            }
            catch (ParseException pe) {
                throw new IllegalArgumentException(pe.toString());
            }
        }
        this.startAllDay = startString.length() == 8;
        this.endAllDay = endString != null && endString.length() == 8;
        this.location = location;
        this.organizer = organizer;
        this.attendees = attendees;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSummary() {
        return this.summary;
    }

    public Date getStart() {
        return this.start;
    }

    public boolean isStartAllDay() {
        return this.startAllDay;
    }

    public Date getEnd() {
        return this.end;
    }

    public boolean isEndAllDay() {
        return this.endAllDay;
    }

    public String getLocation() {
        return this.location;
    }

    public String getOrganizer() {
        return this.organizer;
    }

    public String[] getAttendees() {
        return this.attendees;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(100);
        CalendarParsedResult.maybeAppend(this.summary, result);
        CalendarParsedResult.maybeAppend(CalendarParsedResult.format(this.startAllDay, this.start), result);
        CalendarParsedResult.maybeAppend(CalendarParsedResult.format(this.endAllDay, this.end), result);
        CalendarParsedResult.maybeAppend(this.location, result);
        CalendarParsedResult.maybeAppend(this.organizer, result);
        CalendarParsedResult.maybeAppend(this.attendees, result);
        CalendarParsedResult.maybeAppend(this.description, result);
        return result.toString();
    }

    private static Date parseDate(String when) throws ParseException {
        Date date;
        if (!DATE_TIME.matcher(when).matches()) {
            throw new ParseException(when, 0);
        }
        if (when.length() == 8) {
            return CalendarParsedResult.buildDateFormat().parse(when);
        }
        if (when.length() == 16 && when.charAt(15) == 'Z') {
            date = CalendarParsedResult.buildDateTimeFormat().parse(when.substring(0, 15));
            GregorianCalendar calendar = new GregorianCalendar();
            long milliseconds = date.getTime();
            calendar.setTime(new Date(milliseconds += (long)calendar.get(15)));
            date = new Date(milliseconds += (long)calendar.get(16));
        } else {
            date = CalendarParsedResult.buildDateTimeFormat().parse(when);
        }
        return date;
    }

    private static String format(boolean allDay, Date date) {
        if (date == null) {
            return null;
        }
        DateFormat format = allDay ? DateFormat.getDateInstance(2) : DateFormat.getDateTimeInstance(2, 2);
        return format.format(date);
    }

    private static long parseDurationMS(CharSequence durationString) {
        if (durationString == null) {
            return -1L;
        }
        Matcher m = RFC2445_DURATION.matcher(durationString);
        if (!m.matches()) {
            return -1L;
        }
        long durationMS = 0L;
        for (int i = 0; i < RFC2445_DURATION_FIELD_UNITS.length; ++i) {
            String fieldValue = m.group(i + 1);
            if (fieldValue == null) continue;
            durationMS += RFC2445_DURATION_FIELD_UNITS[i] * (long)Integer.parseInt(fieldValue);
        }
        return durationMS;
    }

    private static DateFormat buildDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    private static DateFormat buildDateTimeFormat() {
        return new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
    }
}

