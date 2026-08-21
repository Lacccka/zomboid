/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.RecipeMapperBuilder;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;

public record ItemMapper(String name, RecipeMapperBuilder[] elements) implements Writeable
{
    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeBlockStart(writer, indent, "%s %s".formatted(key, this.name));
        for (RecipeMapperBuilder element : this.elements) {
            element.write(writer, indent + 1, key);
        }
        this.writeBlockEnd(writer, indent);
    }
}

