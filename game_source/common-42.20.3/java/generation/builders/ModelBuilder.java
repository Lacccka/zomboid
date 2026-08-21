/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.ModelAttachmentBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;
import generation.builders.validation.XFileValidator;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.CullFace;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ModelKey;

public class ModelBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<ModelAttachmentBuilder> attachment = this.listProperty("attachment", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<CullFace> cullFace = this.property("cullFace");
    private final Writeable.Property<Boolean> isStatic = this.property("static");
    private final Writeable.Property<Boolean> invertX = this.property("invertX");
    private final Writeable.Property<Boolean> undoCoreScale = this.property("undoCoreScale");
    private final Writeable.Property<Float> scale = this.property("scale");
    private final Writeable.Property<Integer> colorBlue = this.property("ColorBlue");
    private final Writeable.Property<Integer> colorGreen = this.property("ColorGreen");
    private final Writeable.Property<Integer> colorRed = this.property("ColorRed");
    private final Writeable.Property<PackTextureValidator> texture = this.property("texture");
    private final Writeable.Property<String> animationsMesh = this.property("animationsMesh");
    private final Writeable.Property<String> boneWeight = this.property("boneWeight");
    private final Writeable.Property<XFileValidator> mesh = this.property("mesh");
    private final Writeable.Property<String> postProcess = this.property("postProcess");
    private final Writeable.Property<String> shader = this.property("shader");
    private final Writeable.ListProperty<ItemKey> specialKeyRing = this.listProperty("specialKeyRing", ";", new Writeable.ListProperty.Flags[0]);

    public static ModelBuilder withId(ModelKey id) {
        return new ModelBuilder(id.id());
    }

    private ModelBuilder(String name) {
        super(ScriptType.Model, name);
    }

    public ModelBuilder _static(boolean isStatic) {
        this.isStatic.setValue(isStatic);
        return this;
    }

    public ModelBuilder addAttachment(ModelAttachmentBuilder attachment) {
        this.attachment.addValues((ModelAttachmentBuilder[])new ModelAttachmentBuilder[]{attachment});
        return this;
    }

    public ModelBuilder animationsMesh(String animationsMesh) {
        this.animationsMesh.setValue(animationsMesh);
        return this;
    }

    public ModelBuilder boneWeight(String boneWeight) {
        this.boneWeight.setValue(boneWeight);
        return this;
    }

    public ModelBuilder colorBlue(int colorBlue) {
        this.colorBlue.setValue(colorBlue);
        return this;
    }

    public ModelBuilder colorGreen(int colorGreen) {
        this.colorGreen.setValue(colorGreen);
        return this;
    }

    public ModelBuilder colorRed(int colorRed) {
        this.colorRed.setValue(colorRed);
        return this;
    }

    public ModelBuilder cullFace(CullFace cullFace) {
        this.cullFace.setValue(cullFace);
        return this;
    }

    public ModelBuilder invertX(boolean invertX) {
        this.invertX.setValue(invertX);
        return this;
    }

    public ModelBuilder mesh(String model, String mesh) {
        return this.mesh("%s|%s".formatted(model, mesh));
    }

    public ModelBuilder mesh(String mesh) {
        this.mesh.setValue(XFileValidator.of(mesh));
        return this;
    }

    public ModelBuilder postProcess(String postProcess) {
        this.postProcess.setValue(postProcess);
        return this;
    }

    public ModelBuilder scale(float scale) {
        this.scale.setValue(Float.valueOf(scale));
        return this;
    }

    public ModelBuilder shader(String shader) {
        this.shader.setValue(shader);
        return this;
    }

    public ModelBuilder texture(String texture) {
        this.texture.setValue(PackTextureValidator.of(texture, ""));
        return this;
    }

    public ModelBuilder undoCoreScale(boolean undoCoreScale) {
        this.undoCoreScale.setValue(undoCoreScale);
        return this;
    }

    public ModelBuilder specialKeyRing(ItemKey ... specialKeyRing) {
        this.specialKeyRing.addValues((ItemKey[])specialKeyRing);
        return this;
    }
}

