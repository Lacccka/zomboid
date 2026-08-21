/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class LWJGLUtil {
    public static final int PLATFORM_LINUX = 1;
    public static final int PLATFORM_MACOSX = 2;
    public static final int PLATFORM_WINDOWS = 3;
    public static final String PLATFORM_LINUX_NAME = "linux";
    public static final String PLATFORM_MACOSX_NAME = "macosx";
    public static final String PLATFORM_WINDOWS_NAME = "windows";
    public static final boolean DEBUG = LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.Debug");
    public static final boolean CHECKS = !LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.NoChecks");
    private static final int PLATFORM;

    public static int getPlatform() {
        return PLATFORM;
    }

    public static String getPlatformName() {
        switch (LWJGLUtil.getPlatform()) {
            case 1: {
                return PLATFORM_LINUX_NAME;
            }
            case 2: {
                return PLATFORM_MACOSX_NAME;
            }
            case 3: {
                return PLATFORM_WINDOWS_NAME;
            }
        }
        return "unknown";
    }

    private static String getPrivilegedProperty(final String property_name) {
        return AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return System.getProperty(property_name);
            }
        });
    }

    public static boolean getPrivilegedBoolean(final String property_name) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return Boolean.getBoolean(property_name);
            }
        });
    }

    public static Integer getPrivilegedInteger(final String property_name) {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                return Integer.getInteger(property_name);
            }
        });
    }

    public static Integer getPrivilegedInteger(final String property_name, final int default_val) {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                return Integer.getInteger(property_name, default_val);
            }
        });
    }

    static {
        String osName = LWJGLUtil.getPrivilegedProperty("os.name");
        if (osName.startsWith("Windows")) {
            PLATFORM = 3;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            PLATFORM = 1;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            PLATFORM = 2;
        } else {
            throw new LinkageError("Unknown platform: " + osName);
        }
    }
}

