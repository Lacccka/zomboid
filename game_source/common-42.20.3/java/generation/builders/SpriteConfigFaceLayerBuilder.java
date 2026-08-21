/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpriteConfigFaceLayerBuilder
extends AbstractPropertyBuilder {
    private final Writeable.ListProperty<Row> row = this.listProperty("row", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public SpriteConfigFaceLayerBuilder row(String ... rows) {
        this.row.addValues((Row[])new Row[]{new Row(Stream.of(rows).map(tile -> tile == null ? "false" : PackTextureValidator.of(tile).toString()).collect(Collectors.toList()))});
        return this;
    }

    private record Row(List<String> tiles) implements Writeable
    {
        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            this.writeKeyValue(writer, indent, key, String.join((CharSequence)" ", this.tiles));
        }
    }
}

