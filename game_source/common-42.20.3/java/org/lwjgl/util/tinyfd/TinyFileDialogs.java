/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.tinyfd;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Platform;

public class TinyFileDialogs {
    public static final String tinyfd_version = "tinyfd_version";
    public static final String tinyfd_needs = "tinyfd_needs";
    public static final String tinyfd_response = "tinyfd_response";
    public static final String tinyfd_verbose = "tinyfd_verbose";
    public static final String tinyfd_silent = "tinyfd_silent";
    public static final String tinyfd_allowCursesDialogs = "tinyfd_allowCursesDialogs";
    public static final String tinyfd_forceConsole = "tinyfd_forceConsole";
    public static final String tinyfd_winUtf8 = "tinyfd_winUtf8";

    protected TinyFileDialogs() {
        throw new UnsupportedOperationException();
    }

    public static native long ntinyfd_getGlobalChar(long var0);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_getGlobalChar(@NativeType(value="char const *") ByteBuffer aCharVariableName) {
        if (Checks.CHECKS) {
            Checks.checkNT1(aCharVariableName);
        }
        long __result = TinyFileDialogs.ntinyfd_getGlobalChar(MemoryUtil.memAddress(aCharVariableName));
        return MemoryUtil.memASCIISafe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_getGlobalChar(@NativeType(value="char const *") CharSequence aCharVariableName) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(aCharVariableName, true);
            long aCharVariableNameEncoded = stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_getGlobalChar(aCharVariableNameEncoded);
            String string = MemoryUtil.memASCIISafe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int ntinyfd_getGlobalInt(long var0);

    public static int tinyfd_getGlobalInt(@NativeType(value="char const *") ByteBuffer aIntVariableName) {
        if (Checks.CHECKS) {
            Checks.checkNT1(aIntVariableName);
        }
        return TinyFileDialogs.ntinyfd_getGlobalInt(MemoryUtil.memAddress(aIntVariableName));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int tinyfd_getGlobalInt(@NativeType(value="char const *") CharSequence aIntVariableName) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(aIntVariableName, true);
            long aIntVariableNameEncoded = stack.getPointerAddress();
            int n = TinyFileDialogs.ntinyfd_getGlobalInt(aIntVariableNameEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int ntinyfd_setGlobalInt(long var0, int var2);

    public static int tinyfd_setGlobalInt(@NativeType(value="char const *") ByteBuffer aIntVariableName, int aValue) {
        if (Checks.CHECKS) {
            Checks.checkNT1(aIntVariableName);
        }
        return TinyFileDialogs.ntinyfd_setGlobalInt(MemoryUtil.memAddress(aIntVariableName), aValue);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int tinyfd_setGlobalInt(@NativeType(value="char const *") CharSequence aIntVariableName, int aValue) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(aIntVariableName, true);
            long aIntVariableNameEncoded = stack.getPointerAddress();
            int n = TinyFileDialogs.ntinyfd_setGlobalInt(aIntVariableNameEncoded, aValue);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native void tinyfd_beep();

    public static native int ntinyfd_notifyPopup(long var0, long var2, long var4);

    public static int tinyfd_notifyPopup(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aMessage, @NativeType(value="char const *") ByteBuffer aIconType) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aMessage);
            Checks.checkNT1(aIconType);
        }
        return TinyFileDialogs.ntinyfd_notifyPopup(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aMessage), MemoryUtil.memAddress(aIconType));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int tinyfd_notifyPopup(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aMessage, @NativeType(value="char const *") CharSequence aIconType) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aMessage, true);
            long aMessageEncoded = aMessage == null ? 0L : stack.getPointerAddress();
            stack.nASCII(aIconType, true);
            long aIconTypeEncoded = stack.getPointerAddress();
            int n = TinyFileDialogs.ntinyfd_notifyPopup(aTitleEncoded, aMessageEncoded, aIconTypeEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int ntinyfd_messageBox(long var0, long var2, long var4, long var6, int var8);

    public static int tinyfd_messageBox(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aMessage, @NativeType(value="char const *") ByteBuffer aDialogType, @NativeType(value="char const *") ByteBuffer aIconType, int aDefaultButton) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aMessage);
            Checks.checkNT1(aDialogType);
            Checks.checkNT1(aIconType);
        }
        return TinyFileDialogs.ntinyfd_messageBox(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aMessage), MemoryUtil.memAddress(aDialogType), MemoryUtil.memAddress(aIconType), aDefaultButton);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int tinyfd_messageBox(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aMessage, @NativeType(value="char const *") CharSequence aDialogType, @NativeType(value="char const *") CharSequence aIconType, int aDefaultButton) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aMessage, true);
            long aMessageEncoded = aMessage == null ? 0L : stack.getPointerAddress();
            stack.nASCII(aDialogType, true);
            long aDialogTypeEncoded = stack.getPointerAddress();
            stack.nASCII(aIconType, true);
            long aIconTypeEncoded = stack.getPointerAddress();
            int n = TinyFileDialogs.ntinyfd_messageBox(aTitleEncoded, aMessageEncoded, aDialogTypeEncoded, aIconTypeEncoded, aDefaultButton);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long ntinyfd_inputBox(long var0, long var2, long var4);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_inputBox(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aMessage, @NativeType(value="char const *") @Nullable ByteBuffer aDefaultInput) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aMessage);
            Checks.checkNT1Safe(aDefaultInput);
        }
        long __result = TinyFileDialogs.ntinyfd_inputBox(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aMessage), MemoryUtil.memAddressSafe(aDefaultInput));
        return MemoryUtil.memUTF8Safe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_inputBox(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aMessage, @NativeType(value="char const *") @Nullable CharSequence aDefaultInput) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aMessage, true);
            long aMessageEncoded = aMessage == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aDefaultInput, true);
            long aDefaultInputEncoded = aDefaultInput == null ? 0L : stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_inputBox(aTitleEncoded, aMessageEncoded, aDefaultInputEncoded);
            String string = MemoryUtil.memUTF8Safe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long ntinyfd_saveFileDialog(long var0, long var2, int var4, long var5, long var7);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_saveFileDialog(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aDefaultPathAndOrFile, @NativeType(value="char const * const *") @Nullable PointerBuffer aFilterPatterns, @NativeType(value="char const *") @Nullable ByteBuffer aSingleFilterDescription) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aDefaultPathAndOrFile);
            Checks.checkNT1Safe(aSingleFilterDescription);
        }
        long __result = TinyFileDialogs.ntinyfd_saveFileDialog(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aDefaultPathAndOrFile), Checks.remainingSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aSingleFilterDescription));
        return MemoryUtil.memUTF8Safe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_saveFileDialog(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aDefaultPathAndOrFile, @NativeType(value="char const * const *") @Nullable PointerBuffer aFilterPatterns, @NativeType(value="char const *") @Nullable CharSequence aSingleFilterDescription) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aDefaultPathAndOrFile, true);
            long aDefaultPathAndOrFileEncoded = aDefaultPathAndOrFile == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aSingleFilterDescription, true);
            long aSingleFilterDescriptionEncoded = aSingleFilterDescription == null ? 0L : stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_saveFileDialog(aTitleEncoded, aDefaultPathAndOrFileEncoded, Checks.remainingSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aFilterPatterns), aSingleFilterDescriptionEncoded);
            String string = MemoryUtil.memUTF8Safe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long ntinyfd_openFileDialog(long var0, long var2, int var4, long var5, long var7, int var9);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_openFileDialog(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aDefaultPathAndOrFile, @NativeType(value="char const * const *") @Nullable PointerBuffer aFilterPatterns, @NativeType(value="char const *") @Nullable ByteBuffer aSingleFilterDescription, @NativeType(value="int") boolean aAllowMultipleSelects) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aDefaultPathAndOrFile);
            Checks.checkNT1Safe(aSingleFilterDescription);
        }
        long __result = TinyFileDialogs.ntinyfd_openFileDialog(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aDefaultPathAndOrFile), Checks.remainingSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aSingleFilterDescription), aAllowMultipleSelects ? 1 : 0);
        return MemoryUtil.memUTF8Safe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_openFileDialog(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aDefaultPathAndOrFile, @NativeType(value="char const * const *") @Nullable PointerBuffer aFilterPatterns, @NativeType(value="char const *") @Nullable CharSequence aSingleFilterDescription, @NativeType(value="int") boolean aAllowMultipleSelects) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aDefaultPathAndOrFile, true);
            long aDefaultPathAndOrFileEncoded = aDefaultPathAndOrFile == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aSingleFilterDescription, true);
            long aSingleFilterDescriptionEncoded = aSingleFilterDescription == null ? 0L : stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_openFileDialog(aTitleEncoded, aDefaultPathAndOrFileEncoded, Checks.remainingSafe(aFilterPatterns), MemoryUtil.memAddressSafe(aFilterPatterns), aSingleFilterDescriptionEncoded, aAllowMultipleSelects ? 1 : 0);
            String string = MemoryUtil.memUTF8Safe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long ntinyfd_selectFolderDialog(long var0, long var2);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_selectFolderDialog(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aDefaultPath) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aDefaultPath);
        }
        long __result = TinyFileDialogs.ntinyfd_selectFolderDialog(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aDefaultPath));
        return MemoryUtil.memUTF8Safe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_selectFolderDialog(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aDefaultPath) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nUTF8Safe(aDefaultPath, true);
            long aDefaultPathEncoded = aDefaultPath == null ? 0L : stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_selectFolderDialog(aTitleEncoded, aDefaultPathEncoded);
            String string = MemoryUtil.memUTF8Safe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long ntinyfd_colorChooser(long var0, long var2, long var4, long var6);

    @NativeType(value="char const *")
    public static @Nullable String tinyfd_colorChooser(@NativeType(value="char const *") @Nullable ByteBuffer aTitle, @NativeType(value="char const *") @Nullable ByteBuffer aDefaultHexRGB, @NativeType(value="unsigned char *") @Nullable ByteBuffer aDefaultRGB, @NativeType(value="unsigned char *") ByteBuffer aoResultRGB) {
        if (Checks.CHECKS) {
            Checks.checkNT1Safe(aTitle);
            Checks.checkNT1Safe(aDefaultHexRGB);
            Checks.checkSafe((Buffer)aDefaultRGB, 3);
            Checks.check((Buffer)aoResultRGB, 3);
        }
        long __result = TinyFileDialogs.ntinyfd_colorChooser(MemoryUtil.memAddressSafe(aTitle), MemoryUtil.memAddressSafe(aDefaultHexRGB), MemoryUtil.memAddressSafe(aDefaultRGB), MemoryUtil.memAddress(aoResultRGB));
        return MemoryUtil.memUTF8Safe(__result);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable String tinyfd_colorChooser(@NativeType(value="char const *") @Nullable CharSequence aTitle, @NativeType(value="char const *") @Nullable CharSequence aDefaultHexRGB, @NativeType(value="unsigned char *") @Nullable ByteBuffer aDefaultRGB, @NativeType(value="unsigned char *") ByteBuffer aoResultRGB) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)aDefaultRGB, 3);
            Checks.check((Buffer)aoResultRGB, 3);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8Safe(aTitle, true);
            long aTitleEncoded = aTitle == null ? 0L : stack.getPointerAddress();
            stack.nASCIISafe(aDefaultHexRGB, true);
            long aDefaultHexRGBEncoded = aDefaultHexRGB == null ? 0L : stack.getPointerAddress();
            long __result = TinyFileDialogs.ntinyfd_colorChooser(aTitleEncoded, aDefaultHexRGBEncoded, MemoryUtil.memAddressSafe(aDefaultRGB), MemoryUtil.memAddress(aoResultRGB));
            String string = MemoryUtil.memUTF8Safe(__result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    static {
        Library.loadSystem(System::load, System::loadLibrary, TinyFileDialogs.class, "org.lwjgl.tinyfd", Platform.mapLibraryNameBundled("lwjgl_tinyfd"));
        if (Platform.get() == Platform.WINDOWS) {
            TinyFileDialogs.tinyfd_setGlobalInt(tinyfd_winUtf8, 1);
        }
    }
}

