package org.spongepowered.asm.mixin;

public @interface Mixin {
    String[] targets() default {};
    boolean remap() default true;
}
