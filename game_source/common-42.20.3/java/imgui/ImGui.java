/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImDrawData;
import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGuiIO;
import imgui.ImGuiPlatformIO;
import imgui.ImGuiStorage;
import imgui.ImGuiStyle;
import imgui.ImGuiViewport;
import imgui.ImGuiWindowClass;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.assertion.ImAssertCallback;
import imgui.callback.ImGuiInputTextCallback;
import imgui.internal.ImGuiContext;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImLong;
import imgui.type.ImShort;
import imgui.type.ImString;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.Properties;

public class ImGui {
    private static final String LIB_PATH_PROP = "imgui.library.path";
    private static final String LIB_NAME_PROP = "imgui.library.name";
    private static final String LIB_NAME_DEFAULT = System.getProperty("os.arch").contains("64") ? "imgui-java64" : "imgui-java";
    private static final String LIB_TMP_DIR_PREFIX = "imgui-java-natives";
    private static final ImGuiContext IMGUI_CONTEXT;
    private static final ImGuiIO IMGUI_IO;
    private static final ImDrawList WINDOW_DRAW_LIST;
    private static final ImDrawList BACKGROUND_DRAW_LIST;
    private static final ImDrawList FOREGROUND_DRAW_LIST;
    private static final ImGuiStorage IMGUI_STORAGE;
    private static final ImGuiViewport WINDOW_VIEWPORT;
    private static final ImGuiViewport FIND_VIEWPORT;
    private static final ImDrawData DRAW_DATA;
    private static final ImFont FONT;
    private static final ImGuiStyle STYLE;
    private static final ImGuiViewport MAIN_VIEWPORT;
    private static final ImGuiPlatformIO PLATFORM_IO;
    private static WeakReference<Object> payloadRef;
    private static final byte[] PAYLOAD_PLACEHOLDER_DATA;

    private static String resolveFullLibName() {
        String libSuffix;
        String libPrefix;
        boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        if (isWin) {
            libPrefix = "";
            libSuffix = ".dll";
        } else if (isMac) {
            libPrefix = "lib";
            libSuffix = ".dylib";
        } else {
            libPrefix = "lib";
            libSuffix = ".so";
        }
        return System.getProperty(LIB_NAME_PROP, libPrefix + LIB_NAME_DEFAULT + libSuffix);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String tryLoadFromClasspath(String fullLibName) {
        try (InputStream is = ImGui.class.getClassLoader().getResourceAsStream("io/imgui/java/native-bin/" + fullLibName);){
            Path libBin;
            block21: {
                if (is == null) {
                    String string = null;
                    return string;
                }
                String version = ImGui.getVersionString().orElse("undefined");
                Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), new String[0]).resolve(LIB_TMP_DIR_PREFIX).resolve(version);
                if (!Files.exists(tmpDir, new LinkOption[0])) {
                    Files.createDirectories(tmpDir, new FileAttribute[0]);
                }
                libBin = tmpDir.resolve(fullLibName);
                try {
                    Files.copy(is, libBin, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (AccessDeniedException e) {
                    if (Files.exists(libBin, new LinkOption[0])) break block21;
                    throw e;
                }
            }
            String string = libBin.toAbsolutePath().toString();
            return string;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Optional<String> getVersionString() {
        Properties properties = new Properties();
        try (InputStream is = ImGui.class.getResourceAsStream("/imgui/imgui-java.properties");){
            if (is == null) return Optional.empty();
            properties.load(is);
            Optional<String> optional = Optional.of(properties.get("imgui.java.version").toString());
            return optional;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void init() {
    }

    private static native void nInitJni();

    public static ImGuiContext createContext() {
        ImGui.IMGUI_CONTEXT.ptr = ImGui.nCreateContext();
        return IMGUI_CONTEXT;
    }

    private static native long nCreateContext();

    public static ImGuiContext createContext(ImFontAtlas sharedFontAtlas) {
        ImGui.IMGUI_CONTEXT.ptr = ImGui.nCreateContext(sharedFontAtlas.ptr);
        return IMGUI_CONTEXT;
    }

    private static native long nCreateContext(long var0);

    public static native void destroyContext();

    public static void destroyContext(ImGuiContext ctx) {
        ImGui.nDestroyContext(ctx.ptr);
    }

    private static native void nDestroyContext(long var0);

    public static ImGuiContext getCurrentContext() {
        ImGui.IMGUI_CONTEXT.ptr = ImGui.nGetCurrentContext();
        return IMGUI_CONTEXT;
    }

    private static native long nGetCurrentContext();

    public static void setCurrentContext(ImGuiContext ctx) {
        ImGui.nSetCurrentContext(ctx.ptr);
    }

    private static native void nSetCurrentContext(long var0);

    public static native void setAssertCallback(ImAssertCallback var0);

    public static ImGuiIO getIO() {
        ImGui.IMGUI_IO.ptr = ImGui.nGetIO();
        return IMGUI_IO;
    }

    private static native long nGetIO();

    public static ImGuiStyle getStyle() {
        ImGui.STYLE.ptr = ImGui.nGetStyle();
        return STYLE;
    }

    private static native long nGetStyle();

    public static native void newFrame();

    public static native void endFrame();

    public static native void render();

    public static ImDrawData getDrawData() {
        ImGui.DRAW_DATA.ptr = ImGui.nGetDrawData();
        return DRAW_DATA;
    }

    private static native long nGetDrawData();

    public static native void showDemoWindow();

    public static void showDemoWindow(ImBoolean pOpen) {
        ImGui.nShowDemoWindow(pOpen.getData());
    }

    private static native void nShowDemoWindow(boolean[] var0);

    public static void showMetricsWindow(ImBoolean pOpen) {
        ImGui.nShowMetricsWindow(pOpen.getData());
    }

    public static native void showMetricsWindow();

    private static native void nShowMetricsWindow(boolean[] var0);

    public static void freeDrawData(ImDrawData data) {
        ImGui.nFreeDrawData(data.ptr);
    }

    private static native void nFreeDrawData(long var0);

    public static ImDrawList getCurrentDrawList() {
        ImDrawList dl = new ImDrawList(ImGui.nGetCurrentDrawList());
        if (dl.ptr == 0L) {
            return null;
        }
        return dl;
    }

    private static native long nGetCurrentDrawList();

    public static void showStackToolWindow(ImBoolean pOpen) {
        ImGui.nShowMetricsWindow(pOpen.getData());
    }

    public static native void showStackToolWindow();

    private static native void nShowStackToolWindow(boolean[] var0);

    public static native void showAboutWindow();

    public static void showAboutWindow(ImBoolean pOpen) {
        ImGui.nShowAboutWindow(pOpen.getData());
    }

    private static native void nShowAboutWindow(boolean[] var0);

    public static native void showStyleEditor();

    public static void showStyleEditor(ImGuiStyle ref) {
        ImGui.nShowStyleEditor(ref.ptr);
    }

    private static native void nShowStyleEditor(long var0);

    public static native boolean showStyleSelector(String var0);

    public static native void showFontSelector(String var0);

    public static native void showUserGuide();

    public static native String getVersion();

    public static native void styleColorsDark();

    public static void styleColorsDark(ImGuiStyle style) {
        ImGui.nStyleColorsDark(style.ptr);
    }

    private static native void nStyleColorsDark(long var0);

    public static native void styleColorsLight();

    public static void styleColorsLight(ImGuiStyle style) {
        ImGui.nStyleColorsLight(style.ptr);
    }

    private static native void nStyleColorsLight(long var0);

    public static native void styleColorsClassic();

    public static void styleColorsClassic(ImGuiStyle style) {
        ImGui.nStyleColorsClassic(style.ptr);
    }

    private static native void nStyleColorsClassic(long var0);

    public static native boolean begin(String var0);

    public static boolean begin(String title, ImBoolean pOpen) {
        return ImGui.nBegin(title, pOpen.getData(), 0);
    }

    public static boolean begin(String title, int imGuiWindowFlags) {
        return ImGui.nBegin(title, imGuiWindowFlags);
    }

    public static boolean begin(String title, ImBoolean pOpen, int imGuiWindowFlags) {
        return ImGui.nBegin(title, pOpen.getData(), imGuiWindowFlags);
    }

    private static native boolean nBegin(String var0, int var1);

    private static native boolean nBegin(String var0, boolean[] var1, int var2);

    public static native void end();

    public static native boolean beginChild(String var0);

    public static native boolean beginChild(String var0, float var1, float var2);

    public static native boolean beginChild(String var0, float var1, float var2, boolean var3);

    public static native boolean beginChild(String var0, float var1, float var2, boolean var3, int var4);

    public static native boolean beginChild(int var0);

    public static native boolean beginChild(int var0, float var1, float var2, boolean var3);

    public static native boolean beginChild(int var0, float var1, float var2, boolean var3, int var4);

    public static native void endChild();

    public static native boolean isWindowAppearing();

    public static native boolean isWindowCollapsed();

    public static native boolean isWindowFocused();

    public static native boolean isWindowFocused(int var0);

    public static native boolean isWindowHovered();

    public static native boolean isWindowHovered(int var0);

    public static ImDrawList getWindowDrawList() {
        ImGui.WINDOW_DRAW_LIST.ptr = ImGui.nGetWindowDrawList();
        return WINDOW_DRAW_LIST;
    }

    private static native long nGetWindowDrawList();

    public static native float getWindowDpiScale();

    public static ImVec2 getWindowPos() {
        ImVec2 value = new ImVec2();
        ImGui.getWindowPos(value);
        return value;
    }

    public static native void getWindowPos(ImVec2 var0);

    public static native float getWindowPosX();

    public static native float getWindowPosY();

    public static ImVec2 getWindowSize() {
        ImVec2 value = new ImVec2();
        ImGui.getWindowSize(value);
        return value;
    }

    public static native void getWindowSize(ImVec2 var0);

    public static native float getWindowSizeX();

    public static native float getWindowSizeY();

    public static native float getWindowWidth();

    public static native float getWindowHeight();

    public static ImGuiViewport getWindowViewport() {
        ImGui.WINDOW_VIEWPORT.ptr = ImGui.nGetWindowViewport();
        return WINDOW_VIEWPORT;
    }

    private static native long nGetWindowViewport();

    public static native void setNextWindowPos(float var0, float var1);

    public static native void setNextWindowPos(float var0, float var1, int var2);

    public static native void setNextWindowPos(float var0, float var1, int var2, float var3, float var4);

    public static native void setNextWindowSize(float var0, float var1);

    public static native void setNextWindowSize(float var0, float var1, int var2);

    public static native void setNextWindowSizeConstraints(float var0, float var1, float var2, float var3);

    public static native void setNextWindowContentSize(float var0, float var1);

    public static native void setNextWindowCollapsed(boolean var0);

    public static native void setNextWindowCollapsed(boolean var0, int var1);

    public static native void setNextWindowFocus();

    public static native void setNextWindowBgAlpha(float var0);

    public static native void setNextWindowViewport(int var0);

    public static native void setWindowPos(float var0, float var1);

    public static native void setWindowPos(float var0, float var1, int var2);

    public static native void setWindowSize(float var0, float var1);

    public static native void setWindowSize(float var0, float var1, int var2);

    public static native void setWindowCollapsed(boolean var0);

    public static native void setWindowCollapsed(boolean var0, int var1);

    public static native void setWindowFocus();

    public native void setWindowFontScale(float var1);

    public static native void setWindowPos(String var0, float var1, float var2);

    public static native void setWindowPos(String var0, float var1, float var2, int var3);

    public static native void setWindowSize(String var0, float var1, float var2);

    public static native void setWindowSize(String var0, float var1, float var2, int var3);

    public static native void setWindowCollapsed(String var0, boolean var1);

    public static native void setWindowCollapsed(String var0, boolean var1, int var2);

    public static native void setWindowFocus(String var0);

    public static ImVec2 getContentRegionAvail() {
        ImVec2 value = new ImVec2();
        ImGui.getContentRegionAvail(value);
        return value;
    }

    public static native void getContentRegionAvail(ImVec2 var0);

    public static native float getContentRegionAvailX();

    public static native float getContentRegionAvailY();

    public static ImVec2 getContentRegionMax() {
        ImVec2 value = new ImVec2();
        ImGui.getContentRegionMax(value);
        return value;
    }

    public static native void getContentRegionMax(ImVec2 var0);

    public static native float getContentRegionMaxX();

    public static native float getContentRegionMaxY();

    public static ImVec2 getWindowContentRegionMin() {
        ImVec2 value = new ImVec2();
        ImGui.getWindowContentRegionMin(value);
        return value;
    }

    public static native void getWindowContentRegionMin(ImVec2 var0);

    public static native float getWindowContentRegionMinX();

    public static native float getWindowContentRegionMinY();

    public static ImVec2 getWindowContentRegionMax() {
        ImVec2 value = new ImVec2();
        ImGui.getWindowContentRegionMax(value);
        return value;
    }

    public static native void getWindowContentRegionMax(ImVec2 var0);

    public static native float getWindowContentRegionMaxX();

    public static native float getWindowContentRegionMaxY();

    public static native float getScrollX();

    public static native float getScrollY();

    public static native void setScrollX(float var0);

    public static native void setScrollY(float var0);

    public static native float getScrollMaxX();

    public static native float getScrollMaxY();

    public static native void setScrollHereX();

    public static native void setScrollHereX(float var0);

    public static native void setScrollHereY();

    public static native void setScrollHereY(float var0);

    public static native void setScrollFromPosX(float var0);

    public static native void setScrollFromPosX(float var0, float var1);

    public static native void setScrollFromPosY(float var0);

    public static native void setScrollFromPosY(float var0, float var1);

    public static void pushFont(ImFont font) {
        ImGui.nPushFont(font.ptr);
    }

    private static native void nPushFont(long var0);

    public static native void popFont();

    public static native void pushStyleColor(int var0, float var1, float var2, float var3, float var4);

    public static native void pushStyleColor(int var0, int var1, int var2, int var3, int var4);

    public static native void pushStyleColor(int var0, int var1);

    public static native void popStyleColor();

    public static native void popStyleColor(int var0);

    public static native void pushStyleVar(int var0, float var1);

    public static native void pushStyleVar(int var0, float var1, float var2);

    public static native void popStyleVar();

    public static native void popStyleVar(int var0);

    public static native void pushAllowKeyboardFocus(boolean var0);

    public static native void popAllowKeyboardFocus();

    public static native void pushButtonRepeat(boolean var0);

    public static native void popButtonRepeat();

    public static native void pushItemWidth(float var0);

    public static native void popItemWidth();

    public static native void setNextItemWidth(float var0);

    public static native float calcItemWidth();

    public static native void pushTextWrapPos();

    public static native void pushTextWrapPos(float var0);

    public static native void popTextWrapPos();

    public static ImFont getFont() {
        ImGui.FONT.ptr = ImGui.nGetFont();
        return FONT;
    }

    private static native long nGetFont();

    public static native int getFontSize();

    public static ImVec2 getFontTexUvWhitePixel() {
        ImVec2 value = new ImVec2();
        ImGui.getFontTexUvWhitePixel(value);
        return value;
    }

    public static native void getFontTexUvWhitePixel(ImVec2 var0);

    public static native float getFontTexUvWhitePixelX();

    public static native float getFontTexUvWhitePixelY();

    public static native int getColorU32(int var0);

    public static native int getColorU32(int var0, float var1);

    public static native int getColorU32(float var0, float var1, float var2, float var3);

    public static native int getColorU32i(int var0);

    public ImVec4 getStyleColorVec4(int imGuiStyleVar) {
        ImVec4 value = new ImVec4();
        ImGui.getStyleColorVec4(imGuiStyleVar, value);
        return value;
    }

    public static native void getStyleColorVec4(int var0, ImVec4 var1);

    public static native void separator();

    public static native void sameLine();

    public static native void sameLine(float var0);

    public static native void sameLine(float var0, float var1);

    public static native void newLine();

    public static native void spacing();

    public static native void dummy(float var0, float var1);

    public static native void indent();

    public static native void indent(float var0);

    public static native void unindent();

    public static native void unindent(float var0);

    public static native void beginGroup();

    public static native void endGroup();

    public static ImVec2 getCursorPos() {
        ImVec2 value = new ImVec2();
        ImGui.getCursorPos(value);
        return value;
    }

    public static native void getCursorPos(ImVec2 var0);

    public static native float getCursorPosX();

    public static native float getCursorPosY();

    public static native void setCursorPos(float var0, float var1);

    public static native void setCursorPosX(float var0);

    public static native void setCursorPosY(float var0);

    public static ImVec2 getCursorStartPos() {
        ImVec2 value = new ImVec2();
        ImGui.getCursorStartPos(value);
        return value;
    }

    public static native void getCursorStartPos(ImVec2 var0);

    public static native float getCursorStartPosX();

    public static native float getCursorStartPosY();

    public static ImVec2 getCursorScreenPos() {
        ImVec2 value = new ImVec2();
        ImGui.getCursorScreenPos(value);
        return value;
    }

    public static native void getCursorScreenPos(ImVec2 var0);

    public static native float getCursorScreenPosX();

    public static native float getCursorScreenPosY();

    public static native void setCursorScreenPos(float var0, float var1);

    public static native void alignTextToFramePadding();

    public static native float getTextLineHeight();

    public static native float getTextLineHeightWithSpacing();

    public static native float getFrameHeight();

    public static native float getFrameHeightWithSpacing();

    public static native void pushID(String var0);

    public static native void pushID(String var0, String var1);

    public static native void pushID(long var0);

    public static native void pushID(int var0);

    public static native void popID();

    public static native int getID(String var0);

    public static native int getID(String var0, String var1);

    public static native int getID(long var0);

    public static native void textUnformatted(String var0);

    public static native void text(String var0);

    public static native void textColored(float var0, float var1, float var2, float var3, String var4);

    public static native void textColored(int var0, int var1, int var2, int var3, String var4);

    public static native void textColored(int var0, String var1);

    public static native void textDisabled(String var0);

    public static native void textWrapped(String var0);

    public static native void labelText(String var0, String var1);

    public static native void bulletText(String var0);

    public static native boolean button(String var0);

    public static native boolean button(String var0, float var1, float var2);

    public static native boolean smallButton(String var0);

    public static native boolean invisibleButton(String var0, float var1, float var2);

    public static native boolean invisibleButton(String var0, float var1, float var2, int var3);

    public static native boolean arrowButton(String var0, int var1);

    public static native void image(int var0, float var1, float var2);

    public static native void image(int var0, float var1, float var2, float var3, float var4);

    public static native void image(int var0, float var1, float var2, float var3, float var4, float var5, float var6);

    public static native void image(int var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10);

    public static native void image(int var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14);

    public static native boolean imageButton(int var0, float var1, float var2);

    public static native boolean imageButton(int var0, float var1, float var2, float var3, float var4);

    public static native boolean imageButton(int var0, float var1, float var2, float var3, float var4, float var5, float var6);

    public static native boolean imageButton(int var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7);

    public static native boolean imageButton(int var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, float var9, float var10, float var11);

    public static native boolean imageButton(int var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15);

    public static native boolean checkbox(String var0, boolean var1);

    public static boolean checkbox(String label, ImBoolean active) {
        return ImGui.nCheckbox(label, active.getData());
    }

    private static native boolean nCheckbox(String var0, boolean[] var1);

    public static boolean checkboxFlags(String label, ImInt v, int flagsValue) {
        return ImGui.nCheckboxFlags(label, v.getData(), flagsValue);
    }

    private static native boolean nCheckboxFlags(String var0, int[] var1, int var2);

    public static native boolean radioButton(String var0, boolean var1);

    public static boolean radioButton(String label, ImInt v, int vButton) {
        return ImGui.nRadioButton(label, v.getData(), vButton);
    }

    private static native boolean nRadioButton(String var0, int[] var1, int var2);

    public static native void progressBar(float var0);

    public static native void progressBar(float var0, float var1, float var2);

    public static native void progressBar(float var0, float var1, float var2, String var3);

    public static native void bullet();

    public static native boolean beginCombo(String var0, String var1);

    public static native boolean beginCombo(String var0, String var1, int var2);

    public static native void endCombo();

    public static boolean combo(String label, ImInt currentItem, String[] items) {
        return ImGui.nCombo(label, currentItem.getData(), items, items.length, -1);
    }

    public static boolean combo(String label, ImInt currentItem, String[] items, int popupMaxHeightInItems) {
        return ImGui.nCombo(label, currentItem.getData(), items, items.length, popupMaxHeightInItems);
    }

    private static native boolean nCombo(String var0, int[] var1, String[] var2, int var3, int var4);

    public static boolean combo(String label, ImInt currentItem, String itemsSeparatedByZeros) {
        return ImGui.nCombo(label, currentItem.getData(), itemsSeparatedByZeros, -1);
    }

    public static boolean combo(String label, ImInt currentItem, String itemsSeparatedByZeros, int popupMaxHeightInItems) {
        return ImGui.nCombo(label, currentItem.getData(), itemsSeparatedByZeros, popupMaxHeightInItems);
    }

    private static native boolean nCombo(String var0, int[] var1, String var2, int var3);

    public static native boolean dragFloat(String var0, float[] var1);

    public static native boolean dragFloat(String var0, float[] var1, float var2);

    public static native boolean dragFloat(String var0, float[] var1, float var2, float var3, float var4);

    public static native boolean dragFloat(String var0, float[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragFloat(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    public static native boolean dragFloat2(String var0, float[] var1);

    public static native boolean dragFloat2(String var0, float[] var1, float var2);

    public static native boolean dragFloat2(String var0, float[] var1, float var2, float var3);

    public static native boolean dragFloat2(String var0, float[] var1, float var2, float var3, float var4);

    public static native boolean dragFloat2(String var0, float[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragFloat2(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    public static native boolean dragFloat3(String var0, float[] var1);

    public static native boolean dragFloat3(String var0, float[] var1, float var2);

    public static native boolean dragFloat3(String var0, float[] var1, float var2, float var3);

    public static native boolean dragFloat3(String var0, float[] var1, float var2, float var3, float var4);

    public static native boolean dragFloat3(String var0, float[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragFloat3(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    public static native boolean dragFloat4(String var0, float[] var1);

    public static native boolean dragFloat4(String var0, float[] var1, float var2);

    public static native boolean dragFloat4(String var0, float[] var1, float var2, float var3);

    public static native boolean dragFloat4(String var0, float[] var1, float var2, float var3, float var4);

    public static native boolean dragFloat4(String var0, float[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragFloat4(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6, String var7);

    public static native boolean dragFloatRange2(String var0, float[] var1, float[] var2, float var3, float var4, float var5, String var6, String var7, int var8);

    public static native boolean dragInt(String var0, int[] var1);

    public static native boolean dragInt(String var0, int[] var1, float var2);

    public static native boolean dragInt(String var0, int[] var1, float var2, float var3);

    public static native boolean dragInt(String var0, int[] var1, float var2, float var3, float var4);

    public static native boolean dragInt(String var0, int[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragInt2(String var0, int[] var1);

    public static native boolean dragInt2(String var0, int[] var1, float var2);

    public static native boolean dragInt2(String var0, int[] var1, float var2, float var3);

    public static native boolean dragInt2(String var0, int[] var1, float var2, float var3, float var4);

    public static native boolean dragInt2(String var0, int[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragInt3(String var0, int[] var1);

    public static native boolean dragInt3(String var0, int[] var1, float var2);

    public static native boolean dragInt3(String var0, int[] var1, float var2, float var3);

    public static native boolean dragInt3(String var0, int[] var1, float var2, float var3, float var4);

    public static native boolean dragInt3(String var0, int[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragInt4(String var0, int[] var1);

    public static native boolean dragInt4(String var0, int[] var1, float var2);

    public static native boolean dragInt4(String var0, int[] var1, float var2, float var3);

    public static native boolean dragInt4(String var0, int[] var1, float var2, float var3, float var4);

    public static native boolean dragInt4(String var0, int[] var1, float var2, float var3, float var4, String var5);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2, float var3);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2, float var3, float var4);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2, float var3, float var4, float var5);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2, float var3, float var4, float var5, String var6);

    public static native boolean dragIntRange2(String var0, int[] var1, int[] var2, float var3, float var4, float var5, String var6, String var7);

    public static boolean dragScalar(String label, int dataType, ImInt pData, float vSpeed) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed);
    }

    private static native boolean nDragScalar(String var0, int var1, int[] var2, float var3);

    public static boolean dragScalar(String label, int dataType, ImInt pData, float vSpeed, int pMin) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin);
    }

    private static native boolean nDragScalar(String var0, int var1, int[] var2, float var3, int var4);

    public static boolean dragScalar(String label, int dataType, ImInt pData, float vSpeed, int pMin, int pMax) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalar(String var0, int var1, int[] var2, float var3, int var4, int var5);

    public static boolean dragScalar(String label, int dataType, ImInt pData, float vSpeed, int pMin, int pMax, String format) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalar(String label, int dataType, ImInt pData, float vSpeed, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int var1, int[] var2, float var3, int var4, int var5, String var6, int var7);

    public static boolean dragScalar(String label, int dataType, ImFloat pData, float vSpeed) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed);
    }

    private static native boolean nDragScalar(String var0, int var1, float[] var2, float var3);

    public static boolean dragScalar(String label, int dataType, ImFloat pData, float vSpeed, float pMin) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin);
    }

    private static native boolean nDragScalar(String var0, int var1, float[] var2, float var3, float var4);

    public static boolean dragScalar(String label, int dataType, ImFloat pData, float vSpeed, float pMin, float pMax) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalar(String var0, int var1, float[] var2, float var3, float var4, float var5);

    public static boolean dragScalar(String label, int dataType, ImFloat pData, float vSpeed, float pMin, float pMax, String format) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalar(String label, int dataType, ImFloat pData, float vSpeed, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int var1, float[] var2, float var3, float var4, float var5, String var6, int var7);

    public static boolean dragScalar(String label, int dataType, ImDouble pData, float vSpeed) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed);
    }

    private static native boolean nDragScalar(String var0, int var1, double[] var2, float var3);

    public static boolean dragScalar(String label, int dataType, ImDouble pData, float vSpeed, double pMin) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin);
    }

    private static native boolean nDragScalar(String var0, int var1, double[] var2, float var3, double var4);

    public static boolean dragScalar(String label, int dataType, ImDouble pData, float vSpeed, double pMin, double pMax) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalar(String var0, int var1, double[] var2, float var3, double var4, double var6);

    public static boolean dragScalar(String label, int dataType, ImDouble pData, float vSpeed, double pMin, double pMax, String format) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalar(String label, int dataType, ImDouble pData, float vSpeed, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int var1, double[] var2, float var3, double var4, double var6, String var8, int var9);

    public static boolean dragScalar(String label, int dataType, ImLong pData, float vSpeed) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed);
    }

    private static native boolean nDragScalar(String var0, int var1, long[] var2, float var3);

    public static boolean dragScalar(String label, int dataType, ImLong pData, float vSpeed, long pMin) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin);
    }

    private static native boolean nDragScalar(String var0, int var1, long[] var2, float var3, long var4);

    public static boolean dragScalar(String label, int dataType, ImLong pData, float vSpeed, long pMin, long pMax) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalar(String var0, int var1, long[] var2, float var3, long var4, long var6);

    public static boolean dragScalar(String label, int dataType, ImLong pData, float vSpeed, long pMin, long pMax, String format) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalar(String label, int dataType, ImLong pData, float vSpeed, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int var1, long[] var2, float var3, long var4, long var6, String var8, int var9);

    public static boolean dragScalar(String label, int dataType, ImShort pData, float vSpeed) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed);
    }

    private static native boolean nDragScalar(String var0, int var1, short[] var2, float var3);

    public static boolean dragScalar(String label, int dataType, ImShort pData, float vSpeed, short pMin) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin);
    }

    private static native boolean nDragScalar(String var0, int var1, short[] var2, float var3, short var4);

    public static boolean dragScalar(String label, int dataType, ImShort pData, float vSpeed, short pMin, short pMax) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalar(String var0, int var1, short[] var2, float var3, short var4, short var5);

    public static boolean dragScalar(String label, int dataType, ImShort pData, float vSpeed, short pMin, short pMax, String format) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalar(String label, int dataType, ImShort pData, float vSpeed, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalar(label, dataType, pData.getData(), vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalar(String var0, int var1, short[] var2, float var3, short var4, short var5, String var6, int var7);

    public static boolean dragScalarN(String label, int dataType, ImInt pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed);
    }

    private static native boolean nDragScalarN(String var0, int var1, int[] var2, int var3, float var4);

    public static boolean dragScalarN(String label, int dataType, ImInt pData, int components, float vSpeed, int pMin) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin);
    }

    private static native boolean nDragScalarN(String var0, int var1, int[] var2, int var3, float var4, int var5);

    public static boolean dragScalarN(String label, int dataType, ImInt pData, int components, float vSpeed, int pMin, int pMax) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalarN(String var0, int var1, int[] var2, int var3, float var4, int var5, int var6);

    public static boolean dragScalarN(String label, int dataType, ImInt pData, int components, float vSpeed, int pMin, int pMax, String format) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalarN(String label, int dataType, ImInt pData, int components, float vSpeed, int pMin, int pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int var1, int[] var2, int var3, float var4, int var5, int var6, String var7, int var8);

    public static boolean dragScalarN(String label, int dataType, ImFloat pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed);
    }

    private static native boolean nDragScalarN(String var0, int var1, float[] var2, int var3, float var4);

    public static boolean dragScalarN(String label, int dataType, ImFloat pData, int components, float vSpeed, float pMin) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin);
    }

    private static native boolean nDragScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5);

    public static boolean dragScalarN(String label, int dataType, ImFloat pData, int components, float vSpeed, float pMin, float pMax) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, float var6);

    public static boolean dragScalarN(String label, int dataType, ImFloat pData, int components, float vSpeed, float pMin, float pMax, String format) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalarN(String label, int dataType, ImFloat pData, int components, float vSpeed, float pMin, float pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, float var6, String var7, int var8);

    public static boolean dragScalarN(String label, int dataType, ImDouble pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed);
    }

    private static native boolean nDragScalarN(String var0, int var1, double[] var2, int var3, float var4);

    public static boolean dragScalarN(String label, int dataType, ImDouble pData, int components, float vSpeed, double pMin) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin);
    }

    private static native boolean nDragScalarN(String var0, int var1, double[] var2, int var3, float var4, double var5);

    public static boolean dragScalarN(String label, int dataType, ImDouble pData, int components, float vSpeed, double pMin, double pMax) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalarN(String var0, int var1, double[] var2, int var3, float var4, double var5, double var7);

    public static boolean dragScalarN(String label, int dataType, ImDouble pData, int components, float vSpeed, double pMin, double pMax, String format) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalarN(String label, int dataType, ImDouble pData, int components, float vSpeed, double pMin, double pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int var1, double[] var2, int var3, float var4, double var5, double var7, String var9, int var10);

    public static boolean dragScalarN(String label, int dataType, ImLong pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed);
    }

    private static native boolean nDragScalarN(String var0, int var1, long[] var2, int var3, float var4);

    public static boolean dragScalarN(String label, int dataType, ImLong pData, int components, float vSpeed, long pMin) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin);
    }

    private static native boolean nDragScalarN(String var0, int var1, long[] var2, int var3, float var4, long var5);

    public static boolean dragScalarN(String label, int dataType, ImLong pData, int components, float vSpeed, long pMin, long pMax) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalarN(String var0, int var1, long[] var2, int var3, float var4, long var5, long var7);

    public static boolean dragScalarN(String label, int dataType, ImLong pData, int components, float vSpeed, long pMin, long pMax, String format) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalarN(String label, int dataType, ImLong pData, int components, float vSpeed, long pMin, long pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int var1, long[] var2, int var3, float var4, long var5, long var7, String var9, int var10);

    public static boolean dragScalarN(String label, int dataType, ImShort pData, int components, float vSpeed) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed);
    }

    private static native boolean nDragScalarN(String var0, int var1, short[] var2, int var3, float var4);

    public static boolean dragScalarN(String label, int dataType, ImShort pData, int components, float vSpeed, short pMin) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin);
    }

    private static native boolean nDragScalarN(String var0, int var1, short[] var2, int var3, float var4, short var5);

    public static boolean dragScalarN(String label, int dataType, ImShort pData, int components, float vSpeed, short pMin, short pMax) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax);
    }

    private static native boolean nDragScalarN(String var0, int var1, short[] var2, int var3, float var4, short var5, short var6);

    public static boolean dragScalarN(String label, int dataType, ImShort pData, int components, float vSpeed, short pMin, short pMax, String format) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, 0);
    }

    public static boolean dragScalarN(String label, int dataType, ImShort pData, int components, float vSpeed, short pMin, short pMax, String format, int imGuiSliderFlags) {
        return ImGui.nDragScalarN(label, dataType, pData.getData(), components, vSpeed, pMin, pMax, format, imGuiSliderFlags);
    }

    private static native boolean nDragScalarN(String var0, int var1, short[] var2, int var3, float var4, short var5, short var6, String var7, int var8);

    public static native boolean sliderFloat(String var0, float[] var1, float var2, float var3);

    public static native boolean sliderFloat(String var0, float[] var1, float var2, float var3, String var4);

    public static native boolean sliderFloat(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static native boolean sliderFloat2(String var0, float[] var1, float var2, float var3);

    public static native boolean sliderFloat2(String var0, float[] var1, float var2, float var3, String var4);

    public static native boolean sliderFloat2(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static native boolean sliderFloat3(String var0, float[] var1, float var2, float var3);

    public static native boolean sliderFloat3(String var0, float[] var1, float var2, float var3, String var4);

    public static native boolean sliderFloat3(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static native boolean sliderFloat4(String var0, float[] var1, float var2, float var3);

    public static native boolean sliderFloat4(String var0, float[] var1, float var2, float var3, String var4);

    public static native boolean sliderFloat4(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static native boolean sliderAngle(String var0, float[] var1);

    public static native boolean sliderAngle(String var0, float[] var1, float var2);

    public static native boolean sliderAngle(String var0, float[] var1, float var2, float var3);

    public static native boolean sliderAngle(String var0, float[] var1, float var2, float var3, String var4);

    public static native boolean sliderInt(String var0, int[] var1, int var2, int var3);

    public static native boolean sliderInt(String var0, int[] var1, int var2, int var3, String var4);

    public static native boolean sliderInt2(String var0, int[] var1, int var2, int var3);

    public static native boolean sliderInt2(String var0, int[] var1, int var2, int var3, String var4);

    public static native boolean sliderInt3(String var0, int[] var1, int var2, int var3);

    public static native boolean sliderInt3(String var0, int[] var1, int var2, int var3, String var4);

    public static native boolean sliderInt4(String var0, int[] var1, int var2, int var3);

    public static native boolean sliderInt4(String var0, int[] var1, int var2, int var3, String var4);

    public static boolean sliderScalar(String label, int dataType, ImInt v, int vMin, int vMax) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalar(String var0, int var1, int[] var2, int var3, int var4);

    public static boolean sliderScalar(String label, int dataType, ImInt v, int vMin, int vMax, String format) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalar(String label, int dataType, ImInt v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int var1, int[] var2, int var3, int var4, String var5, int var6);

    public static boolean sliderScalar(String label, int dataType, ImFloat v, float vMin, float vMax) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalar(String var0, int var1, float[] var2, float var3, float var4);

    public static boolean sliderScalar(String label, int dataType, ImFloat v, float vMin, float vMax, String format) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalar(String label, int dataType, ImFloat v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int var1, float[] var2, float var3, float var4, String var5, int var6);

    public static boolean sliderScalar(String label, int dataType, ImLong v, long vMin, long vMax) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalar(String var0, int var1, long[] var2, long var3, long var5);

    public static boolean sliderScalar(String label, int dataType, ImLong v, long vMin, long vMax, String format) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalar(String label, int dataType, ImLong v, long vMin, long vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int var1, long[] var2, long var3, long var5, String var7, int var8);

    public static boolean sliderScalar(String label, int dataType, ImDouble v, double vMin, double vMax) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalar(String var0, int var1, double[] var2, double var3, double var5);

    public static boolean sliderScalar(String label, int dataType, ImDouble v, double vMin, double vMax, String format) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalar(String label, int dataType, ImDouble v, double vMin, double vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int var1, double[] var2, double var3, double var5, String var7, int var8);

    public static boolean sliderScalar(String label, int dataType, ImShort v, short vMin, short vMax) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalar(String var0, int var1, short[] var2, short var3, short var4);

    public static boolean sliderScalar(String label, int dataType, ImShort v, short vMin, short vMax, String format) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalar(String label, int dataType, ImShort v, short vMin, short vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalar(label, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalar(String var0, int var1, short[] var2, short var3, short var4, String var5, int var6);

    public static boolean sliderScalarN(String label, int dataType, int components, ImInt v, int vMin, int vMax) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, int[] var3, int var4, int var5);

    public static boolean sliderScalarN(String label, int dataType, int components, ImInt v, int vMin, int vMax, String format) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalarN(String label, int dataType, int components, ImInt v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, int[] var3, int var4, int var5, String var6, int var7);

    public static boolean sliderScalarN(String label, int dataType, int components, ImFloat v, float vMin, float vMax) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, float[] var3, float var4, float var5);

    public static boolean sliderScalarN(String label, int dataType, int components, ImFloat v, float vMin, float vMax, String format) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalarN(String label, int dataType, int components, ImFloat v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, float[] var3, float var4, float var5, String var6, int var7);

    public static boolean sliderScalarN(String label, int dataType, int components, ImLong v, long vMin, long vMax) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, long[] var3, long var4, long var6);

    public static boolean sliderScalarN(String label, int dataType, int components, ImLong v, long vMin, long vMax, String format) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalarN(String label, int dataType, int components, ImLong v, long vMin, long vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, long[] var3, long var4, long var6, String var8, int var9);

    public static boolean sliderScalarN(String label, int dataType, int components, ImDouble v, double vMin, double vMax) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, double[] var3, double var4, double var6);

    public static boolean sliderScalarN(String label, int dataType, int components, ImDouble v, double vMin, double vMax, String format) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalarN(String label, int dataType, int components, ImDouble v, double vMin, double vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, double[] var3, double var4, double var6, String var8, int var9);

    public static boolean sliderScalarN(String label, int dataType, int components, ImShort v, short vMin, short vMax) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, short[] var3, short var4, short var5);

    public static boolean sliderScalarN(String label, int dataType, int components, ImShort v, short vMin, short vMax, String format) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean sliderScalarN(String label, int dataType, int components, ImShort v, short vMin, short vMax, String format, int imGuiSliderFlags) {
        return ImGui.nSliderScalarN(label, dataType, components, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nSliderScalarN(String var0, int var1, int var2, short[] var3, short var4, short var5, String var6, int var7);

    public static native boolean vSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5);

    public static native boolean vSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6);

    public static native boolean vSliderFloat(String var0, float var1, float var2, float[] var3, float var4, float var5, String var6, int var7);

    public static native boolean vSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5);

    public static native boolean vSliderInt(String var0, float var1, float var2, int[] var3, int var4, int var5, String var6);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImInt v, int vMin, int vMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, int[] var4, int var5, int var6);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImInt v, int vMin, int vMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImInt v, int vMin, int vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, int[] var4, int var5, int var6, String var7, int var8);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImFloat v, float vMin, float vMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, float[] var4, float var5, float var6);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImFloat v, float vMin, float vMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImFloat v, float vMin, float vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, float[] var4, float var5, float var6, String var7, int var8);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImLong v, long vMin, long vMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, long[] var4, long var5, long var7);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImLong v, long vMin, long vMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImLong v, long vMin, long vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, long[] var4, long var5, long var7, String var9, int var10);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImDouble v, double vMin, double vMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, double[] var4, double var5, double var7);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImDouble v, double vMin, double vMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImDouble v, double vMin, double vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, double[] var4, double var5, double var7, String var9, int var10);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImShort v, short vMin, short vMax) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, short[] var4, short var5, short var6);

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImShort v, short vMin, short vMax, String format) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, 0);
    }

    public static boolean vSliderScalar(String label, float sizeX, float sizeY, int dataType, ImShort v, short vMin, short vMax, String format, int imGuiSliderFlags) {
        return ImGui.nVSliderScalar(label, sizeX, sizeY, dataType, v.getData(), vMin, vMax, format, imGuiSliderFlags);
    }

    private static native boolean nVSliderScalar(String var0, float var1, float var2, int var3, short[] var4, short var5, short var6, String var7, int var8);

    private static native void nInitInputTextData();

    public static boolean inputText(String label, ImString text) {
        return ImGui.preInputText(false, label, null, text);
    }

    public static boolean inputText(String label, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(false, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputText(String label, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(false, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextMultiline(String label, ImString text) {
        return ImGui.preInputText(true, label, null, text);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height) {
        return ImGui.preInputText(true, label, null, text, width, height);
    }

    public static boolean inputTextMultiline(String label, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(true, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(true, label, null, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height, int imGuiInputTextFlags) {
        return ImGui.preInputText(true, label, null, text, width, height, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(true, label, null, text, width, height, imGuiInputTextFlags, callback);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text) {
        return ImGui.preInputText(false, label, hint, text);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text, int imGuiInputTextFlags) {
        return ImGui.preInputText(false, label, hint, text, 0.0f, 0.0f, imGuiInputTextFlags);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text, int imGuiInputTextFlags, ImGuiInputTextCallback callback) {
        return ImGui.preInputText(false, label, hint, text, 0.0f, 0.0f, imGuiInputTextFlags, callback);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text) {
        return ImGui.preInputText(multiline, label, hint, text, 0.0f, 0.0f);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height) {
        return ImGui.preInputText(multiline, label, hint, text, width, height, 0);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height, int flags) {
        return ImGui.preInputText(multiline, label, hint, text, width, height, flags, null);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height, int flags, ImGuiInputTextCallback callback) {
        String hintLabel;
        ImString.InputData inputData = text.inputData;
        if (inputData.isResizable) {
            flags |= 0x40000;
        }
        if (!inputData.allowedChars.isEmpty()) {
            flags |= 0x200;
        }
        if ((hintLabel = hint) == null) {
            hintLabel = "";
        }
        return ImGui.nInputText(multiline, hint != null, label, hintLabel, text, text.getData(), text.getData().length, width, height, flags, inputData, inputData.allowedChars, callback);
    }

    private static native boolean nInputText(boolean var0, boolean var1, String var2, String var3, ImString var4, byte[] var5, int var6, float var7, float var8, int var9, ImString.InputData var10, String var11, ImGuiInputTextCallback var12);

    public static boolean inputFloat(String label, ImFloat v) {
        return ImGui.nInputFloat(label, v.getData(), 0.0f, 0.0f, "%.3f", 0);
    }

    public static boolean inputFloat(String label, ImFloat v, float step) {
        return ImGui.nInputFloat(label, v.getData(), step, 0.0f, "%.3f", 0);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast) {
        return ImGui.nInputFloat(label, v.getData(), step, stepFast, "%.3f", 0);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast, String format) {
        return ImGui.nInputFloat(label, v.getData(), step, stepFast, format, 0);
    }

    public static boolean inputFloat(String label, ImFloat v, float step, float stepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputFloat(label, v.getData(), step, stepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputFloat(String var0, float[] var1, float var2, float var3, String var4, int var5);

    public static native boolean inputFloat2(String var0, float[] var1);

    public static native boolean inputFloat2(String var0, float[] var1, String var2);

    public static native boolean inputFloat2(String var0, float[] var1, String var2, int var3);

    public static native boolean inputFloat3(String var0, float[] var1);

    public static native boolean inputFloat3(String var0, float[] var1, String var2);

    public static native boolean inputFloat3(String var0, float[] var1, String var2, int var3);

    public static native boolean inputFloat4(String var0, float[] var1);

    public static native boolean inputFloat4(String var0, float[] var1, String var2);

    public static native boolean inputFloat4(String var0, float[] var1, String var2, int var3);

    public static boolean inputInt(String label, ImInt v) {
        return ImGui.nInputInt(label, v.getData(), 1, 100, 0);
    }

    public static boolean inputInt(String label, ImInt v, int step) {
        return ImGui.nInputInt(label, v.getData(), step, 100, 0);
    }

    public static boolean inputInt(String label, ImInt v, int step, int stepFast) {
        return ImGui.nInputInt(label, v.getData(), step, stepFast, 0);
    }

    public static boolean inputInt(String label, ImInt v, int step, int stepFast, int imGuiInputTextFlags) {
        return ImGui.nInputInt(label, v.getData(), step, stepFast, imGuiInputTextFlags);
    }

    private static native boolean nInputInt(String var0, int[] var1, int var2, int var3, int var4);

    public static native boolean inputInt2(String var0, int[] var1);

    public static native boolean inputInt2(String var0, int[] var1, int var2);

    public static native boolean inputInt3(String var0, int[] var1);

    public static native boolean inputInt3(String var0, int[] var1, int var2);

    public static native boolean inputInt4(String var0, int[] var1);

    public static native boolean inputInt4(String var0, int[] var1, int var2);

    public static boolean inputDouble(String label, ImDouble v) {
        return ImGui.nInputDouble(label, v.getData(), 0.0, 0.0, "%.6f", 0);
    }

    public static boolean inputDouble(String label, ImDouble v, double step) {
        return ImGui.nInputDouble(label, v.getData(), step, 0.0, "%.6f", 0);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast) {
        return ImGui.nInputDouble(label, v.getData(), step, stepFast, "%.6f", 0);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast, String format) {
        return ImGui.nInputDouble(label, v.getData(), step, stepFast, format, 0);
    }

    public static boolean inputDouble(String label, ImDouble v, double step, double stepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputDouble(label, v.getData(), step, stepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputDouble(String var0, double[] var1, double var2, double var4, String var6, int var7);

    public static boolean inputScalar(String label, int dataType, ImInt pData) {
        return ImGui.nInputScalar(label, dataType, pData.getData());
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2);

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep);
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3);

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast);
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4);

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format);
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4, String var5);

    public static boolean inputScalar(String label, int dataType, ImInt pData, int pStep, int pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, int[] var2, int var3, int var4, String var5, int var6);

    public static boolean inputScalar(String label, int dataType, ImFloat pData) {
        return ImGui.nInputScalar(label, dataType, pData.getData());
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2);

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep);
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3);

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast);
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4);

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format);
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4, String var5);

    public static boolean inputScalar(String label, int dataType, ImFloat pData, float pStep, float pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, float[] var2, float var3, float var4, String var5, int var6);

    public static boolean inputScalar(String label, int dataType, ImLong pData) {
        return ImGui.nInputScalar(label, dataType, pData.getData());
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2);

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep);
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3);

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast);
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5);

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format);
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5, String var7);

    public static boolean inputScalar(String label, int dataType, ImLong pData, long pStep, long pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, long[] var2, long var3, long var5, String var7, int var8);

    public static boolean inputScalar(String label, int dataType, ImDouble pData) {
        return ImGui.nInputScalar(label, dataType, pData.getData());
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2);

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep);
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3);

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast);
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5);

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format);
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5, String var7);

    public static boolean inputScalar(String label, int dataType, ImDouble pData, double pStep, double pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, double[] var2, double var3, double var5, String var7, int var8);

    public static boolean inputScalar(String label, int dataType, ImShort pData) {
        return ImGui.nInputScalar(label, dataType, pData.getData());
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2);

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep);
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3);

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast);
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4);

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format);
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4, String var5);

    public static boolean inputScalar(String label, int dataType, ImShort pData, short pStep, short pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalar(label, dataType, pData.getData(), pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalar(String var0, int var1, short[] var2, short var3, short var4, String var5, int var6);

    public static boolean inputScalarN(String label, int dataType, ImInt pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3);

    public static boolean inputScalarN(String label, int dataType, ImInt pData, int components, int pStep) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4);

    public static boolean inputScalarN(String label, int dataType, ImInt pData, int components, int pStep, int pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5);

    public static boolean inputScalarN(String label, int dataType, ImInt pData, int components, int pStep, int pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5, String var6);

    public static boolean inputScalarN(String label, int dataType, ImInt pData, int components, int pStep, int pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, int[] var2, int var3, int var4, int var5, String var6, int var7);

    public static boolean inputScalarN(String label, int dataType, ImFloat pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3);

    public static boolean inputScalarN(String label, int dataType, ImFloat pData, int components, float pStep) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4);

    public static boolean inputScalarN(String label, int dataType, ImFloat pData, int components, float pStep, float pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5);

    public static boolean inputScalarN(String label, int dataType, ImFloat pData, int components, float pStep, float pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, String var6);

    public static boolean inputScalarN(String label, int dataType, ImFloat pData, int components, float pStep, float pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, float[] var2, int var3, float var4, float var5, String var6, int var7);

    public static boolean inputScalarN(String label, int dataType, ImLong pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3);

    public static boolean inputScalarN(String label, int dataType, ImLong pData, int components, long pStep) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4);

    public static boolean inputScalarN(String label, int dataType, ImLong pData, int components, long pStep, long pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6);

    public static boolean inputScalarN(String label, int dataType, ImLong pData, int components, long pStep, long pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6, String var8);

    public static boolean inputScalarN(String label, int dataType, ImLong pData, int components, long pStep, long pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, long[] var2, int var3, long var4, long var6, String var8, int var9);

    public static boolean inputScalarN(String label, int dataType, ImDouble pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3);

    public static boolean inputScalarN(String label, int dataType, ImDouble pData, int components, double pStep) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4);

    public static boolean inputScalarN(String label, int dataType, ImDouble pData, int components, double pStep, double pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6);

    public static boolean inputScalarN(String label, int dataType, ImDouble pData, int components, double pStep, double pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6, String var8);

    public static boolean inputScalarN(String label, int dataType, ImDouble pData, int components, double pStep, double pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, double[] var2, int var3, double var4, double var6, String var8, int var9);

    public static boolean inputScalarN(String label, int dataType, ImShort pData, int components) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3);

    public static boolean inputScalarN(String label, int dataType, ImShort pData, int components, short pStep) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4);

    public static boolean inputScalarN(String label, int dataType, ImShort pData, int components, short pStep, short pStepFast) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5);

    public static boolean inputScalarN(String label, int dataType, ImShort pData, int components, short pStep, short pStepFast, String format) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5, String var6);

    public static boolean inputScalarN(String label, int dataType, ImShort pData, int components, short pStep, short pStepFast, String format, int imGuiInputTextFlags) {
        return ImGui.nInputScalarN(label, dataType, pData.getData(), components, pStep, pStepFast, format, imGuiInputTextFlags);
    }

    private static native boolean nInputScalarN(String var0, int var1, short[] var2, int var3, short var4, short var5, String var6, int var7);

    public static native boolean colorEdit3(String var0, float[] var1);

    public static native boolean colorEdit3(String var0, float[] var1, int var2);

    public static native boolean colorEdit4(String var0, float[] var1);

    public static native boolean colorEdit4(String var0, float[] var1, int var2);

    public static native boolean colorPicker3(String var0, float[] var1);

    public static native boolean colorPicker3(String var0, float[] var1, int var2);

    public static native boolean colorPicker4(String var0, float[] var1);

    public static native boolean colorPicker4(String var0, float[] var1, int var2);

    public static native boolean colorPicker4(String var0, float[] var1, int var2, float var3);

    public static native boolean colorButton(String var0, float[] var1);

    public static native boolean colorButton(String var0, float[] var1, int var2);

    public static native boolean colorButton(String var0, float[] var1, int var2, float var3, float var4);

    public static native void setColorEditOptions(int var0);

    public static native boolean treeNode(String var0);

    public static native boolean treeNode(String var0, String var1);

    public static native boolean treeNode(long var0, String var2);

    public static native boolean treeNodeEx(String var0);

    public static native boolean treeNodeEx(String var0, int var1);

    public static native boolean treeNodeEx(String var0, int var1, String var2);

    public static native boolean treeNodeEx(long var0, int var2, String var3);

    public static native void treePush();

    public static native void treePush(String var0);

    public static native void treePush(long var0);

    public static native void treePop();

    public static native float getTreeNodeToLabelSpacing();

    public static native boolean collapsingHeader(String var0);

    public static native boolean collapsingHeader(String var0, int var1);

    public static boolean collapsingHeader(String label, ImBoolean pVisible) {
        return ImGui.nCollapsingHeader(label, pVisible.getData(), 0);
    }

    public static boolean collapsingHeader(String label, ImBoolean pVisible, int imGuiTreeNodeFlags) {
        return ImGui.nCollapsingHeader(label, pVisible.getData(), imGuiTreeNodeFlags);
    }

    private static native boolean nCollapsingHeader(String var0, boolean[] var1, int var2);

    public static native void setNextItemOpen(boolean var0);

    public static native void setNextItemOpen(boolean var0, int var1);

    public static native boolean selectable(String var0);

    public static native boolean selectable(String var0, boolean var1);

    public static native boolean selectable(String var0, boolean var1, int var2);

    public static native boolean selectable(String var0, boolean var1, int var2, float var3, float var4);

    public static boolean selectable(String label, ImBoolean selected) {
        return ImGui.nSelectable(label, selected.getData(), 0, 0.0f, 0.0f);
    }

    public static boolean selectable(String label, ImBoolean selected, int imGuiSelectableFlags) {
        return ImGui.nSelectable(label, selected.getData(), imGuiSelectableFlags, 0.0f, 0.0f);
    }

    public static boolean selectable(String label, ImBoolean selected, int imGuiSelectableFlags, float sizeX, float sizeY) {
        return ImGui.nSelectable(label, selected.getData(), imGuiSelectableFlags, sizeX, sizeY);
    }

    private static native boolean nSelectable(String var0, boolean[] var1, int var2, float var3, float var4);

    public static native boolean beginListBox(String var0);

    public static native boolean beginListBox(String var0, float var1, float var2);

    public static native void endListBox();

    public static void listBox(String label, ImInt currentItem, String[] items) {
        ImGui.nListBox(label, currentItem.getData(), items, items.length, -1);
    }

    public static void listBox(String label, ImInt currentItem, String[] items, int heightInItems) {
        ImGui.nListBox(label, currentItem.getData(), items, items.length, heightInItems);
    }

    private static native boolean nListBox(String var0, int[] var1, String[] var2, int var3, int var4);

    public static native void plotLines(String var0, float[] var1, int var2);

    public static native void plotLines(String var0, float[] var1, int var2, int var3);

    public static native void plotLines(String var0, float[] var1, int var2, int var3, String var4);

    public static native void plotLines(String var0, float[] var1, int var2, int var3, String var4, float var5);

    public static native void plotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6);

    public static native void plotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8);

    public static native void plotLines(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8, int var9);

    public static native void plotHistogram(String var0, float[] var1, int var2);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3, String var4);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8);

    public static native void plotHistogram(String var0, float[] var1, int var2, int var3, String var4, float var5, float var6, float var7, float var8, int var9);

    public static native void value(String var0, boolean var1);

    public static native void value(String var0, int var1);

    public static native void value(String var0, long var1);

    public static native void value(String var0, float var1);

    public static native void value(String var0, float var1, String var2);

    public static native boolean beginMenuBar();

    public static native void endMenuBar();

    public static native boolean beginMainMenuBar();

    public static native void endMainMenuBar();

    public static native boolean beginMenu(String var0);

    public static native boolean beginMenu(String var0, boolean var1);

    public static native void endMenu();

    public static native boolean menuItem(String var0);

    public static boolean menuItem(String label, String shortcut) {
        return ImGui.nMenuItem(label, shortcut, false, true);
    }

    public static boolean menuItem(String label, String shortcut, boolean selected) {
        return ImGui.nMenuItem(label, shortcut, selected, true);
    }

    public static boolean menuItem(String label, String shortcut, boolean selected, boolean enabled) {
        return ImGui.nMenuItem(label, shortcut, selected, enabled);
    }

    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected) {
        return ImGui.nMenuItem(label, shortcut, pSelected.getData(), true);
    }

    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected, boolean enabled) {
        return ImGui.nMenuItem(label, shortcut, pSelected.getData(), enabled);
    }

    private static native boolean nMenuItem(String var0, String var1, boolean var2, boolean var3);

    private static native boolean nMenuItem(String var0, String var1, boolean[] var2, boolean var3);

    public static native void beginTooltip();

    public static native void endTooltip();

    public static native void setTooltip(String var0);

    public static native boolean beginPopup(String var0);

    public static native boolean beginPopup(String var0, int var1);

    public static native boolean beginPopupModal(String var0);

    public static boolean beginPopupModal(String name, ImBoolean pOpen) {
        return ImGui.nBeginPopupModal(name, pOpen.getData(), 0);
    }

    public static boolean beginPopupModal(String name, int imGuiWindowFlags) {
        return ImGui.nBeginPopupModal(name, imGuiWindowFlags);
    }

    public static boolean beginPopupModal(String name, ImBoolean pOpen, int imGuiWindowFlags) {
        return ImGui.nBeginPopupModal(name, pOpen.getData(), imGuiWindowFlags);
    }

    private static native boolean nBeginPopupModal(String var0, int var1);

    private static native boolean nBeginPopupModal(String var0, boolean[] var1, int var2);

    public static native void endPopup();

    public static native void openPopup(String var0);

    public static native void openPopup(String var0, int var1);

    public static native void openPopupOnItemClick();

    public static native void openPopupOnItemClick(String var0);

    public static native void openPopupOnItemClick(int var0);

    public static native void openPopupOnItemClick(String var0, int var1);

    public static native void closeCurrentPopup();

    public static native boolean beginPopupContextItem();

    public static native boolean beginPopupContextItem(String var0);

    public static native boolean beginPopupContextItem(int var0);

    public static native boolean beginPopupContextItem(String var0, int var1);

    public static native boolean beginPopupContextWindow();

    public static native boolean beginPopupContextWindow(String var0);

    public static native boolean beginPopupContextWindow(int var0);

    public static native boolean beginPopupContextWindow(String var0, int var1);

    public static native boolean beginPopupContextVoid();

    public static native boolean beginPopupContextVoid(String var0);

    public static native boolean beginPopupContextVoid(int var0);

    public static native boolean beginPopupContextVoid(String var0, int var1);

    public static native boolean isPopupOpen(String var0);

    public static native boolean isPopupOpen(String var0, int var1);

    public static native boolean beginTable(String var0, int var1);

    public static native boolean beginTable(String var0, int var1, int var2);

    public static native boolean beginTable(String var0, int var1, int var2, float var3, float var4);

    public static native boolean beginTable(String var0, int var1, int var2, float var3, float var4, float var5);

    public static native void endTable();

    public static native void tableNextRow();

    public static native void tableNextRow(int var0);

    public static native void tableNextRow(int var0, float var1);

    public static native boolean tableNextColumn();

    public static native boolean tableSetColumnIndex(int var0);

    public static native void tableSetupColumn(String var0);

    public static native void tableSetupColumn(String var0, int var1);

    public static native void tableSetupColumn(String var0, int var1, float var2);

    public static native void tableSetupColumn(String var0, int var1, float var2, int var3);

    public static native void tableSetupScrollFreeze(int var0, int var1);

    public static native void tableHeadersRow();

    public static native void tableHeader(String var0);

    public static native int tableGetColumnCount();

    public static native int tableGetColumnIndex();

    public static native int tableGetRowIndex();

    public static native String tableGetColumnName();

    public static native String tableGetColumnName(int var0);

    public static native int tableGetColumnFlags();

    public static native int tableGetColumnFlags(int var0);

    public static native void tableSetBgColor(int var0, int var1);

    public static native void tableSetBgColor(int var0, int var1, int var2);

    public static native void columns();

    public static native void columns(int var0);

    public static native void columns(int var0, String var1);

    public static native void columns(int var0, String var1, boolean var2);

    public static native void nextColumn();

    public static native int getColumnIndex();

    public static native float getColumnWidth();

    public static native float getColumnWidth(int var0);

    public static native void setColumnWidth(int var0, float var1);

    public static native float getColumnOffset();

    public static native float getColumnOffset(int var0);

    public static native void setColumnOffset(int var0, float var1);

    public static native int getColumnsCount();

    public static native boolean beginTabBar(String var0);

    public static native boolean beginTabBar(String var0, int var1);

    public static native void endTabBar();

    public static native boolean beginTabItem(String var0);

    public static boolean beginTabItem(String label, ImBoolean pOpen) {
        return ImGui.nBeginTabItem(label, pOpen.getData(), 0);
    }

    public static boolean beginTabItem(String label, int imGuiTabItemFlags) {
        return ImGui.nBeginTabItem(label, imGuiTabItemFlags);
    }

    public static boolean beginTabItem(String label, ImBoolean pOpen, int imGuiTabItemFlags) {
        return ImGui.nBeginTabItem(label, pOpen.getData(), imGuiTabItemFlags);
    }

    private static native boolean nBeginTabItem(String var0, int var1);

    private static native boolean nBeginTabItem(String var0, boolean[] var1, int var2);

    public static native void endTabItem();

    public static native boolean tabItemButton(String var0);

    public static native boolean tabItemButton(String var0, int var1);

    public static native void setTabItemClosed(String var0);

    public static int dockSpace(int imGuiID) {
        return ImGui.nDockSpace(imGuiID, 0.0f, 0.0f, 0, 0L);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, 0, 0L);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags, 0L);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags, ImGuiWindowClass imGuiWindowClass) {
        return ImGui.nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags, imGuiWindowClass.ptr);
    }

    private static native int nDockSpace(int var0, float var1, float var2, int var3, long var4);

    public static int dockSpaceOverViewport() {
        return ImGui.nDockSpaceOverViewport(0L, 0, 0L);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, 0, 0L);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags, 0L);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return ImGui.nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags, windowClass.ptr);
    }

    private static native int nDockSpaceOverViewport(long var0, int var2, long var3);

    public static native void setNextWindowDockID(int var0);

    public static native void setNextWindowDockID(int var0, int var1);

    public static void setNextWindowClass(ImGuiWindowClass windowClass) {
        ImGui.nSetNextWindowClass(windowClass.ptr);
    }

    private static native void nSetNextWindowClass(long var0);

    public static native int getWindowDockID();

    public static native boolean isWindowDocked();

    public static native void logToTTY();

    public static native void logToTTY(int var0);

    public static native void logToFile();

    public static native void logToFile(int var0);

    public static native void logToFile(int var0, String var1);

    public static native void logToClipboard();

    public static native void logToClipboard(int var0);

    public static native void logFinish();

    public static native void logButtons();

    public static native void logText(String var0);

    public static native boolean beginDragDropSource();

    public static native boolean beginDragDropSource(int var0);

    public static boolean setDragDropPayload(String dataType, Object payload) {
        return ImGui.setDragDropPayload(dataType, payload, 0);
    }

    public static boolean setDragDropPayload(String dataType, Object payload, int imGuiCond) {
        if (payloadRef == null || payloadRef.get() != payload) {
            payloadRef = new WeakReference<Object>(payload);
        }
        return ImGui.nSetDragDropPayload(dataType, PAYLOAD_PLACEHOLDER_DATA, 1, imGuiCond);
    }

    public static boolean setDragDropPayload(Object payload) {
        return ImGui.setDragDropPayload(payload, 0);
    }

    public static boolean setDragDropPayload(Object payload, int imGuiCond) {
        return ImGui.setDragDropPayload(String.valueOf(payload.getClass().hashCode()), payload, imGuiCond);
    }

    private static native boolean nSetDragDropPayload(String var0, byte[] var1, int var2, int var3);

    public static native void endDragDropSource();

    public static native boolean beginDragDropTarget();

    public static <T> T acceptDragDropPayload(String dataType) {
        return ImGui.acceptDragDropPayload(dataType, 0);
    }

    public static <T> T acceptDragDropPayload(String dataType, Class<T> aClass) {
        return ImGui.acceptDragDropPayload(dataType, 0, aClass);
    }

    public static <T> T acceptDragDropPayload(String dataType, int imGuiDragDropFlags) {
        return ImGui.acceptDragDropPayload(dataType, imGuiDragDropFlags, null);
    }

    public static <T> T acceptDragDropPayload(String dataType, int imGuiDragDropFlags, Class<T> aClass) {
        Object rawPayload;
        if (payloadRef != null && ImGui.nAcceptDragDropPayload(dataType, imGuiDragDropFlags) && (rawPayload = payloadRef.get()) != null && (aClass == null || rawPayload.getClass().isAssignableFrom(aClass))) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T acceptDragDropPayload(Class<T> aClass) {
        return ImGui.acceptDragDropPayload(String.valueOf(aClass.hashCode()), 0, aClass);
    }

    public static <T> T acceptDragDropPayload(Class<T> aClass, int imGuiDragDropFlags) {
        return ImGui.acceptDragDropPayload(String.valueOf(aClass.hashCode()), imGuiDragDropFlags, aClass);
    }

    private static native boolean nAcceptDragDropPayload(String var0, int var1);

    public static native void endDragDropTarget();

    public static <T> T getDragDropPayload() {
        Object rawPayload;
        if (payloadRef != null && ImGui.nHasDragDropPayload() && (rawPayload = payloadRef.get()) != null) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T getDragDropPayload(String dataType) {
        Object rawPayload;
        if (payloadRef != null && ImGui.nHasDragDropPayload(dataType) && (rawPayload = payloadRef.get()) != null) {
            return rawPayload;
        }
        return null;
    }

    public static <T> T getDragDropPayload(Class<T> aClass) {
        return ImGui.getDragDropPayload(String.valueOf(aClass.hashCode()));
    }

    private static native boolean nHasDragDropPayload();

    private static native boolean nHasDragDropPayload(String var0);

    public static void beginDisabled() {
        ImGui.beginDisabled(true);
    }

    public static native void beginDisabled(boolean var0);

    public static native void endDisabled();

    public static native void pushClipRect(float var0, float var1, float var2, float var3, boolean var4);

    public static native void popClipRect();

    public static native void setItemDefaultFocus();

    public static native void setKeyboardFocusHere();

    public static native void setKeyboardFocusHere(int var0);

    public static native boolean isItemHovered();

    public static native boolean isItemHovered(int var0);

    public static native boolean isItemActive();

    public static native boolean isItemFocused();

    public static native boolean isItemClicked();

    public static native boolean isItemClicked(int var0);

    public static native boolean isItemVisible();

    public static native boolean isItemEdited();

    public static native boolean isItemActivated();

    public static native boolean isItemDeactivated();

    public static native boolean isItemDeactivatedAfterEdit();

    public static native boolean isItemToggledOpen();

    public static native boolean isAnyItemHovered();

    public static native boolean isAnyItemActive();

    public static native boolean isAnyItemFocused();

    public static ImVec2 getItemRectMin() {
        ImVec2 value = new ImVec2();
        ImGui.getItemRectMin(value);
        return value;
    }

    public static native void getItemRectMin(ImVec2 var0);

    public static native float getItemRectMinX();

    public static native float getItemRectMinY();

    public static ImVec2 getItemRectMax() {
        ImVec2 value = new ImVec2();
        ImGui.getItemRectMax(value);
        return value;
    }

    public static native void getItemRectMax(ImVec2 var0);

    public static native float getItemRectMaxX();

    public static native float getItemRectMaxY();

    public static ImVec2 getItemRectSize() {
        ImVec2 value = new ImVec2();
        ImGui.getItemRectSize(value);
        return value;
    }

    public static native void getItemRectSize(ImVec2 var0);

    public static native float getItemRectSizeX();

    public static native float getItemRectSizeY();

    public static native void setItemAllowOverlap();

    public static ImGuiViewport getMainViewport() {
        ImGui.MAIN_VIEWPORT.ptr = ImGui.nGetMainViewport();
        return MAIN_VIEWPORT;
    }

    private static native long nGetMainViewport();

    public static native boolean isRectVisible(float var0, float var1);

    public static native boolean isRectVisible(float var0, float var1, float var2, float var3);

    public static native double getTime();

    public static native int getFrameCount();

    public static ImDrawList getBackgroundDrawList() {
        ImGui.BACKGROUND_DRAW_LIST.ptr = ImGui.nGetBackgroundDrawList();
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetBackgroundDrawList();

    public static ImDrawList getForegroundDrawList() {
        ImGui.FOREGROUND_DRAW_LIST.ptr = ImGui.nGetForegroundDrawList();
        return FOREGROUND_DRAW_LIST;
    }

    private static native long nGetForegroundDrawList();

    public static ImDrawList getBackgroundDrawList(ImGuiViewport viewport) {
        ImGui.BACKGROUND_DRAW_LIST.ptr = ImGui.nGetBackgroundDrawList(viewport.ptr);
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetBackgroundDrawList(long var0);

    public static ImDrawList getForegroundDrawList(ImGuiViewport viewport) {
        ImGui.BACKGROUND_DRAW_LIST.ptr = ImGui.nGetForegroundDrawList(viewport.ptr);
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetForegroundDrawList(long var0);

    public static native String getStyleColorName(int var0);

    public static void setStateStorage(ImGuiStorage storage) {
        ImGui.nSetStateStorage(storage.ptr);
    }

    private static native void nSetStateStorage(long var0);

    public static ImGuiStorage getStateStorage() {
        ImGui.IMGUI_STORAGE.ptr = ImGui.nGetStateStorage();
        return IMGUI_STORAGE;
    }

    private static native long nGetStateStorage();

    public static native boolean beginChildFrame(int var0, float var1, float var2);

    public static native boolean beginChildFrame(int var0, float var1, float var2, int var3);

    public static native void endChildFrame();

    public static ImVec2 calcTextSize(String text) {
        ImVec2 value = new ImVec2();
        ImGui.calcTextSize(value, text);
        return value;
    }

    public static ImVec2 calcTextSize(String text, boolean hideTextAfterDoubleHash) {
        ImVec2 value = new ImVec2();
        ImGui.calcTextSize(value, text, hideTextAfterDoubleHash);
        return value;
    }

    public static ImVec2 calcTextSize(String text, boolean hideTextAfterDoubleHash, float wrapWidth) {
        ImVec2 value = new ImVec2();
        ImGui.calcTextSize(value, text, hideTextAfterDoubleHash, wrapWidth);
        return value;
    }

    public static native void calcTextSize(ImVec2 var0, String var1);

    public static native void calcTextSize(ImVec2 var0, String var1, boolean var2);

    public static native void calcTextSize(ImVec2 var0, String var1, float var2);

    public static native void calcTextSize(ImVec2 var0, String var1, boolean var2, float var3);

    public final ImVec4 colorConvertU32ToFloat4(int in) {
        ImVec4 value = new ImVec4();
        ImGui.colorConvertU32ToFloat4(in, value);
        return value;
    }

    public static native void colorConvertU32ToFloat4(int var0, ImVec4 var1);

    public static native int colorConvertFloat4ToU32(float var0, float var1, float var2, float var3);

    public static native void colorConvertRGBtoHSV(float[] var0, float[] var1);

    public static native void colorConvertHSVtoRGB(float[] var0, float[] var1);

    public static native int getKeyIndex(int var0);

    public static native boolean isKeyDown(int var0);

    public static native boolean isKeyPressed(int var0);

    public static native boolean isKeyPressed(int var0, boolean var1);

    public static native boolean isKeyReleased(int var0);

    public static native boolean getKeyPressedAmount(int var0, float var1, float var2);

    public static native void captureKeyboardFromApp();

    public static native void captureKeyboardFromApp(boolean var0);

    public static native boolean isMouseDown(int var0);

    public static native boolean isAnyMouseDown();

    public static native boolean isMouseClicked(int var0);

    public static native boolean isMouseClicked(int var0, boolean var1);

    public static native boolean isMouseDoubleClicked(int var0);

    public static native int getMouseClickedCount(int var0);

    public static native boolean isMouseReleased(int var0);

    public static native boolean isMouseDragging(int var0);

    public static native boolean isMouseDragging(int var0, float var1);

    public static native boolean isMouseHoveringRect(float var0, float var1, float var2, float var3);

    public static native boolean isMouseHoveringRect(float var0, float var1, float var2, float var3, boolean var4);

    public static native boolean isMousePosValid();

    public static native boolean isMousePosValid(float var0, float var1);

    public static ImVec2 getMousePos() {
        ImVec2 value = new ImVec2();
        ImGui.getMousePos(value);
        return value;
    }

    public static native void getMousePos(ImVec2 var0);

    public static native float getMousePosX();

    public static native float getMousePosY();

    public static ImVec2 getMousePosOnOpeningCurrentPopup() {
        ImVec2 value = new ImVec2();
        ImGui.getMousePosOnOpeningCurrentPopup(value);
        return value;
    }

    public static native void getMousePosOnOpeningCurrentPopup(ImVec2 var0);

    public static native float getMousePosOnOpeningCurrentPopupX();

    public static native float getMousePosOnOpeningCurrentPopupY();

    public static ImVec2 getMouseDragDelta() {
        ImVec2 value = new ImVec2();
        ImGui.getMouseDragDelta(value);
        return value;
    }

    public static native void getMouseDragDelta(ImVec2 var0);

    public static native float getMouseDragDeltaX();

    public static native float getMouseDragDeltaY();

    public static ImVec2 getMouseDragDelta(int button) {
        ImVec2 value = new ImVec2();
        ImGui.getMouseDragDelta(value, button);
        return value;
    }

    public static native void getMouseDragDelta(ImVec2 var0, int var1);

    public static native float getMouseDragDeltaX(int var0);

    public static native float getMouseDragDeltaY(int var0);

    public static ImVec2 getMouseDragDelta(int button, float lockThreshold) {
        ImVec2 value = new ImVec2();
        ImGui.getMouseDragDelta(value, button, lockThreshold);
        return value;
    }

    public static native void getMouseDragDelta(ImVec2 var0, int var1, float var2);

    public static native float getMouseDragDeltaX(int var0, float var1);

    public static native float getMouseDragDeltaY(int var0, float var1);

    public static native void resetMouseDragDelta();

    public static native void resetMouseDragDelta(int var0);

    public static native int getMouseCursor();

    public static native void setMouseCursor(int var0);

    public static native void captureMouseFromApp();

    public static native void captureMouseFromApp(boolean var0);

    public static native String getClipboardText();

    public static native void setClipboardText(String var0);

    public static native void loadIniSettingsFromDisk(String var0);

    public static native void loadIniSettingsFromMemory(String var0);

    public static native void loadIniSettingsFromMemory(String var0, int var1);

    public static native void saveIniSettingsToDisk(String var0);

    public static native String saveIniSettingsToMemory();

    public static native String saveIniSettingsToMemory(long var0);

    public static ImGuiPlatformIO getPlatformIO() {
        ImGui.PLATFORM_IO.ptr = ImGui.nGetPlatformIO();
        return PLATFORM_IO;
    }

    private static native long nGetPlatformIO();

    public static native void updatePlatformWindows();

    public static native void renderPlatformWindowsDefault();

    public static native void destroyPlatformWindows();

    public static ImGuiViewport findViewportByID(int imGuiID) {
        ImGui.FIND_VIEWPORT.ptr = ImGui.nFindViewportByID(imGuiID);
        return FIND_VIEWPORT;
    }

    private static native long nFindViewportByID(int var0);

    public static ImGuiViewport findViewportByPlatformHandle(long platformHandle) {
        ImGui.FIND_VIEWPORT.ptr = ImGui.nFindViewportByPlatformHandle(platformHandle);
        return FIND_VIEWPORT;
    }

    private static native long nFindViewportByPlatformHandle(long var0);

    static {
        String libPath = System.getProperty(LIB_PATH_PROP);
        String libName = System.getProperty(LIB_NAME_PROP, LIB_NAME_DEFAULT);
        String fullLibName = ImGui.resolveFullLibName();
        if (libPath != null) {
            System.load(Paths.get(libPath, new String[0]).resolve(fullLibName).toAbsolutePath().toString());
        } else {
            try {
                System.loadLibrary(libName);
            }
            catch (Error | Exception e) {
                String extractedLibAbsPath = ImGui.tryLoadFromClasspath(fullLibName);
                if (extractedLibAbsPath != null) {
                    System.load(extractedLibAbsPath);
                }
                throw e;
            }
        }
        IMGUI_CONTEXT = new ImGuiContext(0L);
        IMGUI_IO = new ImGuiIO(0L);
        WINDOW_DRAW_LIST = new ImDrawList(0L);
        BACKGROUND_DRAW_LIST = new ImDrawList(0L);
        FOREGROUND_DRAW_LIST = new ImDrawList(0L);
        IMGUI_STORAGE = new ImGuiStorage(0L);
        WINDOW_VIEWPORT = new ImGuiViewport(0L);
        FIND_VIEWPORT = new ImGuiViewport(0L);
        DRAW_DATA = new ImDrawData(0L);
        FONT = new ImFont(0L);
        STYLE = new ImGuiStyle(0L);
        MAIN_VIEWPORT = new ImGuiViewport(0L);
        PLATFORM_IO = new ImGuiPlatformIO(0L);
        ImGui.nInitJni();
        ImFontAtlas.nInit();
        ImGuiPlatformIO.init();
        ImGui.nInitInputTextData();
        ImGui.setAssertCallback(new ImAssertCallback(){

            @Override
            public void imAssertCallback(String assertion, int line, String file) {
                System.err.println("Dear ImGui Assertion Failed: " + assertion);
                System.err.println("Assertion Located At: " + file + ":" + line);
                Thread.dumpStack();
            }
        });
        payloadRef = null;
        PAYLOAD_PLACEHOLDER_DATA = new byte[1];
    }
}

