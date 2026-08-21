/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.stdlib;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.Platform;

public class OsLib
implements JavaFunction {
    private static final int DATE = 0;
    private static final int DIFFTIME = 1;
    private static final int TIME = 2;
    private static final int NUM_FUNCS = 3;
    private static final String[] funcnames = new String[3];
    private static final OsLib[] funcs;
    private static final String TABLE_FORMAT = "*t";
    private static final String DEFAULT_FORMAT = "%c";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MIN = "min";
    private static final String SEC = "sec";
    private static final String WDAY = "wday";
    private static final String YDAY = "yday";
    private static final Object MILLISECOND;
    private static final TimeZone tzone;
    public static final int TIME_DIVIDEND = 1000;
    public static final double TIME_DIVIDEND_INVERTED = 0.001;
    private static final int MILLIS_PER_DAY = 86400000;
    private static final int MILLIS_PER_WEEK = 604800000;
    private final int methodId;
    private static final String[] shortDayNames;
    private static final String[] longDayNames;
    private static final String[] shortMonthNames;
    private static final String[] longMonthNames;

    public static void register(Platform platform, KahluaTable environment) {
        KahluaTable os = platform.newTable();
        for (int i = 0; i < 3; ++i) {
            os.rawset(funcnames[i], (Object)funcs[i]);
        }
        environment.rawset("os", (Object)os);
    }

    private OsLib(int methodId) {
        this.methodId = methodId;
    }

    @Override
    public int call(LuaCallFrame cf, int nargs) {
        switch (this.methodId) {
            case 0: {
                return this.date(cf, nargs);
            }
            case 1: {
                return this.difftime(cf, nargs);
            }
            case 2: {
                return this.time(cf, nargs);
            }
        }
        throw new RuntimeException("Undefined method called on os.");
    }

    private int time(LuaCallFrame cf, int nargs) {
        if (nargs == 0) {
            double t = (double)System.currentTimeMillis() * 0.001;
            cf.push(KahluaUtil.toDouble(t));
        } else {
            KahluaTable table = (KahluaTable)KahluaUtil.getArg(cf, 1, "time");
            double t = (double)OsLib.getDateFromTable(table).getTime() * 0.001;
            cf.push(KahluaUtil.toDouble(t));
        }
        return 1;
    }

    private int difftime(LuaCallFrame cf, int nargs) {
        double t2 = KahluaUtil.getDoubleArg(cf, 1, "difftime");
        double t1 = KahluaUtil.getDoubleArg(cf, 2, "difftime");
        cf.push(KahluaUtil.toDouble(t2 - t1));
        return 1;
    }

    private int date(LuaCallFrame cf, int nArguments) {
        Platform platform = cf.getPlatform();
        if (nArguments == 0) {
            return cf.push(OsLib.getdate(DEFAULT_FORMAT, platform));
        }
        String format = KahluaUtil.getStringArg(cf, 1, "date");
        if (nArguments == 1) {
            return cf.push(OsLib.getdate(format, platform));
        }
        double t = KahluaUtil.getDoubleArg(cf, 2, "date");
        long time = (long)(t * 1000.0);
        return cf.push(OsLib.getdate(format, time, platform));
    }

    public static Object getdate(String format, Platform platform) {
        return OsLib.getdate(format, Calendar.getInstance().getTime().getTime(), platform);
    }

    public static Object getdate(String format, long time, Platform platform) {
        Calendar calendar = null;
        int si = 0;
        if (format.charAt(si) == '!') {
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            ++si;
        } else {
            calendar = Calendar.getInstance(tzone);
        }
        calendar.setTime(new Date(time));
        if (calendar == null) {
            return null;
        }
        if (format.startsWith(TABLE_FORMAT, si)) {
            return OsLib.getTableFromDate(calendar, platform);
        }
        return OsLib.formatTime(format.substring(si), calendar);
    }

    public static String formatTime(String format, Calendar cal) {
        StringBuilder buffer = new StringBuilder();
        for (int stringIndex = 0; stringIndex < format.length(); ++stringIndex) {
            if (format.charAt(stringIndex) != '%' || stringIndex + 1 == format.length()) {
                buffer.append(format.charAt(stringIndex));
                continue;
            }
            buffer.append(OsLib.strftime(format.charAt(++stringIndex), cal));
        }
        return buffer.toString();
    }

    private static String format2Digits(int value) {
        Object retval = Integer.toString(value);
        if (value < 10) {
            retval = "0" + (String)retval;
        }
        return retval;
    }

    private static String strftime(char format, Calendar cal) {
        switch (format) {
            case 'a': {
                return shortDayNames[cal.get(7) - 1];
            }
            case 'A': {
                return longDayNames[cal.get(7) - 1];
            }
            case 'b': {
                return shortMonthNames[cal.get(2)];
            }
            case 'B': {
                return longMonthNames[cal.get(2)];
            }
            case 'c': {
                return cal.getTime().toString();
            }
            case 'C': {
                return Integer.toString(cal.get(1) / 100);
            }
            case 'd': {
                return OsLib.format2Digits(cal.get(5));
            }
            case 'D': {
                return OsLib.formatTime("%m/%d/%y", cal);
            }
            case 'e': {
                return cal.get(5) < 10 ? " " + OsLib.strftime('d', cal) : OsLib.strftime('d', cal);
            }
            case 'h': {
                return OsLib.strftime('b', cal);
            }
            case 'H': {
                return OsLib.format2Digits(cal.get(11));
            }
            case 'I': {
                return OsLib.format2Digits(cal.get(10));
            }
            case 'j': {
                return Integer.toString(OsLib.getDayOfYear(cal));
            }
            case 'm': {
                return OsLib.format2Digits(cal.get(2) + 1);
            }
            case 'M': {
                return OsLib.format2Digits(cal.get(12));
            }
            case 'n': {
                return "\n";
            }
            case 'p': {
                return cal.get(9) == 0 ? "AM" : "PM";
            }
            case 'r': {
                return OsLib.formatTime("%I:%M:%S %p", cal);
            }
            case 'R': {
                return OsLib.formatTime("%H:%M", cal);
            }
            case 'S': {
                return OsLib.format2Digits(cal.get(13));
            }
            case 'U': {
                return Integer.toString(OsLib.getWeekOfYear(cal, true, false));
            }
            case 'V': {
                return Integer.toString(OsLib.getWeekOfYear(cal, false, true));
            }
            case 'w': {
                return Integer.toString(cal.get(7) - 1);
            }
            case 'W': {
                return Integer.toString(OsLib.getWeekOfYear(cal, false, false));
            }
            case 'y': {
                return Integer.toString(cal.get(1) % 100);
            }
            case 'Y': {
                return Integer.toString(cal.get(1));
            }
            case 'Z': {
                return cal.getTimeZone().getID();
            }
        }
        return null;
    }

    public static KahluaTable getTableFromDate(Calendar c, Platform platform) {
        KahluaTable time = platform.newTable();
        time.rawset(YEAR, (Object)KahluaUtil.toDouble(c.get(1)));
        time.rawset(MONTH, (Object)KahluaUtil.toDouble(c.get(2) + 1));
        time.rawset(DAY, (Object)KahluaUtil.toDouble(c.get(5)));
        time.rawset(HOUR, (Object)KahluaUtil.toDouble(c.get(11)));
        time.rawset(MIN, (Object)KahluaUtil.toDouble(c.get(12)));
        time.rawset(SEC, (Object)KahluaUtil.toDouble(c.get(13)));
        time.rawset(WDAY, (Object)KahluaUtil.toDouble(c.get(7)));
        time.rawset(YDAY, (Object)KahluaUtil.toDouble(OsLib.getDayOfYear(c)));
        time.rawset(MILLISECOND, (Object)KahluaUtil.toDouble(c.get(14)));
        return time;
    }

    public static Date getDateFromTable(KahluaTable time) {
        Calendar c = Calendar.getInstance(tzone);
        c.set(1, (int)KahluaUtil.fromDouble(time.rawget(YEAR)));
        c.set(2, (int)KahluaUtil.fromDouble(time.rawget(MONTH)) - 1);
        c.set(5, (int)KahluaUtil.fromDouble(time.rawget(DAY)));
        Object hour = time.rawget(HOUR);
        Object minute = time.rawget(MIN);
        Object seconds = time.rawget(SEC);
        Object milliseconds = time.rawget(MILLISECOND);
        if (hour != null) {
            c.set(11, (int)KahluaUtil.fromDouble(hour));
        } else {
            c.set(11, 0);
        }
        if (minute != null) {
            c.set(12, (int)KahluaUtil.fromDouble(minute));
        } else {
            c.set(12, 0);
        }
        if (seconds != null) {
            c.set(13, (int)KahluaUtil.fromDouble(seconds));
        } else {
            c.set(13, 0);
        }
        if (milliseconds != null) {
            c.set(14, (int)KahluaUtil.fromDouble(milliseconds));
        } else {
            c.set(14, 0);
        }
        return c.getTime();
    }

    public static int getDayOfYear(Calendar c) {
        Calendar c2 = Calendar.getInstance(c.getTimeZone());
        c2.setTime(c.getTime());
        c2.set(2, 0);
        c2.set(5, 1);
        long diff = c.getTime().getTime() - c2.getTime().getTime();
        return (int)Math.ceil((double)diff / 8.64E7);
    }

    public static int getWeekOfYear(Calendar c, boolean weekStartsSunday, boolean jan1midweek) {
        Calendar c2 = Calendar.getInstance(c.getTimeZone());
        c2.setTime(c.getTime());
        c2.set(2, 0);
        c2.set(5, 1);
        int dayOfWeek = c2.get(7);
        if (weekStartsSunday && dayOfWeek != 1) {
            c2.set(5, 7 - dayOfWeek + 1);
        } else if (dayOfWeek != 2) {
            c2.set(5, 7 - dayOfWeek + 1 + 1);
        }
        long diff = c.getTime().getTime() - c2.getTime().getTime();
        int w = (int)(diff / 604800000L);
        if (jan1midweek && 7 - dayOfWeek >= 4) {
            ++w;
        }
        return w;
    }

    static {
        OsLib.funcnames[0] = "date";
        OsLib.funcnames[1] = "difftime";
        OsLib.funcnames[2] = "time";
        funcs = new OsLib[3];
        for (int i = 0; i < 3; ++i) {
            OsLib.funcs[i] = new OsLib(i);
        }
        MILLISECOND = "milli";
        tzone = TimeZone.getTimeZone("UTC");
        shortDayNames = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        longDayNames = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        shortMonthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        longMonthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    }
}

