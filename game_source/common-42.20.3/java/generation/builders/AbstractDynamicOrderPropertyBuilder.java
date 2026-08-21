/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AbstractDynamicOrderPropertyBuilder
extends AbstractPropertyBuilder {
    private final Set<Writeable.WriteableProperty<?>> order = new LinkedHashSet();

    public AbstractDynamicOrderPropertyBuilder() {
    }

    public AbstractDynamicOrderPropertyBuilder(String name) {
        super(name);
    }

    public AbstractDynamicOrderPropertyBuilder(String name, BiFunction<String, String, String> keyMaker) {
        super(name, keyMaker);
    }

    @Override
    protected <T> Writeable.Property<T> property(String name) {
        return this.registerInternal(new OrderedProperty(this, name));
    }

    @Override
    protected <T> Writeable.Property<T> property(String name, Function<T, String> toString2) {
        return this.registerInternal(new OrderedProperty<Function<T, String>>(this, name, toString2));
    }

    @Override
    protected <T> Writeable.Property<T> property(String name, T defaultValue) {
        return this.registerInternal(new OrderedProperty<T>(this, name, defaultValue));
    }

    @Override
    protected <T> Writeable.ListProperty<T> listProperty(String name, Writeable.ListProperty.Flags ... flags) {
        return this.listProperty(name, (String)null, flags);
    }

    @Override
    protected <T> Writeable.ListProperty<T> listProperty(String name, String joinKey, Writeable.ListProperty.Flags ... flags) {
        return this.registerInternal(new OrderedListProperty(this, name, joinKey, flags));
    }

    @Override
    protected Writeable.WriteableProperty<?>[] getProperties() {
        return this.order.toArray(new Writeable.WriteableProperty[0]);
    }

    private class OrderedProperty<T>
    extends Writeable.Property<T> {
        final /* synthetic */ AbstractDynamicOrderPropertyBuilder this$0;

        public OrderedProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key);
        }

        public OrderedProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key, Function<T, String> toString2) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key, toString2);
        }

        public OrderedProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key, T defaultValue) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key, defaultValue);
        }

        public OrderedProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key, Function<T, String> toString2, T defaultValue) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key, toString2, defaultValue);
        }

        @Override
        public void setValue(T value) {
            this.this$0.order.add(this);
            super.setValue(value);
        }
    }

    private class OrderedListProperty<T>
    extends Writeable.ListProperty<T> {
        final /* synthetic */ AbstractDynamicOrderPropertyBuilder this$0;

        public OrderedListProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key, Writeable.ListProperty.Flags ... flags) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key, flags);
        }

        public OrderedListProperty(AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder, String key, String joinKey, Writeable.ListProperty.Flags ... flags) {
            AbstractDynamicOrderPropertyBuilder abstractDynamicOrderPropertyBuilder2 = abstractDynamicOrderPropertyBuilder;
            Objects.requireNonNull(abstractDynamicOrderPropertyBuilder2);
            this.this$0 = abstractDynamicOrderPropertyBuilder2;
            super(key, joinKey, flags);
        }

        @Override
        @SafeVarargs
        public final void addValues(T ... values2) {
            this.this$0.order.add(this);
            super.addValues(values2);
        }
    }
}

