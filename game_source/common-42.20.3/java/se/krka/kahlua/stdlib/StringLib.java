/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.stdlib;

import java.util.ArrayList;
import java.util.List;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import zombie.Lua.LuaManager;
import zombie.core.BoxedStaticValues;
import zombie.popman.ObjectPool;

public final class StringLib
implements JavaFunction {
    private static final int SUB = 0;
    private static final int CHAR = 1;
    private static final int BYTE = 2;
    private static final int LOWER = 3;
    private static final int UPPER = 4;
    private static final int REVERSE = 5;
    private static final int FORMAT = 6;
    private static final int FIND = 7;
    private static final int MATCH = 8;
    private static final int GSUB = 9;
    private static final int TRIM = 10;
    private static final int SPLIT = 11;
    private static final int SORT = 12;
    private static final int CONTAINS = 13;
    private static final int NUM_FUNCTIONS = 14;
    private static final boolean[] SPECIALS = new boolean[256];
    private static final int LUA_MAXCAPTURES = 32;
    private static final char L_ESC = '%';
    private static final int CAP_UNFINISHED = -1;
    private static final int CAP_POSITION = -2;
    private static final String[] names;
    private static final StringLib[] functions;
    private static final Class<?> STRING_CLASS;
    private final int methodId;
    private static final char[] digits;
    private static final int STRING_POINTER_POOL_LOADING = 65536;
    private static final int STRING_POINTER_POOL_RUNTIME = 512;
    private static final int MATCH_STATE_POOL_SIZE = 64;
    private static final int ALLOCATIONS_POOL_SIZE = 16;
    private static final ObjectPool<MatchState> s_MatchStatePool;
    private static final ObjectPool<StringPointer> s_StringPointerPool;
    private static final ObjectPool<Allocations> s_AllocationsPool;

    public StringLib(int index) {
        this.methodId = index;
    }

    public static void register(Platform platform, KahluaTable env) {
        KahluaTable string = platform.newTable();
        for (int i = 0; i < 14; ++i) {
            string.rawset(names[i], (Object)functions[i]);
        }
        string.rawset("__index", (Object)string);
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, env);
        metatables.rawset(STRING_CLASS, (Object)string);
        env.rawset("string", (Object)string);
    }

    public String toString() {
        return names[this.methodId];
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (this.methodId) {
            case 0: {
                return this.sub(callFrame, nArguments);
            }
            case 1: {
                return this.stringChar(callFrame, nArguments);
            }
            case 2: {
                return this.stringByte(callFrame, nArguments);
            }
            case 3: {
                return this.lower(callFrame, nArguments);
            }
            case 4: {
                return this.upper(callFrame, nArguments);
            }
            case 5: {
                return this.reverse(callFrame, nArguments);
            }
            case 6: {
                return this.format(callFrame, nArguments);
            }
            case 7: {
                return StringLib.findAux(callFrame, true);
            }
            case 8: {
                return StringLib.findAux(callFrame, false);
            }
            case 9: {
                return StringLib.gsub(callFrame, nArguments);
            }
            case 10: {
                return StringLib.trim(callFrame, nArguments);
            }
            case 11: {
                return StringLib.split(callFrame, nArguments);
            }
            case 12: {
                return StringLib.sort(callFrame, nArguments);
            }
            case 13: {
                return this.contains(callFrame, nArguments);
            }
        }
        return 0;
    }

    private long unsigned(long v) {
        if (v < 0L) {
            v += 0x100000000L;
        }
        return v;
    }

    private int format(LuaCallFrame callFrame, int nArguments) {
        String f = KahluaUtil.getStringArg(callFrame, 1, names[6]);
        int len = f.length();
        int argc = 2;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            char c = f.charAt(i);
            if (c == '%') {
                KahluaUtil.luaAssert(++i < len, "incomplete option to 'format'");
                c = f.charAt(i);
                if (c == '%') {
                    result.append('%');
                    continue;
                }
                boolean repr = false;
                boolean zeroPadding = false;
                boolean leftJustify = false;
                boolean showPlus = false;
                boolean spaceForSign = false;
                block38: while (true) {
                    switch (c) {
                        case '-': {
                            leftJustify = true;
                            break;
                        }
                        case '+': {
                            showPlus = true;
                            break;
                        }
                        case ' ': {
                            spaceForSign = true;
                            break;
                        }
                        case '#': {
                            repr = true;
                            break;
                        }
                        case '0': {
                            zeroPadding = true;
                            break;
                        }
                        default: {
                            break block38;
                        }
                    }
                    KahluaUtil.luaAssert(++i < len, "incomplete option to 'format'");
                    c = f.charAt(i);
                }
                int width = 0;
                while (c >= '0' && c <= '9') {
                    width = 10 * width + c - 48;
                    KahluaUtil.luaAssert(++i < len, "incomplete option to 'format'");
                    c = f.charAt(i);
                }
                int precision = 0;
                boolean hasPrecision = false;
                if (c == '.') {
                    hasPrecision = true;
                    KahluaUtil.luaAssert(++i < len, "incomplete option to 'format'");
                    c = f.charAt(i);
                    while (c >= '0' && c <= '9') {
                        precision = 10 * precision + c - 48;
                        KahluaUtil.luaAssert(++i < len, "incomplete option to 'format'");
                        c = f.charAt(i);
                    }
                }
                if (leftJustify) {
                    zeroPadding = false;
                }
                int base = 10;
                boolean upperCase = false;
                int defaultPrecision = 6;
                String basePrepend = "";
                switch (c) {
                    case 'c': {
                        zeroPadding = false;
                        break;
                    }
                    case 'o': {
                        base = 8;
                        defaultPrecision = 1;
                        basePrepend = "0";
                        break;
                    }
                    case 'x': {
                        base = 16;
                        defaultPrecision = 1;
                        basePrepend = "0x";
                        break;
                    }
                    case 'X': {
                        base = 16;
                        defaultPrecision = 1;
                        upperCase = true;
                        basePrepend = "0X";
                        break;
                    }
                    case 'u': {
                        defaultPrecision = 1;
                        break;
                    }
                    case 'd': 
                    case 'i': {
                        defaultPrecision = 1;
                        break;
                    }
                    case 'e': {
                        break;
                    }
                    case 'E': {
                        upperCase = true;
                        break;
                    }
                    case 'g': {
                        break;
                    }
                    case 'G': {
                        upperCase = true;
                        break;
                    }
                    case 'f': {
                        break;
                    }
                    case 's': {
                        zeroPadding = false;
                        break;
                    }
                    case 'q': {
                        width = 0;
                        break;
                    }
                    default: {
                        throw new RuntimeException("invalid option '%" + c + "' to 'format'");
                    }
                }
                if (!hasPrecision) {
                    precision = defaultPrecision;
                }
                if (hasPrecision && base != 10) {
                    zeroPadding = false;
                }
                char padCharacter = zeroPadding ? (char)'0' : ' ';
                int resultStartLength = result.length();
                if (!leftJustify) {
                    this.extend(result, width, padCharacter);
                }
                switch (c) {
                    case 'c': {
                        result.append((char)this.getDoubleArg(callFrame, argc).shortValue());
                        break;
                    }
                    case 'X': 
                    case 'o': 
                    case 'u': 
                    case 'x': {
                        long vLong = this.getDoubleArg(callFrame, argc).longValue();
                        vLong = this.unsigned(vLong);
                        if (repr) {
                            if (base == 8) {
                                int digits = 0;
                                long vLong2 = vLong;
                                while (vLong2 > 0L) {
                                    vLong2 /= 8L;
                                    ++digits;
                                }
                                if (precision <= digits) {
                                    result.append(basePrepend);
                                }
                            } else if (base == 16 && vLong != 0L) {
                                result.append(basePrepend);
                            }
                        }
                        if (vLong == 0L && precision <= 0) break;
                        StringLib.stringBufferAppend(result, vLong, base, false, precision);
                        break;
                    }
                    case 'd': 
                    case 'i': {
                        Double v = this.getDoubleArg(callFrame, argc);
                        long vLong = v.longValue();
                        if (vLong < 0L) {
                            result.append('-');
                            vLong = -vLong;
                        } else if (showPlus) {
                            result.append('+');
                        } else if (spaceForSign) {
                            result.append(' ');
                        }
                        if (vLong == 0L && precision <= 0) break;
                        StringLib.stringBufferAppend(result, vLong, base, false, precision);
                        break;
                    }
                    case 'E': 
                    case 'e': 
                    case 'f': {
                        Double v = this.getDoubleArg(callFrame, argc);
                        boolean isNaN = v.isInfinite() || v.isNaN();
                        double vDouble = v;
                        if (KahluaUtil.isNegative(vDouble)) {
                            if (!isNaN) {
                                result.append('-');
                            }
                            vDouble = -vDouble;
                        } else if (showPlus) {
                            result.append('+');
                        } else if (spaceForSign) {
                            result.append(' ');
                        }
                        if (isNaN) {
                            result.append(KahluaUtil.numberToString(v));
                            break;
                        }
                        if (c == 'f') {
                            this.appendPrecisionNumber(result, vDouble, precision, repr);
                            break;
                        }
                        this.appendScientificNumber(result, vDouble, precision, repr, false);
                        break;
                    }
                    case 'G': 
                    case 'g': {
                        Double v;
                        if (precision <= 0) {
                            precision = 1;
                        }
                        boolean isNaN = (v = this.getDoubleArg(callFrame, argc)).isInfinite() || v.isNaN();
                        double vDouble = v;
                        if (KahluaUtil.isNegative(vDouble)) {
                            if (!isNaN) {
                                result.append('-');
                            }
                            vDouble = -vDouble;
                        } else if (showPlus) {
                            result.append('+');
                        } else if (spaceForSign) {
                            result.append(' ');
                        }
                        if (isNaN) {
                            result.append(KahluaUtil.numberToString(v));
                            break;
                        }
                        double x = StringLib.roundToSignificantNumbers(vDouble, precision);
                        if (x == 0.0 || x >= 1.0E-4 && x < (double)KahluaUtil.ipow(10L, precision)) {
                            int iPartSize;
                            if (x == 0.0) {
                                iPartSize = 1;
                            } else if (Math.floor(x) == 0.0) {
                                iPartSize = 0;
                            } else {
                                double longValue = x;
                                iPartSize = 1;
                                while (longValue >= 10.0) {
                                    longValue /= 10.0;
                                    ++iPartSize;
                                }
                            }
                            this.appendSignificantNumber(result, x, precision - iPartSize, repr);
                            break;
                        }
                        this.appendScientificNumber(result, x, precision - 1, repr, true);
                        break;
                    }
                    case 's': {
                        String s = this.getStringArg(callFrame, argc);
                        int n = s.length();
                        if (hasPrecision) {
                            n = Math.min(precision, s.length());
                        }
                        this.append(result, s, 0, n);
                        break;
                    }
                    case 'q': {
                        String q = this.getStringArg(callFrame, argc);
                        result.append('\"');
                        block43: for (int j = 0; j < q.length(); ++j) {
                            char d = q.charAt(j);
                            switch (d) {
                                case '\\': {
                                    result.append("\\");
                                    continue block43;
                                }
                                case '\n': {
                                    result.append("\\\n");
                                    continue block43;
                                }
                                case '\r': {
                                    result.append("\\r");
                                    continue block43;
                                }
                                case '\"': {
                                    result.append("\\\"");
                                    continue block43;
                                }
                                default: {
                                    result.append(d);
                                }
                            }
                        }
                        result.append('\"');
                        break;
                    }
                    default: {
                        throw new RuntimeException("Internal error");
                    }
                }
                if (leftJustify) {
                    currentResultLength = result.length();
                    d = width - (currentResultLength - resultStartLength);
                    if (d > 0) {
                        this.extend(result, d, ' ');
                    }
                } else {
                    int signPos;
                    char ch;
                    currentResultLength = result.length();
                    d = currentResultLength - resultStartLength - width;
                    if ((d = Math.min(d, width)) > 0) {
                        result.delete(resultStartLength, resultStartLength + d);
                    }
                    if (zeroPadding && ((ch = result.charAt(signPos = resultStartLength + width - d)) == '+' || ch == '-' || ch == ' ')) {
                        result.setCharAt(signPos, '0');
                        result.setCharAt(resultStartLength, ch);
                    }
                }
                if (upperCase) {
                    this.stringBufferUpperCase(result, resultStartLength);
                }
                ++argc;
                continue;
            }
            result.append(c);
        }
        callFrame.push(result.toString());
        return 1;
    }

    private void append(StringBuilder buffer, String s, int start, int end) {
        for (int i = start; i < end; ++i) {
            buffer.append(s.charAt(i));
        }
    }

    private void extend(StringBuilder buffer, int extraWidth, char padCharacter) {
        int preLength = buffer.length();
        buffer.setLength(preLength + extraWidth);
        for (int i = extraWidth - 1; i >= 0; --i) {
            buffer.setCharAt(preLength + i, padCharacter);
        }
    }

    private void stringBufferUpperCase(StringBuilder buffer, int start) {
        int length = buffer.length();
        for (int i = start; i < length; ++i) {
            char c = buffer.charAt(i);
            if (c < 'a' || c > 'z') continue;
            buffer.setCharAt(i, (char)(c - 32));
        }
    }

    private static void stringBufferAppend(StringBuilder sb, double value, int base, boolean printZero, int minDigits) {
        int startPos = sb.length();
        while (value > 0.0 || minDigits > 0) {
            double newValue = Math.floor(value / (double)base);
            sb.append(digits[(int)(value - newValue * (double)base)]);
            value = newValue;
            --minDigits;
        }
        int endPos = sb.length() - 1;
        if (startPos > endPos && printZero) {
            sb.append('0');
        } else {
            int swapCount = (1 + endPos - startPos) / 2;
            for (int i = swapCount - 1; i >= 0; --i) {
                int leftPos = startPos + i;
                int rightPos = endPos - i;
                char left = sb.charAt(leftPos);
                char right = sb.charAt(rightPos);
                sb.setCharAt(leftPos, right);
                sb.setCharAt(rightPos, left);
            }
        }
    }

    private void appendPrecisionNumber(StringBuilder buffer, double number, int precision, boolean requirePeriod) {
        number = StringLib.roundToPrecision(number, precision);
        double iPart = Math.floor(number);
        double fPart = number - iPart;
        for (int i = 0; i < precision; ++i) {
            fPart *= 10.0;
        }
        fPart = KahluaUtil.round(iPart + fPart) - iPart;
        StringLib.stringBufferAppend(buffer, iPart, 10, true, 0);
        if (requirePeriod || precision > 0) {
            buffer.append('.');
        }
        StringLib.stringBufferAppend(buffer, fPart, 10, false, precision);
    }

    private void appendSignificantNumber(StringBuilder buffer, double number, int significantDecimals, boolean includeTrailingZeros) {
        double iPart = Math.floor(number);
        StringLib.stringBufferAppend(buffer, iPart, 10, true, 0);
        double fPart = StringLib.roundToSignificantNumbers(number - iPart, significantDecimals);
        boolean hasNotStarted = iPart == 0.0 && fPart != 0.0;
        int zeroPaddingBefore = 0;
        int scanLength = significantDecimals;
        for (int i = 0; i < scanLength; ++i) {
            if (Math.floor(fPart *= 10.0) != 0.0 || fPart == 0.0) continue;
            ++zeroPaddingBefore;
            if (!hasNotStarted) continue;
            ++scanLength;
        }
        fPart = KahluaUtil.round(fPart);
        if (!includeTrailingZeros) {
            while (fPart > 0.0 && fPart % 10.0 == 0.0) {
                fPart /= 10.0;
                --significantDecimals;
            }
        }
        buffer.append('.');
        int periodPos = buffer.length();
        this.extend(buffer, zeroPaddingBefore, '0');
        int prePos = buffer.length();
        StringLib.stringBufferAppend(buffer, fPart, 10, false, 0);
        int postPos = buffer.length();
        int len = postPos - prePos;
        if (includeTrailingZeros && len < significantDecimals) {
            int padRightSize = significantDecimals - len - zeroPaddingBefore;
            this.extend(buffer, padRightSize, '0');
        }
        if (!includeTrailingZeros && periodPos == buffer.length()) {
            buffer.delete(periodPos - 1, buffer.length());
        }
    }

    private void appendScientificNumber(StringBuilder buffer, double x, int precision, boolean repr, boolean useSignificantNumbers) {
        int exponent = 0;
        for (int i = 0; i < 2; ++i) {
            if (x >= 1.0) {
                while (x >= 10.0) {
                    x /= 10.0;
                    ++exponent;
                }
            } else {
                while (x > 0.0 && x < 1.0) {
                    x *= 10.0;
                    --exponent;
                }
            }
            x = StringLib.roundToPrecision(x, precision);
        }
        int absExponent = Math.abs(exponent);
        char expSign = exponent >= 0 ? (char)'+' : '-';
        if (useSignificantNumbers) {
            this.appendSignificantNumber(buffer, x, precision, repr);
        } else {
            this.appendPrecisionNumber(buffer, x, precision, repr);
        }
        buffer.append('e');
        buffer.append(expSign);
        StringLib.stringBufferAppend(buffer, absExponent, 10, true, 2);
    }

    private String getStringArg(LuaCallFrame callFrame, int argc) {
        return this.getStringArg(callFrame, argc, names[6]);
    }

    private String getStringArg(LuaCallFrame callFrame, int argc, String funcname) {
        return KahluaUtil.getStringArg(callFrame, argc, funcname);
    }

    private Double getDoubleArg(LuaCallFrame callFrame, int argc) {
        return this.getDoubleArg(callFrame, argc, names[6]);
    }

    private Double getDoubleArg(LuaCallFrame callFrame, int argc, String name) {
        return KahluaUtil.getNumberArg(callFrame, argc, name);
    }

    private int lower(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
        String s = this.getStringArg(callFrame, 1, names[3]);
        callFrame.push(s.toLowerCase());
        return 1;
    }

    private int upper(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
        String s = this.getStringArg(callFrame, 1, names[4]);
        callFrame.push(s.toUpperCase());
        return 1;
    }

    private int contains(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "not enough arguments");
        String s = this.getStringArg(callFrame, 1, names[13]);
        String s2 = this.getStringArg(callFrame, 2, names[13]);
        callFrame.push(s.contains(s2));
        return 1;
    }

    private int reverse(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
        String s = this.getStringArg(callFrame, 1, names[5]);
        s = new StringBuilder(s).reverse().toString();
        callFrame.push(s);
        return 1;
    }

    private int stringByte(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
        String s = this.getStringArg(callFrame, 1, names[2]);
        int i = this.nullDefault(1, KahluaUtil.getOptionalNumberArg(callFrame, 2));
        int j = this.nullDefault(i, KahluaUtil.getOptionalNumberArg(callFrame, 3));
        int len = s.length();
        if (i < 0) {
            i += len + 1;
        }
        if (i <= 0) {
            i = 1;
        }
        if (j < 0) {
            j += len + 1;
        } else if (j > len) {
            j = len;
        }
        int nReturns = 1 + j - i;
        if (nReturns <= 0) {
            return 0;
        }
        callFrame.setTop(nReturns);
        int offset = i - 1;
        for (int i2 = 0; i2 < nReturns; ++i2) {
            char c = s.charAt(offset + i2);
            callFrame.set(i2, KahluaUtil.toDouble(c));
        }
        return nReturns;
    }

    private int nullDefault(int defaultValue, Double val) {
        if (val == null) {
            return defaultValue;
        }
        return val.intValue();
    }

    private int stringChar(LuaCallFrame callFrame, int nArguments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nArguments; ++i) {
            int num = this.getDoubleArg(callFrame, i + 1, names[1]).intValue();
            sb.append((char)num);
        }
        return callFrame.push(sb.toString());
    }

    private int sub(LuaCallFrame callFrame, int nArguments) {
        String s = this.getStringArg(callFrame, 1, names[0]);
        double start = this.getDoubleArg(callFrame, 2, names[0]);
        double end = -1.0;
        if (nArguments >= 3) {
            end = this.getDoubleArg(callFrame, 3, names[0]);
        }
        int istart = (int)start;
        int iend = (int)end;
        int len = s.length();
        if (istart < 0) {
            istart = Math.max(len + istart + 1, 1);
        } else if (istart == 0) {
            istart = 1;
        }
        if (iend < 0) {
            iend = Math.max(0, iend + len + 1);
        } else if (iend > len) {
            iend = len;
        }
        if (istart > iend) {
            return callFrame.push("");
        }
        String res = s.substring(istart - 1, iend);
        return callFrame.push(res);
    }

    public static double roundToPrecision(double x, int precision) {
        double roundingOffset = KahluaUtil.ipow(10L, precision);
        return KahluaUtil.round(x * roundingOffset) / roundingOffset;
    }

    public static double roundToSignificantNumbers(double x, int precision) {
        if (x == 0.0) {
            return 0.0;
        }
        if (x < 0.0) {
            return -StringLib.roundToSignificantNumbers(-x, precision);
        }
        double lowerLimit = KahluaUtil.ipow(10L, precision - 1);
        double upperLimit = lowerLimit * 10.0;
        double multiplier = 1.0;
        while (multiplier * x < lowerLimit) {
            multiplier *= 10.0;
        }
        while (multiplier * x >= upperLimit) {
            multiplier /= 10.0;
        }
        return KahluaUtil.round(x * multiplier) / multiplier;
    }

    private static Object push_onecapture(MatchState ms, int i, StringPointer s, StringPointer e) {
        if (i >= ms.level) {
            if (i == 0) {
                String res = s.string.substring(s.index, e.index);
                ms.callFrame.push(res);
                return res;
            }
            throw new RuntimeException("invalid capture index");
        }
        int l = ms.capture[i].len;
        if (l == -1) {
            throw new RuntimeException("unfinished capture");
        }
        if (l == -2) {
            Double res = BoxedStaticValues.toDouble(ms.srcInit.length() - ms.capture[i].init.length() + 1);
            ms.callFrame.push(res);
            return res;
        }
        int index = ms.capture[i].init.index;
        String res = ms.capture[i].init.string.substring(index, index + l);
        ms.callFrame.push(res);
        return res;
    }

    private static int push_captures(MatchState ms, StringPointer s, StringPointer e) {
        int nlevels = ms.level == 0 && s != null ? 1 : ms.level;
        KahluaUtil.luaAssert(nlevels <= 32, "too many captures");
        for (int i = 0; i < nlevels; ++i) {
            StringLib.push_onecapture(ms, i, s, e);
        }
        return nlevels;
    }

    private static boolean noSpecialChars(String pattern) {
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (c >= '\u0100' || !SPECIALS[c]) continue;
            return false;
        }
        return true;
    }

    public static void onLoadingFinished() {
        s_StringPointerPool.setMaxSize(512);
        s_StringPointerPool.clear();
    }

    private static int findAux(LuaCallFrame callFrame, boolean find) {
        int n;
        block8: {
            Allocations allocations = s_AllocationsPool.alloc();
            try {
                n = StringLib.findAuxInner(allocations, callFrame, find);
                if (allocations == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (allocations != null) {
                        try {
                            allocations.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            allocations.close();
        }
        return n;
    }

    private static int findAuxInner(Allocations allocations, LuaCallFrame callFrame, boolean find) {
        int init;
        String f = find ? names[7] : names[8];
        String source2 = KahluaUtil.getStringArg(callFrame, 1, f);
        String pattern = KahluaUtil.getStringArg(callFrame, 2, f);
        Double i = KahluaUtil.getOptionalNumberArg(callFrame, 3);
        boolean plain = KahluaUtil.boolEval(KahluaUtil.getOptionalArg(callFrame, 4));
        int n = init = i == null ? 0 : i.intValue() - 1;
        if (init < 0) {
            if ((init += source2.length()) < 0) {
                init = 0;
            }
        } else if (init > source2.length()) {
            init = source2.length();
        }
        if (find && (plain || StringLib.noSpecialChars(pattern))) {
            int pos = source2.indexOf(pattern, init);
            if (pos > -1) {
                return callFrame.push(KahluaUtil.toDouble(pos + 1), KahluaUtil.toDouble(pos + pattern.length()));
            }
        } else {
            StringPointer s = allocations.allocStringPointer().set(source2);
            StringPointer p = allocations.allocStringPointer().set(pattern);
            boolean anchor = false;
            if (p.getChar() == '^') {
                anchor = true;
                p.postIncrString(1);
            }
            StringPointer s1 = s.getClone();
            s1.postIncrString(init);
            MatchState ms = allocations.allocMatchState().set(callFrame, s.getClone(), s.getStringLength());
            do {
                ms.level = 0;
                StringPointer res = StringLib.match(ms, s1, p);
                if (res == null) continue;
                if (find) {
                    return callFrame.push(s.length() - s1.length() + 1, s.length() - res.length()) + StringLib.push_captures(ms, null, null);
                }
                return StringLib.push_captures(ms, s1, res);
            } while (s1.postIncrStringI(1) < ms.endIndex && !anchor);
        }
        return callFrame.pushNil();
    }

    private static StringPointer startCapture(MatchState ms, StringPointer s, StringPointer p, int what) {
        int level = ms.level;
        KahluaUtil.luaAssert(level < 32, "too many captures");
        ms.capture[level].init = s.getClone();
        ms.capture[level].init.setIndex(s.getIndex());
        ms.capture[level].len = what;
        ms.level = level + 1;
        StringPointer res = StringLib.match(ms, s, p);
        if (res == null) {
            --ms.level;
        }
        return res;
    }

    private static int captureToClose(MatchState ms) {
        int level = ms.level;
        --level;
        while (level >= 0) {
            if (ms.capture[level].len == -1) {
                return level;
            }
            --level;
        }
        throw new RuntimeException("invalid pattern capture");
    }

    private static StringPointer endCapture(MatchState ms, StringPointer s, StringPointer p) {
        int l = StringLib.captureToClose(ms);
        ms.capture[l].len = ms.capture[l].init.length() - s.length();
        StringPointer res = StringLib.match(ms, s, p);
        if (res == null) {
            ms.capture[l].len = -1;
        }
        return res;
    }

    private static int checkCapture(MatchState ms, int l) {
        KahluaUtil.luaAssert((l -= 49) < 0 || l >= ms.level || ms.capture[l].len == -1, "invalid capture index");
        return l;
    }

    private static StringPointer matchCapture(MatchState ms, StringPointer s, int l) {
        l = StringLib.checkCapture(ms, l);
        int len = ms.capture[l].len;
        if (ms.endIndex - s.length() >= len && ms.capture[l].init.compareTo(s, len) == 0) {
            StringPointer sp = s.getClone();
            sp.postIncrString(len);
            return sp;
        }
        return null;
    }

    private static StringPointer matchBalance(MatchState ms, StringPointer ss, StringPointer p) {
        KahluaUtil.luaAssert(p.getChar() != '\u0000' && p.getChar(1) != '\u0000', "unbalanced pattern");
        StringPointer s = ss.getClone();
        if (s.getChar() != p.getChar()) {
            return null;
        }
        char b = p.getChar();
        char e = p.getChar(1);
        int cont = 1;
        while (s.preIncrStringI(1) < ms.endIndex) {
            if (s.getChar() == e) {
                if (--cont != 0) continue;
                StringPointer sp = s.getClone();
                sp.postIncrString(1);
                return sp;
            }
            if (s.getChar() != b) continue;
            ++cont;
        }
        return null;
    }

    private static StringPointer classEnd(StringPointer pp) {
        StringPointer p = pp.getClone();
        switch (p.postIncrString(1)) {
            case '%': {
                KahluaUtil.luaAssert(p.getChar() != '\u0000', "malformed pattern (ends with '%')");
                p.postIncrString(1);
                return p;
            }
            case '[': {
                if (p.getChar() == '^') {
                    p.postIncrString(1);
                }
                do {
                    KahluaUtil.luaAssert(p.getChar() != '\u0000', "malformed pattern (missing ']')");
                    if (p.postIncrString(1) != '%' || p.getChar() == '\u0000') continue;
                    p.postIncrString(1);
                } while (p.getChar() != ']');
                p.postIncrString(1);
                return p;
            }
        }
        return p;
    }

    private static boolean singleMatch(char c, StringPointer p, StringPointer ep) {
        switch (p.getChar()) {
            case '.': {
                return true;
            }
            case '%': {
                return StringLib.matchClass(p.getChar(1), c);
            }
            case '[': {
                StringPointer sp = ep.getClone();
                sp.postIncrString(-1);
                return StringLib.matchBracketClass(c, p, sp);
            }
        }
        return p.getChar() == c;
    }

    private static StringPointer minExpand(MatchState ms, StringPointer ss, StringPointer p, StringPointer ep) {
        StringPointer sp = ep.getClone();
        StringPointer s = ss.getClone();
        sp.postIncrString(1);
        while (true) {
            StringPointer res;
            if ((res = StringLib.match(ms, s, sp)) != null) {
                return res;
            }
            if (s.getIndex() >= ms.endIndex || !StringLib.singleMatch(s.getChar(), p, ep)) break;
            s.postIncrString(1);
        }
        return null;
    }

    private static StringPointer maxExpand(MatchState ms, StringPointer s, StringPointer p, StringPointer ep) {
        int i = 0;
        while (s.getIndex() + i < ms.endIndex && StringLib.singleMatch(s.getChar(i), p, ep)) {
            ++i;
        }
        while (i >= 0) {
            StringPointer sp1 = s.getClone();
            sp1.postIncrString(i);
            StringPointer sp2 = ep.getClone();
            sp2.postIncrString(1);
            StringPointer res = StringLib.match(ms, sp1, sp2);
            if (res != null) {
                return res;
            }
            --i;
        }
        return null;
    }

    private static boolean matchBracketClass(char c, StringPointer pp, StringPointer ecc) {
        StringPointer p = pp.getClone();
        StringPointer ec = ecc.getClone();
        boolean sig = true;
        if (p.getChar(1) == '^') {
            sig = false;
            p.postIncrString(1);
        }
        while (p.preIncrStringI(1) < ec.getIndex()) {
            if (p.getChar() == '%') {
                p.postIncrString(1);
                if (!StringLib.matchClass(p.getChar(), c)) continue;
                return sig;
            }
            if (p.getChar(1) == '-' && p.getIndex() + 2 < ec.getIndex()) {
                p.postIncrString(2);
                if (p.getChar(-2) > c || c > p.getChar()) continue;
                return sig;
            }
            if (p.getChar() != c) continue;
            return sig;
        }
        return !sig;
    }

    private static StringPointer match(MatchState ms, StringPointer ss, StringPointer pp) {
        StringPointer s = ss.getClone();
        StringPointer p = pp.getClone();
        boolean isContinue = true;
        boolean isDefault = false;
        block17: while (isContinue) {
            StringPointer ep;
            isContinue = false;
            isDefault = false;
            switch (p.getChar()) {
                case '(': {
                    StringPointer p1 = p.getClone();
                    if (p.getChar(1) == ')') {
                        p1.postIncrString(2);
                        return StringLib.startCapture(ms, s, p1, -2);
                    }
                    p1.postIncrString(1);
                    return StringLib.startCapture(ms, s, p1, -1);
                }
                case ')': {
                    StringPointer p1 = p.getClone();
                    p1.postIncrString(1);
                    return StringLib.endCapture(ms, s, p1);
                }
                case '%': {
                    StringPointer p1;
                    switch (p.getChar(1)) {
                        case 'b': {
                            p1 = p.getClone();
                            p1.postIncrString(2);
                            s = StringLib.matchBalance(ms, s, p1);
                            if (s == null) {
                                return null;
                            }
                            p.postIncrString(4);
                            isContinue = true;
                            continue block17;
                        }
                        case 'f': {
                            p.postIncrString(2);
                            KahluaUtil.luaAssert(p.getChar() == '[', "missing '[' after '%%f' in pattern");
                            ep = StringLib.classEnd(p);
                            char previous = s.getIndex() == ms.srcInit.getIndex() ? (char)'\u0000' : s.getChar(-1);
                            StringPointer ep1 = ep.getClone();
                            ep1.postIncrString(-1);
                            if (StringLib.matchBracketClass(previous, p, ep1) || !StringLib.matchBracketClass(s.getChar(), p, ep1)) {
                                return null;
                            }
                            p = ep;
                            isContinue = true;
                            continue block17;
                        }
                    }
                    if (Character.isDigit(p.getChar(1))) {
                        if ((s = StringLib.matchCapture(ms, s, p.getChar(1))) == null) {
                            return null;
                        }
                        p.postIncrString(2);
                        isContinue = true;
                        continue block17;
                    }
                    isDefault = true;
                    break;
                }
                case '\u0000': {
                    return s;
                }
                case '$': {
                    if (p.getChar(1) == '\u0000') {
                        return s.getIndex() == ms.endIndex ? s : null;
                    }
                }
                default: {
                    isDefault = true;
                }
            }
            if (!isDefault) continue;
            ep = StringLib.classEnd(p);
            boolean m = s.getIndex() < ms.endIndex && StringLib.singleMatch(s.getChar(), p, ep);
            switch (ep.getChar()) {
                case '?': {
                    StringPointer res;
                    StringPointer s1 = s.getClone();
                    s1.postIncrString(1);
                    StringPointer ep1 = ep.getClone();
                    ep1.postIncrString(1);
                    if (m && (res = StringLib.match(ms, s1, ep1)) != null) {
                        return res;
                    }
                    p = ep;
                    p.postIncrString(1);
                    isContinue = true;
                    continue block17;
                }
                case '*': {
                    return StringLib.maxExpand(ms, s, p, ep);
                }
                case '+': {
                    StringPointer s1 = s.getClone();
                    s1.postIncrString(1);
                    return m ? StringLib.maxExpand(ms, s1, p, ep) : null;
                }
                case '-': {
                    return StringLib.minExpand(ms, s, p, ep);
                }
            }
            if (!m) {
                return null;
            }
            s.postIncrString(1);
            p = ep;
            isContinue = true;
        }
        return null;
    }

    private static boolean matchClass(char classIdentifier, char c) {
        boolean res;
        char lowerClassIdentifier = Character.toLowerCase(classIdentifier);
        switch (lowerClassIdentifier) {
            case 'a': {
                res = Character.isLowerCase(c) || Character.isUpperCase(c);
                break;
            }
            case 'c': {
                res = StringLib.isControl(c);
                break;
            }
            case 'd': {
                res = Character.isDigit(c);
                break;
            }
            case 'l': {
                res = Character.isLowerCase(c);
                break;
            }
            case 'p': {
                res = StringLib.isPunct(c);
                break;
            }
            case 's': {
                res = StringLib.isSpace(c);
                break;
            }
            case 'u': {
                res = Character.isUpperCase(c);
                break;
            }
            case 'w': {
                res = Character.isLowerCase(c) || Character.isUpperCase(c) || Character.isDigit(c);
                break;
            }
            case 'x': {
                res = StringLib.isHex(c);
                break;
            }
            case 'z': {
                res = c == '\u0000';
                break;
            }
            default: {
                return classIdentifier == c;
            }
        }
        return lowerClassIdentifier == classIdentifier == res;
    }

    private static boolean isPunct(char c) {
        return c >= '!' && c <= '/' || c >= ':' && c <= '@' || c >= '[' && c <= '`' || c >= '{' && c <= '~';
    }

    private static boolean isSpace(char c) {
        return c >= '\t' && c <= '\r' || c == ' ';
    }

    private static boolean isControl(char c) {
        return c >= '\u0000' && c <= '\u001f' || c == '\u007f';
    }

    private static boolean isHex(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    private static int gsub(LuaCallFrame cf, int nargs) {
        int n;
        block8: {
            Allocations allocations = s_AllocationsPool.alloc();
            try {
                n = StringLib.gsub(allocations, cf, nargs);
                if (allocations == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (allocations != null) {
                        try {
                            allocations.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            allocations.close();
        }
        return n;
    }

    private static int gsub(Allocations allocations, LuaCallFrame cf, int nargs) {
        Double num;
        String srcTemp = KahluaUtil.getStringArg(cf, 1, names[9]);
        String pTemp = KahluaUtil.getStringArg(cf, 2, names[9]);
        Object repl = KahluaUtil.getArg(cf, 3, names[9]);
        String tmp = KahluaUtil.rawTostring(repl);
        if (tmp != null) {
            repl = tmp;
        }
        int maxSubstitutions = (num = KahluaUtil.getOptionalNumberArg(cf, 4)) == null ? Integer.MAX_VALUE : num.intValue();
        StringPointer pattern = allocations.allocStringPointer().set(pTemp);
        StringPointer src = allocations.allocStringPointer().set(srcTemp);
        boolean anchor = false;
        if (pattern.getChar() == '^') {
            anchor = true;
            pattern.postIncrString(1);
        }
        if (!(repl instanceof Double || repl instanceof String || repl instanceof LuaClosure || repl instanceof JavaFunction || repl instanceof KahluaTable)) {
            KahluaUtil.fail("string/function/table expected, got " + String.valueOf(repl));
        }
        MatchState ms = allocations.allocMatchState().set(cf, src.getClone(), src.length());
        int n = 0;
        StringBuilder b = new StringBuilder();
        while (n < maxSubstitutions) {
            ms.level = 0;
            StringPointer e = StringLib.match(ms, src, pattern);
            if (e != null) {
                ++n;
                StringLib.addValue(ms, repl, b, src, e);
            }
            if (e != null && e.getIndex() > src.getIndex()) {
                src.setIndex(e.getIndex());
            } else {
                if (src.getIndex() >= ms.endIndex) break;
                b.append(src.postIncrString(1));
            }
            if (!anchor) continue;
            break;
        }
        return cf.push(b.append(src.getString()).toString(), n);
    }

    private static int trim(LuaCallFrame cf, int nargs) {
        String srcTemp = KahluaUtil.getStringArg(cf, 1, names[10]);
        return cf.push(srcTemp.trim());
    }

    private static int split(LuaCallFrame cf, int nargs) {
        String srcTemp = KahluaUtil.getStringArg(cf, 1, names[11]);
        String srcTemp2 = KahluaUtil.getStringArg(cf, 2, names[11]);
        String[] spl = srcTemp.split(srcTemp2);
        KahluaTable t = LuaManager.platform.newTable();
        for (int n = 0; n < spl.length; ++n) {
            t.rawset(n + 1, (Object)spl[n]);
        }
        return cf.push(t);
    }

    private static int sort(LuaCallFrame cf, int nargs) {
        String srcTemp2;
        String srcTemp = KahluaUtil.getStringArg(cf, 1, names[12]);
        return cf.push(srcTemp.compareTo(srcTemp2 = KahluaUtil.getStringArg(cf, 2, names[12])) > 0);
    }

    private static void addValue(MatchState ms, Object repl, StringBuilder b, StringPointer src, StringPointer e) {
        String replString = KahluaUtil.rawTostring(repl);
        if (replString != null) {
            b.append(StringLib.addString(ms, replString, src, e));
        } else {
            Object captures = ms.getCapture(0);
            String match = captures != null ? KahluaUtil.rawTostring(captures) : src.getStringSubString(e.getIndex() - src.getIndex());
            Object res = null;
            if (repl instanceof KahluaTable) {
                KahluaTable kahluaTable = (KahluaTable)repl;
                res = kahluaTable.rawget(match);
            } else {
                res = ms.callFrame.getThread().call(repl, match, null, null);
            }
            if (res == null) {
                res = match;
            }
            b.append(KahluaUtil.rawTostring(res));
        }
    }

    private static String addString(MatchState ms, String repl, StringPointer s, StringPointer e) {
        StringPointer replStr = ms.allocations.allocStringPointer().set(repl);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repl.length(); ++i) {
            char c = replStr.getChar(i);
            if (c != '%') {
                buf.append(c);
                continue;
            }
            if (!Character.isDigit(c = replStr.getChar(++i))) {
                buf.append(c);
                continue;
            }
            if (c == '0') {
                int len = s.getStringLength() - e.length();
                buf.append(s.getStringSubString(len));
                continue;
            }
            Object o = ms.getCapture(c - 49);
            if (o == null) {
                throw new KahluaException((Object)"invalid capture index");
            }
            buf.append(KahluaUtil.tostring(o, null));
        }
        return buf.toString();
    }

    static {
        String s = "^$*+?.([%-";
        for (int i = 0; i < "^$*+?.([%-".length(); ++i) {
            StringLib.SPECIALS["^$*+?.([%-".charAt((int)i)] = true;
        }
        STRING_CLASS = "".getClass();
        names = new String[14];
        StringLib.names[0] = "sub";
        StringLib.names[1] = "char";
        StringLib.names[2] = "byte";
        StringLib.names[3] = "lower";
        StringLib.names[4] = "upper";
        StringLib.names[5] = "reverse";
        StringLib.names[6] = "format";
        StringLib.names[7] = "find";
        StringLib.names[8] = "match";
        StringLib.names[9] = "gsub";
        StringLib.names[10] = "trim";
        StringLib.names[11] = "split";
        StringLib.names[12] = "sort";
        StringLib.names[13] = "contains";
        functions = new StringLib[14];
        for (int i = 0; i < 14; ++i) {
            StringLib.functions[i] = new StringLib(i);
        }
        digits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        s_MatchStatePool = new ObjectPool<MatchState>(MatchState::new, "StringLib.s_MatchStatePool", 64);
        s_StringPointerPool = new ObjectPool<StringPointer>(StringPointer::new, "StringLib.s_StringPointerPool", 65536);
        s_AllocationsPool = new ObjectPool<Allocations>(Allocations::new, "StringLib.s_AllocationsPool", 16);
    }

    public static class MatchState {
        Allocations allocations;
        public LuaCallFrame callFrame;
        public StringPointer srcInit;
        public int endIndex;
        public final Capture[] capture = new Capture[32];
        public int level;

        MatchState() {
            for (int i = 0; i < 32; ++i) {
                this.capture[i] = new Capture();
            }
        }

        public MatchState set(LuaCallFrame callFrame, StringPointer srcInit, int endIndex) {
            this.callFrame = callFrame;
            this.srcInit = srcInit;
            this.endIndex = endIndex;
            return this;
        }

        public Object getCapture(int i) {
            if (i >= this.level) {
                return null;
            }
            if (this.capture[i].len == -2) {
                return (double)(this.srcInit.length() - this.capture[i].init.length() + 1);
            }
            return this.capture[i].init.getStringSubString(this.capture[i].len);
        }

        public static class Capture {
            public StringPointer init;
            public int len;
        }
    }

    public static class StringPointer {
        Allocations allocations;
        private String string;
        private int index;

        public StringPointer set(String original) {
            this.string = original;
            this.index = 0;
            return this;
        }

        public StringPointer set(String original, int index) {
            this.string = original;
            this.index = index;
            return this;
        }

        public StringPointer getClone() {
            return this.allocations.allocStringPointer().set(this.string, this.index);
        }

        public int getIndex() {
            return this.index;
        }

        public void setIndex(int ind) {
            this.index = ind;
        }

        public String getString() {
            if (this.index == 0) {
                return this.string;
            }
            return this.string.substring(this.index);
        }

        public int getStringLength() {
            return this.getStringLength(0);
        }

        public int getStringLength(int i) {
            return this.string.length() - (this.index + i);
        }

        public String getStringSubString(int len) {
            return this.string.substring(this.index, this.index + len);
        }

        public char getChar() {
            return this.getChar(0);
        }

        public char getChar(int strIndex) {
            int i = this.index + strIndex;
            if (i >= this.string.length()) {
                return '\u0000';
            }
            return this.string.charAt(i);
        }

        public int length() {
            return this.string.length() - this.index;
        }

        public int postIncrStringI(int num) {
            int oldIndex = this.index;
            this.index += num;
            return oldIndex;
        }

        public int preIncrStringI(int num) {
            this.index += num;
            return this.index;
        }

        public char postIncrString(int num) {
            char c = this.getChar();
            this.index += num;
            return c;
        }

        public int compareTo(StringPointer cmp, int len) {
            for (int i = 0; i < len; ++i) {
                int val = this.getChar(i) - cmp.getChar(i);
                if (val == 0) continue;
                return val;
            }
            return 0;
        }
    }

    private static final class Allocations
    implements AutoCloseable {
        private static final int TRIM_THRESHOLD = 1024;
        final ArrayList<MatchState> matchState = new ArrayList();
        final ArrayList<StringPointer> stringPointer = new ArrayList();

        private Allocations() {
        }

        MatchState allocMatchState() {
            MatchState matchState1 = s_MatchStatePool.alloc();
            matchState1.allocations = this;
            this.matchState.add(matchState1);
            return matchState1;
        }

        StringPointer allocStringPointer() {
            StringPointer stringPointer1 = s_StringPointerPool.alloc();
            stringPointer1.allocations = this;
            this.stringPointer.add(stringPointer1);
            return stringPointer1;
        }

        void release() {
            boolean oversized = this.matchState.size() > 1024 || this.stringPointer.size() > 1024;
            s_MatchStatePool.releaseAll((List<MatchState>)this.matchState);
            this.matchState.clear();
            s_StringPointerPool.releaseAll((List<StringPointer>)this.stringPointer);
            this.stringPointer.clear();
            if (oversized) {
                this.matchState.trimToSize();
                this.stringPointer.trimToSize();
            }
            s_AllocationsPool.release(this);
        }

        @Override
        public void close() throws Exception {
            this.release();
        }
    }
}

