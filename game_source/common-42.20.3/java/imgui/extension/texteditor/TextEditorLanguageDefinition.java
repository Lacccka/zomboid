/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.texteditor;

import imgui.binding.ImGuiStructDestroyable;
import java.util.Map;

public final class TextEditorLanguageDefinition
extends ImGuiStructDestroyable {
    public TextEditorLanguageDefinition() {
    }

    public TextEditorLanguageDefinition(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public native void setName(String var1);

    public void setKeywords(String[] keywords) {
        this.nSetKeywords(keywords, keywords.length);
    }

    private native void nSetKeywords(String[] var1, int var2);

    public void setIdentifiers(Map<String, String> identifiers) {
        String[] keys2 = identifiers.keySet().toArray(new String[0]);
        String[] decl = identifiers.values().toArray(new String[0]);
        this.nSetIdentifiers(keys2, keys2.length, decl, decl.length);
    }

    private native void nSetIdentifiers(String[] var1, int var2, String[] var3, int var4);

    public void setPreprocIdentifiers(Map<String, String> identifiers) {
        String[] keys2 = identifiers.keySet().toArray(new String[0]);
        String[] decl = identifiers.values().toArray(new String[0]);
        this.nSetPreprocIdentifiers(keys2, keys2.length, decl, decl.length);
    }

    private native void nSetPreprocIdentifiers(String[] var1, int var2, String[] var3, int var4);

    public native void setCommentStart(String var1);

    public native void setCommentEnd(String var1);

    public native void setSingleLineComment(String var1);

    public native void setPreprocChar(char var1);

    public native void setAutoIdentation(boolean var1);

    public void setTokenRegexStrings(Map<String, Integer> tokenRegexStrings) {
        String[] keys2 = tokenRegexStrings.keySet().toArray(new String[0]);
        int[] paletteIndexes = tokenRegexStrings.values().stream().mapToInt(i -> i).toArray();
        this.nSetTokenRegexStrings(keys2, keys2.length, paletteIndexes, paletteIndexes.length);
    }

    private native void nSetTokenRegexStrings(String[] var1, int var2, int[] var3, int var4);

    private native void setCaseSensitive(boolean var1);

    public static TextEditorLanguageDefinition cPlusPlus() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nCPlusPlus());
    }

    public static TextEditorLanguageDefinition hlsl() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nHLSL());
    }

    public static TextEditorLanguageDefinition glsl() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nGLSL());
    }

    public static TextEditorLanguageDefinition c() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nC());
    }

    public static TextEditorLanguageDefinition sql() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nSQL());
    }

    public static TextEditorLanguageDefinition angelScript() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nAngelScript());
    }

    public static TextEditorLanguageDefinition lua() {
        return new TextEditorLanguageDefinition(TextEditorLanguageDefinition.nLua());
    }

    private static native long nCPlusPlus();

    private static native long nHLSL();

    private static native long nGLSL();

    private static native long nC();

    private static native long nSQL();

    private static native long nAngelScript();

    private static native long nLua();
}

