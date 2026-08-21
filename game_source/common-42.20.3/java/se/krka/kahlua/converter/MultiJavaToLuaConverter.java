/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.converter;

import java.util.ArrayList;
import java.util.List;
import se.krka.kahlua.converter.JavaToLuaConverter;

public class MultiJavaToLuaConverter<JavaType>
implements JavaToLuaConverter<JavaType> {
    private final List<JavaToLuaConverter> converters = new ArrayList<JavaToLuaConverter>();
    private final Class<JavaType> clazz;

    public MultiJavaToLuaConverter(Class<JavaType> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<JavaType> getJavaType() {
        return this.clazz;
    }

    @Override
    public Object fromJavaToLua(JavaType javaObject) {
        for (JavaToLuaConverter converter : this.converters) {
            Object res = converter.fromJavaToLua(javaObject);
            if (res == null) continue;
            return res;
        }
        return null;
    }

    public void add(JavaToLuaConverter converter) {
        this.converters.add(converter);
    }
}

