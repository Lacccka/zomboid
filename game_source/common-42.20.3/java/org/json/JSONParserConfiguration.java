/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import org.json.ParserConfiguration;

public class JSONParserConfiguration
extends ParserConfiguration {
    private boolean overwriteDuplicateKey = false;
    private boolean useNativeNulls;
    private boolean strictMode;

    @Override
    protected JSONParserConfiguration clone() {
        JSONParserConfiguration clone = new JSONParserConfiguration();
        clone.overwriteDuplicateKey = this.overwriteDuplicateKey;
        clone.strictMode = this.strictMode;
        clone.maxNestingDepth = this.maxNestingDepth;
        clone.keepStrings = this.keepStrings;
        clone.useNativeNulls = this.useNativeNulls;
        return clone;
    }

    public JSONParserConfiguration withMaxNestingDepth(int maxNestingDepth) {
        JSONParserConfiguration clone = this.clone();
        clone.maxNestingDepth = maxNestingDepth;
        return clone;
    }

    public JSONParserConfiguration withOverwriteDuplicateKey(boolean overwriteDuplicateKey) {
        JSONParserConfiguration clone = this.clone();
        clone.overwriteDuplicateKey = overwriteDuplicateKey;
        return clone;
    }

    public JSONParserConfiguration withUseNativeNulls(boolean useNativeNulls) {
        JSONParserConfiguration clone = this.clone();
        clone.useNativeNulls = useNativeNulls;
        return clone;
    }

    public JSONParserConfiguration withStrictMode() {
        return this.withStrictMode(true);
    }

    public JSONParserConfiguration withStrictMode(boolean mode) {
        JSONParserConfiguration clone = this.clone();
        clone.strictMode = mode;
        return clone;
    }

    public boolean isOverwriteDuplicateKey() {
        return this.overwriteDuplicateKey;
    }

    public boolean isUseNativeNulls() {
        return this.useNativeNulls;
    }

    public boolean isStrictMode() {
        return this.strictMode;
    }
}

