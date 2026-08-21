/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.glfw;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.SharedLibrary;

public class GLFWNativeGLX {
    protected GLFWNativeGLX() {
        throw new UnsupportedOperationException();
    }

    @NativeType(value="GLXContext")
    public static long glfwGetGLXContext(@NativeType(value="GLFWwindow *") long window) {
        long __functionAddress = Functions.GetGLXContext;
        if (Checks.CHECKS) {
            Checks.check(window);
        }
        return JNI.invokePP(window, __functionAddress);
    }

    @NativeType(value="GLXWindow")
    public static long glfwGetGLXWindow(@NativeType(value="GLFWwindow *") long window) {
        long __functionAddress = Functions.GetGLXWindow;
        if (Checks.CHECKS) {
            Checks.check(window);
        }
        return JNI.invokePP(window, __functionAddress);
    }

    public static int nglfwGetGLXFBConfig(long window, long config) {
        long __functionAddress = Functions.GetGLXFBConfig;
        if (Checks.CHECKS) {
            Checks.check(window);
        }
        return JNI.invokePPI(window, config, __functionAddress);
    }

    @NativeType(value="int")
    public static boolean glfwGetGLXFBConfig(@NativeType(value="GLFWwindow *") long window, @NativeType(value="GLXFBConfig *") PointerBuffer config) {
        if (Checks.CHECKS) {
            Checks.check(config, 1);
        }
        return GLFWNativeGLX.nglfwGetGLXFBConfig(window, MemoryUtil.memAddress(config)) != 0;
    }

    public static void setPath(FunctionProvider sharedLibrary) {
        if (!(sharedLibrary instanceof SharedLibrary)) {
            APIUtil.apiLog("GLFW OpenGL path override not set: Function provider is not a shared library.");
            return;
        }
        String path = ((SharedLibrary)sharedLibrary).getPath();
        if (path == null) {
            APIUtil.apiLog("GLFW OpenGL path override not set: Could not resolve the shared library path.");
            return;
        }
        GLFWNativeGLX.setPath(path);
    }

    public static void setPath(@Nullable String path) {
        long override = GLFW.getLibrary().getFunctionAddress("_glfw_opengl_library");
        if (override == 0L) {
            APIUtil.apiLog("GLFW OpenGL path override not set: Could not resolve override symbol.");
            return;
        }
        long a = MemoryUtil.memGetAddress(override);
        if (a != 0L) {
            MemoryUtil.nmemFree(a);
        }
        MemoryUtil.memPutAddress(override, path == null ? 0L : MemoryUtil.memAddress(MemoryUtil.memUTF8(path)));
    }

    public static final class Functions {
        public static final long GetGLXContext = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetGLXContext");
        public static final long GetGLXWindow = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetGLXWindow");
        public static final long GetGLXFBConfig = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetGLXFBConfig");

        private Functions() {
        }
    }
}

