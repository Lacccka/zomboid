/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

class NativeImageChecker {
    static final boolean isGraalVmNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private NativeImageChecker() {
    }
}

