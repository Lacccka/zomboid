/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.SpriteConfigFaceBuilder;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import zombie.scripting.objects.SpriteOverlayConfigKey;

public class ComponentSpriteOverlayConfigBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<StyleBuilder> style = this.listProperty("style", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public ComponentSpriteOverlayConfigBuilder() {
        super("SpriteOverlayConfig");
    }

    public ComponentSpriteOverlayConfigBuilder addStyle(StyleBuilder style) {
        this.style.addValues((StyleBuilder[])new StyleBuilder[]{style});
        return this;
    }

    public static class StyleBuilder
    extends AbstractPropertyBuilder
    implements Writeable {
        private final Writeable.ListProperty<ProgressBuilder> progress = this.listProperty("progress", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

        public StyleBuilder(SpriteOverlayConfigKey key) {
            super(key.toString());
        }

        public StyleBuilder addProgress(ProgressBuilder percentage) {
            this.progress.addValues((ProgressBuilder[])new ProgressBuilder[]{percentage});
            return this;
        }

        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            super.write(writer, indent, key);
        }
    }

    public static class ProgressBuilder
    extends AbstractPropertyBuilder
    implements Writeable {
        private final Writeable.ListProperty<SpriteConfigFaceBuilder> face = this.listProperty("face", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

        public ProgressBuilder(int progress) {
            super(Integer.toString(progress));
        }

        public ProgressBuilder addFace(SpriteConfigFaceBuilder face) {
            this.face.addValues((SpriteConfigFaceBuilder[])new SpriteConfigFaceBuilder[]{face});
            return this;
        }

        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            super.write(writer, indent, key);
        }
    }
}

