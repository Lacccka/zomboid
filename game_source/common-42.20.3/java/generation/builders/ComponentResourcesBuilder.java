/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import java.util.stream.Stream;

public class ComponentResourcesBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<Group> groups = this.listProperty("group", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public ComponentResourcesBuilder() {
        super("Resources");
    }

    public ComponentResourcesBuilder addGroup(String name, String ... elements) {
        this.groups.addValues((Group[])new Group[]{new Group(name, (GroupElement[])Stream.of(elements).map(GroupElement::new).toArray(GroupElement[]::new))});
        return this;
    }

    public static final class Group
    extends AbstractPropertyBuilder {
        private final Writeable.ListProperty<GroupElement> elements = this.listProperty("elements", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK, Writeable.ListProperty.Flags.HIDE_KEY);

        public Group(String name, GroupElement ... elements) {
            super(name);
            this.elements.addValues((GroupElement[])elements);
        }
    }

    public record GroupElement(String data) {
        @Override
        public String toString() {
            return this.data;
        }
    }
}

