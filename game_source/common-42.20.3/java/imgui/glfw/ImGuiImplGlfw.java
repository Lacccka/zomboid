/*
 * Decompiled with CFR 0.152.
 */
package imgui.glfw;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiPlatformIO;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.callback.ImPlatformFuncViewport;
import imgui.callback.ImPlatformFuncViewportFloat;
import imgui.callback.ImPlatformFuncViewportImVec2;
import imgui.callback.ImPlatformFuncViewportString;
import imgui.callback.ImPlatformFuncViewportSuppBoolean;
import imgui.callback.ImPlatformFuncViewportSuppImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.lwjgl3.glfw.ImGuiImplGlfwNative;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import zombie.core.Core;
import zombie.core.opengl.RenderThread;

public class ImGuiImplGlfw {
    private static final String OS = System.getProperty("os.name", "generic").toLowerCase();
    protected static final boolean IS_WINDOWS = OS.contains("win");
    protected static final boolean IS_APPLE = OS.contains("mac") || OS.contains("darwin");
    private long windowPtr;
    private boolean glfwHawWindowTopmost;
    private boolean glfwHasWindowAlpha;
    private boolean glfwHasPerMonitorDpi;
    private boolean glfwHasFocusWindow;
    private boolean glfwHasFocusOnShow;
    private boolean glfwHasMonitorWorkArea;
    private boolean glfwHasOsxWindowPosFix;
    private final int[] winWidth = new int[1];
    private final int[] winHeight = new int[1];
    private final int[] fbWidth = new int[1];
    private final int[] fbHeight = new int[1];
    private final long[] mouseCursors = new long[9];
    private final long[] keyOwnerWindows = new long[512];
    private final float[] emptyNavInputs = new float[21];
    private final boolean[] mouseJustPressed = new boolean[5];
    private final ImVec2 mousePosBackup = new ImVec2();
    private final double[] mouseX = new double[1];
    private final double[] mouseY = new double[1];
    private final int[] windowX = new int[1];
    private final int[] windowY = new int[1];
    private final int[] monitorX = new int[1];
    private final int[] monitorY = new int[1];
    private final int[] monitorWorkAreaX = new int[1];
    private final int[] monitorWorkAreaY = new int[1];
    private final int[] monitorWorkAreaWidth = new int[1];
    private final int[] monitorWorkAreaHeight = new int[1];
    private final float[] monitorContentScaleX = new float[1];
    private final float[] monitorContentScaleY = new float[1];
    private GLFWWindowFocusCallback prevUserCallbackWindowFocus;
    private GLFWMouseButtonCallback prevUserCallbackMouseButton;
    private GLFWScrollCallback prevUserCallbackScroll;
    private GLFWKeyCallback prevUserCallbackKey;
    private GLFWCharCallback prevUserCallbackChar;
    private GLFWMonitorCallback prevUserCallbackMonitor;
    private GLFWCursorEnterCallback prevUserCallbackCursorEnter;
    private boolean callbacksInstalled;
    private boolean wantUpdateMonitors = true;
    private double time;
    private long mouseWindowPtr;

    public void mouseButtonCallback(long windowId, int button, int action, int mods) {
        if (this.prevUserCallbackMouseButton != null && windowId == this.windowPtr) {
            this.prevUserCallbackMouseButton.invoke(windowId, button, action, mods);
        }
        if (action == 1 && button >= 0 && button < this.mouseJustPressed.length) {
            this.mouseJustPressed[button] = true;
        }
    }

    public void scrollCallback(long windowId, double xOffset, double yOffset) {
        if (this.prevUserCallbackScroll != null && windowId == this.windowPtr) {
            this.prevUserCallbackScroll.invoke(windowId, xOffset, yOffset);
        }
        ImGuiIO io = ImGui.getIO();
        io.setMouseWheelH(io.getMouseWheelH() + (float)xOffset);
        io.setMouseWheel(io.getMouseWheel() + (float)yOffset);
    }

    public void keyCallback(long windowId, int key, int scancode, int action, int mods) {
        if (this.prevUserCallbackKey != null && windowId == this.windowPtr) {
            this.prevUserCallbackKey.invoke(windowId, key, scancode, action, mods);
        }
        ImGuiIO io = ImGui.getIO();
        if (key >= 0 && key < this.keyOwnerWindows.length) {
            if (action == 1) {
                io.setKeysDown(key, true);
                this.keyOwnerWindows[key] = windowId;
            } else if (action == 0) {
                io.setKeysDown(key, false);
                this.keyOwnerWindows[key] = 0L;
            }
        }
        io.setKeyCtrl(io.getKeysDown(341) || io.getKeysDown(345));
        io.setKeyShift(io.getKeysDown(340) || io.getKeysDown(344));
        io.setKeyAlt(io.getKeysDown(342) || io.getKeysDown(346));
        io.setKeySuper(io.getKeysDown(343) || io.getKeysDown(347));
    }

    public void windowFocusCallback(long windowId, boolean focused) {
        if (this.prevUserCallbackWindowFocus != null && windowId == this.windowPtr) {
            this.prevUserCallbackWindowFocus.invoke(windowId, focused);
        }
        ImGui.getIO().addFocusEvent(focused);
    }

    public void cursorEnterCallback(long windowId, boolean entered) {
        if (this.prevUserCallbackCursorEnter != null && windowId == this.windowPtr) {
            this.prevUserCallbackCursorEnter.invoke(windowId, entered);
        }
        if (entered) {
            this.mouseWindowPtr = windowId;
        }
        if (!entered && this.mouseWindowPtr == windowId) {
            this.mouseWindowPtr = 0L;
        }
    }

    public void charCallback(long windowId, int c) {
        if (this.prevUserCallbackChar != null && windowId == this.windowPtr) {
            this.prevUserCallbackChar.invoke(windowId, c);
        }
        ImGuiIO io = ImGui.getIO();
        io.addInputCharacter(c);
    }

    public void monitorCallback(long windowId, int event) {
        this.wantUpdateMonitors = true;
    }

    public boolean init(final long windowId, boolean installCallbacks) {
        this.windowPtr = windowId;
        this.detectGlfwVersionAndEnabledFeatures();
        ImGuiIO io = ImGui.getIO();
        io.addBackendFlags(1030);
        io.setBackendPlatformName("imgui_java_impl_glfw");
        int[] keyMap = new int[]{258, 263, 262, 265, 264, 266, 267, 268, 269, 260, 261, 259, 32, 257, 256, 335, 65, 67, 86, 88, 89, 90, 298};
        io.setKeyMap(keyMap);
        io.setGetClipboardTextFn(new ImStrSupplier(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public String get() {
                String clipboardString = GLFW.glfwGetClipboardString(windowId);
                return clipboardString != null ? clipboardString : "";
            }
        });
        io.setSetClipboardTextFn(new ImStrConsumer(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public void accept(String str) {
                GLFW.glfwSetClipboardString(windowId, str);
            }
        });
        GLFWErrorCallback prevErrorCallback = GLFW.glfwSetErrorCallback(null);
        this.mouseCursors[0] = GLFW.glfwCreateStandardCursor(221185);
        this.mouseCursors[1] = GLFW.glfwCreateStandardCursor(221186);
        this.mouseCursors[2] = GLFW.glfwCreateStandardCursor(221185);
        this.mouseCursors[3] = GLFW.glfwCreateStandardCursor(221190);
        this.mouseCursors[4] = GLFW.glfwCreateStandardCursor(221189);
        this.mouseCursors[5] = GLFW.glfwCreateStandardCursor(221185);
        this.mouseCursors[6] = GLFW.glfwCreateStandardCursor(221185);
        this.mouseCursors[7] = GLFW.glfwCreateStandardCursor(221188);
        this.mouseCursors[8] = GLFW.glfwCreateStandardCursor(221185);
        GLFW.glfwSetErrorCallback(prevErrorCallback);
        if (installCallbacks) {
            this.callbacksInstalled = true;
            this.prevUserCallbackWindowFocus = GLFW.glfwSetWindowFocusCallback(windowId, this::windowFocusCallback);
            this.prevUserCallbackCursorEnter = GLFW.glfwSetCursorEnterCallback(windowId, this::cursorEnterCallback);
            this.prevUserCallbackMouseButton = GLFW.glfwSetMouseButtonCallback(windowId, this::mouseButtonCallback);
            this.prevUserCallbackScroll = GLFW.glfwSetScrollCallback(windowId, this::scrollCallback);
            this.prevUserCallbackKey = GLFW.glfwSetKeyCallback(windowId, this::keyCallback);
            this.prevUserCallbackChar = GLFW.glfwSetCharCallback(windowId, this::charCallback);
        }
        this.updateMonitors();
        this.prevUserCallbackMonitor = GLFW.glfwSetMonitorCallback(this::monitorCallback);
        ImGuiViewport mainViewport = ImGui.getMainViewport();
        mainViewport.setPlatformHandle(this.windowPtr);
        if (IS_WINDOWS) {
            mainViewport.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(windowId));
        }
        if (io.hasConfigFlags(1024)) {
            this.initPlatformInterface();
        }
        return true;
    }

    public void newFrame() {
        ImGuiIO io = ImGui.getIO();
        if (Core.debug) {
            RenderThread.invokeOnRenderContext(() -> {
                GLFW.glfwGetWindowSize(this.windowPtr, this.winWidth, this.winHeight);
                GLFW.glfwGetFramebufferSize(this.windowPtr, this.fbWidth, this.fbHeight);
                io.setDisplaySize(this.winWidth[0], this.winHeight[0]);
                if (this.winWidth[0] > 0 && this.winHeight[0] > 0) {
                    float scaleX = (float)this.fbWidth[0] / (float)this.winWidth[0];
                    float scaleY = (float)this.fbHeight[0] / (float)this.winHeight[0];
                    io.setDisplayFramebufferScale(scaleX, scaleY);
                }
                if (this.wantUpdateMonitors) {
                    this.updateMonitors();
                }
                double currentTime = GLFW.glfwGetTime();
                io.setDeltaTime(this.time > 0.0 ? (float)(currentTime - this.time) : 0.016666668f);
                this.time = currentTime;
                this.updateMousePosAndButtons();
                this.updateMouseCursor();
                this.updateGamepads();
            });
        }
    }

    public void dispose() {
        this.shutdownPlatformInterface();
        try {
            if (this.callbacksInstalled) {
                GLFW.glfwSetWindowFocusCallback(this.windowPtr, this.prevUserCallbackWindowFocus).free();
                GLFW.glfwSetCursorEnterCallback(this.windowPtr, this.prevUserCallbackCursorEnter).free();
                GLFW.glfwSetMouseButtonCallback(this.windowPtr, this.prevUserCallbackMouseButton).free();
                GLFW.glfwSetScrollCallback(this.windowPtr, this.prevUserCallbackScroll).free();
                GLFW.glfwSetKeyCallback(this.windowPtr, this.prevUserCallbackKey).free();
                GLFW.glfwSetCharCallback(this.windowPtr, this.prevUserCallbackChar).free();
                this.callbacksInstalled = false;
            }
            GLFW.glfwSetMonitorCallback(this.prevUserCallbackMonitor).free();
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
        for (int i = 0; i < 9; ++i) {
            GLFW.glfwDestroyCursor(this.mouseCursors[i]);
        }
    }

    private void detectGlfwVersionAndEnabledFeatures() {
        int[] major = new int[1];
        int[] minor = new int[1];
        int[] rev = new int[1];
        GLFW.glfwGetVersion(major, minor, rev);
        int version = major[0] * 1000 + minor[0] * 100 + rev[0] * 10;
        this.glfwHawWindowTopmost = version >= 3200;
        this.glfwHasWindowAlpha = version >= 3300;
        this.glfwHasPerMonitorDpi = version >= 3300;
        this.glfwHasFocusWindow = version >= 3200;
        this.glfwHasFocusOnShow = version >= 3300;
        this.glfwHasMonitorWorkArea = version >= 3300;
    }

    private void updateMousePosAndButtons() {
        ImGuiIO io = ImGui.getIO();
        for (int i = 0; i < 5; ++i) {
            io.setMouseDown(i, this.mouseJustPressed[i] || GLFW.glfwGetMouseButton(this.windowPtr, i) != 0);
            this.mouseJustPressed[i] = false;
        }
        io.getMousePos(this.mousePosBackup);
        io.setMousePos(-3.4028235E38f, -3.4028235E38f);
        io.setMouseHoveredViewport(0);
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        for (int n = 0; n < platformIO.getViewportsSize(); ++n) {
            long mouseWindowPtr;
            ImGuiViewport viewport = platformIO.getViewports(n);
            long windowPtr = viewport.getPlatformHandle();
            boolean focused = GLFW.glfwGetWindowAttrib(windowPtr, 131073) != 0;
            long l = mouseWindowPtr = this.mouseWindowPtr == windowPtr || focused ? windowPtr : 0L;
            if (focused) {
                for (int i = 0; i < 5; ++i) {
                    io.setMouseDown(i, GLFW.glfwGetMouseButton(windowPtr, i) != 0);
                }
            }
            if (io.getWantSetMousePos() && focused) {
                GLFW.glfwSetCursorPos(windowPtr, this.mousePosBackup.x - viewport.getPosX(), this.mousePosBackup.y - viewport.getPosY());
            }
            if (mouseWindowPtr == 0L) continue;
            GLFW.glfwGetCursorPos(mouseWindowPtr, this.mouseX, this.mouseY);
            if (io.hasConfigFlags(1024)) {
                GLFW.glfwGetWindowPos(windowPtr, this.windowX, this.windowY);
                io.setMousePos((float)this.mouseX[0] + (float)this.windowX[0], (float)this.mouseY[0] + (float)this.windowY[0]);
                continue;
            }
            io.setMousePos((float)this.mouseX[0], (float)this.mouseY[0]);
        }
    }

    private void updateMouseCursor() {
        boolean cursorDisabled;
        ImGuiIO io = ImGui.getIO();
        boolean noCursorChange = io.hasConfigFlags(32);
        boolean bl = cursorDisabled = GLFW.glfwGetInputMode(this.windowPtr, 208897) == 212995;
        if (noCursorChange || cursorDisabled) {
            return;
        }
        int imguiCursor = ImGui.getMouseCursor();
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        for (int n = 0; n < platformIO.getViewportsSize(); ++n) {
            long windowPtr = platformIO.getViewports(n).getPlatformHandle();
            if (imguiCursor == -1 || io.getMouseDrawCursor()) {
                GLFW.glfwSetInputMode(windowPtr, 208897, 212994);
                continue;
            }
            GLFW.glfwSetCursor(windowPtr, this.mouseCursors[imguiCursor] != 0L ? this.mouseCursors[imguiCursor] : this.mouseCursors[0]);
            GLFW.glfwSetInputMode(windowPtr, 208897, 212993);
        }
    }

    private void updateGamepads() {
        ImGuiIO io = ImGui.getIO();
        if (!io.hasConfigFlags(2)) {
            return;
        }
        io.setNavInputs(this.emptyNavInputs);
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(0);
        int buttonsCount = buttons.limit();
        FloatBuffer axis = GLFW.glfwGetJoystickAxes(0);
        int axisCount = axis.limit();
        this.mapButton(0, 0, buttons, buttonsCount, io);
        this.mapButton(1, 1, buttons, buttonsCount, io);
        this.mapButton(3, 2, buttons, buttonsCount, io);
        this.mapButton(2, 3, buttons, buttonsCount, io);
        this.mapButton(4, 13, buttons, buttonsCount, io);
        this.mapButton(5, 11, buttons, buttonsCount, io);
        this.mapButton(6, 10, buttons, buttonsCount, io);
        this.mapButton(7, 12, buttons, buttonsCount, io);
        this.mapButton(12, 4, buttons, buttonsCount, io);
        this.mapButton(13, 5, buttons, buttonsCount, io);
        this.mapButton(14, 4, buttons, buttonsCount, io);
        this.mapButton(15, 5, buttons, buttonsCount, io);
        this.mapAnalog(8, 0, -0.3f, -0.9f, axis, axisCount, io);
        this.mapAnalog(9, 0, 0.3f, 0.9f, axis, axisCount, io);
        this.mapAnalog(10, 1, 0.3f, 0.9f, axis, axisCount, io);
        this.mapAnalog(11, 1, -0.3f, -0.9f, axis, axisCount, io);
        if (axisCount > 0 && buttonsCount > 0) {
            io.addBackendFlags(1);
        } else {
            io.removeBackendFlags(1);
        }
    }

    private void mapButton(int navNo, int buttonNo, ByteBuffer buttons, int buttonsCount, ImGuiIO io) {
        if (buttonsCount > buttonNo && buttons.get(buttonNo) == 1) {
            io.setNavInputs(navNo, 1.0f);
        }
    }

    private void mapAnalog(int navNo, int axisNo, float v0, float v1, FloatBuffer axis, int axisCount, ImGuiIO io) {
        float v = axisCount > axisNo ? axis.get(axisNo) : v0;
        if ((v = (v - v0) / (v1 - v0)) > 1.0f) {
            v = 1.0f;
        }
        if (io.getNavInputs(navNo) < v) {
            io.setNavInputs(navNo, v);
        }
    }

    private void updateMonitors() {
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        platformIO.resizeMonitors(0);
        for (int n = 0; n < monitors.limit(); ++n) {
            long monitor = monitors.get(n);
            GLFW.glfwGetMonitorPos(monitor, this.monitorX, this.monitorY);
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            float mainPosX = this.monitorX[0];
            float mainPosY = this.monitorY[0];
            float mainSizeX = vidMode.width();
            float mainSizeY = vidMode.height();
            if (this.glfwHasMonitorWorkArea) {
                GLFW.glfwGetMonitorWorkarea(monitor, this.monitorWorkAreaX, this.monitorWorkAreaY, this.monitorWorkAreaWidth, this.monitorWorkAreaHeight);
            }
            float workPosX = 0.0f;
            float workPosY = 0.0f;
            float workSizeX = 0.0f;
            float workSizeY = 0.0f;
            if (this.glfwHasMonitorWorkArea && this.monitorWorkAreaWidth[0] > 0 && this.monitorWorkAreaHeight[0] > 0) {
                workPosX = this.monitorWorkAreaX[0];
                workPosY = this.monitorWorkAreaY[0];
                workSizeX = this.monitorWorkAreaWidth[0];
                workSizeY = this.monitorWorkAreaHeight[0];
            }
            if (this.glfwHasPerMonitorDpi) {
                GLFW.glfwGetMonitorContentScale(monitor, this.monitorContentScaleX, this.monitorContentScaleY);
            }
            float dpiScale = this.monitorContentScaleX[0];
            platformIO.pushMonitors(mainPosX, mainPosY, mainSizeX, mainSizeY, workPosX, workPosY, workSizeX, workSizeY, dpiScale);
        }
        this.wantUpdateMonitors = false;
    }

    private void windowCloseCallback(long windowId) {
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        vp.setPlatformRequestClose(true);
    }

    private void windowPosCallback(long windowId, int xPos, int yPos) {
        boolean ignoreEvent;
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
        boolean bl = ignoreEvent = ImGui.getFrameCount() <= data.ignoreWindowPosEventFrame + 1;
        if (ignoreEvent) {
            return;
        }
        vp.setPlatformRequestMove(true);
    }

    private void windowSizeCallback(long windowId, int width, int height) {
        boolean ignoreEvent;
        ImGuiViewport vp = ImGui.findViewportByPlatformHandle(windowId);
        ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
        boolean bl = ignoreEvent = ImGui.getFrameCount() <= data.ignoreWindowSizeEventFrame + 1;
        if (ignoreEvent) {
            return;
        }
        vp.setPlatformRequestResize(true);
    }

    private void initPlatformInterface() {
        ImGuiPlatformIO platformIO = ImGui.getPlatformIO();
        platformIO.setPlatformCreateWindow(new CreateWindowFunction(this));
        platformIO.setPlatformDestroyWindow(new DestroyWindowFunction(this));
        platformIO.setPlatformShowWindow(new ShowWindowFunction());
        platformIO.setPlatformGetWindowPos(new GetWindowPosFunction());
        platformIO.setPlatformSetWindowPos(new SetWindowPosFunction());
        platformIO.setPlatformGetWindowSize(new GetWindowSizeFunction());
        platformIO.setPlatformSetWindowSize(new SetWindowSizeFunction(this));
        platformIO.setPlatformSetWindowTitle(new SetWindowTitleFunction());
        platformIO.setPlatformSetWindowFocus(new SetWindowFocusFunction(this));
        platformIO.setPlatformGetWindowFocus(new GetWindowFocusFunction());
        platformIO.setPlatformGetWindowMinimized(new GetWindowMinimizedFunction());
        platformIO.setPlatformSetWindowAlpha(new SetWindowAlphaFunction(this));
        platformIO.setPlatformRenderWindow(new RenderWindowFunction());
        platformIO.setPlatformSwapBuffers(new SwapBuffersFunction());
        ImGuiViewport mainViewport = ImGui.getMainViewport();
        ImGuiViewportDataGlfw data = new ImGuiViewportDataGlfw();
        data.window = this.windowPtr;
        data.windowOwned = false;
        mainViewport.setPlatformUserData(data);
    }

    private void shutdownPlatformInterface() {
    }

    private static final class ImGuiViewportDataGlfw {
        long window;
        boolean windowOwned;
        int ignoreWindowPosEventFrame = -1;
        int ignoreWindowSizeEventFrame = -1;

        private ImGuiViewportDataGlfw() {
        }
    }

    private final class CreateWindowFunction
    extends ImPlatformFuncViewport {
        final /* synthetic */ ImGuiImplGlfw this$0;

        private CreateWindowFunction(ImGuiImplGlfw imGuiImplGlfw) {
            ImGuiImplGlfw imGuiImplGlfw2 = imGuiImplGlfw;
            Objects.requireNonNull(imGuiImplGlfw2);
            this.this$0 = imGuiImplGlfw2;
        }

        @Override
        public void accept(ImGuiViewport vp) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = new ImGuiViewportDataGlfw();
                vp.setPlatformUserData(data);
                GLFW.glfwWindowHint(131076, 0);
                GLFW.glfwWindowHint(131073, 0);
                if (this.this$0.glfwHasFocusOnShow) {
                    GLFW.glfwWindowHint(131084, 0);
                }
                GLFW.glfwWindowHint(131077, vp.hasFlags(8) ? 0 : 1);
                if (this.this$0.glfwHawWindowTopmost) {
                    GLFW.glfwWindowHint(131079, vp.hasFlags(512) ? 1 : 0);
                }
                data.window = GLFW.glfwCreateWindow((int)vp.getSizeX(), (int)vp.getSizeY(), "No Title Yet", 0L, this.this$0.windowPtr);
                data.windowOwned = true;
                vp.setPlatformHandle(data.window);
                if (IS_WINDOWS) {
                    vp.setPlatformHandleRaw(GLFWNativeWin32.glfwGetWin32Window(data.window));
                }
                GLFW.glfwSetWindowPos(data.window, (int)vp.getPosX(), (int)vp.getPosY());
                GLFW.glfwSetMouseButtonCallback(data.window, this.this$0::mouseButtonCallback);
                GLFW.glfwSetScrollCallback(data.window, this.this$0::scrollCallback);
                GLFW.glfwSetKeyCallback(data.window, this.this$0::keyCallback);
                GLFW.glfwSetCharCallback(data.window, this.this$0::charCallback);
                GLFW.glfwSetWindowCloseCallback(data.window, this.this$0::windowCloseCallback);
                GLFW.glfwSetWindowPosCallback(data.window, this.this$0::windowPosCallback);
                GLFW.glfwSetWindowSizeCallback(data.window, this.this$0::windowSizeCallback);
                GLFW.glfwMakeContextCurrent(data.window);
                GLFW.glfwSwapInterval(0);
            });
        }
    }

    private final class DestroyWindowFunction
    extends ImPlatformFuncViewport {
        final /* synthetic */ ImGuiImplGlfw this$0;

        private DestroyWindowFunction(ImGuiImplGlfw imGuiImplGlfw) {
            ImGuiImplGlfw imGuiImplGlfw2 = imGuiImplGlfw;
            Objects.requireNonNull(imGuiImplGlfw2);
            this.this$0 = imGuiImplGlfw2;
        }

        @Override
        public void accept(ImGuiViewport vp) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            if (data != null && data.windowOwned) {
                for (int i = 0; i < this.this$0.keyOwnerWindows.length; ++i) {
                    if (this.this$0.keyOwnerWindows[i] != data.window) continue;
                    this.this$0.keyCallback(data.window, i, 0, 0, 0);
                }
                Callbacks.glfwFreeCallbacks(data.window);
                GLFW.glfwDestroyWindow(data.window);
            }
            vp.setPlatformUserData(null);
            vp.setPlatformHandle(0L);
        }
    }

    private static final class ShowWindowFunction
    extends ImPlatformFuncViewport {
        private ShowWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                if (IS_WINDOWS && vp.hasFlags(16)) {
                    ImGuiImplGlfwNative.win32hideFromTaskBar(vp.getPlatformHandleRaw());
                }
                GLFW.glfwShowWindow(data.window);
            });
        }
    }

    private static final class GetWindowPosFunction
    extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] posX = new int[1];
        private final int[] posY = new int[1];

        private GetWindowPosFunction() {
        }

        @Override
        public void get(ImGuiViewport vp, ImVec2 dstImVec2) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            GLFW.glfwGetWindowPos(data.window, this.posX, this.posY);
            dstImVec2.x = this.posX[0];
            dstImVec2.y = this.posY[0];
        }
    }

    private static final class SetWindowPosFunction
    extends ImPlatformFuncViewportImVec2 {
        private SetWindowPosFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, ImVec2 imVec2) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                data.ignoreWindowPosEventFrame = ImGui.getFrameCount();
                GLFW.glfwSetWindowPos(data.window, (int)imVec2.x, (int)imVec2.y);
            });
        }
    }

    private static final class GetWindowSizeFunction
    extends ImPlatformFuncViewportSuppImVec2 {
        private final int[] width = new int[1];
        private final int[] height = new int[1];

        private GetWindowSizeFunction() {
        }

        @Override
        public void get(ImGuiViewport vp, ImVec2 dstImVec2) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            GLFW.glfwGetWindowSize(data.window, this.width, this.height);
            dstImVec2.x = this.width[0];
            dstImVec2.y = this.height[0];
        }
    }

    private final class SetWindowSizeFunction
    extends ImPlatformFuncViewportImVec2 {
        private final int[] x;
        private final int[] y;
        private final int[] width;
        private final int[] height;
        final /* synthetic */ ImGuiImplGlfw this$0;

        private SetWindowSizeFunction(ImGuiImplGlfw imGuiImplGlfw) {
            ImGuiImplGlfw imGuiImplGlfw2 = imGuiImplGlfw;
            Objects.requireNonNull(imGuiImplGlfw2);
            this.this$0 = imGuiImplGlfw2;
            this.x = new int[1];
            this.y = new int[1];
            this.width = new int[1];
            this.height = new int[1];
        }

        @Override
        public void accept(ImGuiViewport vp, ImVec2 imVec2) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                if (IS_APPLE && !this.this$0.glfwHasOsxWindowPosFix) {
                    GLFW.glfwGetWindowPos(data.window, this.x, this.y);
                    GLFW.glfwGetWindowSize(data.window, this.width, this.height);
                    GLFW.glfwSetWindowPos(data.window, this.x[0], this.y[0] - this.height[0] + (int)imVec2.y);
                }
                data.ignoreWindowSizeEventFrame = ImGui.getFrameCount();
                GLFW.glfwSetWindowSize(data.window, (int)imVec2.x, (int)imVec2.y);
            });
        }
    }

    private static final class SetWindowTitleFunction
    extends ImPlatformFuncViewportString {
        private SetWindowTitleFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp, String str) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            RenderThread.invokeOnRenderContext(() -> GLFW.glfwSetWindowTitle(data.window, str));
        }
    }

    private final class SetWindowFocusFunction
    extends ImPlatformFuncViewport {
        final /* synthetic */ ImGuiImplGlfw this$0;

        private SetWindowFocusFunction(ImGuiImplGlfw imGuiImplGlfw) {
            ImGuiImplGlfw imGuiImplGlfw2 = imGuiImplGlfw;
            Objects.requireNonNull(imGuiImplGlfw2);
            this.this$0 = imGuiImplGlfw2;
        }

        @Override
        public void accept(ImGuiViewport vp) {
            RenderThread.invokeOnRenderContext(() -> {
                if (this.this$0.glfwHasFocusWindow) {
                    ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                    GLFW.glfwFocusWindow(data.window);
                }
            });
        }
    }

    private static final class GetWindowFocusFunction
    extends ImPlatformFuncViewportSuppBoolean {
        private GetWindowFocusFunction() {
        }

        @Override
        public boolean get(ImGuiViewport vp) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            return GLFW.glfwGetWindowAttrib(data.window, 131073) != 0;
        }
    }

    private static final class GetWindowMinimizedFunction
    extends ImPlatformFuncViewportSuppBoolean {
        private GetWindowMinimizedFunction() {
        }

        @Override
        public boolean get(ImGuiViewport vp) {
            ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
            return GLFW.glfwGetWindowAttrib(data.window, 131074) != 0;
        }
    }

    private final class SetWindowAlphaFunction
    extends ImPlatformFuncViewportFloat {
        final /* synthetic */ ImGuiImplGlfw this$0;

        private SetWindowAlphaFunction(ImGuiImplGlfw imGuiImplGlfw) {
            ImGuiImplGlfw imGuiImplGlfw2 = imGuiImplGlfw;
            Objects.requireNonNull(imGuiImplGlfw2);
            this.this$0 = imGuiImplGlfw2;
        }

        @Override
        public void accept(ImGuiViewport vp, float f) {
            if (this.this$0.glfwHasWindowAlpha) {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                GLFW.glfwSetWindowOpacity(data.window, f);
            }
        }
    }

    private static final class RenderWindowFunction
    extends ImPlatformFuncViewport {
        private RenderWindowFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                GLFW.glfwMakeContextCurrent(data.window);
            });
        }
    }

    private static final class SwapBuffersFunction
    extends ImPlatformFuncViewport {
        private SwapBuffersFunction() {
        }

        @Override
        public void accept(ImGuiViewport vp) {
            RenderThread.invokeOnRenderContext(() -> {
                ImGuiViewportDataGlfw data = (ImGuiViewportDataGlfw)vp.getPlatformUserData();
                GLFW.glfwMakeContextCurrent(data.window);
                GLFW.glfwSwapBuffers(data.window);
            });
        }
    }
}

