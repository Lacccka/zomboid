/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.Named;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractPropertyBuilder
implements Writeable,
Named {
    private final Map<String, Writeable.WriteableProperty<?>> properties;
    private final String name;
    private final BiFunction<String, String, String> keyMaker;

    public AbstractPropertyBuilder() {
        this(null, (a, b) -> a);
    }

    public AbstractPropertyBuilder(String name) {
        String string = "%s %s";
        this(name, (arg_0, arg_1) -> AbstractPropertyBuilder.lambda$new$1("%s %s", arg_0, arg_1));
    }

    public AbstractPropertyBuilder(String name, BiFunction<String, String, String> keyMaker) {
        this.properties = new LinkedHashMap();
        this.name = name;
        this.keyMaker = keyMaker;
    }

    @Override
    public String getName() {
        return this.name;
    }

    <T extends Writeable.WriteableProperty<?>> T registerInternal(T property) {
        if (this.properties.containsKey(property.getKey())) {
            // empty if block
        }
        this.properties.put(property.getKey(), property);
        return property;
    }

    protected <T> Writeable.Property<T> property(String name) {
        return this.registerInternal(new Writeable.Property(name));
    }

    protected <T> Writeable.Property<T> property(String name, Function<T, String> toString2) {
        return this.registerInternal(new Writeable.Property<Function<T, String>>(name, toString2));
    }

    protected <T> Writeable.Property<T> property(String name, T defaultValue) {
        return this.registerInternal(new Writeable.Property<T>(name, defaultValue));
    }

    protected <T> Writeable.ListProperty<T> listProperty(String name, Writeable.ListProperty.Flags ... flags) {
        return this.listProperty(name, (String)null, flags);
    }

    protected <T> Writeable.ListProperty<T> listProperty(String name, String joinKey, Writeable.ListProperty.Flags ... flags) {
        return this.registerInternal(new Writeable.ListProperty(name, joinKey, flags));
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        this.writeProperties(writer, indent, this.keyMaker.apply(key, this.name), this.getProperties());
    }

    protected Writeable.WriteableProperty<?>[] getProperties() {
        return this.properties.values().toArray(new Writeable.WriteableProperty[0]);
    }

    public <T> Optional<T> get(String key) {
        return Optional.ofNullable(this.properties.get(key)).map(Writeable.WriteableProperty::getValue);
    }

    private static /* synthetic */ String lambda$new$1(String rec$, Object xva$0, Object xva$1) {
        return "%s %s".formatted(xva$0, xva$1);
    }
}

