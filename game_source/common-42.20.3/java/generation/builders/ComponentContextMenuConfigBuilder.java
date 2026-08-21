/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;

public class ComponentContextMenuConfigBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<ContextEntryBuilder> contextEntry = this.listProperty("contextEntry", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public ComponentContextMenuConfigBuilder() {
        super("ContextMenuConfig");
    }

    public ComponentContextMenuConfigBuilder addEntry(ContextEntryBuilder ... entry) {
        this.contextEntry.addValues((ContextEntryBuilder[])entry);
        return this;
    }

    public static class ContextEntryBuilder
    extends AbstractDynamicOrderPropertyBuilder {
        private final Writeable.Property<String> menu = this.property("menu");
        private final Writeable.Property<Boolean> allowDistance = this.property("allowDistance");
        private final Writeable.Property<String> customFunction = this.property("customFunction");
        private final Writeable.Property<String> customSubmenu = this.property("customSubmenu");
        private final Writeable.Property<String> extraParam = this.property("extraParam");
        private final Writeable.Property<String> icon = this.property("icon");
        private final Writeable.Property<String> openWindow = this.property("openWindow");
        private final Writeable.Property<Integer> time = this.property("time");
        private final Writeable.Property<String> timedAction = this.property("timedAction");

        public ContextEntryBuilder menu(String menu) {
            this.menu.setValue(menu);
            return this;
        }

        public ContextEntryBuilder allowDistance(boolean allowDistance) {
            this.allowDistance.setValue(allowDistance);
            return this;
        }

        public ContextEntryBuilder customFunction(String customFunction) {
            this.customFunction.setValue(customFunction);
            return this;
        }

        public ContextEntryBuilder customSubmenu(String customSubmenu) {
            this.customSubmenu.setValue(customSubmenu);
            return this;
        }

        public ContextEntryBuilder extraParam(String extraParam) {
            this.extraParam.setValue(extraParam);
            return this;
        }

        public ContextEntryBuilder icon(String icon) {
            this.icon.setValue(icon);
            return this;
        }

        public ContextEntryBuilder openWindow(String openWindow) {
            this.openWindow.setValue(openWindow);
            return this;
        }

        public ContextEntryBuilder time(int time) {
            this.time.setValue(time);
            return this;
        }

        public ContextEntryBuilder timedAction(String timedAction) {
            this.timedAction.setValue(timedAction);
            return this;
        }
    }
}

