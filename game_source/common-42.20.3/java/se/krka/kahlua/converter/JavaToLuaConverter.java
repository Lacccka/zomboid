/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.converter;

public interface JavaToLuaConverter<JavaType> {
    public Class<JavaType> getJavaType();

    public Object fromJavaToLua(JavaType var1);
}

