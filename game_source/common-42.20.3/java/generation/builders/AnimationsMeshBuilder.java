/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PathValidator;
import zombie.scripting.ScriptType;

public class AnimationsMeshBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<String> meshFile = this.property("meshFile");
    private final Writeable.ListProperty<PathValidator> animationDirectory = this.listProperty("animationDirectory", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<String> animationPrefix = this.listProperty("animationPrefix", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Boolean> keepMeshAnimations = this.property("keepMeshAnimations");
    private final Writeable.Property<String> postProcess = this.property("postProcess");

    public static AnimationsMeshBuilder withId(String id) {
        return new AnimationsMeshBuilder(id);
    }

    public AnimationsMeshBuilder(String name) {
        super(ScriptType.AnimationMesh, name);
    }

    public AnimationsMeshBuilder meshFile(String meshFile) {
        this.meshFile.setValue(meshFile);
        return this;
    }

    public AnimationsMeshBuilder animationDirectory(String animationDirectory) {
        this.animationDirectory.addValues((PathValidator[])new PathValidator[]{PathValidator.of(animationDirectory)});
        return this;
    }

    public AnimationsMeshBuilder animationPrefix(String animationPrefix) {
        this.animationPrefix.addValues((String[])new String[]{animationPrefix});
        return this;
    }

    public AnimationsMeshBuilder keepMeshAnimations(boolean keepMeshAnimations) {
        this.keepMeshAnimations.setValue(keepMeshAnimations);
        return this;
    }

    public AnimationsMeshBuilder postProcess(String postProcess) {
        this.postProcess.setValue(postProcess);
        return this;
    }
}

