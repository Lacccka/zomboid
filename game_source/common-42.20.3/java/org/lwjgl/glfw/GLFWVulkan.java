/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.vulkan.VK
 *  org.lwjgl.vulkan.VkAllocationCallbacks
 *  org.lwjgl.vulkan.VkInstance
 *  org.lwjgl.vulkan.VkPhysicalDevice
 */
package org.lwjgl.glfw;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class GLFWVulkan {
    protected GLFWVulkan() {
        throw new UnsupportedOperationException();
    }

    public static void glfwInitVulkanLoader(@NativeType(value="PFN_vkGetInstanceProcAddr") long loader) {
        long __functionAddress = Functions.InitVulkanLoader;
        JNI.invokePV(loader, __functionAddress);
    }

    @NativeType(value="int")
    public static boolean glfwVulkanSupported() {
        long __functionAddress = Functions.VulkanSupported;
        return JNI.invokeI(__functionAddress) != 0;
    }

    public static long nglfwGetRequiredInstanceExtensions(long count) {
        long __functionAddress = Functions.GetRequiredInstanceExtensions;
        return JNI.invokePP(count, __functionAddress);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const **")
    public static @Nullable PointerBuffer glfwGetRequiredInstanceExtensions() {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        IntBuffer count = stack.callocInt(1);
        try {
            long __result = GLFWVulkan.nglfwGetRequiredInstanceExtensions(MemoryUtil.memAddress(count));
            PointerBuffer pointerBuffer = MemoryUtil.memPointerBufferSafe(__result, count.get(0));
            return pointerBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static long nglfwGetInstanceProcAddress(long instance, long procname) {
        long __functionAddress = Functions.GetInstanceProcAddress;
        return JNI.invokePPP(instance, procname, __functionAddress);
    }

    @NativeType(value="GLFWvkproc")
    public static long glfwGetInstanceProcAddress(@NativeType(value="VkInstance") @Nullable VkInstance instance, @NativeType(value="char const *") ByteBuffer procname) {
        if (Checks.CHECKS) {
            Checks.checkNT1(procname);
        }
        return GLFWVulkan.nglfwGetInstanceProcAddress(MemoryUtil.memAddressSafe((Pointer)instance), MemoryUtil.memAddress(procname));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="GLFWvkproc")
    public static long glfwGetInstanceProcAddress(@NativeType(value="VkInstance") @Nullable VkInstance instance, @NativeType(value="char const *") CharSequence procname) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(procname, true);
            long procnameEncoded = stack.getPointerAddress();
            long l = GLFWVulkan.nglfwGetInstanceProcAddress(MemoryUtil.memAddressSafe((Pointer)instance), procnameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="int")
    public static boolean glfwGetPhysicalDevicePresentationSupport(VkInstance instance, VkPhysicalDevice device, @NativeType(value="uint32_t") int queuefamily) {
        long __functionAddress = Functions.GetPhysicalDevicePresentationSupport;
        return JNI.invokePPI(instance.address(), device.address(), queuefamily, __functionAddress) != 0;
    }

    public static int nglfwCreateWindowSurface(long instance, long window, long allocator, long surface) {
        long __functionAddress = Functions.CreateWindowSurface;
        if (Checks.CHECKS) {
            Checks.check(window);
        }
        return JNI.invokePPPPI(instance, window, allocator, surface, __functionAddress);
    }

    @NativeType(value="VkResult")
    public static int glfwCreateWindowSurface(VkInstance instance, @NativeType(value="GLFWwindow *") long window, @NativeType(value="VkAllocationCallbacks const *") @Nullable VkAllocationCallbacks allocator, @NativeType(value="VkSurfaceKHR *") LongBuffer surface) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)surface, 1);
        }
        return GLFWVulkan.nglfwCreateWindowSurface(instance.address(), window, MemoryUtil.memAddressSafe((Pointer)allocator), MemoryUtil.memAddress(surface));
    }

    @NativeType(value="VkResult")
    public static int glfwCreateWindowSurface(VkInstance instance, @NativeType(value="GLFWwindow *") long window, @NativeType(value="VkAllocationCallbacks const *") @Nullable VkAllocationCallbacks allocator, @NativeType(value="VkSurfaceKHR *") long[] surface) {
        long __functionAddress = Functions.CreateWindowSurface;
        if (Checks.CHECKS) {
            Checks.check(window);
            Checks.check(surface, 1);
        }
        return JNI.invokePPPPI(instance.address(), window, MemoryUtil.memAddressSafe((Pointer)allocator), surface, __functionAddress);
    }

    public static void setPath(FunctionProvider sharedLibrary) {
        if (!(sharedLibrary instanceof SharedLibrary)) {
            APIUtil.apiLog("GLFW Vulkan path override not set: function provider is not a shared library.");
            return;
        }
        String path = ((SharedLibrary)sharedLibrary).getPath();
        if (path == null) {
            APIUtil.apiLog("GLFW Vulkan path override not set: Could not resolve the shared library path.");
            return;
        }
        GLFWVulkan.setPath(path);
    }

    public static void setPath(@Nullable String path) {
        long override = GLFW.getLibrary().getFunctionAddress("_glfw_vulkan_library");
        if (override == 0L) {
            APIUtil.apiLog("GLFW Vulkan path override not set: Could not resolve override symbol.");
            return;
        }
        long a = MemoryUtil.memGetAddress(override);
        if (a != 0L) {
            MemoryUtil.nmemFree(a);
        }
        MemoryUtil.memPutAddress(override, path == null ? 0L : MemoryUtil.memAddress(MemoryUtil.memUTF8(path)));
    }

    static {
        if (Platform.get() == Platform.MACOSX) {
            GLFWVulkan.setPath(VK.getFunctionProvider());
        }
    }

    public static final class Functions {
        public static final long InitVulkanLoader = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwInitVulkanLoader");
        public static final long VulkanSupported = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwVulkanSupported");
        public static final long GetRequiredInstanceExtensions = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetRequiredInstanceExtensions");
        public static final long GetInstanceProcAddress = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetInstanceProcAddress");
        public static final long GetPhysicalDevicePresentationSupport = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetPhysicalDevicePresentationSupport");
        public static final long CreateWindowSurface = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwCreateWindowSurface");

        private Functions() {
        }
    }
}

