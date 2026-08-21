/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class RuntimeEnvironment {
    private static Boolean containsLine(String path, String line) {
        Boolean bl;
        block8: {
            Stream<String> stream = Files.lines(Paths.get(path, new String[0]));
            try {
                bl = stream.anyMatch(test -> test.contains(line));
                if (stream == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return false;
                }
            }
            stream.close();
        }
        return bl;
    }

    public static Boolean inContainer() {
        return RuntimeEnvironment.inDocker() != false || RuntimeEnvironment.inPodman() != false;
    }

    static Boolean inDocker() {
        return RuntimeEnvironment.containsLine("/proc/1/cgroup", "/docker");
    }

    static Boolean inPodman() {
        return RuntimeEnvironment.containsLine("/proc/1/environ", "container=podman");
    }

    static Boolean inWsl() {
        return RuntimeEnvironment.containsLine("/proc/1/environ", "container=wslcontainer_host_id");
    }

    @Deprecated
    public RuntimeEnvironment() {
    }
}

