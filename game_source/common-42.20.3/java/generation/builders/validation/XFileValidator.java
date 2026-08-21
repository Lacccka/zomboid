/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.validation.AbstractFileValidator;

public class XFileValidator
extends AbstractFileValidator {
    public static XFileValidator of(String name) {
        return new XFileValidator(name, new String[]{"media/models_X/%s.x", "media/models_X/%s.fbx", "media/models_X/%s", "media/models_X/%s.glb", "media/models/%s.txt"});
    }

    private XFileValidator(String name, String ... pathTemplate) {
        super(name, "models_X", pathTemplate);
    }
}

