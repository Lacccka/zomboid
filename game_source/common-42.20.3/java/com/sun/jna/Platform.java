/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import com.sun.jna.ELFAnalyser;
import com.sun.jna.Native;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Platform {
    public static final int UNSPECIFIED = -1;
    public static final int MAC = 0;
    public static final int LINUX = 1;
    public static final int WINDOWS = 2;
    public static final int SOLARIS = 3;
    public static final int FREEBSD = 4;
    public static final int OPENBSD = 5;
    public static final int WINDOWSCE = 6;
    public static final int AIX = 7;
    public static final int ANDROID = 8;
    public static final int GNU = 9;
    public static final int KFREEBSD = 10;
    public static final int NETBSD = 11;
    public static final int DRAGONFLYBSD = 12;
    public static final boolean RO_FIELDS;
    public static final boolean HAS_BUFFERS;
    public static final boolean HAS_AWT;
    public static final boolean HAS_JAWT;
    public static final String MATH_LIBRARY_NAME;
    public static final String C_LIBRARY_NAME;
    public static final boolean HAS_DLL_CALLBACKS;
    public static final String RESOURCE_PREFIX;
    private static final int osType;
    public static final String ARCH;

    private Platform() {
    }

    public static final int getOSType() {
        return osType;
    }

    public static final boolean isMac() {
        return osType == 0;
    }

    public static final boolean isAndroid() {
        return osType == 8;
    }

    public static final boolean isLinux() {
        return osType == 1;
    }

    public static final boolean isAIX() {
        return osType == 7;
    }

    public static final boolean isWindowsCE() {
        return osType == 6;
    }

    public static final boolean isWindows() {
        return osType == 2 || osType == 6;
    }

    public static final boolean isSolaris() {
        return osType == 3;
    }

    public static final boolean isDragonFlyBSD() {
        return osType == 12;
    }

    public static final boolean isFreeBSD() {
        return osType == 4;
    }

    public static final boolean isOpenBSD() {
        return osType == 5;
    }

    public static final boolean isNetBSD() {
        return osType == 11;
    }

    public static final boolean isGNU() {
        return osType == 9;
    }

    public static final boolean iskFreeBSD() {
        return osType == 10;
    }

    public static final boolean isX11() {
        return !Platform.isWindows() && !Platform.isMac();
    }

    public static final boolean hasRuntimeExec() {
        return !Platform.isWindowsCE() || !"J9".equals(System.getProperty("java.vm.name"));
    }

    public static final boolean is64Bit() {
        String model = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"));
        if (model != null) {
            return "64".equals(model);
        }
        if ("x86-64".equals(ARCH) || "ia64".equals(ARCH) || "ppc64".equals(ARCH) || "ppc64le".equals(ARCH) || "sparcv9".equals(ARCH) || "mips64".equals(ARCH) || "mips64el".equals(ARCH) || "loongarch64".equals(ARCH) || "amd64".equals(ARCH) || "aarch64".equals(ARCH)) {
            return true;
        }
        return Native.POINTER_SIZE == 8;
    }

    public static final boolean isIntel() {
        return ARCH.startsWith("x86");
    }

    public static final boolean isPPC() {
        return ARCH.startsWith("ppc");
    }

    public static final boolean isARM() {
        return ARCH.startsWith("arm") || ARCH.startsWith("aarch");
    }

    public static final boolean isSPARC() {
        return ARCH.startsWith("sparc");
    }

    public static final boolean isMIPS() {
        return ARCH.equals("mips") || ARCH.equals("mips64") || ARCH.equals("mipsel") || ARCH.equals("mips64el");
    }

    public static final boolean isLoongArch() {
        return ARCH.startsWith("loongarch");
    }

    public static final boolean isRISCV() {
        return ARCH.startsWith("riscv");
    }

    static String getCanonicalArchitecture(String arch2, int platform) {
        if ("powerpc".equals(arch2 = arch2.toLowerCase().trim())) {
            arch2 = "ppc";
        } else if ("powerpc64".equals(arch2)) {
            arch2 = "ppc64";
        } else if ("i386".equals(arch2) || "i686".equals(arch2)) {
            arch2 = "x86";
        } else if ("x86_64".equals(arch2) || "amd64".equals(arch2)) {
            arch2 = "x86-64";
        } else if ("zarch_64".equals(arch2)) {
            arch2 = "s390x";
        }
        if ("ppc64".equals(arch2) && "little".equals(System.getProperty("sun.cpu.endian"))) {
            arch2 = "ppc64le";
        }
        if ("arm".equals(arch2) && platform == 1 && Platform.isSoftFloat()) {
            arch2 = "armel";
        }
        return arch2;
    }

    static boolean isSoftFloat() {
        try {
            File self = new File("/proc/self/exe");
            if (self.exists()) {
                ELFAnalyser ahfd = ELFAnalyser.analyse(self.getCanonicalPath());
                return !ahfd.isArmHardFloat();
            }
        }
        catch (IOException ex) {
            Logger.getLogger(Platform.class.getName()).log(Level.INFO, "Failed to read '/proc/self/exe' or the target binary.", ex);
        }
        catch (SecurityException ex) {
            Logger.getLogger(Platform.class.getName()).log(Level.INFO, "SecurityException while analysing '/proc/self/exe' or the target binary.", ex);
        }
        return false;
    }

    static String getNativeLibraryResourcePrefix() {
        String prefix = System.getProperty("jna.prefix");
        if (prefix != null) {
            return prefix;
        }
        return Platform.getNativeLibraryResourcePrefix(Platform.getOSType(), System.getProperty("os.arch"), System.getProperty("os.name"));
    }

    static String getNativeLibraryResourcePrefix(int osType, String arch2, String name) {
        String osPrefix;
        arch2 = Platform.getCanonicalArchitecture(arch2, osType);
        switch (osType) {
            case 8: {
                if (arch2.startsWith("arm")) {
                    arch2 = "arm";
                }
                osPrefix = "android-" + arch2;
                break;
            }
            case 2: {
                osPrefix = "win32-" + arch2;
                break;
            }
            case 6: {
                osPrefix = "w32ce-" + arch2;
                break;
            }
            case 0: {
                osPrefix = "darwin-" + arch2;
                break;
            }
            case 1: {
                osPrefix = "linux-" + arch2;
                break;
            }
            case 3: {
                osPrefix = "sunos-" + arch2;
                break;
            }
            case 12: {
                osPrefix = "dragonflybsd-" + arch2;
                break;
            }
            case 4: {
                osPrefix = "freebsd-" + arch2;
                break;
            }
            case 5: {
                osPrefix = "openbsd-" + arch2;
                break;
            }
            case 11: {
                osPrefix = "netbsd-" + arch2;
                break;
            }
            case 10: {
                osPrefix = "kfreebsd-" + arch2;
                break;
            }
            default: {
                osPrefix = name.toLowerCase();
                int space = osPrefix.indexOf(" ");
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space);
                }
                osPrefix = osPrefix + "-" + arch2;
            }
        }
        return osPrefix;
    }

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            if ("dalvik".equals(System.getProperty("java.vm.name").toLowerCase())) {
                osType = 8;
                System.setProperty("jna.nounpack", "true");
            } else {
                osType = 1;
            }
        } else {
            osType = osName.startsWith("AIX") ? 7 : (osName.startsWith("Mac") || osName.startsWith("Darwin") ? 0 : (osName.startsWith("Windows CE") ? 6 : (osName.startsWith("Windows") ? 2 : (osName.startsWith("Solaris") || osName.startsWith("SunOS") ? 3 : (osName.startsWith("FreeBSD") ? 4 : (osName.startsWith("OpenBSD") ? 5 : (osName.equalsIgnoreCase("gnu") ? 9 : (osName.equalsIgnoreCase("gnu/kfreebsd") ? 10 : (osName.equalsIgnoreCase("netbsd") ? 11 : (osName.equalsIgnoreCase("dragonflybsd") ? 12 : -1))))))))));
        }
        boolean hasBuffers = false;
        try {
            Class.forName("java.nio.Buffer");
            hasBuffers = true;
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        HAS_AWT = osType != 6 && osType != 8 && osType != 7;
        HAS_JAWT = HAS_AWT && osType != 0;
        HAS_BUFFERS = hasBuffers;
        boolean bl = RO_FIELDS = osType != 6;
        String string = osType == 2 ? "msvcrt" : (C_LIBRARY_NAME = osType == 6 ? "coredll" : "c");
        MATH_LIBRARY_NAME = osType == 2 ? "msvcrt" : (osType == 6 ? "coredll" : "m");
        ARCH = Platform.getCanonicalArchitecture(System.getProperty("os.arch"), osType);
        HAS_DLL_CALLBACKS = osType == 2 && !ARCH.startsWith("aarch");
        RESOURCE_PREFIX = Platform.getNativeLibraryResourcePrefix();
    }
}

