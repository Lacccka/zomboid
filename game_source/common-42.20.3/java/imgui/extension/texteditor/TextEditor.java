/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.texteditor;

import imgui.binding.ImGuiStructDestroyable;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import java.util.Map;

public final class TextEditor
extends ImGuiStructDestroyable {
    public TextEditor() {
    }

    public TextEditor(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public void setLanguageDefinition(TextEditorLanguageDefinition definition) {
        this.nSetLanguageDefinition(definition.ptr);
    }

    public native void nSetLanguageDefinition(long var1);

    public native int[] getPalette();

    public void setPalette(int[] palette) {
        this.nSetPalette(palette, palette.length);
    }

    public native void nSetPalette(int[] var1, int var2);

    public void setErrorMarkers(Map<Integer, String> errorMarkers) {
        int[] keys2 = errorMarkers.keySet().stream().mapToInt(i -> i).toArray();
        String[] values2 = errorMarkers.values().toArray(new String[0]);
        this.nSetErrorMarkers(keys2, keys2.length, values2, values2.length);
    }

    private native void nSetErrorMarkers(int[] var1, int var2, String[] var3, int var4);

    public void setBreakpoints(int[] breakpoints) {
        this.nSetBreakpoints(breakpoints, breakpoints.length);
    }

    private native void nSetBreakpoints(int[] var1, int var2);

    public int getBreakpointCount() {
        return this.nGetBreakpointCount();
    }

    private native int nGetBreakpointCount();

    public int getBreakpoint(int index) {
        return this.nGetBreakpoint(index);
    }

    private native int nGetBreakpoint(int var1);

    public native void render(String var1);

    public native void setText(String var1);

    public native String getText();

    public void setTextLines(String[] lines) {
        this.nSetTextLines(lines, lines.length);
    }

    private native void nSetTextLines(String[] var1, int var2);

    public native String[] getTextLines();

    public native String getSelectedText();

    public native String getCurrentLineText();

    public native int getTotalLines();

    public native boolean isOverwrite();

    public native void setReadOnly(boolean var1);

    public native void setExecutingLine(int var1);

    public native boolean isReadOnly();

    public native boolean isTextChanged();

    public native boolean isFocused();

    public native boolean isCursorPositionChanged();

    public native boolean isColorizerEnabled();

    public native void setColorizerEnable(boolean var1);

    public native int getCursorPositionLine();

    public native int getCursorPositionColumn();

    public native void setCursorPosition(int var1, int var2);

    public native void setHandleMouseInputs(boolean var1);

    public native boolean isHandleMouseInputsEnabled();

    public native void setHandleKeyboardInputs(boolean var1);

    public native boolean isHandleKeyboardInputsEnabled();

    public native void setImGuiChildIgnored(boolean var1);

    public native boolean isImGuiChildIgnored();

    public native void setShowWhitespaces(boolean var1);

    public native boolean isShowingWhitespaces();

    public native void setTabSize(int var1);

    public native int getTabSize();

    public native void insertText(String var1);

    public native void moveUp(int var1, boolean var2);

    public native void moveDown(int var1, boolean var2);

    public native void moveLeft(int var1, boolean var2, boolean var3);

    public native void moveRight(int var1, boolean var2, boolean var3);

    public native void moveTop(boolean var1);

    public native void moveBottom(boolean var1);

    public native void moveHome(boolean var1);

    public native void moveEnd(boolean var1);

    public native void setSelectionStart(int var1, int var2);

    public native void setSelectionEnd(int var1, int var2);

    public native void setSelection(int var1, int var2, int var3, int var4, int var5);

    public native void selectWordUnderCursor();

    public native void selectAll();

    public native boolean hasSelection();

    public native void copy();

    public native void cut();

    public native void paste();

    public native void delete();

    public native boolean canUndo();

    public native boolean canRedo();

    public native void undo(int var1);

    public native void redo(int var1);

    public native int[] getDarkPalette();

    public native int[] getLightPalette();

    public native int[] getRetroBluePalette();
}

