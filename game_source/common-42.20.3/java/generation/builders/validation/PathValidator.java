/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.validation.AbstractFileValidator;

public class PathValidator
extends AbstractFileValidator {
    public static PathValidator of(String name) {
        return new PathValidator(name, "media/anims_X");
    }

    private PathValidator(String name, String ... pathTemplate) {
        super(name, "folder", pathTemplate);
    }
}

