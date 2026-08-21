/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.stream.Stream;

public abstract class AbstractFileValidator
implements Writeable {
    private final String name;

    protected AbstractFileValidator(String name, String kind, String ... pathTemplates) {
        this.name = name;
        if (name.contains("|")) {
            name = name.split("\\|")[0];
        }
        String finalName = name;
        if (Stream.of(pathTemplates).map(f -> f.formatted(finalName)).map(x$0 -> Paths.get(x$0, new String[0])).noneMatch(x$0 -> Files.exists(x$0, new LinkOption[0]))) {
            System.err.println("Missing %s: %s".formatted(kind, name));
        }
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeKeyValue(writer, indent, key, this.name);
    }

    public String toString() {
        return this.name;
    }
}

