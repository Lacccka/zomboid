/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.validation.AbstractFileValidator;

public class AnimationXmlValidator
extends AbstractFileValidator {
    public static AnimationXmlValidator mask(String name) {
        return new AnimationXmlValidator(name, "media/AnimSets/player/masking%s/%%s.xml".formatted(name.toLowerCase().contains("left") ? "left" : "right"));
    }

    public static AnimationXmlValidator of(String name) {
        return new AnimationXmlValidator(name, "media/AnimSets/player/actions/%s.xml");
    }

    private AnimationXmlValidator(String name, String pathTemplate) {
        super(name, "AnimSetXml", pathTemplate);
    }
}

