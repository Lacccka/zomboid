/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.builders.Named;
import generation.builders.Writeable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

public class ScriptGenerator<T extends Named & Writeable> {
    public Path path;
    public boolean shouldAppend;
    public final List<T> elements = new ArrayList<T>();
    public String module = "Base";

    public ScriptGenerator(String name, boolean shouldAppend) {
        this.path = Paths.get("media/scripts/generated/%s.txt".formatted(name), new String[0]);
        this.shouldAppend = shouldAppend;
    }

    public ScriptGenerator<T> add(T element) {
        this.elements.add(element);
        return this;
    }

    public ScriptGenerator<T> module(String module) {
        this.module = module;
        return this;
    }

    public void write() {
        try {
            List<Object> oldLines;
            if (this.shouldAppend) {
                List<String> lines = Files.readAllLines(this.path);
                oldLines = lines.subList(2, lines.size() - 1);
            } else {
                oldLines = new ArrayList();
            }
            Files.createDirectories(this.path.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(this.path, new OpenOption[0]);){
                writer.write("module %s\n{\n".formatted(this.module));
                for (String string : oldLines) {
                    writer.write("%s\n".formatted(string));
                }
                boolean first = oldLines.isEmpty();
                for (Named model : this.elements) {
                    if (!first) {
                        writer.write("\n");
                    }
                    first = false;
                    ((Writeable)((Object)model)).write(writer, 1, model.getName());
                }
                writer.write("}\n");
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to write: " + String.valueOf(this.path), e);
        }
    }
}

