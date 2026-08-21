/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Writeable {
    public void write(Writer var1, int var2, String var3) throws IOException;

    default public void writeValue(Writer writer, int indent, Object value) throws IOException {
        writer.write("%s%s,\n".formatted(this.indent(indent), value));
    }

    default public void writeKeyValue(Writer writer, int indent, Object key, Object value) throws IOException {
        this.writeValue(writer, indent, "%s = %s".formatted(key, value));
    }

    default public void writeProperties(Writer writer, int indent, Object key, WriteableProperty<?> ... properties) throws IOException {
        this.writeBlockStart(writer, indent, key);
        for (WriteableProperty<?> property : properties) {
            property.write(writer, indent + 1);
        }
        this.writeBlockEnd(writer, indent);
    }

    default public void writeBlockStart(Writer writer, int indent, Object key) throws IOException {
        writer.write("%s%s\n%s{\n".formatted(this.indent(indent), key, this.indent(indent)));
    }

    default public void writeBlockEnd(Writer writer, int indent) throws IOException {
        writer.write("%s}\n".formatted(this.indent(indent)));
    }

    default public String indent(int indent) {
        return "    ".repeat(indent);
    }

    default public String fromArray(Object[] values2) {
        if (Arrays.stream(values2).collect(Collectors.toCollection(HashSet::new)).size() != values2.length) {
            System.out.println("Found duplicate value in: %s".formatted(Arrays.toString(values2)));
        }
        return Arrays.stream(values2).map(Object::toString).collect(Collectors.joining(";"));
    }

    default public String formatFloat(float value) {
        return !Float.isInfinite(value) && (double)value == Math.floor(value) ? String.format(Locale.ROOT, "%.1f", Float.valueOf(value)) : new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }

    default public Stream<Float> stream(float ... floats) {
        return IntStream.range(0, floats.length).mapToObj(x -> Float.valueOf(floats[x]));
    }

    public static interface WriteableProperty<T>
    extends Writeable {
        public String getKey();

        public T getValue();

        public String getAsString();

        public boolean shouldWrite();

        default public void write(Writer writer, int indent) throws IOException {
            if (this.shouldWrite()) {
                this.write(writer, indent, this.getKey());
            }
        }

        @Override
        default public void write(Writer writer, int indent, String key) throws IOException {
            this.writeKeyValue(writer, indent, key, this.getAsString());
        }
    }

    public static class ListProperty<T>
    implements WriteableProperty<List<T>> {
        private final String key;
        private final String joinKey;
        private final Set<Flags> flags;
        private final List<T> list = new ArrayList<T>();

        public ListProperty(String key, Flags ... flags) {
            this(key, (String)null, flags);
        }

        public ListProperty(String key, String joinKey, Flags ... flags) {
            this.key = key;
            this.joinKey = joinKey;
            this.flags = EnumSet.noneOf(Flags.class);
            Collections.addAll(this.flags, flags);
            if (this.joinKey != null) {
                this.flags.add(Flags.HIDE_OUTER_BLOCK);
            }
        }

        public void addValues(T ... values2) {
            Collections.addAll(this.list, values2);
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public List<T> getValue() {
            return this.list;
        }

        @Override
        public String getAsString() {
            throw new IllegalStateException("Yeah don't call me!");
        }

        @Override
        public boolean shouldWrite() {
            return this.flags.contains((Object)Flags.SHOW_IF_EMPTY) || !this.list.isEmpty();
        }

        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            List<T> printList;
            boolean writeOuterBlock;
            boolean bl = writeOuterBlock = !this.flags.contains((Object)Flags.HIDE_OUTER_BLOCK);
            if (writeOuterBlock) {
                this.writeBlockStart(writer, indent, key);
            }
            int extraIndent = writeOuterBlock ? 1 : 0;
            Function<Object, String> formatter = e -> {
                String string;
                if (e instanceof Float) {
                    Float f = (Float)e;
                    string = this.formatFloat(f.floatValue());
                } else {
                    string = e.toString();
                }
                return string;
            };
            List<Object> list = printList = this.flags.contains((Object)Flags.KEEP_DUPLICATES) ? this.list : new LinkedHashSet<T>(this.list).stream().toList();
            if (this.joinKey != null) {
                this.write(writer, indent + extraIndent, key, printList.stream().map(formatter).collect(Collectors.joining(this.joinKey)));
            } else {
                for (T element : printList) {
                    if (element instanceof Writeable) {
                        Writeable writeable = (Writeable)element;
                        writeable.write(writer, indent + extraIndent, key);
                        continue;
                    }
                    this.write(writer, indent + extraIndent, key, formatter.apply(element));
                }
            }
            if (writeOuterBlock) {
                this.writeBlockEnd(writer, indent);
            }
        }

        private void write(Writer writer, int indent, Object key, String element) throws IOException {
            if (this.flags.contains((Object)Flags.HIDE_KEY)) {
                this.writeValue(writer, indent, element);
            } else {
                this.writeKeyValue(writer, indent, key, element);
            }
        }

        public static enum Flags {
            HIDE_OUTER_BLOCK,
            SHOW_IF_EMPTY,
            HIDE_KEY,
            KEEP_DUPLICATES;

        }
    }

    public static class Property<T>
    implements WriteableProperty<T> {
        private final String key;
        private final boolean hasDefaultValue;
        private T value;
        private final Function<T, String> toString;

        protected Property(String key) {
            this(key, null);
        }

        protected Property(String key, Function<T, String> toString2) {
            this(key, toString2, null);
        }

        protected Property(String key, T defaultValue) {
            this(key, Objects::toString, defaultValue);
        }

        protected Property(String key, Function<T, String> toString2, T defaultValue) {
            this.key = key;
            boolean bl = this.hasDefaultValue = defaultValue != null;
            if (this.hasDefaultValue) {
                this.setValue(defaultValue);
            }
            this.toString = toString2;
        }

        public void setValue(T value) {
            boolean oldShouldWrite = this.shouldWrite();
            T oldValue = this.value;
            this.value = value;
            if (this.hasDefaultValue) {
                if (!this.shouldWrite()) {
                    System.out.println("Either duplicate or null passed into property: %s".formatted(this.key));
                }
            } else if (Objects.equals(oldValue, this.value) || oldShouldWrite == this.shouldWrite()) {
                System.out.println("Either duplicate or null passed into property: %s".formatted(this.key));
            }
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public String getAsString() {
            return this.toString.apply(this.value);
        }

        @Override
        public boolean shouldWrite() {
            return this.value != null;
        }
    }
}

