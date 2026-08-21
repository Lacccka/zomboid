/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.SpriteOverlayConfigKey;

public record RecipeOverlayMapperBuilder(String inputItem, SpriteOverlayConfigKey outputOverlay) implements Writeable
{
    private static final String DEFAULT = "default";

    public static RecipeOverlayMapperBuilder overlayMapper(ItemKey inputItem, SpriteOverlayConfigKey outputOverlay) {
        return new RecipeOverlayMapperBuilder(inputItem.toString(), outputOverlay);
    }

    public static RecipeOverlayMapperBuilder overlayMapperDefault(SpriteOverlayConfigKey defaultOverlay) {
        return new RecipeOverlayMapperBuilder(DEFAULT, defaultOverlay);
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeKeyValue(writer, indent, this.inputItem, this.outputOverlay);
    }
}

