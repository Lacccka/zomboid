/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMFunctionAddress;
import org.lwjgl.system.ffm.FFMNullable;
import org.lwjgl.system.ffm.FFMPointer;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class JNI {
    private static final JNIBindings jni = FFM.ffmGenerate(JNIBindings.class, FFM.ffmConfigBuilder(MethodHandles.lookup()).withChecks(false).build());

    private JNI() {
    }

    public static byte invokePB(long param0, long __functionAddress) {
        return jni.invokePB(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static short invokeC(long __functionAddress) {
        return jni.invokeC(MemorySegment.ofAddress(__functionAddress));
    }

    public static short invokeC(int param0, long __functionAddress) {
        return jni.invokeC(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static short invokePC(long param0, long __functionAddress) {
        return jni.invokePC(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static short invokeCC(int param0, short param1, long __functionAddress) {
        return jni.invokeCC(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short invokeCC(short param0, boolean param1, long __functionAddress) {
        return jni.invokeCC(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short invokePC(long param0, int param1, long __functionAddress) {
        return jni.invokePC(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short invokeJC(int param0, int param1, long param2, long __functionAddress) {
        return jni.invokeJC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokeCUC(short param0, byte param1, long __functionAddress) {
        return jni.invokeCUC(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short invokePCC(long param0, short param1, long __functionAddress) {
        return jni.invokePCC(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short invokeCCC(short param0, short param1, boolean param2, long __functionAddress) {
        return jni.invokeCCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokePCC(int param0, long param1, short param2, long __functionAddress) {
        return jni.invokePCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokePCC(long param0, int param1, short param2, long __functionAddress) {
        return jni.invokePCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokeUPC(byte param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokeUPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokePCC(long param0, int param1, int param2, short param3, long __functionAddress) {
        return jni.invokePCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short invokeCJC(int param0, boolean param1, short param2, int param3, long param4, long __functionAddress) {
        return jni.invokeCJC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short invokeCPCC(short param0, long param1, short param2, long __functionAddress) {
        return jni.invokeCPCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokeCPPC(short param0, long param1, long param2, long __functionAddress) {
        return jni.invokeCPPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokePPCC(long param0, long param1, short param2, long __functionAddress) {
        return jni.invokePPCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short invokeCCJC(short param0, short param1, int param2, long param3, long __functionAddress) {
        return jni.invokeCCJC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short invokePCCC(long param0, short param1, short param2, int param3, int param4, long __functionAddress) {
        return jni.invokePCCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short invokeCCCCC(short param0, short param1, short param2, short param3, long __functionAddress) {
        return jni.invokeCCCCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short invokePJUPC(long param0, long param1, byte param2, long param3, long __functionAddress) {
        return jni.invokePJUPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short invokeCCJPC(short param0, boolean param1, short param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokeCCJPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short invokePCCCCC(long param0, short param1, short param2, short param3, short param4, long __functionAddress) {
        return jni.invokePCCCCC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short invokeCCCJPC(short param0, short param1, short param2, boolean param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.invokeCCCJPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static short invokeCCCJPC(short param0, short param1, boolean param2, short param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.invokeCCCJPC(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static double invokeD(long __functionAddress) {
        return jni.invokeD(MemorySegment.ofAddress(__functionAddress));
    }

    public static double invokeD(int param0, long __functionAddress) {
        return jni.invokeD(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static double invokePD(long param0, long __functionAddress) {
        return jni.invokePD(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static double invokePD(long param0, int param1, long __functionAddress) {
        return jni.invokePD(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static double invokePD(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePD(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static double invokePPD(long param0, long param1, long __functionAddress) {
        return jni.invokePPD(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static float invokeF(long __functionAddress) {
        return jni.invokeF(MemorySegment.ofAddress(__functionAddress));
    }

    public static float invokeF(int param0, long __functionAddress) {
        return jni.invokeF(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static float invokePF(long param0, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static float invokePF(float param0, long param1, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static float invokePF(long param0, int param1, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static float invokePF(float param0, float param1, long param2, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float invokePF(int param0, long param1, float param2, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float invokePF(long param0, float param1, float param2, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float invokePF(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float invokePPF(long param0, long param1, long __functionAddress) {
        return jni.invokePPF(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static float invokePPF(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePPF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float invokePPF(long param0, float param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokeI(long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress));
    }

    public static int invokeI(int param0, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int invokeI(boolean param0, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int invokeI(int param0, float param1, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokeI(int param0, int param1, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokeI(int param0, boolean param1, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokeI(int param0, int param1, int param2, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokeI(int param0, int param1, int param2, int param3, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokeI(int param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokeI(int param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.invokeI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokeJI(long param0, long __functionAddress) {
        return jni.invokeJI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int invokePI(long param0, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int invokeCI(int param0, short param1, long __functionAddress) {
        return jni.invokeCI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePI(int param0, long param1, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePI(long param0, int param1, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePI(long param0, boolean param1, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokeCI(int param0, short param1, boolean param2, long __functionAddress) {
        return jni.invokeCI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePI(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePI(long param0, int param1, boolean param2, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePI(long param0, int param1, int param2, int param3, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePI(long param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePI(long param0, int param1, int param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        return jni.invokePI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokeCPI(short param0, long param1, long __functionAddress) {
        return jni.invokeCPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePCI(long param0, short param1, long __functionAddress) {
        return jni.invokePCI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePJI(long param0, long param1, long __functionAddress) {
        return jni.invokePJI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePNI(long param0, long param1, long __functionAddress) {
        return jni.invokePNI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePPI(long param0, long param1, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int invokePJI(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePNI(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePNI(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(int param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, long param1, float param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, boolean param1, long param2, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPI(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, int param1, long param2, boolean param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, int param1, boolean param2, long param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, long param1, boolean param2, boolean param3, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPI(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPI(long param0, int param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPI(long param0, int param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPI(int param0, long param1, int param2, long param3, int param4, boolean param5, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPI(long param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPI(long param0, int param1, int param2, int param3, int param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokeCPUI(short param0, long param1, byte param2, long __functionAddress) {
        return jni.invokeCPUI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokeJPPI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokeJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePCPI(long param0, short param1, long param2, long __functionAddress) {
        return jni.invokePCPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePNNI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePNNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePNPI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPCI(long param0, long param1, short param2, long __functionAddress) {
        return jni.invokePPCI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPJI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPNI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePPPI(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int invokePNPI(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.invokePNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePNPI(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPNI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPI(int param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPI(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPI(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePNNI(long param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.invokePNNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, int param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, long param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPI(long param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPI(long param0, int param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPI(long param0, int param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPI(long param0, long param1, int param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPI(long param0, long param1, long param2, int param3, boolean param4, float param5, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPI(long param0, int param1, int param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPI(long param0, int param1, int param2, int param3, long param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPI(long param0, int param1, int param2, long param3, int param4, long param5, int param6, int param7, long __functionAddress) {
        return jni.invokePPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePNPPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePNPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPNNI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPNNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPNPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPNI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePPPPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePUUUI(long param0, byte param1, byte param2, byte param3, long __functionAddress) {
        return jni.invokePUUUI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int invokePNNPI(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePNNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPNI(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPI(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPI(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPI(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPI(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, long param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, long param1, int param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, long param1, long param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, long param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPI(long param0, int param1, int param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPI(long param0, int param1, int param2, long param3, long param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPI(long param0, int param1, long param2, int param3, long param4, int param5, long param6, int param7, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPI(long param0, long param1, long param2, int param3, int param4, int param5, long param6, int param7, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPI(long param0, long param1, long param2, long param3, int param4, int param5, int param6, int param7, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPI(long param0, int param1, int param2, long param3, int param4, long param5, long param6, int param7, int param8, long __functionAddress) {
        return jni.invokePPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int invokePNNPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePNNPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPNNPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPNNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPNNI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPNNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPNI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPPPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePPUUUI(long param0, long param1, byte param2, byte param3, byte param4, long __functionAddress) {
        return jni.invokePPUUUI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePUUUUI(long param0, byte param1, byte param2, byte param3, byte param4, long __functionAddress) {
        return jni.invokePUUUUI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int invokePJPPNI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePJPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPNPPI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPNPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPNPPI(long param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPNPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPNJI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPNJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPNNI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPNNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPNPI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPNI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPNI(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPPI(int param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPPI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPPI(long param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPPPPI(long param0, long param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePNPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePNPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPPI(long param0, long param1, int param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPPI(long param0, long param1, int param2, long param3, int param4, long param5, int param6, long param7, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPI(long param0, long param1, int param2, long param3, long param4, int param5, int param6, long param7, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPI(long param0, long param1, long param2, int param3, long param4, int param5, int param6, long param7, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPI(long param0, long param1, long param2, int param3, int param4, int param5, float param6, long param7, long param8, long __functionAddress) {
        return jni.invokePPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int invokePPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePPUUUUI(long param0, long param1, byte param2, byte param3, byte param4, byte param5, long __functionAddress) {
        return jni.invokePPUUUUI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int invokePJJJJPI(long param0, long param1, long param2, long param3, long param4, int param5, long param6, long __functionAddress) {
        return jni.invokePJJJJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPNPPPI(long param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPNPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPPPI(long param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePNNPPPI(long param0, long param1, long param2, int param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePNNPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPPI(int param0, int param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int invokePPPPPPI(long param0, long param1, int param2, long param3, int param4, long param5, int param6, long param7, int param8, long param9, long __functionAddress) {
        return jni.invokePPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static int invokePPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int invokePPPPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.invokePPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int invokePPPPPPPI(long param0, long param1, long param2, int param3, long param4, float param5, float param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.invokePPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static int invokePPPPPPPI(long param0, long param1, long param2, int param3, int param4, long param5, long param6, int param7, long param8, int param9, long param10, int param11, long __functionAddress) {
        return jni.invokePPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static int invokePPPPPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.invokePPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokeJ(long __functionAddress) {
        return jni.invokeJ(MemorySegment.ofAddress(__functionAddress));
    }

    public static long invokeJ(int param0, int param1, long __functionAddress) {
        return jni.invokeJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePJ(long param0, long __functionAddress) {
        return jni.invokePJ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokePJ(long param0, int param1, long __functionAddress) {
        return jni.invokePJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePJ(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJJ(long param0, long param1, long __functionAddress) {
        return jni.invokePJJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePPJ(long param0, long param1, long __functionAddress) {
        return jni.invokePPJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePJJ(int param0, long param1, long param2, long __functionAddress) {
        return jni.invokePJJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJJ(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePJJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJJ(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePJJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPJ(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePPJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokeNN(long param0, long __functionAddress) {
        return jni.invokeNN(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokePN(long param0, long __functionAddress) {
        return jni.invokePN(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokePN(long param0, int param1, long __functionAddress) {
        return jni.invokePN(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokeNNN(long param0, long param1, long __functionAddress) {
        return jni.invokeNNN(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePPN(long param0, long param1, long __functionAddress) {
        return jni.invokePPN(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokeNNNN(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokeNNNN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePNPN(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePNPN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePNPN(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePNPN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPNN(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPNN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePNPNN(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePNPNN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePNPNPN(long param0, long param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, long param9, long param10, long param11, long __functionAddress) {
        return jni.invokePNPNPN(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static long invokeP(long __functionAddress) {
        return jni.invokeP(MemorySegment.ofAddress(__functionAddress));
    }

    public static long invokeP(int param0, long __functionAddress) {
        return jni.invokeP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokeP(boolean param0, long __functionAddress) {
        return jni.invokeP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokeP(int param0, int param1, long __functionAddress) {
        return jni.invokeP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokeP(int param0, int param1, int param2, long __functionAddress) {
        return jni.invokeP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokeCP(short param0, long __functionAddress) {
        return jni.invokeCP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokeJP(long param0, long __functionAddress) {
        return jni.invokeJP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokePP(long param0, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long invokePP(int param0, long param1, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePP(long param0, double param1, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePP(long param0, float param1, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePP(long param0, int param1, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePP(long param0, boolean param1, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePP(int param0, int param1, long param2, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePP(int param0, boolean param1, long param2, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePP(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePP(int param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePP(long param0, int param1, int param2, int param3, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePP(int param0, int param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePP(long param0, float param1, int param2, float param3, int param4, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePP(long param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePP(long param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.invokePP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokeCCP(short param0, short param1, long __functionAddress) {
        return jni.invokeCCP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokeJPP(long param0, long param1, long __functionAddress) {
        return jni.invokeJPP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePJP(long param0, long param1, long __functionAddress) {
        return jni.invokePJP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePNP(long param0, long param1, long __functionAddress) {
        return jni.invokePNP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePPP(long param0, long param1, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokePUP(long param0, byte param1, long __functionAddress) {
        return jni.invokePUP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long invokeCPP(int param0, short param1, long param2, long __functionAddress) {
        return jni.invokeCPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePCP(long param0, short param1, boolean param2, long __functionAddress) {
        return jni.invokePCP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJP(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJP(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPP(int param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPP(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPP(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPP(long param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJP(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.invokePJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, long param1, boolean param2, boolean param3, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, boolean param1, boolean param2, long param3, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPP(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPP(long param0, int param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePJP(long param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.invokePJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPP(int param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPP(long param0, long param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePUP(long param0, int param1, byte param2, int param3, boolean param4, boolean param5, long __functionAddress) {
        return jni.invokePUP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPP(int param0, int param1, int param2, int param3, long param4, int param5, long param6, long __functionAddress) {
        return jni.invokePPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokeCCPP(short param0, short param1, long param2, long __functionAddress) {
        return jni.invokeCCPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokeCPCP(short param0, long param1, short param2, long __functionAddress) {
        return jni.invokeCPCP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePJJP(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePJJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPJP(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPPP(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPUP(long param0, long param1, byte param2, long __functionAddress) {
        return jni.invokePPUP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long invokePPPP(int param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPP(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPP(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPP(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPP(long param0, long param1, boolean param2, long param3, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPUP(long param0, long param1, int param2, byte param3, long __functionAddress) {
        return jni.invokePPUP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPP(int param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPP(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPP(long param0, long param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPP(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPP(long param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokeJPPP(int param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokeJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPP(long param0, int param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPP(long param0, long param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPP(long param0, long param1, int param2, int param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPP(int param0, int param1, int param2, long param3, long param4, int param5, long param6, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPP(long param0, long param1, int param2, int param3, long param4, int param5, int param6, long __functionAddress) {
        return jni.invokePPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePBPPP(long param0, byte param1, long param2, long param3, long __functionAddress) {
        return jni.invokePBPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePNNPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePNNPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPJPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPNNP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPNNP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long invokePPPJP(int param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPJP(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPPP(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPPP(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPPP(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPPP(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePJPPP(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePJPPP(long param0, long param1, int param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPJP(long param0, long param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.invokePPPJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPP(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPP(long param0, long param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPP(long param0, long param1, long param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPP(long param0, long param1, long param2, int param3, long param4, int param5, int param6, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPP(long param0, long param1, long param2, long param3, int param4, int param5, int param6, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPP(int param0, int param1, int param2, long param3, long param4, long param5, int param6, long param7, long __functionAddress) {
        return jni.invokePPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePJPJPP(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePJPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePNNNPP(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePNNNPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPBPPP(long param0, long param1, byte param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPBPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long invokeCCCUJP(short param0, short param1, short param2, byte param3, int param4, long param5, long __functionAddress) {
        return jni.invokeCCCUJP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPJPP(long param0, long param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPNP(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPNP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPJPPP(long param0, long param1, long param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, int param3, long param4, int param5, long param6, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, int param6, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, int param6, int param7, int param8, long __functionAddress) {
        return jni.invokePPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static long invokePPJJPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPJJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePSSCCPP(long param0, short param1, short param2, short param3, short param4, long param5, long __functionAddress) {
        return jni.invokePSSCCPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long invokePPPPPPP(long param0, long param1, int param2, int param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, int param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, int param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, int param6, long param7, int param8, int param9, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokePPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, int param8, int param9, long __functionAddress) {
        return jni.invokePPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokePPPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPJPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, long __functionAddress) {
        return jni.invokePPPPPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static long invokePPPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, int param8, long __functionAddress) {
        return jni.invokePPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static long invokePPPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, int param9, long __functionAddress) {
        return jni.invokePPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokePPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.invokePPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long invokePPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long param8, long __functionAddress) {
        return jni.invokePPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static long invokePPPPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, int param8, long param9, long __functionAddress) {
        return jni.invokePPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokePPPPJJPPP(long param0, long param1, long param2, long param3, int param4, long param5, int param6, long param7, int param8, long param9, long param10, long __functionAddress) {
        return jni.invokePPPPJJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPJJPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, int param9, long param10, long __functionAddress) {
        return jni.invokePPPPPJJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPJPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, int param9, long param10, long __functionAddress) {
        return jni.invokePPPPPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, int param9, long param10, long __functionAddress) {
        return jni.invokePPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPJPPP(long param0, int param1, long param2, long param3, long param4, long param5, int param6, int param7, long param8, int param9, long param10, long param11, long __functionAddress) {
        return jni.invokePPPPPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static long invokePPPPPJPPP(long param0, int param1, long param2, long param3, long param4, long param5, int param6, int param7, long param8, int param9, int param10, long param11, long param12, long __functionAddress) {
        return jni.invokePPPPPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }

    public static long invokePPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long param8, int param9, int param10, int param11, int param12, int param13, long __functionAddress) {
        return jni.invokePPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
    }

    public static long invokePPPPPJJJPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, long param7, long param8, int param9, long param10, long __functionAddress) {
        return jni.invokePPPPPJJJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, long param8, int param9, long param10, long __functionAddress) {
        return jni.invokePPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static long invokePPPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long param8, int param9, long param10, int param11, long __functionAddress) {
        return jni.invokePPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static long invokePPPPPJPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, int param8, long param9, int param10, int param11, long param12, long param13, long __functionAddress) {
        return jni.invokePPPPPJPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
    }

    public static long invokePPPPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.invokePPPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long invokePPPPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long param8, int param9, long param10, long param11, int param12, long __functionAddress) {
        return jni.invokePPPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }

    public static long invokePPPPPJPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, int param7, int param8, long param9, long param10, long param11, long param12, long param13, long __functionAddress) {
        return jni.invokePPPPPJPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
    }

    public static long invokePPPPPJPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, int param8, long param9, long param10, int param11, int param12, long param13, long param14, long param15, long __functionAddress) {
        return jni.invokePPPPPJPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15);
    }

    public static long invokePPPPPPPPPPPPP(long param0, int param1, long param2, long param3, long param4, int param5, long param6, long param7, int param8, long param9, long param10, int param11, int param12, int param13, int param14, long param15, long param16, long param17, long param18, long __functionAddress) {
        return jni.invokePPPPPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16, param17, param18);
    }

    public static long invokePPPPPJPPPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long param6, int param7, long param8, long param9, int param10, long param11, long param12, long param13, long param14, long param15, long __functionAddress) {
        return jni.invokePPPPPJPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15);
    }

    public static long invokePPPPPJJPPPPPPP(long param0, long param1, long param2, long param3, long param4, int param5, long param6, int param7, long param8, int param9, long param10, long param11, int param12, long param13, long param14, long param15, long param16, long __functionAddress) {
        return jni.invokePPPPPJJPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16);
    }

    public static short invokePS(long param0, int param1, long __functionAddress) {
        return jni.invokePS(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static byte invokeU(int param0, long __functionAddress) {
        return jni.invokeU(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static byte invokePU(long param0, int param1, long __functionAddress) {
        return jni.invokePU(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static byte invokeUPU(byte param0, long param1, long __functionAddress) {
        return jni.invokeUPU(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeV(long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress));
    }

    public static void invokeV(double param0, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeV(float param0, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeV(int param0, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeV(boolean param0, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeV(int param0, float param1, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeV(int param0, int param1, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeV(int param0, boolean param1, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeV(int param0, int param1, double param2, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeV(int param0, int param1, float param2, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeV(int param0, int param1, int param2, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeV(int param0, float param1, float param2, float param3, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeV(int param0, int param1, int param2, int param3, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeV(int param0, int param1, double param2, double param3, double param4, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeV(int param0, int param1, float param2, float param3, float param4, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeV(int param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        jni.invokeV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeCV(short param0, long __functionAddress) {
        jni.invokeCV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeJV(long param0, long __functionAddress) {
        jni.invokeJV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokePV(long param0, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeUV(byte param0, long __functionAddress) {
        jni.invokeUV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void invokeCV(int param0, short param1, long __functionAddress) {
        jni.invokeCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeCV(short param0, int param1, long __functionAddress) {
        jni.invokeCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeCV(short param0, boolean param1, long __functionAddress) {
        jni.invokeCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeJV(int param0, long param1, long __functionAddress) {
        jni.invokeJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeJV(long param0, int param1, long __functionAddress) {
        jni.invokeJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePV(int param0, long param1, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePV(long param0, float param1, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePV(long param0, int param1, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePV(long param0, boolean param1, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeUV(byte param0, int param1, long __functionAddress) {
        jni.invokeUV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeUV(byte param0, boolean param1, long __functionAddress) {
        jni.invokeUV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeCV(short param0, int param1, int param2, long __functionAddress) {
        jni.invokeCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeJV(int param0, int param1, long param2, long __functionAddress) {
        jni.invokeJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(int param0, int param1, long param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(int param0, long param1, boolean param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, double param1, double param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, float param1, float param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, int param1, double param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, int param1, float param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, int param1, int param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(long param0, int param1, boolean param2, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePV(int param0, int param1, int param2, long param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(long param0, float param1, float param2, float param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(long param0, float param1, float param2, int param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(long param0, int param1, int param2, double param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(long param0, int param1, int param2, float param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(long param0, int param1, int param2, int param3, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePV(int param0, int param1, long param2, int param3, int param4, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePV(long param0, int param1, float param2, float param3, float param4, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePV(long param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeUV(byte param0, float param1, float param2, float param3, float param4, long __functionAddress) {
        jni.invokeUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePV(int param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(int param0, int param1, int param2, int param3, long param4, boolean param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(long param0, int param1, int param2, double param3, double param4, double param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(long param0, int param1, int param2, float param3, float param4, float param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(long param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(long param0, int param1, int param2, int param3, int param4, boolean param5, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePV(int param0, int param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.invokePV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokeCCV(short param0, short param1, long __functionAddress) {
        jni.invokeCCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeCPV(short param0, long param1, long __functionAddress) {
        jni.invokeCPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePCV(long param0, short param1, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePJV(long param0, long param1, long __functionAddress) {
        jni.invokePJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePNV(long param0, long param1, long __functionAddress) {
        jni.invokePNV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePPV(long param0, long param1, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokePUV(long param0, byte param1, long __functionAddress) {
        jni.invokePUV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeUPV(byte param0, long param1, long __functionAddress) {
        jni.invokeUPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void invokeCPV(short param0, int param1, long param2, long __functionAddress) {
        jni.invokeCPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeCPV(short param0, long param1, int param2, long __functionAddress) {
        jni.invokeCPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePCV(long param0, int param1, short param2, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePCV(long param0, short param1, boolean param2, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePJV(int param0, long param1, long param2, long __functionAddress) {
        jni.invokePJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePJV(long param0, int param1, long param2, long __functionAddress) {
        jni.invokePJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePJV(long param0, long param1, int param2, long __functionAddress) {
        jni.invokePJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPV(int param0, long param1, long param2, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPV(long param0, int param1, long param2, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPV(long param0, long param1, float param2, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPV(long param0, long param1, int param2, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPV(long param0, long param1, boolean param2, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeUCV(byte param0, short param1, int param2, long __functionAddress) {
        jni.invokeUCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePBV(long param0, int param1, int param2, byte param3, long __functionAddress) {
        jni.invokePBV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePCV(long param0, int param1, int param2, short param3, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePCV(long param0, short param1, int param2, int param3, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePJV(long param0, int param1, int param2, long param3, long __functionAddress) {
        jni.invokePJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(int param0, int param1, long param2, long param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(int param0, long param1, long param2, int param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(long param0, int param1, int param2, long param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(long param0, int param1, long param2, int param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(long param0, int param1, long param2, boolean param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPV(long param0, long param1, int param2, int param3, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePSV(long param0, int param1, int param2, short param3, long __functionAddress) {
        jni.invokePSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePUV(long param0, int param1, int param2, byte param3, long __functionAddress) {
        jni.invokePUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeUCV(byte param0, short param1, int param2, int param3, long __functionAddress) {
        jni.invokeUCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeUPV(byte param0, long param1, int param2, int param3, long __functionAddress) {
        jni.invokeUPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePCV(long param0, short param1, int param2, int param3, int param4, long __functionAddress) {
        jni.invokePCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPV(int param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPV(long param0, float param1, long param2, int param3, int param4, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPV(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPV(long param0, long param1, int param2, int param3, int param4, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPV(int param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPV(long param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPV(long param0, int param1, int param2, long param3, int param4, int param5, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPV(long param0, long param1, float param2, float param3, float param4, float param5, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPV(int param0, int param1, int param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPV(long param0, int param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPV(long param0, int param1, int param2, int param3, int param4, long param5, boolean param6, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPV(long param0, long param1, int param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPV(long param0, long param1, float param2, float param3, float param4, float param5, float param6, float param7, long __functionAddress) {
        jni.invokePPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokeCCPV(short param0, short param1, long param2, long __functionAddress) {
        jni.invokeCCPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeCPCV(short param0, long param1, short param2, long __functionAddress) {
        jni.invokeCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeCPPV(short param0, long param1, long param2, long __functionAddress) {
        jni.invokeCPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeJPPV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokeJPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePJPV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokePJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePNNV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokePNNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePNPV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokePNPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPNV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokePPNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokePPPV(long param0, long param1, long param2, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void invokeCCCV(short param0, short param1, short param2, int param3, long __functionAddress) {
        jni.invokeCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeCCUV(short param0, short param1, int param2, byte param3, long __functionAddress) {
        jni.invokeCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePJPV(long param0, long param1, long param2, int param3, long __functionAddress) {
        jni.invokePJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPJV(long param0, int param1, long param2, long param3, long __functionAddress) {
        jni.invokePPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(int param0, long param1, long param2, long param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(long param0, int param1, long param2, long param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(long param0, long param1, int param2, long param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(long param0, long param1, long param2, float param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(long param0, long param1, long param2, int param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPV(long param0, long param1, long param2, boolean param3, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePUCV(long param0, byte param1, short param2, int param3, long __functionAddress) {
        jni.invokePUCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeUCCV(byte param0, short param1, short param2, int param3, long __functionAddress) {
        jni.invokeUCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeCCUV(short param0, short param1, int param2, float param3, byte param4, long __functionAddress) {
        jni.invokeCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeJJJV(int param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.invokeJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePNNV(long param0, long param1, int param2, int param3, long param4, long __functionAddress) {
        jni.invokePNNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(int param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, long param1, int param2, int param3, long param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, long param1, long param2, float param3, float param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, long param1, long param2, int param3, boolean param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPV(long param0, long param1, long param2, boolean param3, boolean param4, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePUCV(long param0, byte param1, short param2, int param3, int param4, long __functionAddress) {
        jni.invokePUCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePUPV(long param0, byte param1, long param2, int param3, int param4, long __functionAddress) {
        jni.invokePUPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeUCCV(byte param0, short param1, int param2, int param3, short param4, long __functionAddress) {
        jni.invokeUCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeUCUV(byte param0, short param1, byte param2, int param3, int param4, long __functionAddress) {
        jni.invokeUCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeUPCV(byte param0, long param1, int param2, int param3, short param4, long __functionAddress) {
        jni.invokeUPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeCCUV(short param0, short param1, int param2, int param3, int param4, byte param5, long __functionAddress) {
        jni.invokeCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(int param0, int param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, int param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, int param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, long param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, long param1, long param2, int param3, boolean param4, boolean param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, boolean param1, int param2, long param3, long param4, int param5, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPV(long param0, int param1, int param2, long param3, int param4, long param5, int param6, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPV(long param0, int param1, long param2, int param3, long param4, int param5, int param6, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPV(long param0, long param1, float param2, float param3, float param4, float param5, long param6, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPV(long param0, long param1, long param2, float param3, float param4, float param5, float param6, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPV(long param0, long param1, long param2, int param3, int param4, int param5, boolean param6, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPV(long param0, long param1, int param2, int param3, int param4, long param5, int param6, boolean param7, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePPPV(long param0, long param1, float param2, float param3, float param4, float param5, float param6, float param7, long param8, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void invokePPPV(long param0, long param1, long param2, float param3, float param4, float param5, float param6, float param7, float param8, long __functionAddress) {
        jni.invokePPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void invokeCCPCV(short param0, short param1, long param2, short param3, long __functionAddress) {
        jni.invokeCCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeCCUPV(short param0, short param1, byte param2, long param3, long __functionAddress) {
        jni.invokeCCUPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePCPCV(long param0, short param1, long param2, short param3, long __functionAddress) {
        jni.invokePCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePNPPV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.invokePNPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokePPPPV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void invokeCCCUV(short param0, short param1, short param2, int param3, byte param4, long __functionAddress) {
        jni.invokeCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePCCUV(long param0, short param1, short param2, int param3, byte param4, long __functionAddress) {
        jni.invokePCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePJJPV(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.invokePJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPCPV(long param0, long param1, short param2, int param3, long param4, long __functionAddress) {
        jni.invokePPCPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPCV(long param0, long param1, int param2, long param3, short param4, long __functionAddress) {
        jni.invokePPPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPV(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPV(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPV(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPV(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPV(long param0, long param1, long param2, long param3, boolean param4, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePUCCV(long param0, byte param1, short param2, short param3, int param4, long __functionAddress) {
        jni.invokePUCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeCCCUV(short param0, short param1, short param2, int param3, int param4, byte param5, long __functionAddress) {
        jni.invokeCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePJJJV(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPV(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPV(long param0, long param1, long param2, float param3, float param4, long param5, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePUCCV(long param0, byte param1, short param2, int param3, int param4, short param5, long __functionAddress) {
        jni.invokePUCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePUCUV(long param0, byte param1, short param2, byte param3, int param4, int param5, long __functionAddress) {
        jni.invokePUCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePUPCV(long param0, byte param1, long param2, int param3, int param4, short param5, long __functionAddress) {
        jni.invokePUPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokeCCCUV(short param0, short param1, short param2, int param3, int param4, int param5, byte param6, long __functionAddress) {
        jni.invokeCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePCCUV(long param0, short param1, short param2, int param3, int param4, int param5, byte param6, long __functionAddress) {
        jni.invokePCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPPV(long param0, long param1, long param2, float param3, float param4, float param5, float param6, long param7, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePPPPV(long param0, long param1, long param2, int param3, int param4, int param5, float param6, long param7, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePPPPV(long param0, long param1, long param2, float param3, float param4, float param5, float param6, float param7, float param8, long param9, long __functionAddress) {
        jni.invokePPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void invokeCCCCCV(short param0, short param1, short param2, short param3, short param4, long __functionAddress) {
        jni.invokeCCCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokeCCUPPV(short param0, short param1, byte param2, long param3, long param4, long __functionAddress) {
        jni.invokeCCUPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPCPPV(long param0, long param1, short param2, long param3, long param4, long __functionAddress) {
        jni.invokePPCPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePPPPPV(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void invokePCCCUV(long param0, short param1, short param2, short param3, int param4, byte param5, long __functionAddress) {
        jni.invokePCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePJPPPV(long param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        jni.invokePJPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPPV(int param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPPV(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPPV(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPPV(long param0, long param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePCCCUV(long param0, short param1, short param2, short param3, int param4, int param5, byte param6, long __functionAddress) {
        jni.invokePCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPPPV(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPPPV(long param0, long param1, long param2, long param3, int param4, long param5, boolean param6, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePCCCCV(long param0, short param1, short param2, short param3, boolean param4, boolean param5, short param6, int param7, long __functionAddress) {
        jni.invokePCCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePCCCUV(long param0, short param1, short param2, short param3, int param4, int param5, int param6, byte param7, long __functionAddress) {
        jni.invokePCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePPPPPV(long param0, long param1, int param2, long param3, int param4, long param5, int param6, long param7, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokePPPPPV(long param0, long param1, int param2, long param3, long param4, int param5, int param6, long param7, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokeCCCCUV(short param0, short param1, short param2, int param3, short param4, int param5, int param6, int param7, byte param8, long __functionAddress) {
        jni.invokeCCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void invokePPPPPV(int param0, long param1, int param2, long param3, long param4, long param5, int param6, long param7, int param8, boolean param9, long __functionAddress) {
        jni.invokePPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void invokeCCCCPCV(short param0, short param1, short param2, short param3, long param4, short param5, long __functionAddress) {
        jni.invokeCCCCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePPPPPPV(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.invokePPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void invokePCCCCUV(long param0, short param1, int param2, short param3, short param4, short param5, byte param6, long __functionAddress) {
        jni.invokePCCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPPPPV(int param0, long param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.invokePPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePCCCCUV(long param0, short param1, short param2, short param3, int param4, short param5, int param6, int param7, int param8, byte param9, long __functionAddress) {
        jni.invokePCCCCUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void invokePPPPPPPV(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.invokePPPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void invokePPPPPPPV(long param0, int param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        jni.invokePPPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void invokeCCUCCCCPCV(short param0, short param1, byte param2, short param3, short param4, short param5, short param6, long param7, short param8, long __functionAddress) {
        jni.invokeCCUCCCCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void invokeCUCCCCCCPV(short param0, byte param1, short param2, short param3, short param4, short param5, short param6, short param7, long param8, long __functionAddress) {
        jni.invokeCUCCCCCCPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void invokeCCUUCCCCPCV(short param0, short param1, byte param2, byte param3, short param4, short param5, short param6, short param7, long param8, short param9, long __functionAddress) {
        jni.invokeCCUUCCCCPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void invokeCCUUUUUUUUUV(short param0, short param1, float param2, byte param3, byte param4, byte param5, byte param6, byte param7, byte param8, byte param9, byte param10, byte param11, long __functionAddress) {
        jni.invokeCCUUUUUUUUUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static void invokeCCUCCCCUCCCCCCV(short param0, short param1, byte param2, short param3, short param4, short param5, short param6, byte param7, short param8, short param9, short param10, short param11, short param12, short param13, long __functionAddress) {
        jni.invokeCCUCCCCUCCCCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
    }

    public static void invokePCCUCCCCUCCCCCCV(long param0, short param1, short param2, byte param3, short param4, short param5, short param6, short param7, byte param8, short param9, short param10, short param11, short param12, short param13, short param14, long __functionAddress) {
        jni.invokePCCUCCCCUCCCCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14);
    }

    public static boolean invokeZ(long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress));
    }

    public static boolean invokeZ(int param0, long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean invokeZ(boolean param0, long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean invokeZ(float param0, float param1, long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokeZ(int param0, float param1, long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokeZ(int param0, int param1, long __functionAddress) {
        return jni.invokeZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePZ(long param0, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean invokeJZ(long param0, int param1, long __functionAddress) {
        return jni.invokeJZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePZ(int param0, long param1, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePZ(long param0, float param1, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePZ(long param0, int param1, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePZ(long param0, boolean param1, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokeJZ(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokeJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(int param0, int param1, long param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(int param0, long param1, float param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(int param0, long param1, int param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(int param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, float param1, float param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, float param1, int param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, int param1, int param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, int param1, boolean param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, boolean param1, int param2, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePZ(long param0, float param1, float param2, float param3, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePZ(long param0, int param1, int param2, int param3, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePZ(long param0, float param1, float param2, float param3, float param4, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePZ(int param0, int param1, int param2, float param3, boolean param4, long param5, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePZ(long param0, int param1, int param2, float param3, float param4, float param5, float param6, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePZ(long param0, int param1, int param2, boolean param3, float param4, float param5, float param6, long __functionAddress) {
        return jni.invokePZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePBZ(long param0, byte param1, long __functionAddress) {
        return jni.invokePBZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePCZ(long param0, short param1, long __functionAddress) {
        return jni.invokePCZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePJZ(long param0, long param1, long __functionAddress) {
        return jni.invokePJZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePPZ(long param0, long param1, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePSZ(long param0, short param1, long __functionAddress) {
        return jni.invokePSZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokePUZ(long param0, byte param1, long __functionAddress) {
        return jni.invokePUZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokeUPZ(byte param0, long param1, long __functionAddress) {
        return jni.invokeUPZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean invokeJPZ(long param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokeJPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePJZ(int param0, long param1, long param2, long __functionAddress) {
        return jni.invokePJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPZ(int param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPZ(long param0, int param1, long param2, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPZ(long param0, long param1, int param2, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPZ(long param0, long param1, boolean param2, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePSZ(long param0, int param1, short param2, long __functionAddress) {
        return jni.invokePSZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePUZ(long param0, int param1, byte param2, long __functionAddress) {
        return jni.invokePUZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPZ(long param0, float param1, float param2, long param3, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPZ(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPZ(long param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPZ(long param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPZ(long param0, boolean param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPZ(long param0, long param1, int param2, int param3, float param4, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPZ(int param0, int param1, int param2, long param3, int param4, int param5, long param6, int param7, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static boolean invokePPZ(int param0, int param1, int param2, long param3, int param4, int param5, long param6, int param7, boolean param8, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static boolean invokePPZ(int param0, int param1, int param2, int param3, int param4, long param5, int param6, int param7, int param8, int param9, long param10, int param11, long __functionAddress) {
        return jni.invokePPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static boolean invokePCCZ(long param0, short param1, short param2, long __functionAddress) {
        return jni.invokePCCZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePPPZ(long param0, long param1, long param2, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean invokePCCZ(long param0, short param1, short param2, int param3, long __functionAddress) {
        return jni.invokePCCZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePJJZ(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.invokePJJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePJPZ(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePJPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPZ(int param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPZ(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPZ(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPZ(long param0, long param1, long param2, boolean param3, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPZ(long param0, boolean param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePSSZ(long param0, int param1, short param2, short param3, long __functionAddress) {
        return jni.invokePSSZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokeCCJZ(short param0, boolean param1, short param2, int param3, long param4, long __functionAddress) {
        return jni.invokeCCJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePJPZ(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePJPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPZ(long param0, float param1, float param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPZ(long param0, long param1, long param2, boolean param3, int param4, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPZ(long param0, long param1, int param2, long param3, int param4, boolean param5, long __functionAddress) {
        return jni.invokePPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPJZ(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePUUUZ(long param0, byte param1, byte param2, byte param3, long __functionAddress) {
        return jni.invokePUUUZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean invokePPPPZ(int param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPZ(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPZ(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, float param3, long param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPZ(long param0, boolean param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePJPPZ(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePJPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPZ(long param0, long param1, float param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, float param3, float param4, float param5, float param6, float param7, long param8, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static boolean invokePPPPZ(long param0, long param1, int param2, int param3, int param4, int param5, float param6, int param7, long param8, long param9, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static boolean invokePPPPZ(long param0, long param1, long param2, float param3, float param4, float param5, float param6, float param7, long param8, float param9, long __functionAddress) {
        return jni.invokePPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static boolean invokePPPPPZ(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPUPZ(long param0, long param1, long param2, byte param3, long param4, long __functionAddress) {
        return jni.invokePPPUPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePUUUUZ(long param0, byte param1, byte param2, byte param3, byte param4, long __functionAddress) {
        return jni.invokePUUUUZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static boolean invokePPPPPZ(int param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPPZ(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPPZ(long param0, long param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPPZ(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePPPPPZ(long param0, long param1, long param2, long param3, double param4, long param5, int param6, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePPPPPZ(long param0, long param1, long param2, long param3, long param4, boolean param5, int param6, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePUUUUZ(long param0, int param1, int param2, byte param3, byte param4, byte param5, byte param6, long __functionAddress) {
        return jni.invokePUUUUZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static boolean invokePPPPPZ(long param0, long param1, long param2, int param3, long param4, int param5, long param6, int param7, long __functionAddress) {
        return jni.invokePPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static boolean invokePPJJPPZ(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPJJPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPPPZ(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.invokePPPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static boolean invokePPPPPPZ(long param0, long param1, long param2, int param3, long param4, int param5, long param6, int param7, int param8, long param9, int param10, int param11, long __functionAddress) {
        return jni.invokePPPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static short callC(int param0, long __functionAddress) {
        return jni.callC(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static float callF(int param0, int param1, int param2, long __functionAddress) {
        return jni.callF(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static float callPF(long param0, float param1, long __functionAddress) {
        return jni.callPF(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callI(long __functionAddress) {
        return jni.callI(MemorySegment.ofAddress(__functionAddress));
    }

    public static int callI(int param0, long __functionAddress) {
        return jni.callI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int callI(int param0, int param1, long __functionAddress) {
        return jni.callI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callI(int param0, int param1, int param2, long __functionAddress) {
        return jni.callI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(long param0, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static int callPI(int param0, long param1, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callPI(long param0, float param1, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callPI(long param0, int param1, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callPI(int param0, int param1, long param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(int param0, long param1, int param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(long param0, float param1, float param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(long param0, float param1, int param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(long param0, int param1, float param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPI(long param0, int param1, int param2, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callJI(int param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.callJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(int param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(long param0, float param1, float param2, float param3, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(long param0, int param1, float param2, float param3, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(long param0, int param1, int param2, int param3, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(long param0, int param1, int param2, boolean param3, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPI(int param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPI(long param0, int param1, float param2, float param3, int param4, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPI(long param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPI(int param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPI(int param0, long param1, int param2, int param3, float param4, int param5, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPI(long param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPI(long param0, int param1, int param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPI(int param0, int param1, long param2, int param3, int param4, int param5, int param6, float param7, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPI(long param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8, long __functionAddress) {
        return jni.callPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPJI(long param0, long param1, long __functionAddress) {
        return jni.callPJI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callPPI(long param0, long param1, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static int callPJI(long param0, int param1, long param2, long __functionAddress) {
        return jni.callPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPJI(long param0, long param1, float param2, long __functionAddress) {
        return jni.callPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPJI(long param0, long param1, int param2, long __functionAddress) {
        return jni.callPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(int param0, long param1, long param2, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(long param0, float param1, long param2, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(long param0, int param1, long param2, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(long param0, long param1, float param2, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(long param0, long param1, int param2, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPI(int param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, int param1, float param2, long param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, long param1, float param2, float param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, long param1, float param2, int param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPI(long param0, int param1, float param2, float param3, long param4, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPI(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPI(long param0, int param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPI(long param0, long param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPI(long param0, long param1, int param2, int param3, boolean param4, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPI(int param0, long param1, int param2, int param3, float param4, long param5, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPI(long param0, int param1, float param2, float param3, float param4, long param5, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPI(long param0, long param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPI(long param0, int param1, float param2, float param3, float param4, int param5, long param6, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPI(long param0, int param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPI(long param0, int param1, float param2, float param3, float param4, float param5, int param6, long param7, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPI(long param0, int param1, int param2, int param3, int param4, int param5, int param6, long param7, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPI(int param0, int param1, long param2, long param3, int param4, int param5, int param6, int param7, float param8, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPI(long param0, int param1, float param2, float param3, float param4, float param5, float param6, int param7, long param8, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPI(long param0, int param1, int param2, float param3, float param4, float param5, float param6, float param7, int param8, long param9, int param10, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static int callPPI(long param0, int param1, int param2, int param3, int param4, int param5, int param6, long param7, int param8, int param9, int param10, int param11, int param12, int param13, int param14, int param15, int param16, long __functionAddress) {
        return jni.callPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16);
    }

    public static int callPJJI(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPJPI(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPJI(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPPPI(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static int callPJJI(long param0, long param1, long param2, float param3, long __functionAddress) {
        return jni.callPJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJJI(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.callPJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJPI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJPI(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.callPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPJI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPJI(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.callPPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPNI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPPNI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPPI(int param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPPI(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPPI(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPPI(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJJI(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.callPJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPJPI(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.callPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPJI(long param0, int param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.callPPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(int param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(long param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPI(long param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPI(long param0, int param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPI(long param0, int param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPI(long param0, long param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPI(long param0, long param1, long param2, int param3, int param4, int param5, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPI(long param0, float param1, float param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPI(long param0, int param1, int param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPI(long param0, int param1, long param2, long param3, int param4, int param5, int param6, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPI(long param0, long param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        return jni.callPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callJPPI(int param0, int param1, int param2, int param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callJJPPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callJJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJJJI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPJJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJJPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPJJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJPPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPJPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPNPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPNPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPPPPI(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static int callPJJJI(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPJJJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPJPPI(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPPI(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPPI(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPPI(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPPI(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPJPPI(long param0, long param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(int param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, int param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, int param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, int param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, long param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, long param1, long param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPI(long param0, long param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPJPPI(long param0, long param1, int param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPI(long param0, int param1, int param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPI(long param0, int param1, long param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPI(long param0, long param1, long param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPI(int param0, long param1, long param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, int param11, int param12, int param13, int param14, int param15, int param16, int param17, int param18, int param19, int param20, long param21, long param22, long __functionAddress) {
        return jni.callPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16, param17, param18, param19, param20, param21, param22);
    }

    public static int callPJJPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPJJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPJPPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPJPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPJPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPPPPPI(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static int callPJJJPI(long param0, long param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPJJJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPJPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPJPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPPI(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPPI(long param0, long param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPPI(long param0, long param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPJPPI(long param0, int param1, long param2, long param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPJPPI(long param0, long param1, int param2, long param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPPI(int param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPPI(long param0, int param1, long param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPPI(long param0, int param1, long param2, long param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callJPPPPI(int param0, int param1, long param2, long param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callJPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPJPPJI(long param0, long param1, int param2, int param3, long param4, long param5, long param6, int param7, long __functionAddress) {
        return jni.callPJPPJI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPI(long param0, int param1, long param2, int param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPJJJJPI(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPJJJJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPJPI(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPJPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static int callPJJPPPI(long param0, long param1, long param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPJJPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPJPPPPI(long param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPJPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPJPPI(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPPPPPPI(long param0, long param1, long param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static int callPJPPPPI(long param0, int param1, long param2, long param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.callPJPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPJPPI(long param0, int param1, long param2, long param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPI(int param0, int param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPI(long param0, int param1, long param2, int param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPI(long param0, int param1, long param2, long param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPPI(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPPI(long param0, long param1, long param2, long param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPPPPPPI(long param0, int param1, long param2, int param3, long param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPPPI(long param0, int param1, long param2, long param3, int param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, int param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPPPI(long param0, long param1, long param2, int param3, long param4, long param5, int param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPJPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPJPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static int callPPJPPPPPI(long param0, long param1, long param2, long param3, long param4, int param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.callPPJPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, int param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static int callPPPPPPPPI(long param0, long param1, long param2, long param3, int param4, long param5, long param6, int param7, long param8, long param9, long __functionAddress) {
        return jni.callPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static int callPPPPPPPPI(long param0, int param1, int param2, long param3, long param4, long param5, long param6, int param7, long param8, long param9, long param10, long __functionAddress) {
        return jni.callPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static int callPPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, int param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.callPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static int callPPPPPPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long param6, long param7, int param8, long param9, long param10, long __functionAddress) {
        return jni.callPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static int callPPPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, int param7, long param8, long param9, long param10, long __functionAddress) {
        return jni.callPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static int callPPPPPPPPPPI(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, int param8, long param9, long param10, long param11, long __functionAddress) {
        return jni.callPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static int callPPPPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, int param8, long param9, long param10, long param11, long __functionAddress) {
        return jni.callPPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static int callPPPPPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, int param10, long param11, long param12, long __functionAddress) {
        return jni.callPPPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }

    public static int callPPPPPPPPPPPPI(long param0, long param1, int param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long param10, int param11, long param12, long param13, long __functionAddress) {
        return jni.callPPPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
    }

    public static int callPPPPPPPPPPPPPPPI(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long param10, long param11, int param12, long param13, long param14, long param15, long __functionAddress) {
        return jni.callPPPPPPPPPPPPPPPI(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15);
    }

    public static long callJ(long __functionAddress) {
        return jni.callJ(MemorySegment.ofAddress(__functionAddress));
    }

    public static long callJ(int param0, long __functionAddress) {
        return jni.callJ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long callJ(int param0, int param1, long __functionAddress) {
        return jni.callJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callJ(int param0, int param1, boolean param2, int param3, int param4, long __functionAddress) {
        return jni.callJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPJ(long param0, int param1, long __functionAddress) {
        return jni.callPJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPPJ(long param0, long param1, long __functionAddress) {
        return jni.callPPJ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPJJ(long param0, long param1, int param2, int param3, long __functionAddress) {
        return jni.callPJJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPJJJ(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPJJJ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPN(long param0, long __functionAddress) {
        return jni.callPN(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long callP(long __functionAddress) {
        return jni.callP(MemorySegment.ofAddress(__functionAddress));
    }

    public static long callP(int param0, long __functionAddress) {
        return jni.callP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long callP(int param0, int param1, long __functionAddress) {
        return jni.callP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callP(int param0, float param1, float param2, float param3, long __functionAddress) {
        return jni.callP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callJP(long param0, long __functionAddress) {
        return jni.callJP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long callPP(long param0, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static long callPP(int param0, long param1, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPP(long param0, int param1, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPP(int param0, int param1, long param2, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPP(int param0, long param1, int param2, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPP(long param0, int param1, int param2, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPP(int param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.callPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callJJP(long param0, long param1, long __functionAddress) {
        return jni.callJJP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPNP(long param0, long param1, long __functionAddress) {
        return jni.callPNP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPPP(long param0, long param1, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static long callPPP(int param0, long param1, long param2, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPPP(long param0, int param1, long param2, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPPP(long param0, long param1, int param2, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPPP(int param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPP(long param0, int param1, int param2, long param3, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPP(int param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPP(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPP(long param0, long param1, int param2, int param3, int param4, long __functionAddress) {
        return jni.callPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPNP(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPPNP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPPPP(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static long callPJPP(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.callPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPJPP(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.callPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPP(int param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPP(long param0, int param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPP(long param0, long param1, int param2, long param3, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPP(long param0, long param1, long param2, int param3, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPP(long param0, long param1, int param2, int param3, long param4, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPP(long param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        return jni.callPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPJPP(long param0, long param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        return jni.callPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callJJPPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callJJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPJPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPNPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPNPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPPPPP(long param0, long param1, long param2, long param3, long __functionAddress) {
        return jni.callPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static long callPJPPP(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPJPPP(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.callPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPPP(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPPP(long param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPPP(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.callPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPPP(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        return jni.callPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPJPPP(long param0, long param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        return jni.callPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPJPPPP(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPJPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPJPP(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPPPJPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static long callPPPPPP(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPPPPPP(long param0, long param1, long param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPPPPPP(long param0, int param1, int param2, long param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long callPJJPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPJJPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPJPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPJPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPPJPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPJPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPPPJPPP(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPPPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static long callPPPPPPP(long param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long callPPJPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPJPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long callPPPPJPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        return jni.callPPPPJPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static long callPPPPPPPP(long param0, int param1, long param2, long param3, int param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.callPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static long callPPPPPPPP(int param0, long param1, long param2, int param3, int param4, int param5, int param6, long param7, long param8, long param9, int param10, long param11, long param12, long __functionAddress) {
        return jni.callPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }

    public static long callPJPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPJPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static long callPPJPPPPPP(long param0, long param1, int param2, long param3, long param4, long param5, int param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.callPPJPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long callPJPPPPPPPPP(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.callPJPPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static long callPPJPPPPPPPP(long param0, long param1, int param2, long param3, long param4, long param5, long param6, long param7, int param8, long param9, long param10, long param11, long __functionAddress) {
        return jni.callPPJPPPPPPPP(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static short callS(int param0, long __functionAddress) {
        return jni.callS(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static short callPS(long param0, long __functionAddress) {
        return jni.callPS(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static short callPCS(long param0, short param1, long __functionAddress) {
        return jni.callPCS(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short callPPS(long param0, long param1, long __functionAddress) {
        return jni.callPPS(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short callPSS(long param0, short param1, long __functionAddress) {
        return jni.callPSS(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short callSPS(short param0, long param1, long __functionAddress) {
        return jni.callSPS(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static short callPPS(long param0, long param1, int param2, long __functionAddress) {
        return jni.callPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPPS(long param0, int param1, long param2, int param3, long __functionAddress) {
        return jni.callPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short callPCPS(long param0, short param1, long param2, long __functionAddress) {
        return jni.callPCPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPPCS(long param0, long param1, short param2, long __functionAddress) {
        return jni.callPPCS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPPPS(long param0, long param1, long param2, long __functionAddress) {
        return jni.callPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPPSS(long param0, long param1, short param2, long __functionAddress) {
        return jni.callPPSS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPSPS(long param0, short param1, long param2, long __functionAddress) {
        return jni.callPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callSPPS(short param0, long param1, long param2, long __functionAddress) {
        return jni.callSPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callSPSS(short param0, long param1, short param2, long __functionAddress) {
        return jni.callSPSS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static short callPPPS(long param0, int param1, long param2, int param3, long param4, long __functionAddress) {
        return jni.callPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short callPJCCS(long param0, long param1, short param2, short param3, long __functionAddress) {
        return jni.callPJCCS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short callPPSPS(long param0, long param1, short param2, long param3, long __functionAddress) {
        return jni.callPPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static short callPSSPS(long param0, short param1, short param2, long param3, int param4, long __functionAddress) {
        return jni.callPSSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short callPPPPS(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short callPCPPPS(long param0, short param1, long param2, long param3, long param4, long __functionAddress) {
        return jni.callPCPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short callPCPSPS(long param0, short param1, long param2, short param3, long param4, long __functionAddress) {
        return jni.callPCPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static short callPSSPPS(long param0, short param1, short param2, long param3, int param4, long param5, long __functionAddress) {
        return jni.callPSSPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short callPCPPPPS(long param0, short param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPCPPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short callPCSPPPS(long param0, short param1, short param2, long param3, long param4, long param5, long __functionAddress) {
        return jni.callPCSPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short callPPSPSPS(long param0, long param1, short param2, long param3, short param4, long param5, long __functionAddress) {
        return jni.callPPSPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static short callPCCPSPPS(long param0, short param1, short param2, long param3, short param4, long param5, long param6, long __functionAddress) {
        return jni.callPCCPSPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static short callPPSPSPSS(long param0, long param1, short param2, long param3, short param4, long param5, short param6, long __functionAddress) {
        return jni.callPPSPSPSS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static short callSPSSPSPS(short param0, long param1, short param2, short param3, long param4, short param5, long param6, long __functionAddress) {
        return jni.callSPSSPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static short callPCPSPPSPS(long param0, short param1, long param2, short param3, long param4, long param5, short param6, long param7, long __functionAddress) {
        return jni.callPCPSPPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static short callPPPSPSPCS(long param0, long param1, long param2, short param3, long param4, short param5, long param6, short param7, long __functionAddress) {
        return jni.callPPPSPSPCS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static short callSPSPPPSPS(short param0, long param1, short param2, long param3, long param4, long param5, short param6, long param7, long __functionAddress) {
        return jni.callSPSPPPSPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static short callPCPSPPPPPS(long param0, short param1, long param2, short param3, long param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        return jni.callPCPSPPPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static short callPPSPSPSCCS(long param0, long param1, short param2, long param3, short param4, long param5, short param6, short param7, short param8, long __functionAddress) {
        return jni.callPPSPSPSCCS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static short callPPSPSPSPSS(long param0, long param1, short param2, long param3, short param4, long param5, short param6, long param7, short param8, long __functionAddress) {
        return jni.callPPSPSPSPSS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static short callPCPSPSPSCCS(long param0, short param1, long param2, short param3, long param4, short param5, long param6, short param7, short param8, short param9, long __functionAddress) {
        return jni.callPCPSPSPSCCS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static short callPCSSSPSPPPS(long param0, short param1, short param2, short param3, short param4, long param5, short param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.callPCSSSPSPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static short callPSSSPSSPPPS(long param0, short param1, short param2, short param3, long param4, short param5, short param6, long param7, long param8, long param9, long __functionAddress) {
        return jni.callPSSSPSSPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static short callPSPSPPPPPPPS(long param0, short param1, long param2, short param3, long param4, long param5, long param6, long param7, long param8, long param9, long param10, long __functionAddress) {
        return jni.callPSPSPPPPPPPS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static short callPPSPSPSPSPSPSS(long param0, long param1, short param2, long param3, short param4, long param5, short param6, long param7, short param8, long param9, short param10, long param11, short param12, long __functionAddress) {
        return jni.callPPSPSPSPSPSPSS(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12);
    }

    public static void callV(long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress));
    }

    public static void callV(double param0, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callV(float param0, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callV(int param0, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callV(boolean param0, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callV(double param0, double param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(float param0, float param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(float param0, boolean param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(int param0, double param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(int param0, float param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(int param0, int param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(int param0, boolean param1, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callV(double param0, double param1, double param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(float param0, float param1, float param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, double param1, double param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, float param1, float param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, int param1, double param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, int param1, float param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, int param1, int param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(int param0, int param1, boolean param2, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callV(double param0, double param1, double param2, double param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(float param0, float param1, float param2, float param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, double param1, double param2, double param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, float param1, float param2, float param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, double param2, double param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, float param2, float param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, float param2, int param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, int param2, double param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, int param2, float param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, int param2, int param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, int param2, boolean param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, int param1, boolean param2, int param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(boolean param0, boolean param1, boolean param2, boolean param3, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callV(int param0, double param1, double param2, double param3, double param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, float param1, float param2, float param3, float param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, int param1, double param2, double param3, double param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, int param1, float param2, float param3, float param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, int param1, int param2, float param3, int param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, int param1, int param2, boolean param3, int param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(int param0, boolean param1, boolean param2, boolean param3, boolean param4, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callV(double param0, double param1, double param2, double param3, double param4, double param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, double param1, double param2, int param3, double param4, double param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, float param1, float param2, int param3, float param4, float param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, int param1, double param2, double param3, double param4, double param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, int param1, float param2, float param3, float param4, float param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, boolean param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, int param1, int param2, int param3, boolean param4, int param5, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callV(int param0, double param1, double param2, double param3, double param4, double param5, double param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(int param0, int param1, int param2, double param3, double param4, double param5, double param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(int param0, int param1, int param2, float param3, float param4, float param5, float param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, boolean param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(int param0, int param1, int param2, boolean param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callV(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callV(int param0, int param1, int param2, float param3, float param4, float param5, float param6, float param7, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callV(int param0, int param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8, float param9, float param10, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, int param11, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, int param11, int param12, int param13, int param14, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14);
    }

    public static void callV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, int param11, int param12, int param13, int param14, int param15, int param16, long __functionAddress) {
        jni.callV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16);
    }

    public static void callJV(long param0, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callPV(long param0, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callSV(short param0, long __functionAddress) {
        jni.callSV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callUV(byte param0, long __functionAddress) {
        jni.callUV(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static void callCV(int param0, short param1, long __functionAddress) {
        jni.callCV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callJV(int param0, long param1, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callJV(long param0, int param1, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callPV(int param0, long param1, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callPV(long param0, float param1, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callPV(long param0, int param1, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callSV(int param0, short param1, long __functionAddress) {
        jni.callSV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callJV(int param0, int param1, long param2, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPV(int param0, int param1, long param2, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPV(int param0, long param1, int param2, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPV(long param0, float param1, float param2, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPV(long param0, int param1, int param2, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callJV(int param0, long param1, int param2, int param3, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callNV(long param0, int param1, int param2, int param3, long __functionAddress) {
        jni.callNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(int param0, int param1, int param2, long param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(int param0, int param1, long param2, int param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(int param0, int param1, boolean param2, long param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(int param0, long param1, int param2, int param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(long param0, float param1, float param2, float param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(long param0, int param1, int param2, int param3, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPV(int param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, int param1, int param2, long param3, int param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, int param1, int param2, long param3, boolean param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, int param1, int param2, boolean param3, long param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, int param1, long param2, int param3, int param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, long param1, int param2, int param3, int param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(int param0, boolean param1, int param2, int param3, long param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPV(long param0, int param1, int param2, int param3, int param4, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, double param1, double param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, float param1, float param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, int param1, int param2, int param3, long param4, boolean param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, int param1, int param2, long param3, int param4, int param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, int param1, int param2, boolean param3, int param4, long param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, int param1, long param2, int param3, int param4, int param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(int param0, boolean param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPV(long param0, int param1, int param2, int param3, int param4, int param5, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(int param0, int param1, float param2, float param3, float param4, float param5, long param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, long param5, int param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(int param0, int param1, int param2, long param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(int param0, int param1, long param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPV(long param0, int param1, int param2, int param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, long param7, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, boolean param5, int param6, long param7, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, long param7, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, boolean param5, int param6, long param7, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, int param5, boolean param6, int param7, long param8, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPV(int param0, int param1, long param2, int param3, int param4, int param5, int param6, int param7, float param8, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPV(int param0, double param1, double param2, int param3, int param4, double param5, double param6, int param7, int param8, long param9, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callPV(int param0, float param1, float param2, int param3, int param4, float param5, float param6, int param7, int param8, long param9, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, long param9, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callJV(long param0, int param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8, float param9, float param10, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, long param10, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callPV(long param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callJV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, long param10, boolean param11, long __functionAddress) {
        jni.callJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static void callPV(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10, long param11, long __functionAddress) {
        jni.callPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11);
    }

    public static void callPJV(long param0, long param1, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callPPV(long param0, long param1, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callSSV(short param0, short param1, long __functionAddress) {
        jni.callSSV(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static void callJJV(int param0, long param1, long param2, long __functionAddress) {
        jni.callJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPCV(long param0, int param1, short param2, long __functionAddress) {
        jni.callPCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPJV(long param0, int param1, long param2, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPJV(long param0, long param1, float param2, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPJV(long param0, long param1, int param2, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPPV(int param0, long param1, long param2, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPPV(long param0, int param1, long param2, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPPV(long param0, long param1, int param2, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callSSV(int param0, short param1, short param2, long __functionAddress) {
        jni.callSSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callJJV(int param0, int param1, long param2, long param3, long __functionAddress) {
        jni.callJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callJPV(int param0, int param1, long param2, long param3, long __functionAddress) {
        jni.callJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callJPV(int param0, long param1, int param2, long param3, long __functionAddress) {
        jni.callJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJV(int param0, long param1, int param2, long param3, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJV(long param0, int param1, long param2, int param3, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJV(long param0, long param1, int param2, int param3, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(int param0, int param1, long param2, long param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(int param0, long param1, int param2, long param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(int param0, long param1, long param2, int param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(int param0, long param1, long param2, boolean param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(long param0, int param1, int param2, long param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPV(long param0, long param1, int param2, int param3, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJV(long param0, long param1, int param2, int param3, int param4, long __functionAddress) {
        jni.callPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, long param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, long param1, int param2, long param3, int param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(long param0, int param1, int param2, int param3, long param4, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPV(int param0, int param1, int param2, int param3, long param4, long param5, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPV(int param0, int param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPV(int param0, long param1, long param2, int param3, int param4, int param5, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPV(long param0, int param1, long param2, int param3, int param4, int param5, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPV(int param0, int param1, int param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPV(int param0, int param1, int param2, long param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPV(int param0, int param1, long param2, long param3, int param4, int param5, int param6, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPV(int param0, int param1, int param2, int param3, int param4, int param5, long param6, long param7, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPV(int param0, int param1, int param2, int param3, long param4, int param5, int param6, long param7, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, int param4, int param5, int param6, long param7, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPV(int param0, int param1, int param2, long param3, int param4, float param5, float param6, int param7, long param8, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, int param4, int param5, int param6, int param7, long param8, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPPV(int param0, int param1, long param2, int param3, int param4, int param5, long param6, int param7, int param8, float param9, long __functionAddress) {
        jni.callPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callBBBV(byte param0, byte param1, byte param2, long __functionAddress) {
        jni.callBBBV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callCCCV(short param0, short param1, short param2, long __functionAddress) {
        jni.callCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPJJV(long param0, long param1, long param2, long __functionAddress) {
        jni.callPJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPJPV(long param0, long param1, long param2, long __functionAddress) {
        jni.callPJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPPNV(long param0, long param1, long param2, long __functionAddress) {
        jni.callPPNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callPPPV(long param0, long param1, long param2, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callSSSV(short param0, short param1, short param2, long __functionAddress) {
        jni.callSSSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callUUUV(byte param0, byte param1, byte param2, long __functionAddress) {
        jni.callUUUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static void callJJJV(int param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJJV(long param0, long param1, long param2, int param3, long __functionAddress) {
        jni.callPJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJPV(long param0, long param1, int param2, long param3, long __functionAddress) {
        jni.callPJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPV(int param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPV(long param0, int param1, long param2, long param3, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPV(long param0, long param1, int param2, long param3, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPV(long param0, long param1, long param2, int param3, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callSSSV(int param0, short param1, short param2, short param3, long __functionAddress) {
        jni.callSSSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callJJJV(int param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJJV(long param0, int param1, long param2, long param3, int param4, long __functionAddress) {
        jni.callPJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJJV(long param0, long param1, long param2, int param3, int param4, long __functionAddress) {
        jni.callPJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(int param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(int param0, long param1, int param2, long param3, long param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(int param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(int param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(long param0, int param1, int param2, long param3, long param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPV(long param0, int param1, long param2, int param3, long param4, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJPV(long param0, int param1, long param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJPV(long param0, long param1, int param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPJV(int param0, long param1, long param2, int param3, long param4, boolean param5, long __functionAddress) {
        jni.callPPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPJV(long param0, int param1, long param2, int param3, long param4, int param5, long __functionAddress) {
        jni.callPPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPV(int param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPV(int param0, int param1, long param2, int param3, long param4, long param5, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPV(int param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJV(long param0, int param1, int param2, long param3, long param4, int param5, int param6, long __functionAddress) {
        jni.callPJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPV(int param0, int param1, int param2, int param3, long param4, long param5, long param6, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPV(int param0, int param1, long param2, long param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPV(long param0, int param1, long param2, int param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPV(int param0, int param1, int param2, int param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPV(long param0, int param1, int param2, int param3, int param4, int param5, long param6, long param7, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPV(long param0, long param1, int param2, int param3, int param4, int param5, int param6, int param7, long param8, int param9, int param10, int param11, int param12, int param13, int param14, int param15, int param16, int param17, long __functionAddress) {
        jni.callPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14, param15, param16, param17);
    }

    public static void callBBBBV(byte param0, byte param1, byte param2, byte param3, long __functionAddress) {
        jni.callBBBBV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callCCCCV(short param0, short param1, short param2, short param3, long __functionAddress) {
        jni.callCCCCV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJJJV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJJPV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPJPPV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPJPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPNV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPPPNV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callPPPPV(long param0, long param1, long param2, long param3, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callSSSSV(short param0, short param1, short param2, short param3, long __functionAddress) {
        jni.callSSSSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callUUUUV(byte param0, byte param1, byte param2, byte param3, long __functionAddress) {
        jni.callUUUUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static void callJJJJV(int param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJJJV(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        jni.callPJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJJPV(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPJJPV(long param0, long param1, long param2, int param3, long param4, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPPV(long param0, int param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPPV(long param0, long param1, long param2, long param3, int param4, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callSSSSV(int param0, short param1, short param2, short param3, short param4, long __functionAddress) {
        jni.callSSSSV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callUUUUV(int param0, byte param1, byte param2, byte param3, byte param4, long __functionAddress) {
        jni.callUUUUV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callJJJJV(int param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJJV(long param0, long param1, long param2, long param3, int param4, int param5, long __functionAddress) {
        jni.callPJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJPV(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJPV(long param0, long param1, long param2, int param3, int param4, long param5, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJPPV(long param0, long param1, int param2, long param3, int param4, long param5, long __functionAddress) {
        jni.callPJPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPPV(int param0, long param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPPV(int param0, long param1, long param2, long param3, long param4, int param5, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPPV(long param0, int param1, int param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJPV(long param0, long param1, int param2, int param3, long param4, int param5, long param6, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPJJPV(long param0, long param1, int param2, long param3, int param4, int param5, long param6, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPJPPV(long param0, int param1, long param2, int param3, int param4, long param5, long param6, long __functionAddress) {
        jni.callPJPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPPV(int param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPPV(int param0, int param1, long param2, long param3, long param4, long param5, int param6, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPJJPV(long param0, long param1, int param2, long param3, int param4, int param5, long param6, int param7, long __functionAddress) {
        jni.callPJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPJPPV(long param0, int param1, long param2, int param3, int param4, long param5, int param6, long param7, long __functionAddress) {
        jni.callPJPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPPV(long param0, int param1, int param2, int param3, int param4, long param5, int param6, long param7, int param8, long param9, long __functionAddress) {
        jni.callPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public static void callPJJJPV(long param0, long param1, long param2, long param3, long param4, long __functionAddress) {
        jni.callPJJJPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4);
    }

    public static void callPPPPPV(long param0, int param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPJJJJV(long param0, long param1, long param2, long param3, long param4, int param5, int param6, long __functionAddress) {
        jni.callPJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPPPV(int param0, int param1, long param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.callPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPPPPPV(long param0, int param1, int param2, long param3, long param4, long param5, long param6, long __functionAddress) {
        jni.callPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6);
    }

    public static void callPJJJJV(long param0, long param1, int param2, int param3, long param4, long param5, long param6, int param7, long __functionAddress) {
        jni.callPJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPJPPPV(long param0, int param1, int param2, long param3, long param4, int param5, long param6, long param7, long __functionAddress) {
        jni.callPJPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPPPV(long param0, long param1, long param2, long param3, long param4, int param5, int param6, int param7, long __functionAddress) {
        jni.callPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPPPV(long param0, int param1, long param2, int param3, int param4, int param5, long param6, int param7, long param8, int param9, long param10, long __functionAddress) {
        jni.callPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callPPPPPJV(long param0, long param1, long param2, long param3, long param4, long param5, long __functionAddress) {
        jni.callPPPPPJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5);
    }

    public static void callPPPPPPV(long param0, long param1, long param2, int param3, int param4, long param5, long param6, long param7, long __functionAddress) {
        jni.callPPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static void callPPPPPPPV(int param0, int param1, int param2, long param3, int param4, long param5, long param6, long param7, long param8, long param9, long param10, long __functionAddress) {
        jni.callPPPPPPPV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public static void callPPJJJJJJV(long param0, long param1, long param2, long param3, int param4, long param5, long param6, long param7, long param8, long __functionAddress) {
        jni.callPPJJJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    public static void callPJJJJJJJJJJJV(long param0, long param1, long param2, long param3, long param4, long param5, long param6, long param7, long param8, long param9, long param10, long param11, int param12, int param13, int param14, long __functionAddress) {
        jni.callPJJJJJJJJJJJV(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13, param14);
    }

    public static boolean callZ(int param0, long __functionAddress) {
        return jni.callZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean callZ(int param0, int param1, long __functionAddress) {
        return jni.callZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean callZ(int param0, float param1, float param2, long __functionAddress) {
        return jni.callZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean callZ(int param0, int param1, float param2, float param3, long __functionAddress) {
        return jni.callZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3);
    }

    public static boolean callJZ(long param0, long __functionAddress) {
        return jni.callJZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean callPZ(long param0, long __functionAddress) {
        return jni.callPZ(MemorySegment.ofAddress(__functionAddress), param0);
    }

    public static boolean callJZ(int param0, long param1, long __functionAddress) {
        return jni.callJZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean callPZ(int param0, long param1, long __functionAddress) {
        return jni.callPZ(MemorySegment.ofAddress(__functionAddress), param0, param1);
    }

    public static boolean callJZ(int param0, long param1, int param2, long __functionAddress) {
        return jni.callJZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean callPPZ(int param0, long param1, long param2, long __functionAddress) {
        return jni.callPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2);
    }

    public static boolean callPPPPZ(int param0, int param1, int param2, float param3, long param4, long param5, long param6, long param7, long __functionAddress) {
        return jni.callPPPPZ(MemorySegment.ofAddress(__functionAddress), param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public static native short invokeUPC(byte var0, short @Nullable [] var1, boolean var2, long var3);

    public static native short invokeCPCC(short var0, short @Nullable [] var1, short var2, long var3);

    public static native int invokeCPI(short var0, int @Nullable [] var1, long var2);

    public static native int invokePCI(float @Nullable [] var0, short var1, long var2);

    public static native int invokePPI(int var0, long var1, int @Nullable [] var3, long var4);

    public static native int invokePPI(int var0, int @Nullable [] var1, int var2, int @Nullable [] var3, int var4, boolean var5, long var6);

    public static native int invokePPI(int var0, short @Nullable [] var1, int var2, short @Nullable [] var3, int var4, boolean var5, long var6);

    public static native int invokeCPUI(short var0, float @Nullable [] var1, byte var2, long var3);

    public static native int invokeCPUI(short var0, int @Nullable [] var1, byte var2, long var3);

    public static native int invokeCPUI(short var0, short @Nullable [] var1, byte var2, long var3);

    public static native int invokePPCI(long var0, float @Nullable [] var2, short var3, long var4);

    public static native int invokePPPI(int @Nullable [] var0, long var1, long var3, int var5, boolean var6, float var7, long var8);

    public static native int invokePPPI(short @Nullable [] var0, long var1, long var3, int var5, boolean var6, float var7, long var8);

    public static native int invokePPPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int invokePPPPI(long var0, long var2, int var4, int var5, float @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int invokePPPPI(long var0, long var2, int var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int invokePPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5, long var7);

    public static native int invokePNNPPPI(long var0, long var2, long var4, int var6, int var7, int @Nullable [] var8, int @Nullable [] var9, long var10, long var12);

    public static native int invokePPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native int invokePPPPPPPI(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int @Nullable [] var7, int @Nullable [] var8, long var9, long var11);

    public static native int invokePPPPPPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, int @Nullable [] var7, float @Nullable [] var8, int @Nullable [] var9, int @Nullable [] var10, int @Nullable [] var11, long var12);

    public static native long invokePP(double @Nullable [] var0, int var1, long var2);

    public static native long invokePP(float @Nullable [] var0, int var1, long var2);

    public static native long invokePP(int @Nullable [] var0, int var1, long var2);

    public static native long invokePP(long @Nullable [] var0, int var1, long var2);

    public static native long invokePP(short @Nullable [] var0, int var1, long var2);

    public static native long invokePPP(long var0, int @Nullable [] var2, long var3);

    public static native byte invokeUPU(byte var0, int @Nullable [] var1, long var2);

    public static native void invokePV(int var0, double @Nullable [] var1, long var2);

    public static native void invokePV(int var0, float @Nullable [] var1, long var2);

    public static native void invokePV(int var0, int @Nullable [] var1, long var2);

    public static native void invokePV(int var0, int var1, double @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, float @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, int @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int var1, long @Nullable [] var2, long var3);

    public static native void invokePV(int var0, int @Nullable [] var1, boolean var2, long var3);

    public static native void invokePV(int var0, int var1, float @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, int @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, short @Nullable [] var2, int var3, int var4, long var5);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, double @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, short @Nullable [] var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int @Nullable [] var4, boolean var5, long var6);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, double @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, int @Nullable [] var6, long var7);

    public static native void invokePV(int var0, int var1, int var2, int var3, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void invokeUPV(byte var0, float @Nullable [] var1, long var2);

    public static native void invokePJV(int var0, int @Nullable [] var1, long var2, long var4);

    public static native void invokePPV(long var0, int var2, double @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, int var2, float @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, int var2, int @Nullable [] var3, long var4);

    public static native void invokePPV(long var0, float @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, int @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, short @Nullable [] var2, int var3, long var4);

    public static native void invokePPV(long var0, int var2, int var3, double @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, float @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int var3, long @Nullable [] var4, long var5);

    public static native void invokePPV(long var0, int var2, int @Nullable [] var3, boolean var4, long var5);

    public static native void invokePPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void invokePPV(long var0, int var2, int var3, float @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, int @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, short @Nullable [] var4, int var5, int var6, long var7);

    public static native void invokePPV(long var0, int var2, int var3, int var4, int var5, int @Nullable [] var6, boolean var7, long var8);

    public static native void invokeCCPV(short var0, short var1, short @Nullable [] var2, long var3);

    public static native void invokeCPCV(short var0, double @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, float @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, int @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, long @Nullable [] var1, short var2, long var3);

    public static native void invokeCPCV(short var0, short @Nullable [] var1, short var2, long var3);

    public static native void invokeCPPV(short var0, float @Nullable [] var1, float @Nullable [] var2, long var3);

    public static native void invokePNPV(long var0, long var2, short @Nullable [] var4, long var5);

    public static native void invokePPPV(long var0, double @Nullable [] var2, double @Nullable [] var3, long var4);

    public static native void invokePPPV(long var0, float @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void invokePPPV(long var0, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void invokePPPV(int @Nullable [] var0, int @Nullable [] var1, int @Nullable [] var2, long var3);

    public static native void invokePPJV(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void invokePPPV(int var0, float @Nullable [] var1, float @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void invokePPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void invokePPPV(int var0, int var1, double @Nullable [] var2, double @Nullable [] var3, double @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, float @Nullable [] var2, float @Nullable [] var3, float @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native void invokePPPV(int var0, int var1, long @Nullable [] var2, long @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native void invokePPPV(float @Nullable [] var0, int var1, long var2, long var4, int var6, long var7);

    public static native void invokePPPV(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6, long var8);

    public static native void invokePPPV(float @Nullable [] var0, boolean var1, int var2, long var3, long var5, int var7, long var8);

    public static native void invokeCCPCV(short var0, short var1, double @Nullable [] var2, short var3, long var4);

    public static native void invokeCCPCV(short var0, short var1, float @Nullable [] var2, short var3, long var4);

    public static native void invokeCCPCV(short var0, short var1, int @Nullable [] var2, short var3, long var4);

    public static native void invokeCCPCV(short var0, short var1, long @Nullable [] var2, short var3, long var4);

    public static native void invokeCCPCV(short var0, short var1, short @Nullable [] var2, short var3, long var4);

    public static native void invokePCPCV(long var0, short var2, double @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, float @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, int @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, long @Nullable [] var3, short var4, long var5);

    public static native void invokePCPCV(long var0, short var2, short @Nullable [] var3, short var4, long var5);

    public static native void invokePNPPV(long var0, long var2, long var4, short @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, float @Nullable [] var3, float @Nullable [] var4, float @Nullable [] var5, long var6);

    public static native void invokePPPPV(long var0, int var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void invokePPPPV(long var0, int var2, int var3, double @Nullable [] var4, double @Nullable [] var5, double @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, float @Nullable [] var4, float @Nullable [] var5, float @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native void invokePPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native void invokePPPPPV(long var0, long var2, long var4, float @Nullable [] var6, long var7, long var9);

    public static native void invokePPPPPV(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void invokePPPPPV(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8, long var10);

    public static native void invokePPPPPV(int var0, long var1, int var3, float @Nullable [] var4, float @Nullable [] var5, long var6, int var8, long var9, int var11, boolean var12, long var13);

    public static native void invokePPPPPV(int var0, int @Nullable [] var1, int var2, float @Nullable [] var3, float @Nullable [] var4, long var5, int var7, int @Nullable [] var8, int var9, boolean var10, long var11);

    public static native void invokePPPPPV(int var0, short @Nullable [] var1, int var2, float @Nullable [] var3, float @Nullable [] var4, long var5, int var7, short @Nullable [] var8, int var9, boolean var10, long var11);

    public static native boolean invokePZ(int var0, int @Nullable [] var1, boolean var2, long var3);

    public static native boolean invokePPZ(long var0, int @Nullable [] var2, long var3);

    public static native boolean invokePPPZ(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPI(int @Nullable [] var0, long var1);

    public static native int callPI(int var0, int @Nullable [] var1, long var2);

    public static native int callPI(int @Nullable [] var0, int var1, long var2);

    public static native int callPI(int var0, int var1, int @Nullable [] var2, long var3);

    public static native int callPI(int var0, int @Nullable [] var1, int var2, long var3);

    public static native int callPI(int var0, int var1, int var2, int @Nullable [] var3, long var4);

    public static native int callPI(int var0, int var1, int var2, int var3, float @Nullable [] var4, long var5);

    public static native int callPI(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native int callPPI(long var0, int @Nullable [] var2, long var3);

    public static native int callPPI(long var0, long @Nullable [] var2, long var3);

    public static native int callPPI(int @Nullable [] var0, long var1, long var3);

    public static native int callPPI(int var0, long var1, int @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, double @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, float @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, int @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, long @Nullable [] var3, long var4);

    public static native int callPPI(long var0, int var2, short @Nullable [] var3, long var4);

    public static native int callPPI(long var0, long @Nullable [] var2, int var3, long var4);

    public static native int callPPI(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native int callPPI(long var0, int var2, int var3, long @Nullable [] var4, long var5);

    public static native int callPPI(int var0, long var1, int var3, int var4, float var5, int @Nullable [] var6, long var7);

    public static native int callPJPI(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPJPI(long var0, long var2, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, long var2, int @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, long var2, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, int @Nullable [] var2, long var3, long var5);

    public static native int callPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native int callPPPI(long var0, long @Nullable [] var2, long @Nullable [] var3, long var4);

    public static native int callPPPI(int @Nullable [] var0, long var1, int @Nullable [] var3, long var4);

    public static native int callPJPI(long var0, int var2, long var3, int @Nullable [] var5, long var6);

    public static native int callPJPI(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native int callPPPI(int var0, long var1, int @Nullable [] var3, long var4, long var6);

    public static native int callPPPI(long var0, int var2, long var3, double @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, float @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, int @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, long @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, long var3, short @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native int callPPPI(long var0, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, int var2, int @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native int callPPPI(long var0, int var2, long @Nullable [] var3, long var4, long var6);

    public static native int callPPPI(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native int callPPPI(long var0, long var2, int @Nullable [] var4, int var5, long var6);

    public static native int callPPJI(long var0, int var2, long @Nullable [] var3, int var4, long var5, long var7);

    public static native int callPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, long var7);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int @Nullable [] var5, float @Nullable [] var6, long var7);

    public static native int callPPPI(long var0, int var2, int var3, int var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native int callPJPPI(long var0, long var2, int @Nullable [] var4, long @Nullable [] var5, long var6);

    public static native int callPPNPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long var2, long var4, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native int callPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native int callPJPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long var5, long @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, int var2, long var3, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, int var2, int @Nullable [] var3, long var4, long var6, long var8);

    public static native int callPPPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, long var2, int var4, long @Nullable [] var5, long var6, long var8);

    public static native int callPPPPI(long var0, long var2, long var4, int var6, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int @Nullable [] var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native int callPPPPI(long var0, long @Nullable [] var2, int var3, long var4, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native int callPPPPI(long var0, int var2, int var3, long var4, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPI(long var0, int var2, int var3, long var4, long @Nullable [] var6, long var7, long var9);

    public static native int callPPPPI(long var0, int var2, int var3, long @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJPPI(long var0, long var2, int var4, int var5, int var6, long var7, int @Nullable [] var9, long var10);

    public static native int callPPPPI(long var0, int var2, long var3, int var5, int var6, long var7, int @Nullable [] var9, long var10);

    public static native int callPPPPI(int var0, int @Nullable [] var1, long @Nullable [] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20, int @Nullable [] var21, long @Nullable [] var22, long var23);

    public static native int callPJPPPI(long var0, long var2, long var4, long var6, long @Nullable [] var8, long var9);

    public static native int callPPJPPI(long var0, long var2, long var4, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPPPPPI(long var0, long var2, int @Nullable [] var4, int @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native int callPPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, long var5, long var7);

    public static native int callPJPPPI(long var0, long var2, int var4, long var5, long var7, long @Nullable [] var9, long var10);

    public static native int callPPPPPI(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8, long var10);

    public static native int callPPPPPI(long var0, long var2, int var4, long var5, long @Nullable [] var7, long var8, long var10);

    public static native int callPPPPPI(long var0, long var2, long var4, int @Nullable [] var6, int var7, int @Nullable [] var8, long var9);

    public static native int callPPPPPI(long var0, int @Nullable [] var2, float @Nullable [] var3, int var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6, long var8);

    public static native int callPPPPPI(long var0, int var2, long var3, long @Nullable [] var5, int var6, long var7, long var9, long var11);

    public static native int callPPPPPI(long var0, int var2, long @Nullable [] var3, int var4, long var5, long var7, long var9, long var11);

    public static native int callPJPPJI(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9, int var11, long var12);

    public static native int callPJPPJI(long var0, long var2, int var4, int var5, long var6, long @Nullable [] var8, long var9, int var11, long var12);

    public static native int callPJJJJPI(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native int callPPPPPPI(long var0, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7);

    public static native int callPJJPPPI(long var0, long var2, long var4, int var6, long var7, long var9, long @Nullable [] var11, long var12);

    public static native int callPPPPPPI(long var0, long var2, long @Nullable [] var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8, long var10);

    public static native int callPPPPPPI(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native int callPPPPPPPI(long var0, long var2, long var4, long var6, int var8, long var9, int @Nullable [] var11, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, float @Nullable [] var4, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, int @Nullable [] var4, long var5, long var7, int var9, long var10, long var12, long var14);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, double @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, float @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, int @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPPPPPPI(long var0, long var2, int var4, long var5, long var7, short @Nullable [] var9, int var10, long var11, long var13, long var15);

    public static native int callPPJPPPPPI(long var0, long @Nullable [] var2, long var3, long var5, long var7, int var9, long var10, long var12, long var14, long var16);

    public static native int callPPJPPPPPI(long var0, long @Nullable [] var2, long var3, long var5, long var7, int var9, long var10, int @Nullable [] var12, long var13, long var15);

    public static native int callPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, long var7, long var9, int var11, int @Nullable [] var12, int @Nullable [] var13, long var14, long var16);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, double @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, float @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, int @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, short @Nullable [] var13, int var14, long var15, long var17, long var19);

    public static native int callPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, long var7, long var9, long var11, int var13, int @Nullable [] var14, int @Nullable [] var15, long var16, long var18);

    public static native int callPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, float @Nullable [] var7, long var8, long var10, int var12, int @Nullable [] var13, int @Nullable [] var14, long var15, long var17);

    public static native int callPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, int @Nullable [] var7, long var8, long var10, int var12, int @Nullable [] var13, int @Nullable [] var14, long var15, long var17);

    public static native int callPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, int var7, long var8, long var10, long var12, int var14, int @Nullable [] var15, int @Nullable [] var16, long var17, long var19);

    public static native int callPPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, long var7, long var9, long var11, long var13, int var15, int @Nullable [] var16, int @Nullable [] var17, long var18, long var20);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, double @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, float @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, int @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPI(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, short @Nullable [] var19, int var20, long var21, long var23, long var25);

    public static native int callPPPPPPPPPPPPPPPI(long var0, long var2, long @Nullable [] var4, long var5, long var7, long var9, long var11, long var13, long var15, long var17, long var19, long var21, int var23, int @Nullable [] var24, int @Nullable [] var25, long var26, long var28);

    public static native long callPP(int @Nullable [] var0, long var1);

    public static native long callPPP(long var0, int @Nullable [] var2, long var3);

    public static native long callPPP(int var0, long var1, int @Nullable [] var3, long var4);

    public static native long callPPP(long var0, int var2, int @Nullable [] var3, long var4);

    public static native long callPPP(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native long callPPP(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(long var0, long var2, int @Nullable [] var4, long var5);

    public static native long callPPPP(long var0, long @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native long callPJPP(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(int var0, long var1, long @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native long callPPPP(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native long callPPPP(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native long callPPPP(long var0, long var2, int var4, int var5, int @Nullable [] var6, long var7);

    public static native long callPJPP(long var0, long var2, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native long callPPJPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPNPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, long var2, long var4, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, long var2, long @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native long callPJPPP(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native long callPJPPP(long var0, long var2, int @Nullable [] var4, int var5, int @Nullable [] var6, long var7);

    public static native long callPPPPP(long var0, int var2, long var3, long var5, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long var2, int var4, long var5, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long var2, long var4, int var6, int @Nullable [] var7, long var8);

    public static native long callPPPPP(long var0, long @Nullable [] var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native long callPJPPP(long var0, long var2, int var4, int var5, long var6, int @Nullable [] var8, long var9);

    public static native long callPJPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, long var9);

    public static native long callPJPPPP(long var0, long var2, long var4, double @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, float @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPJPPPP(long var0, long var2, long var4, short @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native long callPPPPPP(long var0, int var2, long var3, long var5, long var7, int @Nullable [] var9, long var10);

    public static native long callPPPPPP(long var0, long var2, long @Nullable [] var4, long var5, int var7, int @Nullable [] var8, long var9);

    public static native long callPPPPPP(long var0, int var2, int var3, long var4, int var6, long var7, long var9, int @Nullable [] var11, long var12);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, double @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, float @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJJPPPP(long var0, long var2, long var4, long var6, short @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, float @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, int @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPJPPPPP(long var0, long var2, long var4, long var6, short @Nullable [] var8, int @Nullable [] var9, long var10);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, int @Nullable [] var9, long var10);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, double @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, float @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, int @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPJPPPP(long var0, long @Nullable [] var2, long var3, long var5, short @Nullable [] var7, int @Nullable [] var8, long var9);

    public static native long callPPPJPPP(long var0, long var2, long var4, long var6, long var8, int @Nullable [] var10, long var11);

    public static native long callPPPPPPP(long var0, int var2, long var3, long var5, long var7, int @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, long var9, int @Nullable [] var11, long var12);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, float @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, int @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPJPPPPP(long var0, long @Nullable [] var2, long var3, long var5, long var7, short @Nullable [] var9, int @Nullable [] var10, long var11);

    public static native long callPPPPPPPP(long var0, int var2, long var3, long var5, int var7, long var8, long var10, long var12, int @Nullable [] var14, long var15);

    public static native long callPPPPPPPP(int var0, int @Nullable [] var1, long @Nullable [] var2, int var3, int var4, int var5, int var6, long var7, long var9, long var11, int var13, int @Nullable [] var14, long @Nullable [] var15, long var16);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, int @Nullable [] var14, long var15);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, float @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, int @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPJPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, short @Nullable [] var12, int @Nullable [] var13, long var14);

    public static native long callPPJPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, int var11, long var12, long var14, int @Nullable [] var16, long var17);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, int @Nullable [] var18, long var19);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, float @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, int @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPJPPPPPPPPP(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, short @Nullable [] var16, int @Nullable [] var17, long var18);

    public static native long callPPJPPPPPPPP(long var0, long var2, int var4, long var5, long var7, long var9, long var11, long var13, int var15, long var16, long var18, int @Nullable [] var20, long var21);

    public static native void callPV(double @Nullable [] var0, long var1);

    public static native void callPV(float @Nullable [] var0, long var1);

    public static native void callPV(int @Nullable [] var0, long var1);

    public static native void callPV(short @Nullable [] var0, long var1);

    public static native void callPV(int var0, double @Nullable [] var1, long var2);

    public static native void callPV(int var0, float @Nullable [] var1, long var2);

    public static native void callPV(int var0, int @Nullable [] var1, long var2);

    public static native void callPV(int var0, long @Nullable [] var1, long var2);

    public static native void callPV(int var0, short @Nullable [] var1, long var2);

    public static native void callPV(int var0, int var1, double @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, float @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, long @Nullable [] var2, long var3);

    public static native void callPV(int var0, int var1, short @Nullable [] var2, long var3);

    public static native void callPV(int var0, int @Nullable [] var1, int var2, long var3);

    public static native void callPV(int var0, int var1, int var2, double @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, float @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, int @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, long @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int var2, short @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, double @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, float @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, boolean var2, int @Nullable [] var3, long var4);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPV(int var0, int @Nullable [] var1, int var2, int var3, long var4);

    public static native void callPV(int var0, int var1, int var2, int var3, double @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, float @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, long @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int var3, short @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, boolean var3, double @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, boolean var3, float @Nullable [] var4, long var5);

    public static native void callPV(int var0, int var1, int var2, int @Nullable [] var3, boolean var4, long var5);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, int var4, long var5);

    public static native void callPV(int var0, boolean var1, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, double @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, short @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, boolean var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, float @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, boolean var3, int var4, short @Nullable [] var5, long var6);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, double @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void callPV(int var0, int var1, int @Nullable [] var2, int var3, int var4, int var5, int var6, long var7);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, short @Nullable [] var7, long var8);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, double @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPV(int var0, double var1, double var3, int var5, int var6, double var7, double var9, int var11, int var12, double @Nullable [] var13, long var14);

    public static native void callPV(int var0, float var1, float var2, int var3, int var4, float var5, float var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, double @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, short @Nullable [] var9, long var10);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, double @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, float @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, short @Nullable [] var10, long var11);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, double @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, float @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int @Nullable [] var11, long var12);

    public static native void callPV(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, short @Nullable [] var11, long var12);

    public static native void callPPV(long var0, float @Nullable [] var2, long var3);

    public static native void callPPV(long var0, int @Nullable [] var2, long var3);

    public static native void callPPV(double @Nullable [] var0, double @Nullable [] var1, long var2);

    public static native void callPPV(float @Nullable [] var0, float @Nullable [] var1, long var2);

    public static native void callPPV(int @Nullable [] var0, int @Nullable [] var1, long var2);

    public static native void callPPV(short @Nullable [] var0, short @Nullable [] var1, long var2);

    public static native void callPPV(int var0, long var1, int @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int @Nullable [] var1, float @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int @Nullable [] var1, long @Nullable [] var2, long var3);

    public static native void callPPV(long var0, int var2, float @Nullable [] var3, long var4);

    public static native void callPPV(long var0, int var2, int @Nullable [] var3, long var4);

    public static native void callPPV(int @Nullable [] var0, int var1, int @Nullable [] var2, long var3);

    public static native void callPPV(int var0, int var1, long var2, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long var3, long var5);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, float @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, long var4);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long @Nullable [] var3, long var4);

    public static native void callPPV(int var0, long var1, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, long var1, double @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, float @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, int @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, long @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, long var1, short @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPPV(int var0, long @Nullable [] var1, int @Nullable [] var2, int var3, long var4);

    public static native void callPPV(long var0, int var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, int var2, long var3, int @Nullable [] var5, long var6);

    public static native void callPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPV(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int var1, long var2, int @Nullable [] var4, int var5, long var6);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, int var2, long var3, int var5, long var6);

    public static native void callPPV(int var0, int @Nullable [] var1, int var2, int @Nullable [] var3, int var4, long var5);

    public static native void callPPV(int var0, int @Nullable [] var1, long var2, int var4, int var5, long var6);

    public static native void callPPV(long var0, int var2, int var3, int var4, int @Nullable [] var5, long var6);

    public static native void callPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, long var5, long var7);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, float @Nullable [] var6, long var7);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, short @Nullable [] var6, long var7);

    public static native void callPPV(int var0, int var1, int @Nullable [] var2, long var3, int var5, int var6, long var7);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, int var2, int var3, long var4, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPV(int var0, int var1, int var2, long var3, int var5, float var6, float var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPPV(int var0, int var1, long var2, int var4, int var5, int var6, int var7, int var8, float @Nullable [] var9, long var10);

    public static native void callPJPV(long var0, long var2, long @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, long var2, int @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int @Nullable [] var2, long var3, long var5);

    public static native void callPJPV(long var0, long var2, int var4, long @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, double @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, float @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, long @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, long var1, long var3, short @Nullable [] var5, long var6);

    public static native void callPPPV(long var0, int var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(long var0, int var2, int @Nullable [] var3, long @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int var2, long @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(long var0, long var2, int var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int var1, long var2, long var4, double @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, float @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, short @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, long var3, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, int @Nullable [] var3, long var4, long var6);

    public static native void callPPPV(int var0, long var1, int var3, int @Nullable [] var4, long var5, long var7);

    public static native void callPPPV(int var0, long var1, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int @Nullable [] var1, int @Nullable [] var2, int var3, int @Nullable [] var4, long var5);

    public static native void callPPPV(long var0, int var2, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long var6);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, double @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, float @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, int @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, long @Nullable [] var7, long var8);

    public static native void callPJPV(long var0, long var2, int var4, int var5, int var6, short @Nullable [] var7, long var8);

    public static native void callPPJV(long var0, int var2, long @Nullable [] var3, int var4, long var5, int var7, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, long var6, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int @Nullable [] var3, long var4, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, int @Nullable [] var2, int var3, int @Nullable [] var4, int @Nullable [] var5, long var6);

    public static native void callPPPV(int var0, int @Nullable [] var1, int var2, long var3, int var5, int @Nullable [] var6, long var7);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, float @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, long var2, long var4, int var6, int var7, short @Nullable [] var8, long var9);

    public static native void callPPPV(long var0, int var2, long var3, int var5, int var6, int var7, int @Nullable [] var8, long var9);

    public static native void callPPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, int var5, int @Nullable [] var6, float @Nullable [] var7, long var8);

    public static native void callPPPV(int var0, int var1, int var2, int var3, int @Nullable [] var4, int var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native void callPPPV(long var0, int var2, int var3, int var4, int var5, int var6, int @Nullable [] var7, long var8, long var10);

    public static native void callPJPPV(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native void callPPPPV(long var0, long var2, int @Nullable [] var4, long var5, long var7);

    public static native void callPJJPV(long var0, int var2, long var3, long var5, long @Nullable [] var7, long var8);

    public static native void callPPPPV(long var0, int var2, long var3, int @Nullable [] var5, long var6, long var8);

    public static native void callPPPPV(long @Nullable [] var0, int @Nullable [] var1, int @Nullable [] var2, int @Nullable [] var3, int var4, long var5);

    public static native void callPPPPV(int var0, long var1, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, int var6, long var7);

    public static native void callPPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long var7);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, int @Nullable [] var7, long @Nullable [] var8, long var9);

    public static native void callPPPPV(int var0, int var1, int var2, int @Nullable [] var3, int @Nullable [] var4, int @Nullable [] var5, long var6, long var8);

    public static native void callPPPPV(int var0, int var1, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int var7, long var8);

    public static native void callPJPPV(long var0, int var2, long var3, int var5, int var6, long @Nullable [] var7, int var8, int @Nullable [] var9, long var10);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, double @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, float @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, int @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, long @Nullable [] var8, long var9);

    public static native void callPJJJPV(long var0, long var2, long var4, long var6, short @Nullable [] var8, long var9);

    public static native void callPPPPPV(long var0, int var2, long var3, long @Nullable [] var5, int @Nullable [] var6, long var7, long var9);

    public static native void callPPPPPV(int var0, int var1, long var2, int @Nullable [] var4, int @Nullable [] var5, int @Nullable [] var6, int @Nullable [] var7, long var8);

    public static native void callPPPPPV(long var0, int var2, int var3, long @Nullable [] var4, long @Nullable [] var5, long @Nullable [] var6, long @Nullable [] var7, long var8);

    public static native void callPPPPPV(long var0, int var2, long @Nullable [] var3, int var4, int var5, int var6, long var7, int var9, long var10, int var12, long var13, long var15);

    public static native void callPPPPPPPV(int var0, int var1, int var2, long var3, int var5, long var6, int @Nullable [] var8, int @Nullable [] var9, int @Nullable [] var10, int @Nullable [] var11, long @Nullable [] var12, long var13);

    public static native boolean callPPZ(int var0, int @Nullable [] var1, long var2, long var4);

    public static native boolean callPPPPZ(int var0, int var1, int var2, float var3, float @Nullable [] var4, float @Nullable [] var5, float @Nullable [] var6, float @Nullable [] var7, long var8);

    @FFMFunctionAddress
    private static interface JNIBindings {
        public byte invokePB(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public short invokeC(MemorySegment var1);

        public short invokeC(MemorySegment var1, int var2);

        public short invokePC(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public short invokeCC(MemorySegment var1, int var2, short var3);

        public short invokeCC(MemorySegment var1, short var2, boolean var3);

        public short invokePC(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public short invokeJC(MemorySegment var1, int var2, int var3, long var4);

        public short invokeCUC(MemorySegment var1, short var2, byte var3);

        public short invokePCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public short invokeCCC(MemorySegment var1, short var2, short var3, boolean var4);

        public short invokePCC(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, short var5);

        public short invokePCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, short var5);

        public short invokeUPC(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3, boolean var5);

        public short invokePCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, short var6);

        public short invokeCJC(MemorySegment var1, int var2, boolean var3, short var4, int var5, long var6);

        public short invokeCPCC(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5);

        public short invokeCPPC(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public short invokePPCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6);

        public short invokeCCJC(MemorySegment var1, short var2, short var3, int var4, long var5);

        public short invokePCCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, int var6, int var7);

        public short invokeCCCCC(MemorySegment var1, short var2, short var3, short var4, short var5);

        public short invokePJUPC(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, byte var6, @FFMNullable @FFMPointer long var7);

        public short invokeCCJPC(MemorySegment var1, short var2, boolean var3, short var4, int var5, long var6, @FFMNullable @FFMPointer long var8);

        public short invokePCCCCC(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, short var7);

        public short invokeCCCJPC(MemorySegment var1, short var2, short var3, short var4, boolean var5, int var6, long var7, @FFMNullable @FFMPointer long var9);

        public short invokeCCCJPC(MemorySegment var1, short var2, short var3, boolean var4, short var5, int var6, long var7, @FFMNullable @FFMPointer long var9);

        public double invokeD(MemorySegment var1);

        public double invokeD(MemorySegment var1, int var2);

        public double invokePD(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public double invokePD(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public double invokePD(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public double invokePPD(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public float invokeF(MemorySegment var1);

        public float invokeF(MemorySegment var1, int var2);

        public float invokePF(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public float invokePF(MemorySegment var1, float var2, @FFMNullable @FFMPointer long var3);

        public float invokePF(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public float invokePF(MemorySegment var1, float var2, float var3, @FFMNullable @FFMPointer long var4);

        public float invokePF(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, float var5);

        public float invokePF(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5);

        public float invokePF(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public float invokePPF(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public float invokePPF(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public float invokePPF(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, @FFMNullable @FFMPointer long var5, int var7);

        public int invokeI(MemorySegment var1);

        public int invokeI(MemorySegment var1, int var2);

        public int invokeI(MemorySegment var1, boolean var2);

        public int invokeI(MemorySegment var1, int var2, float var3);

        public int invokeI(MemorySegment var1, int var2, int var3);

        public int invokeI(MemorySegment var1, int var2, boolean var3);

        public int invokeI(MemorySegment var1, int var2, int var3, int var4);

        public int invokeI(MemorySegment var1, int var2, int var3, int var4, int var5);

        public int invokeI(MemorySegment var1, int var2, int var3, int var4, int var5, int var6);

        public int invokeI(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7);

        public int invokeJI(MemorySegment var1, long var2);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public int invokeCI(MemorySegment var1, int var2, short var3);

        public int invokePI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4);

        public int invokeCI(MemorySegment var1, int var2, short var3, boolean var4);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, boolean var5);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7);

        public int invokePI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9);

        public int invokeCPI(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3);

        public int invokePCI(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public int invokePJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public int invokePNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public int invokePJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public int invokePNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        public int invokePNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public int invokePPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, @FFMNullable @FFMPointer long var5);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, boolean var7);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, boolean var5, @FFMNullable @FFMPointer long var6);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6, boolean var7);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8);

        public int invokePPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, int var8, boolean var9);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public int invokePPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, int var10);

        public int invokeCPUI(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, byte var5);

        public int invokeJPPI(MemorySegment var1, long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public int invokePCPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5);

        public int invokePNNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        public int invokePNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public int invokePPCI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6);

        public int invokePPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        public int invokePPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public int invokePNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7);

        public int invokePNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8);

        public int invokePPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7);

        public int invokePPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public int invokePNNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, int var9);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, int var10);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, int var10);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, int var10);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, boolean var9, float var10);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11);

        public int invokePPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, int var12);

        public int invokePNPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int invokePPNNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8);

        public int invokePPNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        public int invokePPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int invokePUUUI(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, byte var5, byte var6);

        public int invokePNNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, long var7, @FFMNullable @FFMPointer long var9);

        public int invokePPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, long var9);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, int var11);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, int var13);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11, int var13);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, int var11, int var12, int var13);

        public int invokePPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, int var14);

        public int invokePNNPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPNNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPPNNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, long var10);

        public int invokePPPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, long var10);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int invokePPUUUI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, byte var6, byte var7, byte var8);

        public int invokePUUUUI(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, byte var5, byte var6, byte var7);

        public int invokePJPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, long var11);

        public int invokePPNPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPNPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPNJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, long var9, long var11);

        public int invokePPPNNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, long var9, long var11);

        public int invokePPPNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, long var11);

        public int invokePPPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, long var11);

        public int invokePPPPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12);

        public int invokePNPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, int var12, @FFMNullable @FFMPointer long var13);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, int var12, @FFMNullable @FFMPointer long var13);

        public int invokePPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, float var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int invokePPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int invokePPUUUUI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, byte var6, byte var7, byte var8, byte var9);

        public int invokePJJJJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, long var10, int var12, @FFMNullable @FFMPointer long var13);

        public int invokePPNPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int invokePPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int invokePPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14);

        public int invokePNNPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int invokePPPPPPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int invokePPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int invokePPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, @FFMNullable @FFMPointer long var16);

        public int invokePPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int invokePPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int invokePPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, float var11, float var12, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17);

        public int invokePPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18, int var20);

        public int invokePPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18);

        public long invokeJ(MemorySegment var1);

        public long invokeJ(MemorySegment var1, int var2, int var3);

        public long invokePJ(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public long invokePJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public long invokePJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public long invokePJJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public long invokePPJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public long invokePJJ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, long var5);

        public long invokePJJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        public long invokePJJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public long invokePPJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public long invokeNN(MemorySegment var1, long var2);

        public long invokePN(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public long invokePN(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public long invokeNNN(MemorySegment var1, long var2, long var4);

        public long invokePPN(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public long invokeNNNN(MemorySegment var1, long var2, long var4, long var6);

        public long invokePNPN(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public long invokePNPN(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8);

        public long invokePPNN(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, long var8);

        public long invokePNPNN(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, long var8);

        public long invokePNPNPN(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, int var8, int var9, int var10, int var11, int var12, @FFMNullable @FFMPointer long var13, long var15, @FFMNullable @FFMPointer long var17);

        @FFMPointer
        public long invokeP(MemorySegment var1);

        @FFMPointer
        public long invokeP(MemorySegment var1, int var2);

        @FFMPointer
        public long invokeP(MemorySegment var1, boolean var2);

        @FFMPointer
        public long invokeP(MemorySegment var1, int var2, int var3);

        @FFMPointer
        public long invokeP(MemorySegment var1, int var2, int var3, int var4);

        @FFMPointer
        public long invokeCP(MemorySegment var1, short var2);

        @FFMPointer
        public long invokeJP(MemorySegment var1, long var2);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        @FFMPointer
        public long invokePP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, double var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, int var2, boolean var3, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        @FFMPointer
        public long invokePP(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        @FFMPointer
        public long invokePP(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, int var5, float var6, int var7);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7);

        @FFMPointer
        public long invokePP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8);

        @FFMPointer
        public long invokeCCP(MemorySegment var1, short var2, short var3);

        @FFMPointer
        public long invokeJPP(MemorySegment var1, long var2, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokePJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        @FFMPointer
        public long invokePNP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokePUP(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4);

        @FFMPointer
        public long invokeCPP(MemorySegment var1, int var2, short var3, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokePCP(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, boolean var5);

        @FFMPointer
        public long invokePJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        @FFMPointer
        public long invokePJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        @FFMPointer
        public long invokePPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6);

        @FFMPointer
        public long invokePJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6, boolean var7);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, boolean var5, @FFMNullable @FFMPointer long var6);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8);

        @FFMPointer
        public long invokePJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, long var8);

        @FFMPointer
        public long invokePPP(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9);

        @FFMPointer
        public long invokePUP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, byte var5, int var6, boolean var7, boolean var8);

        @FFMPointer
        public long invokePPP(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokeCCPP(MemorySegment var1, short var2, short var3, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long invokeCPCP(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5);

        @FFMPointer
        public long invokePJJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        @FFMPointer
        public long invokePPJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        @FFMPointer
        public long invokePPUP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, byte var6);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePPUP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, byte var7);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9);

        @FFMPointer
        public long invokeJPPP(MemorySegment var1, int var2, int var3, int var4, long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, int var10);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, int var10);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokePPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, int var10, int var11);

        @FFMPointer
        public long invokePBPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long invokePNNPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPNNP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPPJP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, long var9);

        @FFMPointer
        public long invokePPPJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, int var10);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        @FFMPointer
        public long invokePJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokePJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11);

        @FFMPointer
        public long invokePPPJP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, int var10, int var11);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, int var12);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, int var11, int var12);

        @FFMPointer
        public long invokePPPPP(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long invokePJPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokePNNNPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokePPBPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, byte var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long invokeCCCUJP(MemorySegment var1, short var2, short var3, short var4, byte var5, int var6, long var7);

        @FFMPointer
        public long invokePPPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, @FFMNullable @FFMPointer long var10, int var12);

        @FFMPointer
        public long invokePPPPNP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, long var11);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12);

        @FFMPointer
        public long invokePPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, int var8, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, int var13);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, int var13);

        @FFMPointer
        public long invokePPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, int var13, int var14, int var15);

        @FFMPointer
        public long invokePPJJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long invokePSSCCPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, short var7, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14, int var16, int var17);

        @FFMPointer
        public long invokePPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, int var16, int var17);

        @FFMPointer
        public long invokePPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        @FFMPointer
        public long invokePPPPPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, @FFMNullable @FFMPointer long var16);

        @FFMPointer
        public long invokePPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17);

        @FFMPointer
        public long invokePPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, @FFMNullable @FFMPointer long var16, int var18);

        @FFMPointer
        public long invokePPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        @FFMPointer
        public long invokePPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17);

        @FFMPointer
        public long invokePPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18);

        @FFMPointer
        public long invokePPPPJJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, long var11, int var13, long var14, int var16, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19);

        @FFMPointer
        public long invokePPPPPJJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, long var16, int var18, @FFMNullable @FFMPointer long var19);

        @FFMPointer
        public long invokePPPPPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, @FFMNullable @FFMPointer long var16, int var18, @FFMNullable @FFMPointer long var19);

        @FFMPointer
        public long invokePPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, @FFMNullable @FFMPointer long var16, int var18, @FFMNullable @FFMPointer long var19);

        @FFMPointer
        public long invokePPPPPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, int var14, long var15, int var17, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long invokePPPPPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, int var14, long var15, int var17, int var18, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21);

        @FFMPointer
        public long invokePPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17, int var19, int var20, int var21, int var22, int var23);

        @FFMPointer
        public long invokePPPPPJJJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, long var15, long var17, int var19, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long invokePPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17, int var19, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long invokePPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17, int var19, @FFMNullable @FFMPointer long var20, int var22);

        @FFMPointer
        public long invokePPPPPJPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, int var16, @FFMNullable @FFMPointer long var17, int var19, int var20, @FFMNullable @FFMPointer long var21, @FFMNullable @FFMPointer long var23);

        @FFMPointer
        public long invokePPPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long invokePPPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17, int var19, @FFMNullable @FFMPointer long var20, @FFMNullable @FFMPointer long var22, int var24);

        @FFMPointer
        public long invokePPPPPJPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, long var13, int var15, int var16, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21, @FFMNullable @FFMPointer long var23, @FFMNullable @FFMPointer long var25);

        @FFMPointer
        public long invokePPPPPJPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, int var16, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19, int var21, int var22, @FFMNullable @FFMPointer long var23, @FFMNullable @FFMPointer long var25, @FFMNullable @FFMPointer long var27);

        @FFMPointer
        public long invokePPPPPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19, int var21, int var22, int var23, int var24, @FFMNullable @FFMPointer long var25, @FFMNullable @FFMPointer long var27, @FFMNullable @FFMPointer long var29, @FFMNullable @FFMPointer long var31);

        @FFMPointer
        public long invokePPPPPJPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, long var13, int var15, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, int var20, @FFMNullable @FFMPointer long var21, @FFMNullable @FFMPointer long var23, @FFMNullable @FFMPointer long var25, @FFMNullable @FFMPointer long var27, @FFMNullable @FFMPointer long var29);

        @FFMPointer
        public long invokePPPPPJJPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, long var13, int var15, long var16, int var18, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21, int var23, @FFMNullable @FFMPointer long var24, @FFMNullable @FFMPointer long var26, @FFMNullable @FFMPointer long var28, @FFMNullable @FFMPointer long var30);

        public short invokePS(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public byte invokeU(MemorySegment var1, int var2);

        public byte invokePU(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public byte invokeUPU(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3);

        public void invokeV(MemorySegment var1);

        public void invokeV(MemorySegment var1, double var2);

        public void invokeV(MemorySegment var1, float var2);

        public void invokeV(MemorySegment var1, int var2);

        public void invokeV(MemorySegment var1, boolean var2);

        public void invokeV(MemorySegment var1, int var2, float var3);

        public void invokeV(MemorySegment var1, int var2, int var3);

        public void invokeV(MemorySegment var1, int var2, boolean var3);

        public void invokeV(MemorySegment var1, int var2, int var3, double var4);

        public void invokeV(MemorySegment var1, int var2, int var3, float var4);

        public void invokeV(MemorySegment var1, int var2, int var3, int var4);

        public void invokeV(MemorySegment var1, int var2, float var3, float var4, float var5);

        public void invokeV(MemorySegment var1, int var2, int var3, int var4, int var5);

        public void invokeV(MemorySegment var1, int var2, int var3, double var4, double var6, double var8);

        public void invokeV(MemorySegment var1, int var2, int var3, float var4, float var5, float var6);

        public void invokeV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6);

        public void invokeCV(MemorySegment var1, short var2);

        public void invokeJV(MemorySegment var1, long var2);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public void invokeUV(MemorySegment var1, byte var2);

        public void invokeCV(MemorySegment var1, int var2, short var3);

        public void invokeCV(MemorySegment var1, short var2, int var3);

        public void invokeCV(MemorySegment var1, short var2, boolean var3);

        public void invokeJV(MemorySegment var1, int var2, long var3);

        public void invokeJV(MemorySegment var1, long var2, int var4);

        public void invokePV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4);

        public void invokeUV(MemorySegment var1, byte var2, int var3);

        public void invokeUV(MemorySegment var1, byte var2, boolean var3);

        public void invokeCV(MemorySegment var1, short var2, int var3, int var4);

        public void invokeJV(MemorySegment var1, int var2, int var3, long var4);

        public void invokePV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        public void invokePV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, boolean var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, double var4, double var6);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, double var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, boolean var5);

        public void invokePV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, int var6);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, double var6);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, float var6);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        public void invokePV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, float var7);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7);

        public void invokeUV(MemorySegment var1, byte var2, float var3, float var4, float var5, float var6);

        public void invokePV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void invokePV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, boolean var8);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, double var6, double var8, double var10);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, float var6, float var7, float var8);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8);

        public void invokePV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, boolean var8);

        public void invokePV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public void invokeCCV(MemorySegment var1, short var2, short var3);

        public void invokeCPV(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public void invokePJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public void invokePNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public void invokePUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4);

        public void invokeUPV(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3);

        public void invokeCPV(MemorySegment var1, short var2, int var3, @FFMNullable @FFMPointer long var4);

        public void invokeCPV(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, int var5);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, short var5);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, boolean var5);

        public void invokePJV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, long var5);

        public void invokePJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        public void invokePJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public void invokePPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6);

        public void invokeUCV(MemorySegment var1, byte var2, short var3, int var4);

        public void invokePBV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, byte var6);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, short var6);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, int var5, int var6);

        public void invokePJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6);

        public void invokePPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public void invokePPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, boolean var7);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public void invokePSV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, short var6);

        public void invokePUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, byte var6);

        public void invokeUCV(MemorySegment var1, byte var2, short var3, int var4, int var5);

        public void invokeUPV(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3, int var5, int var6);

        public void invokePCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, int var5, int var6, int var7);

        public void invokePPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, @FFMNullable @FFMPointer long var5, int var7, int var8);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8);

        public void invokePPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, int var9);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, float var7, float var8, float var9);

        public void invokePPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, boolean var10);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, int var10);

        public void invokePPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, float var7, float var8, float var9, float var10, float var11);

        public void invokeCCPV(MemorySegment var1, short var2, short var3, @FFMNullable @FFMPointer long var4);

        public void invokeCPCV(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5);

        public void invokeCPPV(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public void invokeJPPV(MemorySegment var1, long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public void invokePJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public void invokePNNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        public void invokePNPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public void invokePPNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public void invokeCCCV(MemorySegment var1, short var2, short var3, short var4, int var5);

        public void invokeCCUV(MemorySegment var1, short var2, short var3, int var4, byte var5);

        public void invokePJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8);

        public void invokePPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7);

        public void invokePPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, boolean var8);

        public void invokePUCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, short var5, int var6);

        public void invokeUCCV(MemorySegment var1, byte var2, short var3, short var4, int var5);

        public void invokeCCUV(MemorySegment var1, short var2, short var3, int var4, float var5, byte var6);

        public void invokeJJJV(MemorySegment var1, int var2, int var3, long var4, long var6, long var8);

        public void invokePNNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, long var8);

        public void invokePPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, boolean var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, boolean var8, boolean var9);

        public void invokePUCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, short var5, int var6, int var7);

        public void invokePUPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, @FFMNullable @FFMPointer long var5, int var7, int var8);

        public void invokeUCCV(MemorySegment var1, byte var2, short var3, int var4, int var5, short var6);

        public void invokeUCUV(MemorySegment var1, byte var2, short var3, byte var4, int var5, int var6);

        public void invokeUPCV(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3, int var5, int var6, short var7);

        public void invokeCCUV(MemorySegment var1, short var2, short var3, int var4, int var5, int var6, byte var7);

        public void invokePPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, int var10);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, int var10);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, boolean var9, boolean var10);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, int var10, int var11);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, float var7, float var8, float var9, @FFMNullable @FFMPointer long var10);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, boolean var11);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9, int var11, boolean var12);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, float var7, float var8, float var9, float var10, float var11, @FFMNullable @FFMPointer long var12);

        public void invokePPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11, float var12, float var13);

        public void invokeCCPCV(MemorySegment var1, short var2, short var3, @FFMNullable @FFMPointer long var4, short var6);

        public void invokeCCUPV(MemorySegment var1, short var2, short var3, byte var4, @FFMNullable @FFMPointer long var5);

        public void invokePCPCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7);

        public void invokePNPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void invokeCCCUV(MemorySegment var1, short var2, short var3, short var4, int var5, byte var6);

        public void invokePCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, int var6, byte var7);

        public void invokePJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPCPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, int var7, @FFMNullable @FFMPointer long var8);

        public void invokePPPCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, short var9);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, boolean var10);

        public void invokePUCCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, short var5, short var6, int var7);

        public void invokeCCCUV(MemorySegment var1, short var2, short var3, short var4, int var5, int var6, byte var7);

        public void invokePJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, long var8, long var10);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, @FFMNullable @FFMPointer long var10);

        public void invokePUCCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, short var5, int var6, int var7, short var8);

        public void invokePUCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, short var5, byte var6, int var7, int var8);

        public void invokePUPCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, @FFMNullable @FFMPointer long var5, int var7, int var8, short var9);

        public void invokeCCCUV(MemorySegment var1, short var2, short var3, short var4, int var5, int var6, int var7, byte var8);

        public void invokePCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, int var6, int var7, int var8, byte var9);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11, @FFMNullable @FFMPointer long var12);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, float var11, @FFMNullable @FFMPointer long var12);

        public void invokePPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11, float var12, float var13, @FFMNullable @FFMPointer long var14);

        public void invokeCCCCCV(MemorySegment var1, short var2, short var3, short var4, short var5, short var6);

        public void invokeCCUPPV(MemorySegment var1, short var2, short var3, byte var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void invokePPCPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void invokePCCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, int var7, byte var8);

        public void invokePJPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void invokePPPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11);

        public void invokePCCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, int var7, int var8, byte var9);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, boolean var13);

        public void invokePCCCCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, boolean var7, boolean var8, short var9, int var10);

        public void invokePCCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, int var7, int var8, int var9, byte var10);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13);

        public void invokePPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, int var12, @FFMNullable @FFMPointer long var13);

        public void invokeCCCCUV(MemorySegment var1, short var2, short var3, short var4, int var5, short var6, int var7, int var8, int var9, byte var10);

        public void invokePPPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, boolean var16);

        public void invokeCCCCPCV(MemorySegment var1, short var2, short var3, short var4, short var5, @FFMNullable @FFMPointer long var6, short var8);

        public void invokePPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public void invokePCCCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, int var5, short var6, short var7, short var8, byte var9);

        public void invokePPPPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public void invokePCCCCUV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, int var7, short var8, int var9, int var10, int var11, byte var12);

        public void invokePPPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public void invokePPPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public void invokeCCUCCCCPCV(MemorySegment var1, short var2, short var3, byte var4, short var5, short var6, short var7, short var8, @FFMNullable @FFMPointer long var9, short var11);

        public void invokeCUCCCCCCPV(MemorySegment var1, short var2, byte var3, short var4, short var5, short var6, short var7, short var8, short var9, @FFMNullable @FFMPointer long var10);

        public void invokeCCUUCCCCPCV(MemorySegment var1, short var2, short var3, byte var4, byte var5, short var6, short var7, short var8, short var9, @FFMNullable @FFMPointer long var10, short var12);

        public void invokeCCUUUUUUUUUV(MemorySegment var1, short var2, short var3, float var4, byte var5, byte var6, byte var7, byte var8, byte var9, byte var10, byte var11, byte var12, byte var13);

        public void invokeCCUCCCCUCCCCCCV(MemorySegment var1, short var2, short var3, byte var4, short var5, short var6, short var7, short var8, byte var9, short var10, short var11, short var12, short var13, short var14, short var15);

        public void invokePCCUCCCCUCCCCCCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, byte var6, short var7, short var8, short var9, short var10, byte var11, short var12, short var13, short var14, short var15, short var16, short var17);

        public boolean invokeZ(MemorySegment var1);

        public boolean invokeZ(MemorySegment var1, int var2);

        public boolean invokeZ(MemorySegment var1, boolean var2);

        public boolean invokeZ(MemorySegment var1, float var2, float var3);

        public boolean invokeZ(MemorySegment var1, int var2, float var3);

        public boolean invokeZ(MemorySegment var1, int var2, int var3);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public boolean invokeJZ(MemorySegment var1, long var2, int var4);

        public boolean invokePZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4);

        public boolean invokeJZ(MemorySegment var1, long var2, int var4, int var5);

        public boolean invokePZ(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        public boolean invokePZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, float var5);

        public boolean invokePZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5);

        public boolean invokePZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, boolean var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, int var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, boolean var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, int var5);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6, float var7);

        public boolean invokePZ(MemorySegment var1, int var2, int var3, int var4, float var5, boolean var6, @FFMNullable @FFMPointer long var7);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, float var6, float var7, float var8, float var9);

        public boolean invokePZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, boolean var6, float var7, float var8, float var9);

        public boolean invokePBZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4);

        public boolean invokePCZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public boolean invokePJZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public boolean invokePSZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public boolean invokePUZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4);

        public boolean invokeUPZ(MemorySegment var1, byte var2, @FFMNullable @FFMPointer long var3);

        public boolean invokeJPZ(MemorySegment var1, long var2, @FFMNullable @FFMPointer long var4, boolean var6);

        public boolean invokePJZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, long var5);

        public boolean invokePPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, boolean var6);

        public boolean invokePSZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, short var5);

        public boolean invokePUZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, byte var5);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, @FFMNullable @FFMPointer long var6);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, @FFMNullable @FFMPointer long var5, int var7);

        public boolean invokePPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, float var8);

        public boolean invokePPZ(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, @FFMNullable @FFMPointer long var9, int var11);

        public boolean invokePPZ(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, @FFMNullable @FFMPointer long var9, int var11, boolean var12);

        public boolean invokePPZ(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, int var9, int var10, int var11, int var12, @FFMNullable @FFMPointer long var13, int var15);

        public boolean invokePCCZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public boolean invokePCCZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, int var6);

        public boolean invokePJJZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, long var7);

        public boolean invokePJPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8);

        public boolean invokePPPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, boolean var8);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public boolean invokePSSZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, short var5, short var6);

        public boolean invokeCCJZ(MemorySegment var1, short var2, boolean var3, short var4, int var5, long var6);

        public boolean invokePJPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7, int var9);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, boolean var8, int var9);

        public boolean invokePPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, boolean var10);

        public boolean invokePPPJZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public boolean invokePUUUZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, byte var5, byte var6);

        public boolean invokePPPPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, @FFMNullable @FFMPointer long var9);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, boolean var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public boolean invokePJPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11, float var12, @FFMNullable @FFMPointer long var13);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, float var10, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public boolean invokePPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, float var8, float var9, float var10, float var11, float var12, @FFMNullable @FFMPointer long var13, float var15);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public boolean invokePPPUPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, byte var8, @FFMNullable @FFMPointer long var9);

        public boolean invokePUUUUZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, byte var4, byte var5, byte var6, byte var7);

        public boolean invokePPPPPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, double var10, @FFMNullable @FFMPointer long var12, int var14);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, boolean var12, int var13);

        public boolean invokePUUUUZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, byte var6, byte var7, byte var8, byte var9);

        public boolean invokePPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, int var14);

        public boolean invokePPJJPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public boolean invokePPPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public boolean invokePPPPPPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, int var14, int var15, @FFMNullable @FFMPointer long var16, int var18, int var19);

        public short callC(MemorySegment var1, int var2);

        public float callF(MemorySegment var1, int var2, int var3, int var4);

        public float callPF(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        public int callI(MemorySegment var1);

        public int callI(MemorySegment var1, int var2);

        public int callI(MemorySegment var1, int var2, int var3);

        public int callI(MemorySegment var1, int var2, int var3, int var4);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public int callPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public int callPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        public int callPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, int var5);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public int callJI(MemorySegment var1, int var2, long var3, int var5, int var6);

        public int callPI(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, boolean var6);

        public int callPI(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, int var7);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7);

        public int callPI(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public int callPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, int var6, float var7, int var8);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9);

        public int callPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, float var10);

        public int callPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11);

        public int callPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public int callPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        public int callPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, float var6);

        public int callPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public int callPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, @FFMNullable @FFMPointer long var5);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public int callPPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, @FFMNullable @FFMPointer long var6);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, float var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, float var6, int var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, @FFMNullable @FFMPointer long var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, boolean var8);

        public int callPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, int var6, float var7, @FFMNullable @FFMPointer long var8);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, float var7, @FFMNullable @FFMPointer long var8);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, float var7, int var8, @FFMNullable @FFMPointer long var9);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, float var7, float var8, int var9, @FFMNullable @FFMPointer long var10);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public int callPPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, int var11, float var12);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, float var5, float var6, float var7, float var8, float var9, int var10, @FFMNullable @FFMPointer long var11);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, float var6, float var7, float var8, float var9, float var10, int var11, @FFMNullable @FFMPointer long var12, int var14);

        public int callPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20);

        public int callPJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        public int callPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public int callPPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public int callPJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, float var8);

        public int callPJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8);

        public int callPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7);

        public int callPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7);

        public int callPPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7);

        public int callPPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, long var7);

        public int callPPNI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7);

        public int callPPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public int callPJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, long var7, int var9);

        public int callPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7, int var9);

        public int callPPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, long var8);

        public int callPPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, int var10);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, int var10, int var11);

        public int callPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public int callJPPI(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callJJPPI(MemorySegment var1, long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int callPJJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8);

        public int callPJJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8);

        public int callPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int callPPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        public int callPPNPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public int callPJJJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, long var7, long var9);

        public int callPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public int callPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, int var11);

        public int callPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11);

        public int callPPPPI(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18, int var19, int var20, int var21, int var22, int var23, int var24, @FFMNullable @FFMPointer long var25, @FFMNullable @FFMPointer long var27);

        public int callPJJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPJPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public int callPJJJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, int var10, @FFMNullable @FFMPointer long var11);

        public int callPJPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11);

        public int callPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, long var7, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, long var7, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callJPPPPI(MemorySegment var1, int var2, int var3, long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPJPPJI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, long var12, int var14);

        public int callPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPJJJJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPJPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, long var10, @FFMNullable @FFMPointer long var12);

        public int callPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public int callPJJPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPJPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public int callPJPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int callPPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, long var9, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int callPPPPPPI(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int callPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int callPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int callPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int callPPPPPJPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public int callPPJPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17);

        public int callPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17);

        public int callPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, int var15, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18);

        public int callPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19);

        public int callPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, int var14, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19);

        public int callPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        public int callPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, int var16, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21);

        public int callPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20, @FFMNullable @FFMPointer long var22);

        public int callPPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, int var18, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21, @FFMNullable @FFMPointer long var23);

        public int callPPPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20, int var22, @FFMNullable @FFMPointer long var23, @FFMNullable @FFMPointer long var25);

        public int callPPPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, @FFMNullable @FFMPointer long var17, @FFMNullable @FFMPointer long var19, @FFMNullable @FFMPointer long var21, int var23, @FFMNullable @FFMPointer long var24, @FFMNullable @FFMPointer long var26);

        public int callPPPPPPPPPPPPPPPI(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20, @FFMNullable @FFMPointer long var22, @FFMNullable @FFMPointer long var24, int var26, @FFMNullable @FFMPointer long var27, @FFMNullable @FFMPointer long var29, @FFMNullable @FFMPointer long var31);

        public long callJ(MemorySegment var1);

        public long callJ(MemorySegment var1, int var2);

        public long callJ(MemorySegment var1, int var2, int var3);

        public long callJ(MemorySegment var1, int var2, int var3, boolean var4, int var5, int var6);

        public long callPJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public long callPPJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public long callPJJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7);

        public long callPJJJ(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        public long callPN(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        @FFMPointer
        public long callP(MemorySegment var1);

        @FFMPointer
        public long callP(MemorySegment var1, int var2);

        @FFMPointer
        public long callP(MemorySegment var1, int var2, int var3);

        @FFMPointer
        public long callP(MemorySegment var1, int var2, float var3, float var4, float var5);

        @FFMPointer
        public long callJP(MemorySegment var1, long var2);

        @FFMPointer
        public long callPP(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        @FFMPointer
        public long callPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        @FFMPointer
        public long callPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        @FFMPointer
        public long callPP(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long callPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5);

        @FFMPointer
        public long callPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        @FFMPointer
        public long callPP(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6);

        @FFMPointer
        public long callJJP(MemorySegment var1, long var2, long var4);

        @FFMPointer
        public long callPNP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        @FFMPointer
        public long callPPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        @FFMPointer
        public long callPPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        @FFMPointer
        public long callPPP(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8);

        @FFMPointer
        public long callPPNP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        @FFMPointer
        public long callPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8);

        @FFMPointer
        public long callPPPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long callPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9);

        @FFMPointer
        public long callPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callJJPPP(MemorySegment var1, long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long callPPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long callPPNPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long callPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        @FFMPointer
        public long callPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        @FFMPointer
        public long callPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        @FFMPointer
        public long callPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long callPJPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long callPPPJPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, @FFMNullable @FFMPointer long var10);

        @FFMPointer
        public long callPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        @FFMPointer
        public long callPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11);

        @FFMPointer
        public long callPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        @FFMPointer
        public long callPJJPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long callPJPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long callPPJPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long callPPPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        @FFMPointer
        public long callPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        @FFMPointer
        public long callPPJPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        @FFMPointer
        public long callPPPPJPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        @FFMPointer
        public long callPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        @FFMPointer
        public long callPPPPPPPP(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long callPJPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        @FFMPointer
        public long callPPJPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18);

        @FFMPointer
        public long callPJPPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        @FFMPointer
        public long callPPJPPPPPPPP(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15, int var17, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20, @FFMNullable @FFMPointer long var22);

        public short callS(MemorySegment var1, int var2);

        public short callPS(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public short callPCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public short callPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public short callPSS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4);

        public short callSPS(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3);

        public short callPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public short callPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public short callPCPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5);

        public short callPPCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6);

        public short callPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public short callPPSS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6);

        public short callPSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5);

        public short callSPPS(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public short callSPSS(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5);

        public short callPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8);

        public short callPJCCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, short var6, short var7);

        public short callPPSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7);

        public short callPSSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, @FFMNullable @FFMPointer long var6, int var8);

        public short callPPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        public short callPCPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public short callPCPSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7, @FFMNullable @FFMPointer long var8);

        public short callPSSPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public short callPCPPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public short callPCSPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public short callPPSPSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10);

        public short callPCCPSPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, @FFMNullable @FFMPointer long var6, short var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public short callPPSPSPSS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10, short var12);

        public short callSPSSPSPS(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10);

        public short callPCPSPPSPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, short var12, @FFMNullable @FFMPointer long var13);

        public short callPPPSPSPCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, short var8, @FFMNullable @FFMPointer long var9, short var11, @FFMNullable @FFMPointer long var12, short var14);

        public short callSPSPPPSPS(MemorySegment var1, short var2, @FFMNullable @FFMPointer long var3, short var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, short var12, @FFMNullable @FFMPointer long var13);

        public short callPCPSPPPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16);

        public short callPPSPSPSCCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10, short var12, short var13, short var14);

        public short callPPSPSPSPSS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10, short var12, @FFMNullable @FFMPointer long var13, short var15);

        public short callPCPSPSPSCCS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7, @FFMNullable @FFMPointer long var8, short var10, @FFMNullable @FFMPointer long var11, short var13, short var14, short var15);

        public short callPCSSSPSPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, short var7, @FFMNullable @FFMPointer long var8, short var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public short callPSSSPSSPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, short var5, short var6, @FFMNullable @FFMPointer long var7, short var9, short var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13, @FFMNullable @FFMPointer long var15);

        public short callPSPSPPPPPPPS(MemorySegment var1, @FFMNullable @FFMPointer long var2, short var4, @FFMNullable @FFMPointer long var5, short var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18, @FFMNullable @FFMPointer long var20);

        public short callPPSPSPSPSPSPSS(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, short var6, @FFMNullable @FFMPointer long var7, short var9, @FFMNullable @FFMPointer long var10, short var12, @FFMNullable @FFMPointer long var13, short var15, @FFMNullable @FFMPointer long var16, short var18, @FFMNullable @FFMPointer long var19, short var21);

        public void callV(MemorySegment var1);

        public void callV(MemorySegment var1, double var2);

        public void callV(MemorySegment var1, float var2);

        public void callV(MemorySegment var1, int var2);

        public void callV(MemorySegment var1, boolean var2);

        public void callV(MemorySegment var1, double var2, double var4);

        public void callV(MemorySegment var1, float var2, float var3);

        public void callV(MemorySegment var1, float var2, boolean var3);

        public void callV(MemorySegment var1, int var2, double var3);

        public void callV(MemorySegment var1, int var2, float var3);

        public void callV(MemorySegment var1, int var2, int var3);

        public void callV(MemorySegment var1, int var2, boolean var3);

        public void callV(MemorySegment var1, double var2, double var4, double var6);

        public void callV(MemorySegment var1, float var2, float var3, float var4);

        public void callV(MemorySegment var1, int var2, double var3, double var5);

        public void callV(MemorySegment var1, int var2, float var3, float var4);

        public void callV(MemorySegment var1, int var2, int var3, double var4);

        public void callV(MemorySegment var1, int var2, int var3, float var4);

        public void callV(MemorySegment var1, int var2, int var3, int var4);

        public void callV(MemorySegment var1, int var2, int var3, boolean var4);

        public void callV(MemorySegment var1, double var2, double var4, double var6, double var8);

        public void callV(MemorySegment var1, float var2, float var3, float var4, float var5);

        public void callV(MemorySegment var1, int var2, double var3, double var5, double var7);

        public void callV(MemorySegment var1, int var2, float var3, float var4, float var5);

        public void callV(MemorySegment var1, int var2, int var3, double var4, double var6);

        public void callV(MemorySegment var1, int var2, int var3, float var4, float var5);

        public void callV(MemorySegment var1, int var2, int var3, float var4, int var5);

        public void callV(MemorySegment var1, int var2, int var3, int var4, double var5);

        public void callV(MemorySegment var1, int var2, int var3, int var4, float var5);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5);

        public void callV(MemorySegment var1, int var2, int var3, int var4, boolean var5);

        public void callV(MemorySegment var1, int var2, int var3, boolean var4, int var5);

        public void callV(MemorySegment var1, boolean var2, boolean var3, boolean var4, boolean var5);

        public void callV(MemorySegment var1, int var2, double var3, double var5, double var7, double var9);

        public void callV(MemorySegment var1, int var2, float var3, float var4, float var5, float var6);

        public void callV(MemorySegment var1, int var2, int var3, double var4, double var6, double var8);

        public void callV(MemorySegment var1, int var2, int var3, float var4, float var5, float var6);

        public void callV(MemorySegment var1, int var2, int var3, int var4, float var5, int var6);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6);

        public void callV(MemorySegment var1, int var2, int var3, int var4, boolean var5, int var6);

        public void callV(MemorySegment var1, int var2, boolean var3, boolean var4, boolean var5, boolean var6);

        public void callV(MemorySegment var1, double var2, double var4, double var6, double var8, double var10, double var12);

        public void callV(MemorySegment var1, int var2, double var3, double var5, int var7, double var8, double var10);

        public void callV(MemorySegment var1, int var2, float var3, float var4, int var5, float var6, float var7);

        public void callV(MemorySegment var1, int var2, int var3, double var4, double var6, double var8, double var10);

        public void callV(MemorySegment var1, int var2, int var3, float var4, float var5, float var6, float var7);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, boolean var7);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, boolean var6, int var7);

        public void callV(MemorySegment var1, int var2, double var3, double var5, double var7, double var9, double var11, double var13);

        public void callV(MemorySegment var1, int var2, int var3, int var4, double var5, double var7, double var9, double var11);

        public void callV(MemorySegment var1, int var2, int var3, int var4, float var5, float var6, float var7, float var8);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8);

        public void callV(MemorySegment var1, int var2, int var3, int var4, boolean var5, int var6, int var7, int var8);

        public void callV(MemorySegment var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9);

        public void callV(MemorySegment var1, int var2, int var3, int var4, float var5, float var6, float var7, float var8, float var9);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, boolean var10);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11);

        public void callV(MemorySegment var1, int var2, int var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16);

        public void callV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18);

        public void callJV(MemorySegment var1, long var2);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public void callSV(MemorySegment var1, short var2);

        public void callUV(MemorySegment var1, byte var2);

        public void callCV(MemorySegment var1, int var2, short var3);

        public void callJV(MemorySegment var1, int var2, long var3);

        public void callJV(MemorySegment var1, long var2, int var4);

        public void callPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4);

        public void callSV(MemorySegment var1, int var2, short var3);

        public void callJV(MemorySegment var1, int var2, int var3, long var4);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4);

        public void callPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5);

        public void callJV(MemorySegment var1, int var2, long var3, int var5, int var6);

        public void callNV(MemorySegment var1, long var2, int var4, int var5, int var6);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6);

        public void callPV(MemorySegment var1, int var2, int var3, boolean var4, @FFMNullable @FFMPointer long var5);

        public void callPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, int var6);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, float var4, float var5, float var6);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, boolean var7);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, boolean var5, @FFMNullable @FFMPointer long var6);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public void callPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, int var6, int var7);

        public void callPV(MemorySegment var1, int var2, boolean var3, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, long var7);

        public void callPV(MemorySegment var1, int var2, double var3, double var5, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPV(MemorySegment var1, int var2, float var3, float var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, boolean var8);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, boolean var5, int var6, @FFMNullable @FFMPointer long var7);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8);

        public void callPV(MemorySegment var1, int var2, boolean var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, long var8);

        public void callPV(MemorySegment var1, int var2, int var3, float var4, float var5, float var6, float var7, @FFMNullable @FFMPointer long var8);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, int var9);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, long var9);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, boolean var7, int var8, long var9);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, boolean var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, int var9, long var10);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, int var10, float var11);

        public void callPV(MemorySegment var1, int var2, double var3, double var5, int var7, int var8, double var9, double var11, int var13, int var14, @FFMNullable @FFMPointer long var15);

        public void callPV(MemorySegment var1, int var2, float var3, float var4, int var5, int var6, float var7, float var8, int var9, int var10, @FFMNullable @FFMPointer long var11);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11);

        public void callJV(MemorySegment var1, long var2, int var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, @FFMNullable @FFMPointer long var12);

        public void callPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13);

        public void callJV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, long var12, boolean var14);

        public void callPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, @FFMNullable @FFMPointer long var13);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4);

        public void callSSV(MemorySegment var1, short var2, short var3);

        public void callJJV(MemorySegment var1, int var2, long var3, long var5);

        public void callPCV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, short var5);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, float var6);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6);

        public void callSSV(MemorySegment var1, int var2, short var3, short var4);

        public void callJJV(MemorySegment var1, int var2, int var3, long var4, long var6);

        public void callJPV(MemorySegment var1, int var2, int var3, long var4, @FFMNullable @FFMPointer long var6);

        public void callJPV(MemorySegment var1, int var2, long var3, int var5, @FFMNullable @FFMPointer long var6);

        public void callPJV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, long var6);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, int var7);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, boolean var7);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7);

        public void callPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, int var8);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, int var8);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, int var8);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, @FFMNullable @FFMPointer long var8);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9);

        public void callPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9);

        public void callPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, int var10);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, float var8, float var9, int var10, @FFMNullable @FFMPointer long var11);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, int var10, @FFMNullable @FFMPointer long var11);

        public void callPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9, int var11, int var12, float var13);

        public void callBBBV(MemorySegment var1, byte var2, byte var3, byte var4);

        public void callCCCV(MemorySegment var1, short var2, short var3, short var4);

        public void callPJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6);

        public void callPJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6);

        public void callPPNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6);

        public void callSSSV(MemorySegment var1, short var2, short var3, short var4);

        public void callUUUV(MemorySegment var1, byte var2, byte var3, byte var4);

        public void callJJJV(MemorySegment var1, int var2, long var3, long var5, long var7);

        public void callPJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8);

        public void callPJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7);

        public void callPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8);

        public void callSSSV(MemorySegment var1, int var2, short var3, short var4, short var5);

        public void callJJJV(MemorySegment var1, int var2, int var3, long var4, long var6, long var8);

        public void callPJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, long var7, int var9);

        public void callPJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, int var9);

        public void callPPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8);

        public void callPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, int var9);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8);

        public void callPJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9);

        public void callPPJV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, int var7, long var8, boolean var10);

        public void callPPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, long var8, int var10);

        public void callPPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void callPPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, int var6, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void callPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9);

        public void callPJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, long var8, int var10, int var11);

        public void callPPPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void callPPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPPPV(MemorySegment var1, int var2, int var3, int var4, int var5, @FFMNullable @FFMPointer long var6, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void callPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, int var6, int var7, int var8, int var9, int var10, int var11, @FFMNullable @FFMPointer long var12, int var14, int var15, int var16, int var17, int var18, int var19, int var20, int var21, int var22);

        public void callBBBBV(MemorySegment var1, byte var2, byte var3, byte var4, byte var5);

        public void callCCCCV(MemorySegment var1, short var2, short var3, short var4, short var5);

        public void callPJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, @FFMNullable @FFMPointer long var8);

        public void callPJPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callPPPNV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, long var8);

        public void callPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8);

        public void callSSSSV(MemorySegment var1, short var2, short var3, short var4, short var5);

        public void callUUUUV(MemorySegment var1, byte var2, byte var3, byte var4, byte var5);

        public void callJJJJV(MemorySegment var1, int var2, long var3, long var5, long var7, long var9);

        public void callPJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, int var10);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, long var7, @FFMNullable @FFMPointer long var9);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, @FFMNullable @FFMPointer long var9);

        public void callPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9);

        public void callPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, int var10);

        public void callSSSSV(MemorySegment var1, int var2, short var3, short var4, short var5, short var6);

        public void callUUUUV(MemorySegment var1, int var2, byte var3, byte var4, byte var5, byte var6);

        public void callJJJJV(MemorySegment var1, int var2, int var3, long var4, long var6, long var8, long var10);

        public void callPJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, int var10, int var11);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, long var7, int var9, @FFMNullable @FFMPointer long var10);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, int var8, int var9, @FFMNullable @FFMPointer long var10);

        public void callPJPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, @FFMNullable @FFMPointer long var7, int var9, @FFMNullable @FFMPointer long var10);

        public void callPPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void callPPPPV(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, int var11);

        public void callPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, long var8, int var10, @FFMNullable @FFMPointer long var11);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, long var7, int var9, int var10, @FFMNullable @FFMPointer long var11);

        public void callPJPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, int var7, int var8, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void callPPPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void callPPPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12);

        public void callPJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, long var7, int var9, int var10, @FFMNullable @FFMPointer long var11, int var13);

        public void callPJPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, long var5, int var7, int var8, @FFMNullable @FFMPointer long var9, int var11, @FFMNullable @FFMPointer long var12);

        public void callPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, int var6, int var7, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, int var13, @FFMNullable @FFMPointer long var14);

        public void callPJJJPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, @FFMNullable @FFMPointer long var10);

        public void callPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, @FFMNullable @FFMPointer long var7, @FFMNullable @FFMPointer long var9, @FFMNullable @FFMPointer long var11);

        public void callPJJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, long var10, int var12, int var13);

        public void callPPPPPV(MemorySegment var1, int var2, int var3, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public void callPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);

        public void callPJJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, int var6, int var7, long var8, long var10, long var12, int var14);

        public void callPJPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, int var5, long var6, @FFMNullable @FFMPointer long var8, int var10, @FFMNullable @FFMPointer long var11, @FFMNullable @FFMPointer long var13);

        public void callPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, int var12, int var13, int var14);

        public void callPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, int var4, @FFMNullable @FFMPointer long var5, int var7, int var8, int var9, @FFMNullable @FFMPointer long var10, int var12, @FFMNullable @FFMPointer long var13, int var15, @FFMNullable @FFMPointer long var16);

        public void callPPPPPJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, long var12);

        public void callPPPPPPV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, @FFMNullable @FFMPointer long var6, int var8, int var9, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14);

        public void callPPPPPPPV(MemorySegment var1, int var2, int var3, int var4, @FFMNullable @FFMPointer long var5, int var7, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12, @FFMNullable @FFMPointer long var14, @FFMNullable @FFMPointer long var16, @FFMNullable @FFMPointer long var18);

        public void callPPJJJJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, @FFMNullable @FFMPointer long var4, long var6, long var8, int var10, long var11, long var13, long var15, long var17);

        public void callPJJJJJJJJJJJV(MemorySegment var1, @FFMNullable @FFMPointer long var2, long var4, long var6, long var8, long var10, long var12, long var14, long var16, long var18, long var20, long var22, long var24, int var26, int var27, int var28);

        public boolean callZ(MemorySegment var1, int var2);

        public boolean callZ(MemorySegment var1, int var2, int var3);

        public boolean callZ(MemorySegment var1, int var2, float var3, float var4);

        public boolean callZ(MemorySegment var1, int var2, int var3, float var4, float var5);

        public boolean callJZ(MemorySegment var1, long var2);

        public boolean callPZ(MemorySegment var1, @FFMNullable @FFMPointer long var2);

        public boolean callJZ(MemorySegment var1, int var2, long var3);

        public boolean callPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3);

        public boolean callJZ(MemorySegment var1, int var2, long var3, int var5);

        public boolean callPPZ(MemorySegment var1, int var2, @FFMNullable @FFMPointer long var3, @FFMNullable @FFMPointer long var5);

        public boolean callPPPPZ(MemorySegment var1, int var2, int var3, int var4, float var5, @FFMNullable @FFMPointer long var6, @FFMNullable @FFMPointer long var8, @FFMNullable @FFMPointer long var10, @FFMNullable @FFMPointer long var12);
    }
}

