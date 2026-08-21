/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import zombie.scripting.objects.ItemKey;

public record RecipeMapperBuilder(String inputItem, ItemKey outputItem) implements Writeable
{
    private static final String DEFAULT = "default";

    public static RecipeMapperBuilder itemMapper(List<ItemKey> inputItems, ItemKey outputItem) {
        return new RecipeMapperBuilder(inputItems.stream().map(Object::toString).collect(Collectors.joining(";")), outputItem);
    }

    public static RecipeMapperBuilder itemMapperDefault(ItemKey returnedItem) {
        return new RecipeMapperBuilder(DEFAULT, returnedItem);
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        if (DEFAULT.equals(this.inputItem)) {
            this.writeKeyValue(writer, indent, this.inputItem, this.outputItem);
        } else {
            this.writeKeyValue(writer, indent, this.outputItem, this.inputItem);
        }
    }
}

