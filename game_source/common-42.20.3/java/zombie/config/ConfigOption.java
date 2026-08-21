/*
 * Decompiled with CFR 0.152.
 */
package zombie.config;

import zombie.UsedFromLua;

@UsedFromLua
public abstract class ConfigOption {
    protected final String name;
    private ConfigOptionOnChangeCallback onChangedCallback;

    public ConfigOption(String name) {
        if (name == null || name.isEmpty() || name.contains("=")) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getType();

    public abstract void resetToDefault();

    public abstract void setDefaultToCurrentValue();

    public abstract void parse(String var1);

    public abstract String getValueAsString();

    public String getValueAsLuaString() {
        return this.getValueAsString();
    }

    public abstract void setValueFromObject(Object var1);

    public abstract Object getValueAsObject();

    public abstract boolean isValidString(String var1);

    public abstract String getTooltip();

    public abstract ConfigOption makeCopy();

    public void setOnChangeCallback(ConfigOptionOnChangeCallback onChange) {
        this.onChangedCallback = onChange;
    }

    public void invokeOnChangeEvent() {
        if (this.onChangedCallback != null) {
            this.onChangedCallback.onChanged(this);
        }
    }

    public static interface ConfigOptionOnChangeCallback {
        public void onChanged(ConfigOption var1);
    }
}

