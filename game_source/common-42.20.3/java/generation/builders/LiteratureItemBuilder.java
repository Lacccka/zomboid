/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class LiteratureItemBuilder
extends ItemBuilder<LiteratureItemBuilder> {
    private final Writeable.Property<Boolean> canBeWrite = this.property("CanBeWrite");
    private final Writeable.Property<Integer> lvlSkillTrained = this.property("LvlSkillTrained");
    private final Writeable.Property<Integer> numLevelsTrained = this.property("NumLevelsTrained");
    private final Writeable.Property<Integer> numberOfPages = this.property("NumberOfPages");
    private final Writeable.Property<Integer> pageToWrite = this.property("PageToWrite");
    private final Writeable.Property<String> skillTrained = this.property("SkillTrained");

    public LiteratureItemBuilder(ItemKey item) {
        super(item);
    }

    public LiteratureItemBuilder canBeWrite(boolean canBeWrite) {
        this.canBeWrite.setValue(canBeWrite);
        return this;
    }

    public LiteratureItemBuilder lvlSkillTrained(int lvlSkillTrained) {
        this.lvlSkillTrained.setValue(lvlSkillTrained);
        return this;
    }

    public LiteratureItemBuilder numLevelsTrained(int numLevelsTrained) {
        this.numLevelsTrained.setValue(numLevelsTrained);
        return this;
    }

    public LiteratureItemBuilder numberOfPages(int numberOfPages) {
        this.numberOfPages.setValue(numberOfPages);
        return this;
    }

    public LiteratureItemBuilder pageToWrite(int pageToWrite) {
        this.pageToWrite.setValue(pageToWrite);
        return this;
    }

    public LiteratureItemBuilder skillTrained(String skillTrained) {
        this.skillTrained.setValue(skillTrained);
        return this;
    }
}

