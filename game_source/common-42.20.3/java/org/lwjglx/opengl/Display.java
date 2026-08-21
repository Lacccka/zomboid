/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.opengl;

import imgui.ImDrawData;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import java.awt.Canvas;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjglx.LWJGLException;
import org.lwjglx.LWJGLUtil;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.DisplayMode;
import org.lwjglx.opengl.PixelFormat;
import org.lwjglx.opengl.Sync;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.Clipboard;
import zombie.core.Core;
import zombie.core.SpriteRenderer;
import zombie.core.math.PZMath;
import zombie.core.opengl.RenderThread;
import zombie.debug.DebugLog;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.ui.UIManager;

public class Display {
    private static String windowTitle = "Game";
    private static boolean displayCreated;
    private static boolean displayFocused;
    private static boolean displayVisible;
    private static boolean displayDirty;
    private static boolean displayResizable;
    private static boolean vsyncEnabled;
    private static DisplayMode gameWindowMode;
    private static DisplayMode desktopDisplayMode;
    private static int displayX;
    private static int displayY;
    private static boolean displayResized;
    private static int displayWidth;
    private static int displayHeight;
    private static int displayFramebufferWidth;
    private static int displayFramebufferHeight;
    private static GLFWImage.Buffer displayIcons;
    private static long monitor;
    private static boolean isBorderlessWindow;
    private static boolean latestResized;
    private static int latestWidth;
    private static int latestHeight;
    public static GLCapabilities capabilities;
    public static ImGuiImplGlfw imGuiGlfw;
    public static ImGuiImplGl3 imGuiGl3;
    private static ImPlotContext imPlotContext;
    private static final double[] mouseCursorPosX;
    private static final double[] mouseCursorPosY;
    private static int mouseCursorState;
    static int frameCount;

    public static void init() {
        if (LWJGLUtil.getPlatform() == 1) {
            if ("1".equals(System.getProperty("zomboid.wayland")) && GLFW.glfwPlatformSupported(393219)) {
                GLFW.glfwInitHint(327683, 393219);
            } else {
                GLFW.glfwInitHint(327683, 393220);
            }
        }
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        if (GLFW.glfwGetPlatform() != 393219) {
            Keyboard.create();
        }
        monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(monitor);
        int monitorWidth = vidmode.width();
        int monitorHeight = vidmode.height();
        int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
        int monitorRefreshRate = vidmode.refreshRate();
        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
    }

    public static void create(PixelFormat pixel_format) throws LWJGLException {
        GLFW.glfwWindowHint(135178, pixel_format.getAccumulationBitsPerPixel());
        GLFW.glfwWindowHint(135172, pixel_format.getAlphaBits());
        GLFW.glfwWindowHint(135179, pixel_format.getAuxBuffers());
        GLFW.glfwWindowHint(135173, pixel_format.getDepthBits());
        GLFW.glfwWindowHint(135181, pixel_format.getSamples());
        GLFW.glfwWindowHint(135174, pixel_format.getStencilBits());
        Display.create();
        if (GLFW.glfwGetPlatform() == 393219) {
            Keyboard.create();
        }
    }

    public static void create() throws LWJGLException {
        if (Window.handle != 0L) {
            GLFW.glfwDestroyWindow(Window.handle);
        }
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(monitor);
        int monitorWidth = vidmode.width();
        int monitorHeight = vidmode.height();
        int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
        int monitorRefreshRate = vidmode.refreshRate();
        desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(139265, 196609);
        Callbacks.errorCallback = GLFWErrorCallback.createPrint(System.err);
        GLFW.glfwSetErrorCallback(Callbacks.errorCallback);
        if (Core.debug) {
            GLFW.glfwWindowHint(139271, 1);
        }
        GLFW.glfwWindowHint(131076, 0);
        GLFW.glfwWindowHint(131075, displayResizable ? 1 : 0);
        if (LWJGLUtil.getPlatform() == 2) {
            GLFW.glfwWindowHint(143361, 0);
        }
        boolean bDebug = Core.debug && "true".equalsIgnoreCase(System.getProperty("org.lwjgl.util.Debug"));
        GLFW.glfwWindowHint(139271, bDebug ? 1 : 0);
        Window.handle = GLFW.glfwCreateWindow(gameWindowMode.getWidth(), gameWindowMode.getHeight(), windowTitle, 0L, 0L);
        if (Window.handle == 0L) {
            throw new IllegalStateException("Failed to create Display window");
        }
        if (GLFW.glfwGetPlatform() != 393218 && GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetWindowIcon(Window.handle, displayIcons);
        }
        Callbacks.noise = bDebug;
        Callbacks.initCallbacks();
        Display.calcWindowPos(Display.isBorderlessWindow() || Display.isFullscreen());
        GLFW.glfwSetWindowPos(Window.handle, displayX, displayY);
        GLFW.glfwShowWindow(Window.handle);
        GLFW.glfwMakeContextCurrent(Window.handle);
        capabilities = GL.createCapabilities();
        GLFW.glfwSwapInterval(0);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(16640);
        GLFW.glfwSwapBuffers(Window.handle);
        Display.setVSyncEnabled(vsyncEnabled);
        if (bDebug && Display.capabilities.OpenGL43) {
            int[] ids = new int[]{131185};
            GL43.glDebugMessageControl(33350, 33361, 4352, ids, false);
        }
        int[] _width = new int[1];
        int[] _height = new int[1];
        GLFW.glfwGetWindowSize(Window.handle, _width, _height);
        displayWidth = latestWidth = _width[0];
        displayHeight = latestHeight = _height[0];
        displayCreated = true;
        if (Core.isImGui()) {
            imGuiGl3 = new ImGuiImplGl3();
            imGuiGlfw = new ImGuiImplGlfw();
            ImGui.createContext();
            imPlotContext = ImPlot.createContext();
            ImGuiIO io = ImGui.getIO();
            if (Core.isUseViewports()) {
                io.addConfigFlags(1024);
            }
            io.addConfigFlags(64);
            io.addConfigFlags(32768);
            io.addConfigFlags(16384);
            String glslVersion = null;
            if (GLFW.glfwGetPlatform() == 393218) {
                glslVersion = "#version 120";
            }
            imGuiGl3.init(glslVersion);
            imGuiGlfw.init(Window.handle, true);
        }
    }

    public static boolean isCreated() {
        return displayCreated;
    }

    public static boolean isActive() {
        return displayFocused;
    }

    public static boolean isVisible() {
        return displayVisible;
    }

    public static void setLocation(int new_x, int new_y) {
        System.out.println("TODO: Implement Display.setLocation(int, int)");
    }

    public static void setVSyncEnabled(boolean sync) {
        vsyncEnabled = sync;
        if (sync) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
    }

    public static long getWindow() {
        return Window.handle;
    }

    public static void update() {
        Display.update(true);
    }

    public static void update(boolean processMessages) {
        try {
            Display.swapBuffers();
            displayDirty = false;
        }
        catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        if (processMessages) {
            Display.processMessages();
        }
    }

    private static void updateMouseCursor() {
        int cursorState = RenderThread.isCursorVisible() ? 212993 : 212994;
        boolean lockCursorToWindow = Core.getInstance().getOptionLockCursorToWindow();
        if (lockCursorToWindow) {
            cursorState = 212995;
        }
        if (mouseCursorState != cursorState) {
            boolean bWasDisabled;
            boolean bl = bWasDisabled = mouseCursorState == 212995;
            if (bWasDisabled) {
                GLFW.glfwGetCursorPos(Display.getWindow(), mouseCursorPosX, mouseCursorPosY);
            }
            mouseCursorState = cursorState;
            GLFW.glfwSetInputMode(Display.getWindow(), 208897, cursorState);
            if (bWasDisabled) {
                GLFW.glfwSetCursorPos(Display.getWindow(), mouseCursorPosX[0], mouseCursorPosY[0]);
            }
        }
        if (lockCursorToWindow) {
            GLFW.glfwGetCursorPos(Display.getWindow(), mouseCursorPosX, mouseCursorPosY);
            int posX = (int)mouseCursorPosX[0];
            int posY = (int)mouseCursorPosY[0];
            Display.mouseCursorPosX[0] = PZMath.clamp((int)mouseCursorPosX[0], 0, Display.getWidth());
            Display.mouseCursorPosY[0] = PZMath.clamp((int)mouseCursorPosY[0], 0, Display.getHeight());
            if (posX != (int)mouseCursorPosX[0] || posY != (int)mouseCursorPosY[0]) {
                GLFW.glfwSetCursorPos(Display.getWindow(), mouseCursorPosX[0], mouseCursorPosY[0]);
            }
        }
    }

    public static void processMessages() {
        GLFW.glfwPollEvents();
        Keyboard.poll();
        Mouse.poll();
        Display.updateMouseCursor();
        if (latestResized) {
            latestResized = false;
            displayResized = true;
            displayWidth = latestWidth;
            displayHeight = latestHeight;
            gameWindowMode = gameWindowMode.getFrequency() > 0 ? new DisplayMode(displayWidth, displayHeight, gameWindowMode.getBitsPerPixel(), gameWindowMode.getFrequency()) : new DisplayMode(displayWidth, displayHeight);
        } else {
            displayResized = false;
        }
    }

    public static void swapBuffers() throws LWJGLException {
        GLFW.glfwSwapBuffers(Window.handle);
    }

    public static void destroy() {
        if (Core.isImGui()) {
            if (imGuiGl3 != null) {
                imGuiGl3.dispose();
                imGuiGlfw.dispose();
            }
            ImPlot.destroyContext(imPlotContext);
            ImGui.destroyContext();
        }
        Callbacks.releaseCallbacks();
        GLFW.glfwDestroyWindow(Window.handle);
        displayCreated = false;
    }

    public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException {
        Display.setDisplayModeAndFullscreenInternal(mode, mode.isFullscreenCapable());
    }

    public static void setFullscreen(boolean fullscreen) {
        Display.setDisplayModeAndFullscreenInternal(gameWindowMode, fullscreen);
    }

    public static boolean isFullscreen() {
        if (!Display.isCreated()) {
            return Core.getInstance().isFullScreen();
        }
        return GLFW.glfwGetWindowMonitor(Window.handle) != 0L;
    }

    public static void setBorderlessWindow(boolean borderless) {
        isBorderlessWindow = borderless;
        if (Display.isCreated()) {
            GLFW.glfwSetWindowAttrib(Display.getWindow(), 131077, borderless ? 0 : 1);
        }
    }

    public static boolean isBorderlessWindow() {
        return isBorderlessWindow;
    }

    public static void setDisplayMode(DisplayMode dm) throws LWJGLException {
        if (dm == null) {
            throw new NullPointerException();
        }
        Display.setDisplayModeAndFullscreenInternal(dm, dm.isFullscreenCapable() && Display.isFullscreen());
    }

    private static int getTargetFrequency(DisplayMode mode) {
        return desktopDisplayMode.getHeight() == mode.getHeight() && desktopDisplayMode.getWidth() == mode.getWidth() ? desktopDisplayMode.getFrequency() : -1;
    }

    private static void setDisplayModeAndFullscreenInternal(DisplayMode mode, boolean fullscreen) {
        boolean wasFullscreen = Display.isFullscreen();
        DisplayMode oldMode = gameWindowMode;
        gameWindowMode = mode;
        Core.setFullScreen(fullscreen);
        if (Display.isCreated() && (wasFullscreen != fullscreen || !gameWindowMode.equals(oldMode))) {
            GLFW.glfwHideWindow(Window.handle);
            Display.calcWindowPos(fullscreen || Display.isBorderlessWindow());
            GLFW.glfwSetWindowMonitor(Window.handle, fullscreen ? monitor : 0L, displayX, displayY, gameWindowMode.getWidth(), gameWindowMode.getHeight(), fullscreen ? Display.getTargetFrequency(mode) : -1);
            if (GLFW.glfwGetPlatform() != 393218 && GLFW.glfwGetPlatform() != 393219) {
                GLFW.glfwSetWindowIcon(Window.handle, displayIcons);
            }
            GLFW.glfwShowWindow(Window.handle);
            GLFW.glfwFocusWindow(Window.handle);
            GLFW.glfwMakeContextCurrent(Window.handle);
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLFW.glfwSwapInterval(0);
            GL11.glClear(16640);
            GLFW.glfwSwapBuffers(Window.handle);
            Display.setVSyncEnabled(vsyncEnabled);
        }
    }

    private static void calcWindowPos(boolean fullscreen) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer fbw = stack.callocInt(1);
            IntBuffer fbh = stack.callocInt(1);
            GLFW.glfwGetFramebufferSize(Window.handle, fbw, fbh);
            displayFramebufferWidth = fbw.get(0);
            displayFramebufferHeight = fbh.get(0);
            IntBuffer fblb = stack.callocInt(1);
            IntBuffer fbtb = stack.callocInt(1);
            GLFW.glfwGetWindowFrameSize(Window.handle, fblb, fbtb, null, null);
            int leftFrameSize = fblb.get(0);
            int topFrameSize = fbtb.get(0);
            displayWidth = gameWindowMode.getWidth();
            displayHeight = gameWindowMode.getHeight();
            if (fullscreen) {
                leftFrameSize = 0;
                topFrameSize = 0;
            }
            displayX = leftFrameSize + (desktopDisplayMode.getWidth() - gameWindowMode.getWidth()) / 2;
            displayY = topFrameSize + (desktopDisplayMode.getHeight() - gameWindowMode.getHeight()) / 2;
            if (gameWindowMode.getWidth() > desktopDisplayMode.getWidth()) {
                displayX = leftFrameSize;
            }
            if (gameWindowMode.getHeight() > desktopDisplayMode.getHeight()) {
                displayY = topFrameSize;
            }
            if (Core.isUseGameViewport()) {
                displayY = topFrameSize;
                displayX = 0;
                displayWidth -= leftFrameSize * 2;
                displayHeight -= topFrameSize;
            }
        }
    }

    public static DisplayMode getDisplayMode() {
        return gameWindowMode;
    }

    public static DisplayMode[] getAvailableDisplayModes() throws LWJGLException {
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
        DisplayMode[] displayModes = new DisplayMode[modes.capacity()];
        for (int i = 0; i < displayModes.length; ++i) {
            modes.position(i);
            int w = modes.width();
            int h = modes.height();
            int b = modes.redBits() + modes.greenBits() + modes.blueBits();
            int r = modes.refreshRate();
            displayModes[i] = new DisplayMode(w, h, b, r);
        }
        return displayModes;
    }

    public static DisplayMode getDesktopDisplayMode() {
        return desktopDisplayMode;
    }

    public static boolean wasResized() {
        return displayResized;
    }

    public static int getX() {
        return displayX;
    }

    public static int getY() {
        return displayY;
    }

    public static int getWidth() {
        return latestWidth;
    }

    public static int getHeight() {
        return latestHeight;
    }

    public static int getFramebufferWidth() {
        return displayFramebufferWidth;
    }

    public static int getFramebufferHeight() {
        return displayFramebufferHeight;
    }

    public static void setTitle(String title) {
        windowTitle = title;
        if (Display.isCreated()) {
            GLFW.glfwSetWindowTitle(Window.handle, windowTitle);
        }
    }

    public static boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(Window.handle);
    }

    public static boolean isDirty() {
        return displayDirty;
    }

    public static void setInitialBackground(float red, float green, float blue) {
        System.out.println("TODO: Implement Display.setInitialBackground(float, float, float)");
    }

    public static void setIcon(GLFWImage.Buffer icons) {
        displayIcons = icons;
    }

    public static void setResizable(boolean resizable) {
        displayResizable = resizable;
    }

    public static boolean isResizable() {
        return displayResizable;
    }

    public static void setParent(Canvas parent) throws LWJGLException {
    }

    public static void releaseContext() throws LWJGLException {
        GLFW.glfwMakeContextCurrent(0L);
    }

    public static boolean isCurrent() throws LWJGLException {
        return GLFW.glfwGetCurrentContext() == Window.handle;
    }

    public static void makeCurrent() throws LWJGLException {
        GLFW.glfwMakeContextCurrent(Window.handle);
        GL.setCapabilities(capabilities);
    }

    public static String getAdapter() {
        return "GeNotSupportedAdapter";
    }

    public static String getVersion() {
        return "1.0 NOT SUPPORTED";
    }

    public static void sync(int fps) {
        Sync.sync(fps);
    }

    public static void imGuiNewFrame() {
        if (!Core.isImGui()) {
            return;
        }
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        ++frameCount;
    }

    public static boolean inImGuiFrame() {
        return frameCount > 0;
    }

    public static void drawImGuiDrawData(ImDrawData imDrawData) {
        if (!Core.isImGui()) {
            return;
        }
        imGuiGl3.renderDrawData(imDrawData);
        ImGui.freeDrawData(imDrawData);
    }

    public static ImDrawData imguiEndFrame() {
        if (!Core.isImGui()) {
            return null;
        }
        if (frameCount == 0) {
            return null;
        }
        --frameCount;
        ImGui.endFrame();
        ImGui.render();
        if (Core.isUseGameViewport()) {
            SpriteRenderer.instance.glBuffer(12, 0);
        }
        ImDrawData drawData = ImGui.getDrawData();
        if (Core.isUseViewports()) {
            RenderThread.invokeOnRenderContext(() -> {
                long backupWindowPtr = GLFW.glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(backupWindowPtr);
            });
        }
        return drawData;
    }

    static {
        displayVisible = true;
        displayResizable = true;
        vsyncEnabled = true;
        gameWindowMode = new DisplayMode(640, 480);
        desktopDisplayMode = new DisplayMode(640, 480);
        mouseCursorPosX = new double[1];
        mouseCursorPosY = new double[1];
        mouseCursorState = -1;
    }

    private static final class Window {
        static long handle;

        private Window() {
        }
    }

    private static final class Callbacks {
        static boolean noise;
        static GLFWErrorCallback errorCallback;
        static GLDebugMessageCallback debugMessageCallback;
        static GLFWKeyCallback keyCallback;
        static GLFWCharCallback charCallback;
        static GLFWCursorPosCallback cursorPosCallback;
        static GLFWMouseButtonCallback mouseButtonCallback;
        static GLFWScrollCallback scrollCallback;
        static GLFWWindowFocusCallback windowFocusCallback;
        static GLFWWindowIconifyCallback windowIconifyCallback;
        static GLFWWindowSizeCallback windowSizeCallback;
        static GLFWWindowPosCallback windowPosCallback;
        static GLFWWindowRefreshCallback windowRefreshCallback;
        static GLFWFramebufferSizeCallback framebufferSizeCallback;

        private Callbacks() {
        }

        static void initCallbacks() {
            cursorPosCallback = GLFWCursorPosCallback.create((windowHnd, xpos, ypos) -> Mouse.addMoveEvent(xpos, ypos));
            GLFW.glfwSetCursorPosCallback(Display.getWindow(), cursorPosCallback);
            mouseButtonCallback = GLFWMouseButtonCallback.create((windowHnd, button, action, mods) -> Mouse.addButtonEvent(button, action == 1));
            GLFW.glfwSetMouseButtonCallback(Display.getWindow(), mouseButtonCallback);
            windowFocusCallback = GLFWWindowFocusCallback.create((windowHnd, focused) -> {
                if (noise) {
                    DebugLog.log("glfwSetWindowFocusCallback focused=" + focused);
                }
                displayFocused = focused;
                if (focused) {
                    Clipboard.rememberCurrentValue();
                }
                if (!focused && Core.getInstance().getOptionFocusloss() && GameWindow.isIngameState() && !Core.exiting && !GameClient.client && !GameServer.server && IsoPlayer.hasInstance() && !IsoPlayer.allPlayersDead() && UIManager.getSpeedControls().isReallyVisible()) {
                    UIManager.getSpeedControls().SetCurrentGameSpeed(0);
                }
            });
            GLFW.glfwSetWindowFocusCallback(Display.getWindow(), windowFocusCallback);
            windowIconifyCallback = GLFWWindowIconifyCallback.create((windowHnd, iconified) -> {
                if (noise) {
                    DebugLog.log("glfwSetWindowIconifyCallback iconifed=" + iconified);
                }
                displayVisible = !iconified;
            });
            GLFW.glfwSetWindowIconifyCallback(Display.getWindow(), windowIconifyCallback);
            windowSizeCallback = GLFWWindowSizeCallback.create((window, width, height) -> {
                if (noise) {
                    DebugLog.log("glfwSetWindowSizeCallback width,height=" + width + "," + height);
                }
                if (width + height == 0) {
                    return;
                }
                latestResized = true;
                latestWidth = width;
                latestHeight = height;
            });
            GLFW.glfwSetWindowSizeCallback(Display.getWindow(), windowSizeCallback);
            scrollCallback = GLFWScrollCallback.create((windowHnd, xpos, ypos) -> Mouse.setDWheel(xpos, ypos));
            GLFW.glfwSetScrollCallback(Display.getWindow(), scrollCallback);
            windowPosCallback = GLFWWindowPosCallback.create((windowHnd, xpos, ypos) -> {
                if (noise) {
                    DebugLog.log("glfwSetWindowPosCallback x,y=" + xpos + "," + ypos);
                }
                displayX = xpos;
                displayY = ypos;
            });
            GLFW.glfwSetWindowPosCallback(Display.getWindow(), windowPosCallback);
            windowRefreshCallback = GLFWWindowRefreshCallback.create(windowHnd -> {
                displayDirty = true;
            });
            GLFW.glfwSetWindowRefreshCallback(Display.getWindow(), windowRefreshCallback);
            framebufferSizeCallback = GLFWFramebufferSizeCallback.create((windowHnd, width, height) -> {
                if (noise) {
                    DebugLog.log("glfwSetFramebufferSizeCallback width,height=" + width + "," + height);
                }
                displayFramebufferWidth = width;
                displayFramebufferHeight = height;
            });
            GLFW.glfwSetFramebufferSizeCallback(Display.getWindow(), framebufferSizeCallback);
            keyCallback = GLFWKeyCallback.create((windowHnd, key, scancode, action, mods) -> Keyboard.addKeyEvent(key, action));
            GLFW.glfwSetKeyCallback(Display.getWindow(), keyCallback);
            charCallback = GLFWCharCallback.create((windowHnd, codepoint) -> Keyboard.addCharEvent((char)codepoint));
            GLFW.glfwSetCharCallback(Display.getWindow(), charCallback);
        }

        static void releaseCallbacks() {
            errorCallback.free();
            if (debugMessageCallback != null) {
                debugMessageCallback.free();
            }
            keyCallback.free();
            charCallback.free();
            cursorPosCallback.free();
            mouseButtonCallback.free();
            scrollCallback.free();
            windowFocusCallback.free();
            windowIconifyCallback.free();
            windowSizeCallback.free();
            windowPosCallback.free();
            windowRefreshCallback.free();
            framebufferSizeCallback.free();
        }
    }
}

