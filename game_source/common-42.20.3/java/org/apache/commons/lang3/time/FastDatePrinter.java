/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.CalendarUtils;
import org.apache.commons.lang3.time.DatePrinter;

public class FastDatePrinter
implements DatePrinter,
Serializable {
    private static final Rule[] EMPTY_RULE_ARRAY = new Rule[0];
    private static final long serialVersionUID = 1L;
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    private static final int MAX_DIGITS = 10;
    private static final ConcurrentMap<TimeZoneDisplayKey, String> cTimeZoneDisplayCache = new ConcurrentHashMap<TimeZoneDisplayKey, String>(7);
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;
    private transient Rule[] rules;
    private transient int maxLengthEstimate;

    private static void appendDigits(Appendable buffer, int value) throws IOException {
        buffer.append((char)(value / 10 + 48));
        buffer.append((char)(value % 10 + 48));
    }

    private static void appendFullDigits(Appendable buffer, int value, int minFieldWidth) throws IOException {
        if (value < 10000) {
            int nDigits = 4;
            if (value < 1000) {
                --nDigits;
                if (value < 100) {
                    --nDigits;
                    if (value < 10) {
                        --nDigits;
                    }
                }
            }
            for (int i = minFieldWidth - nDigits; i > 0; --i) {
                buffer.append('0');
            }
            switch (nDigits) {
                case 4: {
                    buffer.append((char)(value / 1000 + 48));
                    value %= 1000;
                }
                case 3: {
                    if (value >= 100) {
                        buffer.append((char)(value / 100 + 48));
                        value %= 100;
                    } else {
                        buffer.append('0');
                    }
                }
                case 2: {
                    if (value >= 10) {
                        buffer.append((char)(value / 10 + 48));
                        value %= 10;
                    } else {
                        buffer.append('0');
                    }
                }
                case 1: {
                    buffer.append((char)(value + 48));
                }
            }
        } else {
            char[] work = new char[10];
            int digit = 0;
            while (value != 0) {
                work[digit++] = (char)(value % 10 + 48);
                value /= 10;
            }
            while (digit < minFieldWidth) {
                buffer.append('0');
                --minFieldWidth;
            }
            while (--digit >= 0) {
                buffer.append(work[digit]);
            }
        }
    }

    static String getTimeZoneDisplay(TimeZone tz, boolean daylight, int style, Locale locale) {
        TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        return cTimeZoneDisplayCache.computeIfAbsent(key, k -> tz.getDisplayName(daylight, style, locale));
    }

    protected FastDatePrinter(String pattern, TimeZone timeZone, Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = LocaleUtils.toLocale(locale);
        this.init();
    }

    private <B extends Appendable> B applyRules(Calendar calendar, B buf) {
        try {
            for (Rule rule : this.rules) {
                rule.appendTo(buf, calendar);
            }
        }
        catch (IOException ioe) {
            ExceptionUtils.asRuntimeException(ioe);
        }
        return buf;
    }

    @Deprecated
    protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
        return this.applyRules(calendar, (Appendable)buf);
    }

    private String applyRulesToString(Calendar c) {
        return this.applyRules(c, new StringBuilder(this.maxLengthEstimate)).toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FastDatePrinter)) {
            return false;
        }
        FastDatePrinter other = (FastDatePrinter)obj;
        return this.pattern.equals(other.pattern) && this.timeZone.equals(other.timeZone) && this.locale.equals(other.locale);
    }

    @Override
    public String format(Calendar calendar) {
        return this.format(calendar, new StringBuilder(this.maxLengthEstimate)).toString();
    }

    @Override
    public <B extends Appendable> B format(Calendar calendar, B buf) {
        if (!calendar.getTimeZone().equals(this.timeZone)) {
            calendar = (Calendar)calendar.clone();
            calendar.setTimeZone(this.timeZone);
        }
        return this.applyRules(calendar, buf);
    }

    @Override
    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        return this.format(calendar.getTime(), buf);
    }

    @Override
    public String format(Date date) {
        Calendar c = this.newCalendar();
        c.setTime(date);
        return this.applyRulesToString(c);
    }

    @Override
    public <B extends Appendable> B format(Date date, B buf) {
        Calendar c = this.newCalendar();
        c.setTime(date);
        return this.applyRules(c, buf);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer buf) {
        Calendar c = this.newCalendar();
        c.setTime(date);
        return this.applyRules(c, (Appendable)buf);
    }

    @Override
    public String format(long millis) {
        Calendar c = this.newCalendar();
        c.setTimeInMillis(millis);
        return this.applyRulesToString(c);
    }

    @Override
    public <B extends Appendable> B format(long millis, B buf) {
        Calendar c = this.newCalendar();
        c.setTimeInMillis(millis);
        return this.applyRules(c, buf);
    }

    @Override
    public StringBuffer format(long millis, StringBuffer buf) {
        Calendar c = this.newCalendar();
        c.setTimeInMillis(millis);
        return this.applyRules(c, (Appendable)buf);
    }

    String format(Object obj) {
        if (obj instanceof Date) {
            return this.format((Date)obj);
        }
        if (obj instanceof Calendar) {
            return this.format((Calendar)obj);
        }
        if (obj instanceof Long) {
            return this.format((Long)obj);
        }
        throw new IllegalArgumentException("Unknown class: " + ClassUtils.getName(obj, "<null>"));
    }

    @Override
    @Deprecated
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Date) {
            return this.format((Date)obj, toAppendTo);
        }
        if (obj instanceof Calendar) {
            return this.format((Calendar)obj, toAppendTo);
        }
        if (obj instanceof Long) {
            return this.format((long)((Long)obj), toAppendTo);
        }
        throw new IllegalArgumentException("Unknown class: " + ClassUtils.getName(obj, "<null>"));
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    public int getMaxLengthEstimate() {
        return this.maxLengthEstimate;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public int hashCode() {
        return this.pattern.hashCode() + 13 * (this.timeZone.hashCode() + 13 * this.locale.hashCode());
    }

    private void init() {
        List<Rule> rulesList = this.parsePattern();
        this.rules = rulesList.toArray(EMPTY_RULE_ARRAY);
        int len = 0;
        int i = this.rules.length;
        while (--i >= 0) {
            len += this.rules[i].estimateLength();
        }
        this.maxLengthEstimate = len;
    }

    private Calendar newCalendar() {
        return Calendar.getInstance(this.timeZone, this.locale);
    }

    protected List<Rule> parsePattern() {
        DateFormatSymbols symbols = new DateFormatSymbols(this.locale);
        ArrayList<Rule> rules = new ArrayList<Rule>();
        String[] ERAs = symbols.getEras();
        String[] months = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();
        String[] weekdays = symbols.getWeekdays();
        String[] shortWeekdays = symbols.getShortWeekdays();
        String[] AmPmStrings = symbols.getAmPmStrings();
        int length = this.pattern.length();
        int[] indexRef = new int[1];
        for (int i = 0; i < length; ++i) {
            Rule rule;
            indexRef[0] = i;
            String token = this.parseToken(this.pattern, indexRef);
            i = indexRef[0];
            int tokenLen = token.length();
            if (tokenLen == 0) break;
            char c = token.charAt(0);
            switch (c) {
                case 'G': {
                    rule = new TextField(0, ERAs);
                    break;
                }
                case 'Y': 
                case 'y': {
                    rule = tokenLen == 2 ? TwoDigitYearField.INSTANCE : this.selectNumberRule(1, Math.max(tokenLen, 4));
                    if (c != 'Y') break;
                    rule = new WeekYear((NumberRule)rule);
                    break;
                }
                case 'M': {
                    if (tokenLen >= 4) {
                        rule = new TextField(2, months);
                        break;
                    }
                    if (tokenLen == 3) {
                        rule = new TextField(2, shortMonths);
                        break;
                    }
                    if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                        break;
                    }
                    rule = UnpaddedMonthField.INSTANCE;
                    break;
                }
                case 'L': {
                    if (tokenLen >= 4) {
                        rule = new TextField(2, CalendarUtils.getInstance(this.locale).getStandaloneLongMonthNames());
                        break;
                    }
                    if (tokenLen == 3) {
                        rule = new TextField(2, CalendarUtils.getInstance(this.locale).getStandaloneShortMonthNames());
                        break;
                    }
                    if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                        break;
                    }
                    rule = UnpaddedMonthField.INSTANCE;
                    break;
                }
                case 'd': {
                    rule = this.selectNumberRule(5, tokenLen);
                    break;
                }
                case 'h': {
                    rule = new TwelveHourField(this.selectNumberRule(10, tokenLen));
                    break;
                }
                case 'H': {
                    rule = this.selectNumberRule(11, tokenLen);
                    break;
                }
                case 'm': {
                    rule = this.selectNumberRule(12, tokenLen);
                    break;
                }
                case 's': {
                    rule = this.selectNumberRule(13, tokenLen);
                    break;
                }
                case 'S': {
                    rule = this.selectNumberRule(14, tokenLen);
                    break;
                }
                case 'E': {
                    rule = new TextField(7, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                }
                case 'u': {
                    rule = new DayInWeekField(this.selectNumberRule(7, tokenLen));
                    break;
                }
                case 'D': {
                    rule = this.selectNumberRule(6, tokenLen);
                    break;
                }
                case 'F': {
                    rule = this.selectNumberRule(8, tokenLen);
                    break;
                }
                case 'w': {
                    rule = this.selectNumberRule(3, tokenLen);
                    break;
                }
                case 'W': {
                    rule = this.selectNumberRule(4, tokenLen);
                    break;
                }
                case 'a': {
                    rule = new TextField(9, AmPmStrings);
                    break;
                }
                case 'k': {
                    rule = new TwentyFourHourField(this.selectNumberRule(11, tokenLen));
                    break;
                }
                case 'K': {
                    rule = this.selectNumberRule(10, tokenLen);
                    break;
                }
                case 'X': {
                    rule = Iso8601_Rule.getRule(tokenLen);
                    break;
                }
                case 'z': {
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(this.timeZone, this.locale, 1);
                        break;
                    }
                    rule = new TimeZoneNameRule(this.timeZone, this.locale, 0);
                    break;
                }
                case 'Z': {
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                        break;
                    }
                    if (tokenLen == 2) {
                        rule = Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                        break;
                    }
                    rule = TimeZoneNumberRule.INSTANCE_COLON;
                    break;
                }
                case '\'': {
                    String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                        break;
                    }
                    rule = new StringLiteral(sub);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Illegal pattern component: " + token);
                }
            }
            rules.add(rule);
        }
        return rules;
    }

    protected String parseToken(String pattern, int[] indexRef) {
        int i;
        StringBuilder buf = new StringBuilder();
        int length = pattern.length();
        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            char peek;
            buf.append(c);
            while (i + 1 < length && (peek = pattern.charAt(i + 1)) == c) {
                buf.append(c);
                ++i;
            }
        } else {
            buf.append('\'');
            boolean inLiteral = false;
            for (i = indexRef[0]; i < length; ++i) {
                c = pattern.charAt(i);
                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        ++i;
                        buf.append(c);
                        continue;
                    }
                    inLiteral = !inLiteral;
                    continue;
                }
                if (inLiteral || (c < 'A' || c > 'Z') && (c < 'a' || c > 'z')) {
                    buf.append(c);
                    continue;
                }
                break;
            }
        }
        indexRef[0] = --i;
        return buf.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init();
    }

    protected NumberRule selectNumberRule(int field, int padding) {
        switch (padding) {
            case 1: {
                return new UnpaddedNumberField(field);
            }
            case 2: {
                return new TwoDigitNumberField(field);
            }
        }
        return new PaddedNumberField(field, padding);
    }

    public String toString() {
        return "FastDatePrinter[" + this.pattern + "," + this.locale + "," + this.timeZone.getID() + "]";
    }

    private static class TimeZoneDisplayKey {
        private final TimeZone timeZone;
        private final int style;
        private final Locale locale;

        TimeZoneDisplayKey(TimeZone timeZone, boolean daylight, int style, Locale locale) {
            this.timeZone = timeZone;
            this.style = daylight ? style | Integer.MIN_VALUE : style;
            this.locale = LocaleUtils.toLocale(locale);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey) {
                TimeZoneDisplayKey other = (TimeZoneDisplayKey)obj;
                return this.timeZone.equals(other.timeZone) && this.style == other.style && this.locale.equals(other.locale);
            }
            return false;
        }

        public int hashCode() {
            return (this.style * 31 + this.locale.hashCode()) * 31 + this.timeZone.hashCode();
        }
    }

    private static interface Rule {
        public void appendTo(Appendable var1, Calendar var2) throws IOException;

        public int estimateLength();
    }

    private static class TextField
    implements Rule {
        private final int field;
        private final String[] values;

        TextField(int field, String[] values2) {
            this.field = field;
            this.values = values2;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            buffer.append(this.values[calendar.get(this.field)]);
        }

        @Override
        public int estimateLength() {
            int max = 0;
            int i = this.values.length;
            while (--i >= 0) {
                int len = this.values[i].length();
                if (len <= max) continue;
                max = len;
            }
            return max;
        }
    }

    private static class TwoDigitYearField
    implements NumberRule {
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        TwoDigitYearField() {
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(1) % 100);
        }

        @Override
        public final void appendTo(Appendable buffer, int value) throws IOException {
            FastDatePrinter.appendDigits(buffer, value % 100);
        }

        @Override
        public int estimateLength() {
            return 2;
        }
    }

    private static interface NumberRule
    extends Rule {
        public void appendTo(Appendable var1, int var2) throws IOException;
    }

    private static class WeekYear
    implements NumberRule {
        private final NumberRule rule;

        WeekYear(NumberRule rule) {
            this.rule = rule;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.rule.appendTo(buffer, calendar.getWeekYear());
        }

        @Override
        public void appendTo(Appendable buffer, int value) throws IOException {
            this.rule.appendTo(buffer, value);
        }

        @Override
        public int estimateLength() {
            return this.rule.estimateLength();
        }
    }

    private static class TwoDigitMonthField
    implements NumberRule {
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        TwoDigitMonthField() {
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(2) + 1);
        }

        @Override
        public final void appendTo(Appendable buffer, int value) throws IOException {
            FastDatePrinter.appendDigits(buffer, value);
        }

        @Override
        public int estimateLength() {
            return 2;
        }
    }

    private static class UnpaddedMonthField
    implements NumberRule {
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        UnpaddedMonthField() {
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(2) + 1);
        }

        @Override
        public final void appendTo(Appendable buffer, int value) throws IOException {
            if (value < 10) {
                buffer.append((char)(value + 48));
            } else {
                FastDatePrinter.appendDigits(buffer, value);
            }
        }

        @Override
        public int estimateLength() {
            return 2;
        }
    }

    private static class TwelveHourField
    implements NumberRule {
        private final NumberRule rule;

        TwelveHourField(NumberRule rule) {
            this.rule = rule;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            int value = calendar.get(10);
            if (value == 0) {
                value = calendar.getLeastMaximum(10) + 1;
            }
            this.rule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(Appendable buffer, int value) throws IOException {
            this.rule.appendTo(buffer, value);
        }

        @Override
        public int estimateLength() {
            return this.rule.estimateLength();
        }
    }

    private static class DayInWeekField
    implements NumberRule {
        private final NumberRule rule;

        DayInWeekField(NumberRule rule) {
            this.rule = rule;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            int value = calendar.get(7);
            this.rule.appendTo(buffer, value == 1 ? 7 : value - 1);
        }

        @Override
        public void appendTo(Appendable buffer, int value) throws IOException {
            this.rule.appendTo(buffer, value);
        }

        @Override
        public int estimateLength() {
            return this.rule.estimateLength();
        }
    }

    private static class TwentyFourHourField
    implements NumberRule {
        private final NumberRule rule;

        TwentyFourHourField(NumberRule rule) {
            this.rule = rule;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            int value = calendar.get(11);
            if (value == 0) {
                value = calendar.getMaximum(11) + 1;
            }
            this.rule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(Appendable buffer, int value) throws IOException {
            this.rule.appendTo(buffer, value);
        }

        @Override
        public int estimateLength() {
            return this.rule.estimateLength();
        }
    }

    private static class Iso8601_Rule
    implements Rule {
        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);
        private final int length;

        static Iso8601_Rule getRule(int tokenLen) {
            switch (tokenLen) {
                case 1: {
                    return ISO8601_HOURS;
                }
                case 2: {
                    return ISO8601_HOURS_MINUTES;
                }
                case 3: {
                    return ISO8601_HOURS_COLON_MINUTES;
                }
            }
            throw new IllegalArgumentException("invalid number of X");
        }

        Iso8601_Rule(int length) {
            this.length = length;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            int offset = calendar.get(15) + calendar.get(16);
            if (offset == 0) {
                buffer.append("Z");
                return;
            }
            if (offset < 0) {
                buffer.append('-');
                offset = -offset;
            } else {
                buffer.append('+');
            }
            int hours = offset / 3600000;
            FastDatePrinter.appendDigits(buffer, hours);
            if (this.length < 5) {
                return;
            }
            if (this.length == 6) {
                buffer.append(':');
            }
            int minutes = offset / 60000 - 60 * hours;
            FastDatePrinter.appendDigits(buffer, minutes);
        }

        @Override
        public int estimateLength() {
            return this.length;
        }
    }

    private static class TimeZoneNameRule
    implements Rule {
        private final Locale locale;
        private final int style;
        private final String standard;
        private final String daylight;

        TimeZoneNameRule(TimeZone timeZone, Locale locale, int style) {
            this.locale = LocaleUtils.toLocale(locale);
            this.style = style;
            this.standard = FastDatePrinter.getTimeZoneDisplay(timeZone, false, style, locale);
            this.daylight = FastDatePrinter.getTimeZoneDisplay(timeZone, true, style, locale);
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            TimeZone zone = calendar.getTimeZone();
            boolean daylight = calendar.get(16) != 0;
            buffer.append(FastDatePrinter.getTimeZoneDisplay(zone, daylight, this.style, this.locale));
        }

        @Override
        public int estimateLength() {
            return Math.max(this.standard.length(), this.daylight.length());
        }
    }

    private static class TimeZoneNumberRule
    implements Rule {
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);
        private final boolean colon;

        TimeZoneNumberRule(boolean colon) {
            this.colon = colon;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            int offset = calendar.get(15) + calendar.get(16);
            if (offset < 0) {
                buffer.append('-');
                offset = -offset;
            } else {
                buffer.append('+');
            }
            int hours = offset / 3600000;
            FastDatePrinter.appendDigits(buffer, hours);
            if (this.colon) {
                buffer.append(':');
            }
            int minutes = offset / 60000 - 60 * hours;
            FastDatePrinter.appendDigits(buffer, minutes);
        }

        @Override
        public int estimateLength() {
            return 5;
        }
    }

    private static class CharacterLiteral
    implements Rule {
        private final char value;

        CharacterLiteral(char value) {
            this.value = value;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            buffer.append(this.value);
        }

        @Override
        public int estimateLength() {
            return 1;
        }
    }

    private static class StringLiteral
    implements Rule {
        private final String value;

        StringLiteral(String value) {
            this.value = value;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            buffer.append(this.value);
        }

        @Override
        public int estimateLength() {
            return this.value.length();
        }
    }

    private static class UnpaddedNumberField
    implements NumberRule {
        private final int field;

        UnpaddedNumberField(int field) {
            this.field = field;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(this.field));
        }

        @Override
        public final void appendTo(Appendable buffer, int value) throws IOException {
            if (value < 10) {
                buffer.append((char)(value + 48));
            } else if (value < 100) {
                FastDatePrinter.appendDigits(buffer, value);
            } else {
                FastDatePrinter.appendFullDigits(buffer, value, 1);
            }
        }

        @Override
        public int estimateLength() {
            return 4;
        }
    }

    private static class TwoDigitNumberField
    implements NumberRule {
        private final int field;

        TwoDigitNumberField(int field) {
            this.field = field;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(this.field));
        }

        @Override
        public final void appendTo(Appendable buffer, int value) throws IOException {
            if (value < 100) {
                FastDatePrinter.appendDigits(buffer, value);
            } else {
                FastDatePrinter.appendFullDigits(buffer, value, 2);
            }
        }

        @Override
        public int estimateLength() {
            return 2;
        }
    }

    private static final class PaddedNumberField
    implements NumberRule {
        private final int field;
        private final int size;

        PaddedNumberField(int field, int size) {
            if (size < 3) {
                throw new IllegalArgumentException();
            }
            this.field = field;
            this.size = size;
        }

        @Override
        public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
            this.appendTo(buffer, calendar.get(this.field));
        }

        @Override
        public void appendTo(Appendable buffer, int value) throws IOException {
            FastDatePrinter.appendFullDigits(buffer, value, this.size);
        }

        @Override
        public int estimateLength() {
            return this.size;
        }
    }
}

