/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.validation.AbstractFileValidator;

public class ShaderValidator
extends AbstractFileValidator {
    public static ShaderValidator of(String name) {
        return new ShaderValidator(name, "media/shaders/%s.frag");
    }

    private ShaderValidator(String name, String ... pathTemplate) {
        super(name, "shader", pathTemplate);
    }
}

