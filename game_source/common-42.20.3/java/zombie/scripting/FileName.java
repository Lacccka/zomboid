/*
 * Decompiled with CFR 0.152.
 */
package zombie.scripting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class FileName {
    private static final int NOT_FOUND = -1;
    private static final char UNIX_NAME_SEPARATOR = '/';
    private static final char WINDOWS_NAME_SEPARATOR = '\\';
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");
    private static final Pattern TRAILING_DOTS_WHITESPACE = Pattern.compile("[.\\s]+$");
    private static final Set<String> RESERVED_NAMES = new HashSet<String>(Arrays.asList("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"));

    public static String sanitize(String name) {
        String baseName;
        if (name == null) {
            return null;
        }
        Object sanitized = INVALID_CHARS.matcher(name).replaceAll("_");
        if (((String)(sanitized = TRAILING_DOTS_WHITESPACE.matcher((CharSequence)sanitized).replaceAll(""))).isEmpty()) {
            sanitized = "_";
        }
        if (RESERVED_NAMES.contains(baseName = ((String)sanitized).split("\\.", 2)[0].toUpperCase(Locale.ROOT))) {
            sanitized = "_" + (String)sanitized;
        }
        return sanitized;
    }

    public static int indexOfLastSeparator(String fileName) {
        if (fileName == null) {
            return -1;
        }
        int lastUnixPos = fileName.lastIndexOf(47);
        int lastWindowsPos = fileName.lastIndexOf(92);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static String getName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return FileName.requireNonNullChars(fileName).substring(FileName.indexOfLastSeparator(fileName) + 1);
    }

    private static String requireNonNullChars(String path) {
        if (path.indexOf(0) >= 0) {
            throw new IllegalArgumentException("Null character present in file/path name. There are no known legitimate use cases for such data, but several injection attacks may use it");
        }
        return path;
    }
}

