/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import generation.builders.validation.AbstractFileValidator;

public class ClothingItemXmlValidator
extends AbstractFileValidator {
    public static ClothingItemXmlValidator of(String name) {
        return new ClothingItemXmlValidator(name);
    }

    private ClothingItemXmlValidator(String name) {
        super(name, "ClothingItemXml", "media/clothing/clothingItems/%s.xml");
    }
}

