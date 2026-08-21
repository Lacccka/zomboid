/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import java.util.logging.Logger;

public final class RuntimeVersion {
    public static final RuntimeDomain OSS_DOMAIN;
    public static final int OSS_MAJOR = 4;
    public static final int OSS_MINOR = 31;
    public static final int OSS_PATCH = 1;
    public static final String OSS_SUFFIX = "";
    public static final RuntimeDomain DOMAIN;
    public static final int MAJOR = 4;
    public static final int MINOR = 31;
    public static final int PATCH = 1;
    public static final String SUFFIX = "";
    private static final int MAX_WARNING_COUNT = 20;
    static int majorWarningLoggedCount;
    static int minorWarningLoggedCount;
    private static final String VERSION_STRING;
    private static final Logger logger;

    public static void validateProtobufGencodeVersion(RuntimeDomain domain, int major, int minor, int patch, String suffix, String location) {
        RuntimeVersion.validateProtobufGencodeVersionImpl(domain, major, minor, patch, suffix, location);
    }

    private static void validateProtobufGencodeVersionImpl(RuntimeDomain domain, int major, int minor, int patch, String suffix, String location) {
        if (RuntimeVersion.checkDisabled()) {
            return;
        }
        if (major < 0 || minor < 0 || patch < 0) {
            throw new ProtobufRuntimeVersionException("Invalid gencode version: " + RuntimeVersion.versionString(major, minor, patch, suffix));
        }
        if (domain != DOMAIN) {
            throw new ProtobufRuntimeVersionException(String.format("Detected mismatched Protobuf Gencode/Runtime domains when loading %s: gencode %s, runtime %s. Cross-domain usage of Protobuf is not supported.", new Object[]{location, domain, DOMAIN}));
        }
        String gencodeVersionString = null;
        if (major != 4) {
            if (major == 3 && majorWarningLoggedCount < 20) {
                gencodeVersionString = RuntimeVersion.versionString(major, minor, patch, suffix);
                logger.warning(String.format(" Protobuf gencode version %s is exactly one major version older than the runtime version %s at %s. Please update the gencode to avoid compatibility violations in the next runtime release.", gencodeVersionString, VERSION_STRING, location));
                ++majorWarningLoggedCount;
            } else {
                throw new ProtobufRuntimeVersionException(String.format("Detected mismatched Protobuf Gencode/Runtime major versions when loading %s: gencode %s, runtime %s. Same major version is required.", location, RuntimeVersion.versionString(major, minor, patch, suffix), VERSION_STRING));
            }
        }
        if (31 < minor || minor == 31 && 1 < patch) {
            if (gencodeVersionString == null) {
                gencodeVersionString = RuntimeVersion.versionString(major, minor, patch, suffix);
            }
            throw new ProtobufRuntimeVersionException(String.format("Detected incompatible Protobuf Gencode/Runtime versions when loading %s: gencode %s, runtime %s. Runtime version cannot be older than the linked gencode version.", location, gencodeVersionString, VERSION_STRING));
        }
        if (!suffix.equals("")) {
            if (gencodeVersionString == null) {
                gencodeVersionString = RuntimeVersion.versionString(major, minor, patch, suffix);
            }
            throw new ProtobufRuntimeVersionException(String.format("Detected mismatched Protobuf Gencode/Runtime version suffixes when loading %s: gencode %s, runtime %s. Version suffixes must be the same.", location, gencodeVersionString, VERSION_STRING));
        }
    }

    private static String versionString(int major, int minor, int patch, String suffix) {
        return String.format("%d.%d.%d%s", major, minor, patch, suffix);
    }

    private static boolean checkDisabled() {
        String disableFlag = System.getenv("TEMPORARILY_DISABLE_PROTOBUF_VERSION_CHECK");
        return disableFlag != null && disableFlag.equals("true");
    }

    private RuntimeVersion() {
    }

    static {
        DOMAIN = OSS_DOMAIN = RuntimeDomain.PUBLIC;
        majorWarningLoggedCount = 0;
        minorWarningLoggedCount = 0;
        VERSION_STRING = RuntimeVersion.versionString(4, 31, 1, "");
        logger = Logger.getLogger(RuntimeVersion.class.getName());
    }

    public static enum RuntimeDomain {
        GOOGLE_INTERNAL,
        PUBLIC;

    }

    public static final class ProtobufRuntimeVersionException
    extends RuntimeException {
        public ProtobufRuntimeVersionException(String message) {
            super(message);
        }
    }
}

