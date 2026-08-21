/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.RecipeElement;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import zombie.entity.components.crafting.InputFlag;
import zombie.entity.components.crafting.ItemApplyMode;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;

public class RecipeItemBuilder
implements RecipeElement,
Writeable {
    private ElementType type;
    private final List<String> items = new ArrayList<String>();
    private final List<Number> counts = new ArrayList<Number>();
    private Variable variable;
    private ItemApplyMode mode;
    private final List<String> flags = new ArrayList<String>();
    private final Set<String> mappers = new LinkedHashSet<String>();
    private boolean overlayMapper;
    private Number defaultCount;
    private final boolean isInput;
    private final Set<String> order = new LinkedHashSet<String>();

    public static RecipeItemBuilder input() {
        return new RecipeItemBuilder(true);
    }

    public static RecipeItemBuilder output() {
        return new RecipeItemBuilder(false);
    }

    public RecipeItemBuilder(boolean isInput) {
        this.isInput = isInput;
    }

    public Set<String> getMappers() {
        return this.mappers;
    }

    public RecipeItemBuilder overlayMapper() {
        if (!this.isInput) {
            throw new IllegalStateException("overlayMapper is not allowed on output");
        }
        this.order.add("overlay");
        this.overlayMapper = true;
        return this;
    }

    public boolean hasOverlayMapper() {
        return this.overlayMapper;
    }

    private RecipeItemBuilder defaultCount(Number defaultCount) {
        if (defaultCount.doubleValue() <= 0.0) {
            throw new IllegalStateException("Default count must be greater than zero");
        }
        if (this.defaultCount != null) {
            throw new IllegalStateException("Default count was already set at %s when trying to set to %s".formatted(this.defaultCount, defaultCount));
        }
        this.defaultCount = defaultCount;
        return this;
    }

    public RecipeItemBuilder anyInput() {
        if (!this.isInput) {
            throw new IllegalStateException("Adding anyInput to output is not allowed");
        }
        this.setType(ElementType.ANY);
        this.defaultCount(1);
        return this;
    }

    public RecipeItemBuilder tag(ItemTag ... tag) {
        this.tag(1, tag);
        return this;
    }

    public RecipeItemBuilder tag(int count, ItemTag ... tag) {
        this.add(count, () -> this.fromArray(tag), ElementType.TAG);
        return this;
    }

    public RecipeItemBuilder tag(double count, ItemTag ... tag) {
        if (!this.isInput) {
            throw new IllegalStateException("Adding tag to output is not allowed, use mapper");
        }
        this.add(count, () -> this.fromArray(tag), ElementType.TAG);
        return this;
    }

    public RecipeItemBuilder item(ItemKey input) {
        return this.item(1, input);
    }

    public RecipeItemBuilder item(int count, ItemKey item) {
        this.add(count, item::toString, ElementType.ITEM);
        return this;
    }

    public RecipeItemBuilder item(double count, ItemKey item) {
        this.add(count, item::toString, ElementType.ITEM);
        return this;
    }

    public RecipeItemBuilder mapper(String output) {
        return this.mapper(1, output);
    }

    public RecipeItemBuilder mapper(int count, String output) {
        if (this.isInput) {
            throw new IllegalStateException("Adding mapper to input is not allowed (use mappers)");
        }
        if (!this.items.isEmpty()) {
            throw new IllegalStateException("Adding a second inputMapper, this is not allowed");
        }
        this.add(count, () -> output, ElementType.MAPPER);
        return this;
    }

    public RecipeItemBuilder variable(int min, int max) {
        this.order.add("variable");
        this.variable = new Variable(min, max);
        return this;
    }

    public RecipeItemBuilder mode(ItemApplyMode mode) {
        this.order.add("mode");
        this.mode = mode;
        return this;
    }

    public RecipeItemBuilder flags(InputFlag ... flags) {
        this.order.add("flags");
        Arrays.stream(flags).map(Enum::toString).forEach(this.flags::add);
        return this;
    }

    public RecipeItemBuilder mappers(String ... mappers) {
        if (!this.isInput) {
            throw new IllegalStateException("Adding mappers to output is not allowed (use mapper)");
        }
        this.order.add("mapper");
        for (String mapper : mappers) {
            if (this.mappers.add(mapper)) continue;
            throw new IllegalStateException("Added duplicated input mapper: %s".formatted(mapper));
        }
        return this;
    }

    private void add(Number count, Supplier<String> input, ElementType type) {
        this.setType(type);
        this.items.add(input.get());
        if (this.counts.isEmpty() && this.defaultCount == null) {
            this.defaultCount(count);
        }
        this.counts.add(count);
    }

    private void setType(ElementType type) {
        if (type != this.type && this.type != null) {
            throw new IllegalStateException("Illegal recipe created, attempted to add %s after already adding %s".formatted(new Object[]{type, this.type}));
        }
        this.type = type;
    }

    private String itemWithCount(String prefix) {
        return IntStream.range(0, this.items.size()).mapToObj(i -> "%s%s".formatted(this.defaultCount.equals(this.counts.get(i)) ? "" : "%s:".formatted(this.counts.get(i)), this.items.get(i))).collect(Collectors.joining(";", prefix + "[", "]"));
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        if (this.counts.isEmpty() && this.type != ElementType.ANY) {
            throw new IllegalStateException("No elements found");
        }
        String items = switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "[*]";
            case 1 -> {
                if (this.isInput || this.items.size() > 1) {
                    yield this.itemWithCount("");
                }
                yield this.items.get(0);
            }
            case 2 -> this.itemWithCount("tags");
            case 3 -> "mapper:%s".formatted(this.items.get(0));
        };
        Number count = this.type == ElementType.ANY ? (Number)1 : (Number)this.defaultCount;
        Object value = "item %s %s".formatted(this.variable == null ? count : "variable[%s:%s]".formatted(this.variable.min(), this.variable.max()), items);
        Iterator<String> iterator2 = this.order.iterator();
        while (iterator2.hasNext()) {
            String kind;
            value = (String)value + (switch (kind = iterator2.next()) {
                case "mode" -> {
                    if (this.mode == null) {
                        yield "";
                    }
                    yield " mode:%s".formatted(this.mode.name().toLowerCase());
                }
                case "flags" -> {
                    if (this.flags.isEmpty()) {
                        yield "";
                    }
                    yield this.flags.stream().collect(Collectors.joining(";", " flags[", "]"));
                }
                case "mapper" -> {
                    if (this.mappers.isEmpty()) {
                        yield "";
                    }
                    yield this.mappers.stream().collect(Collectors.joining(";", " mappers[", "]"));
                }
                case "overlay" -> {
                    if (this.overlayMapper) {
                        yield " overlayMapper";
                    }
                    yield "";
                }
                default -> "";
            });
        }
        this.writeValue(writer, indent, value);
    }

    static enum ElementType {
        ANY,
        ITEM,
        TAG,
        MAPPER;

    }

    private record Variable(int min, int max) {
    }
}

