/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.converter;

import java.util.ArrayList;
import java.util.List;
import se.krka.kahlua.converter.LuaToJavaConverter;

public class MultiLuaToJavaConverter<LuaType, JavaType>
implements LuaToJavaConverter<LuaType, JavaType> {
    private final List<LuaToJavaConverter<LuaType, JavaType>> converters = new ArrayList<LuaToJavaConverter<LuaType, JavaType>>();
    private final Class<LuaType> luaType;
    private final Class<JavaType> javaType;

    public MultiLuaToJavaConverter(Class<LuaType> luaType, Class<JavaType> javaType) {
        this.luaType = luaType;
        this.javaType = javaType;
    }

    @Override
    public Class<LuaType> getLuaType() {
        return this.luaType;
    }

    @Override
    public Class<JavaType> getJavaType() {
        return this.javaType;
    }

    @Override
    public JavaType fromLuaToJava(LuaType luaObject, Class<JavaType> javaClass) {
        for (LuaToJavaConverter<LuaType, JavaType> converter : this.converters) {
            JavaType res = converter.fromLuaToJava(luaObject, javaClass);
            if (res == null) continue;
            return res;
        }
        return null;
    }

    public void add(LuaToJavaConverter<LuaType, JavaType> converter) {
        this.converters.add(converter);
    }
}

