/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.CraftRecipeTag;
import zombie.scripting.objects.EntityCategory;
import zombie.scripting.objects.EntityKey;

public class EntityBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<CraftRecipeTag> tags = this.listProperty("Tags", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<EntityCategory[]> category = this.property("category");
    private final Writeable.ListProperty<ComponentBuilder> addComponent = this.listProperty("component", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public static EntityBuilder withId(EntityKey id) {
        return new EntityBuilder(id.toString());
    }

    private EntityBuilder(String name) {
        super(ScriptType.Entity, name);
    }

    public EntityBuilder tags(CraftRecipeTag ... tags) {
        this.tags.addValues((CraftRecipeTag[])tags);
        return this;
    }

    public EntityBuilder category(EntityCategory ... category) {
        this.category.setValue(category);
        return this;
    }

    public EntityBuilder addComponent(ComponentBuilder addComponent) {
        this.addComponent.addValues((ComponentBuilder[])new ComponentBuilder[]{addComponent});
        return this;
    }
}

