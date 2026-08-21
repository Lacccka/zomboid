/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.converter;

public interface LuaToJavaConverter<LuaType, JavaType> {
    public Class<LuaType> getLuaType();

    public Class<JavaType> getJavaType();

    public JavaType fromLuaToJava(LuaType var1, Class<JavaType> var2);
}

