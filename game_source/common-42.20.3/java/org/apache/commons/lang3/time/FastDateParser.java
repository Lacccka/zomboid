/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastTimeZone;

public class FastDateParser
implements DateParser,
Serializable {
    private static final long serialVersionUID = 3L;
    static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = Comparator.reverseOrder();
    private static final ConcurrentMap<Locale, Strategy>[] caches = new ConcurrentMap[17];
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(1){

        @Override
        int modify(FastDateParser parser, int iValue) {
            return iValue < 100 ? parser.adjustYear(iValue) : iValue;
        }
    };
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(2){

        @Override
        int modify(FastDateParser parser, int iValue) {
            return iValue - 1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(1);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(3);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(4);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(6);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(5);
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(7){

        @Override
        int modify(FastDateParser parser, int iValue) {
            return iValue == 7 ? 1 : iValue + 1;
        }
    };
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(8);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(11);
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(11){

        @Override
        int modify(FastDateParser parser, int iValue) {
            return iValue == 24 ? 0 : iValue;
        }
    };
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(10){

        @Override
        int modify(FastDateParser parser, int iValue) {
            return iValue == 12 ? 0 : iValue;
        }
    };
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(10);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(12);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(13);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(14);
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;
    private final int century;
    private final int startYear;
    private transient List<StrategyAndWidth> patterns;

    private static Map<String, Integer> appendDisplayNames(Calendar calendar, Locale locale, int field, StringBuilder regex) {
        Objects.requireNonNull(calendar, "calendar");
        HashMap<String, Integer> values2 = new HashMap<String, Integer>();
        Locale actualLocale = LocaleUtils.toLocale(locale);
        Map<String, Integer> displayNames = calendar.getDisplayNames(field, 0, actualLocale);
        TreeSet<String> sorted2 = new TreeSet<String>(LONGER_FIRST_LOWERCASE);
        displayNames.forEach((k, v) -> {
            String keyLc = k.toLowerCase(actualLocale);
            if (sorted2.add(keyLc)) {
                values2.put(keyLc, (Integer)v);
            }
        });
        sorted2.forEach(symbol -> FastDateParser.simpleQuote(regex, symbol).append('|'));
        return values2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ConcurrentMap<Locale, Strategy> getCache(int field) {
        ConcurrentMap<Locale, Strategy>[] concurrentMapArray = caches;
        synchronized (caches) {
            if (caches[field] == null) {
                FastDateParser.caches[field] = new ConcurrentHashMap<Locale, Strategy>(3);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return caches[field];
        }
    }

    private static boolean isFormatLetter(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    private static StringBuilder simpleQuote(StringBuilder sb, String value) {
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '$': 
                case '(': 
                case ')': 
                case '*': 
                case '+': 
                case '.': 
                case '?': 
                case '[': 
                case '\\': 
                case '^': 
                case '{': 
                case '|': {
                    sb.append('\\');
                }
            }
            sb.append(c);
        }
        if (sb.charAt(sb.length() - 1) == '.') {
            sb.append('?');
        }
        return sb;
    }

    protected FastDateParser(String pattern, TimeZone timeZone, Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    protected FastDateParser(String pattern, TimeZone timeZone, Locale locale, Date centuryStart) {
        int centuryStartYear;
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.timeZone = Objects.requireNonNull(timeZone, "timeZone");
        this.locale = LocaleUtils.toLocale(locale);
        Calendar definingCalendar = Calendar.getInstance(timeZone, this.locale);
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(1);
        } else if (this.locale.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(1) - 80;
        }
        this.century = centuryStartYear / 100 * 100;
        this.startYear = centuryStartYear - this.century;
        this.init(definingCalendar);
    }

    private int adjustYear(int twoDigitYear) {
        int trial = this.century + twoDigitYear;
        return twoDigitYear >= this.startYear ? trial : trial + 100;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FastDateParser)) {
            return false;
        }
        FastDateParser other = (FastDateParser)obj;
        return this.pattern.equals(other.pattern) && this.timeZone.equals(other.timeZone) && this.locale.equals(other.locale);
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    private Strategy getLocaleSpecificStrategy(int field, Calendar definingCalendar) {
        ConcurrentMap<Locale, Strategy> cache = FastDateParser.getCache(field);
        return cache.computeIfAbsent(this.locale, k -> field == 15 ? new TimeZoneStrategy(this.locale) : new CaseInsensitiveTextStrategy(field, definingCalendar, this.locale));
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    private Strategy getStrategy(char f, int width, Calendar definingCalendar) {
        switch (f) {
            default: {
                throw new IllegalArgumentException("Format '" + f + "' not supported");
            }
            case 'D': {
                return DAY_OF_YEAR_STRATEGY;
            }
            case 'E': {
                return this.getLocaleSpecificStrategy(7, definingCalendar);
            }
            case 'F': {
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            }
            case 'G': {
                return this.getLocaleSpecificStrategy(0, definingCalendar);
            }
            case 'H': {
                return HOUR_OF_DAY_STRATEGY;
            }
            case 'K': {
                return HOUR_STRATEGY;
            }
            case 'L': 
            case 'M': {
                return width >= 3 ? this.getLocaleSpecificStrategy(2, definingCalendar) : NUMBER_MONTH_STRATEGY;
            }
            case 'S': {
                return MILLISECOND_STRATEGY;
            }
            case 'W': {
                return WEEK_OF_MONTH_STRATEGY;
            }
            case 'a': {
                return this.getLocaleSpecificStrategy(9, definingCalendar);
            }
            case 'd': {
                return DAY_OF_MONTH_STRATEGY;
            }
            case 'h': {
                return HOUR12_STRATEGY;
            }
            case 'k': {
                return HOUR24_OF_DAY_STRATEGY;
            }
            case 'm': {
                return MINUTE_STRATEGY;
            }
            case 's': {
                return SECOND_STRATEGY;
            }
            case 'u': {
                return DAY_OF_WEEK_STRATEGY;
            }
            case 'w': {
                return WEEK_OF_YEAR_STRATEGY;
            }
            case 'Y': 
            case 'y': {
                return width > 2 ? LITERAL_YEAR_STRATEGY : ABBREVIATED_YEAR_STRATEGY;
            }
            case 'X': {
                return ISO8601TimeZoneStrategy.getStrategy(width);
            }
            case 'Z': {
                if (width != 2) break;
                return ISO8601TimeZoneStrategy.ISO_8601_3_STRATEGY;
            }
            case 'z': 
        }
        return this.getLocaleSpecificStrategy(15, definingCalendar);
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public int hashCode() {
        return this.pattern.hashCode() + 13 * (this.timeZone.hashCode() + 13 * this.locale.hashCode());
    }

    private void init(Calendar definingCalendar) {
        StrategyAndWidth field;
        this.patterns = new ArrayList<StrategyAndWidth>();
        StrategyParser strategyParser = new StrategyParser(definingCalendar);
        while ((field = strategyParser.getNextStrategy()) != null) {
            this.patterns.add(field);
        }
    }

    @Override
    public Date parse(String source2) throws ParseException {
        ParsePosition pp = new ParsePosition(0);
        Date date = this.parse(source2, pp);
        if (date == null) {
            if (this.locale.equals(JAPANESE_IMPERIAL)) {
                throw new ParseException("(The " + this.locale + " locale does not support dates before 1868 AD)\nUnparseable date: \"" + source2, pp.getErrorIndex());
            }
            throw new ParseException("Unparseable date: " + source2, pp.getErrorIndex());
        }
        return date;
    }

    @Override
    public Date parse(String source2, ParsePosition pos) {
        Calendar cal = Calendar.getInstance(this.timeZone, this.locale);
        cal.clear();
        return this.parse(source2, pos, cal) ? cal.getTime() : null;
    }

    @Override
    public boolean parse(String source2, ParsePosition pos, Calendar calendar) {
        ListIterator<StrategyAndWidth> lt = this.patterns.listIterator();
        while (lt.hasNext()) {
            StrategyAndWidth strategyAndWidth = lt.next();
            int maxWidth = strategyAndWidth.getMaxWidth(lt);
            if (strategyAndWidth.strategy.parse(this, calendar, source2, pos, maxWidth)) continue;
            return false;
        }
        return true;
    }

    @Override
    public Object parseObject(String source2) throws ParseException {
        return this.parse(source2);
    }

    @Override
    public Object parseObject(String source2, ParsePosition pos) {
        return this.parse(source2, pos);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Calendar definingCalendar = Calendar.getInstance(this.timeZone, this.locale);
        this.init(definingCalendar);
    }

    public String toString() {
        return "FastDateParser[" + this.pattern + ", " + this.locale + ", " + this.timeZone.getID() + "]";
    }

    public String toStringAll() {
        return "FastDateParser [pattern=" + this.pattern + ", timeZone=" + this.timeZone + ", locale=" + this.locale + ", century=" + this.century + ", startYear=" + this.startYear + ", patterns=" + this.patterns + "]";
    }

    private static abstract class Strategy {
        private Strategy() {
        }

        boolean isNumber() {
            return false;
        }

        abstract boolean parse(FastDateParser var1, Calendar var2, String var3, ParsePosition var4, int var5);
    }

    private static final class ISO8601TimeZoneStrategy
    extends PatternStrategy {
        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        static Strategy getStrategy(int tokenLen) {
            switch (tokenLen) {
                case 1: {
                    return ISO_8601_1_STRATEGY;
                }
                case 2: {
                    return ISO_8601_2_STRATEGY;
                }
                case 3: {
                    return ISO_8601_3_STRATEGY;
                }
            }
            throw new IllegalArgumentException("invalid number of X");
        }

        ISO8601TimeZoneStrategy(String pattern) {
            this.createPattern(pattern);
        }

        @Override
        void setCalendar(FastDateParser parser, Calendar calendar, String value) {
            calendar.setTimeZone(FastTimeZone.getGmtTimeZone(value));
        }
    }

    private final class StrategyParser {
        private final Calendar definingCalendar;
        private int currentIdx;

        StrategyParser(Calendar definingCalendar) {
            this.definingCalendar = Objects.requireNonNull(definingCalendar, "definingCalendar");
        }

        StrategyAndWidth getNextStrategy() {
            if (this.currentIdx >= FastDateParser.this.pattern.length()) {
                return null;
            }
            char c = FastDateParser.this.pattern.charAt(this.currentIdx);
            if (FastDateParser.isFormatLetter(c)) {
                return this.letterPattern(c);
            }
            return this.literal();
        }

        private StrategyAndWidth letterPattern(char c) {
            int begin = this.currentIdx;
            while (++this.currentIdx < FastDateParser.this.pattern.length() && FastDateParser.this.pattern.charAt(this.currentIdx) == c) {
            }
            int width = this.currentIdx - begin;
            return new StrategyAndWidth(FastDateParser.this.getStrategy(c, width, this.definingCalendar), width);
        }

        private StrategyAndWidth literal() {
            boolean activeQuote = false;
            StringBuilder sb = new StringBuilder();
            while (this.currentIdx < FastDateParser.this.pattern.length()) {
                char c = FastDateParser.this.pattern.charAt(this.currentIdx);
                if (!activeQuote && FastDateParser.isFormatLetter(c)) break;
                if (c == '\'' && (++this.currentIdx == FastDateParser.this.pattern.length() || FastDateParser.this.pattern.charAt(this.currentIdx) != '\'')) {
                    activeQuote = !activeQuote;
                    continue;
                }
                ++this.currentIdx;
                sb.append(c);
            }
            if (activeQuote) {
                throw new IllegalArgumentException("Unterminated quote");
            }
            String formatField = sb.toString();
            return new StrategyAndWidth(new CopyQuotedStrategy(formatField), formatField.length());
        }
    }

    private static final class StrategyAndWidth {
        final Strategy strategy;
        final int width;

        StrategyAndWidth(Strategy strategy, int width) {
            this.strategy = Objects.requireNonNull(strategy, "strategy");
            this.width = width;
        }

        int getMaxWidth(ListIterator<StrategyAndWidth> lt) {
            if (!this.strategy.isNumber() || !lt.hasNext()) {
                return 0;
            }
            Strategy nextStrategy = lt.next().strategy;
            lt.previous();
            return nextStrategy.isNumber() ? this.width : 0;
        }

        public String toString() {
            return "StrategyAndWidth [strategy=" + this.strategy + ", width=" + this.width + "]";
        }
    }

    static class TimeZoneStrategy
    extends PatternStrategy {
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        private static final int ID = 0;
        private final Locale locale;
        private final Map<String, TzInfo> tzNames = new HashMap<String, TzInfo>();

        TimeZoneStrategy(Locale locale) {
            String[][] zones;
            this.locale = LocaleUtils.toLocale(locale);
            StringBuilder sb = new StringBuilder();
            sb.append("((?iu)[+-]\\d{4}|GMT[+-]\\d{1,2}:\\d{2}");
            TreeSet<String> sorted2 = new TreeSet<String>(LONGER_FIRST_LOWERCASE);
            for (String[] stringArray : zones = DateFormatSymbols.getInstance(locale).getZoneStrings()) {
                TzInfo standard;
                String tzId = stringArray[0];
                if (tzId.equalsIgnoreCase("GMT")) continue;
                TimeZone tz = TimeZone.getTimeZone(tzId);
                TzInfo tzInfo = standard = new TzInfo(tz, false);
                for (int i = 1; i < stringArray.length; ++i) {
                    String key;
                    switch (i) {
                        case 3: {
                            tzInfo = new TzInfo(tz, true);
                            break;
                        }
                        case 5: {
                            tzInfo = standard;
                            break;
                        }
                    }
                    String zoneName2 = stringArray[i];
                    if (zoneName2 == null || !sorted2.add(key = zoneName2.toLowerCase(locale))) continue;
                    this.tzNames.put(key, tzInfo);
                }
            }
            for (String[] stringArray : TimeZone.getAvailableIDs()) {
                TimeZone tz;
                String zoneName3;
                String key;
                if (stringArray.equalsIgnoreCase("GMT") || !sorted2.add(key = (zoneName3 = (tz = TimeZone.getTimeZone((String)stringArray)).getDisplayName(locale)).toLowerCase(locale))) continue;
                this.tzNames.put(key, new TzInfo(tz, tz.observesDaylightTime()));
            }
            sorted2.forEach(zoneName -> FastDateParser.simpleQuote(sb.append('|'), zoneName));
            sb.append(")");
            this.createPattern(sb);
        }

        @Override
        void setCalendar(FastDateParser parser, Calendar calendar, String timeZone) {
            TimeZone tz = FastTimeZone.getGmtTimeZone(timeZone);
            if (tz != null) {
                calendar.setTimeZone(tz);
            } else {
                String lowerCase = timeZone.toLowerCase(this.locale);
                TzInfo tzInfo = this.tzNames.get(lowerCase);
                if (tzInfo == null) {
                    tzInfo = this.tzNames.get(lowerCase + '.');
                }
                calendar.set(16, tzInfo.dstOffset);
                calendar.set(15, tzInfo.zone.getRawOffset());
            }
        }

        @Override
        public String toString() {
            return "TimeZoneStrategy [locale=" + this.locale + ", tzNames=" + this.tzNames + ", pattern=" + this.pattern + "]";
        }

        private static final class TzInfo {
            final TimeZone zone;
            final int dstOffset;

            TzInfo(TimeZone tz, boolean useDst) {
                this.zone = tz;
                this.dstOffset = useDst ? tz.getDSTSavings() : 0;
            }

            public String toString() {
                return "TzInfo [zone=" + this.zone + ", dstOffset=" + this.dstOffset + "]";
            }
        }
    }

    private static final class CaseInsensitiveTextStrategy
    extends PatternStrategy {
        private final int field;
        final Locale locale;
        private final Map<String, Integer> lKeyValues;

        CaseInsensitiveTextStrategy(int field, Calendar definingCalendar, Locale locale) {
            this.field = field;
            this.locale = LocaleUtils.toLocale(locale);
            StringBuilder regex = new StringBuilder();
            regex.append("((?iu)");
            this.lKeyValues = FastDateParser.appendDisplayNames(definingCalendar, locale, field, regex);
            regex.setLength(regex.length() - 1);
            regex.append(")");
            this.createPattern(regex);
        }

        @Override
        void setCalendar(FastDateParser parser, Calendar calendar, String value) {
            String lowerCase = value.toLowerCase(this.locale);
            Integer iVal = this.lKeyValues.get(lowerCase);
            if (iVal == null) {
                iVal = this.lKeyValues.get(lowerCase + '.');
            }
            if (9 != this.field || iVal <= 1) {
                calendar.set(this.field, iVal);
            }
        }

        @Override
        public String toString() {
            return "CaseInsensitiveTextStrategy [field=" + this.field + ", locale=" + this.locale + ", lKeyValues=" + this.lKeyValues + ", pattern=" + this.pattern + "]";
        }
    }

    private static class NumberStrategy
    extends Strategy {
        private final int field;

        NumberStrategy(int field) {
            this.field = field;
        }

        @Override
        boolean isNumber() {
            return true;
        }

        int modify(FastDateParser parser, int iValue) {
            return iValue;
        }

        @Override
        boolean parse(FastDateParser parser, Calendar calendar, String source2, ParsePosition pos, int maxWidth) {
            char c;
            int idx;
            int last = source2.length();
            if (maxWidth == 0) {
                for (idx = pos.getIndex(); idx < last && Character.isWhitespace(c = source2.charAt(idx)); ++idx) {
                }
                pos.setIndex(idx);
            } else {
                int end = idx + maxWidth;
                if (last > end) {
                    last = end;
                }
            }
            while (idx < last && Character.isDigit(c = source2.charAt(idx))) {
                ++idx;
            }
            if (pos.getIndex() == idx) {
                pos.setErrorIndex(idx);
                return false;
            }
            int value = Integer.parseInt(source2.substring(pos.getIndex(), idx));
            pos.setIndex(idx);
            calendar.set(this.field, this.modify(parser, value));
            return true;
        }

        public String toString() {
            return "NumberStrategy [field=" + this.field + "]";
        }
    }

    private static abstract class PatternStrategy
    extends Strategy {
        Pattern pattern;

        private PatternStrategy() {
        }

        void createPattern(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        void createPattern(StringBuilder regex) {
            this.createPattern(regex.toString());
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(FastDateParser parser, Calendar calendar, String source2, ParsePosition pos, int maxWidth) {
            Matcher matcher = this.pattern.matcher(source2.substring(pos.getIndex()));
            if (!matcher.lookingAt()) {
                pos.setErrorIndex(pos.getIndex());
                return false;
            }
            pos.setIndex(pos.getIndex() + matcher.end(1));
            this.setCalendar(parser, calendar, matcher.group(1));
            return true;
        }

        abstract void setCalendar(FastDateParser var1, Calendar var2, String var3);

        public String toString() {
            return this.getClass().getSimpleName() + " [pattern=" + this.pattern + "]";
        }
    }

    private static final class CopyQuotedStrategy
    extends Strategy {
        private final String formatField;

        CopyQuotedStrategy(String formatField) {
            this.formatField = formatField;
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(FastDateParser parser, Calendar calendar, String source2, ParsePosition pos, int maxWidth) {
            for (int idx = 0; idx < this.formatField.length(); ++idx) {
                int sIdx = idx + pos.getIndex();
                if (sIdx == source2.length()) {
                    pos.setErrorIndex(sIdx);
                    return false;
                }
                if (this.formatField.charAt(idx) == source2.charAt(sIdx)) continue;
                pos.setErrorIndex(sIdx);
                return false;
            }
            pos.setIndex(this.formatField.length() + pos.getIndex());
            return true;
        }

        public String toString() {
            return "CopyQuotedStrategy [formatField=" + this.formatField + "]";
        }
    }
}

