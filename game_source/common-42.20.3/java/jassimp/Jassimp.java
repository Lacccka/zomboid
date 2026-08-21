/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiBuiltInWrapperProvider;
import jassimp.AiIOSystem;
import jassimp.AiPostProcessSteps;
import jassimp.AiProgressHandler;
import jassimp.AiScene;
import jassimp.AiWrapperProvider;
import jassimp.JassimpLibraryLoader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

public final class Jassimp {
    private static AiWrapperProvider<?, ?, ?, ?, ?> s_wrapperProvider = new AiBuiltInWrapperProvider();
    private static JassimpLibraryLoader s_libraryLoader = new JassimpLibraryLoader();
    private static volatile boolean s_libraryLoaded = false;
    private static final Object s_libraryLoadingLock = new Object();
    public static final AiWrapperProvider<?, ?, ?, ?, ?> BUILTIN = new AiBuiltInWrapperProvider();
    public static int NATIVE_AIVEKTORKEY_SIZE;
    public static int NATIVE_AIQUATKEY_SIZE;
    public static int NATIVE_AIVEKTOR3D_SIZE;
    public static int NATIVE_FLOAT_SIZE;
    public static int NATIVE_INT_SIZE;
    public static int NATIVE_UINT_SIZE;
    public static int NATIVE_DOUBLE_SIZE;
    public static int NATIVE_LONG_SIZE;

    private static native AiScene aiImportFile(String var0, long var1, AiIOSystem<?> var3, AiProgressHandler var4) throws IOException;

    public static AiScene importFile(String string) throws IOException {
        return Jassimp.importFile(string, EnumSet.noneOf(AiPostProcessSteps.class));
    }

    public static AiScene importFile(String string, AiIOSystem<?> aiIOSystem) throws IOException {
        return Jassimp.importFile(string, EnumSet.noneOf(AiPostProcessSteps.class), aiIOSystem);
    }

    public static AiScene importFile(String string, Set<AiPostProcessSteps> set) throws IOException {
        return Jassimp.importFile(string, set, null);
    }

    public static AiScene importFile(String string, Set<AiPostProcessSteps> set, AiIOSystem<?> aiIOSystem) throws IOException {
        return Jassimp.importFile(string, set, aiIOSystem, null);
    }

    public static AiScene importFile(String string, Set<AiPostProcessSteps> set, AiIOSystem<?> aiIOSystem, AiProgressHandler aiProgressHandler) throws IOException {
        Jassimp.loadLibrary();
        return Jassimp.aiImportFile(string, AiPostProcessSteps.toRawValue(set), aiIOSystem, aiProgressHandler);
    }

    public static native int getVKeysize();

    public static native int getQKeysize();

    public static native int getV3Dsize();

    public static native int getfloatsize();

    public static native int getintsize();

    public static native int getuintsize();

    public static native int getdoublesize();

    public static native int getlongsize();

    public static native String getErrorString();

    public static AiWrapperProvider<?, ?, ?, ?, ?> getWrapperProvider() {
        return s_wrapperProvider;
    }

    public static void setWrapperProvider(AiWrapperProvider<?, ?, ?, ?, ?> aiWrapperProvider) {
        s_wrapperProvider = aiWrapperProvider;
    }

    public static void setLibraryLoader(JassimpLibraryLoader jassimpLibraryLoader) {
        s_libraryLoader = jassimpLibraryLoader;
    }

    static Object wrapMatrix(float[] fArray) {
        return s_wrapperProvider.wrapMatrix4f(fArray);
    }

    static Object wrapColor3(float f, float f2, float f3) {
        return Jassimp.wrapColor4(f, f2, f3, 1.0f);
    }

    static Object wrapColor4(float f, float f2, float f3, float f4) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putFloat(f);
        byteBuffer.putFloat(f2);
        byteBuffer.putFloat(f3);
        byteBuffer.putFloat(f4);
        byteBuffer.flip();
        return s_wrapperProvider.wrapColor(byteBuffer, 0);
    }

    static Object wrapVec3(float f, float f2, float f3) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.putFloat(f);
        byteBuffer.putFloat(f2);
        byteBuffer.putFloat(f3);
        byteBuffer.flip();
        return s_wrapperProvider.wrapVector3f(byteBuffer, 0, 3);
    }

    static Object wrapSceneNode(Object object, Object object2, int[] nArray, String string) {
        return s_wrapperProvider.wrapSceneNode(object, object2, nArray, string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadLibrary() {
        if (!s_libraryLoaded) {
            Object object = s_libraryLoadingLock;
            synchronized (object) {
                if (!s_libraryLoaded) {
                    s_libraryLoader.loadLibrary();
                    NATIVE_AIVEKTORKEY_SIZE = Jassimp.getVKeysize();
                    NATIVE_AIQUATKEY_SIZE = Jassimp.getQKeysize();
                    NATIVE_AIVEKTOR3D_SIZE = Jassimp.getV3Dsize();
                    NATIVE_FLOAT_SIZE = Jassimp.getfloatsize();
                    NATIVE_INT_SIZE = Jassimp.getintsize();
                    NATIVE_UINT_SIZE = Jassimp.getuintsize();
                    NATIVE_DOUBLE_SIZE = Jassimp.getdoublesize();
                    NATIVE_LONG_SIZE = Jassimp.getlongsize();
                    s_libraryLoaded = true;
                }
            }
        }
    }

    private Jassimp() {
    }
}

