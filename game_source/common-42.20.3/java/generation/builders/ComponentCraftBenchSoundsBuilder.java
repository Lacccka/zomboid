/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;
import zombie.scripting.objects.SoundKey;

public class ComponentCraftBenchSoundsBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<CraftBenchSound> sounds = this.listProperty("", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public ComponentCraftBenchSoundsBuilder() {
        super("CraftBenchSounds");
    }

    public ComponentCraftBenchSoundsBuilder addSound(String name, SoundKey sound, String ... params) {
        if (params.length >= 4) {
            throw new IllegalArgumentException("The sounds must have at most 3 elements");
        }
        this.sounds.addValues((CraftBenchSound[])new CraftBenchSound[]{new CraftBenchSound(name, sound, params)});
        return this;
    }

    record CraftBenchSound(String name, SoundKey sound, String[] params) implements Writeable
    {
        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            this.writeKeyValue(writer, indent, this.name, "%s%s".formatted(this.sound, this.params.length == 0 ? "" : Arrays.stream(this.params).collect(Collectors.joining(" ", " ", ""))));
        }
    }
}

