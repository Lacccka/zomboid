/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.converter;

import java.util.HashMap;
import java.util.Map;
import se.krka.kahlua.converter.JavaToLuaConverter;
import se.krka.kahlua.converter.LuaToJavaConverter;
import se.krka.kahlua.converter.MultiJavaToLuaConverter;
import se.krka.kahlua.converter.MultiLuaToJavaConverter;

public class KahluaConverterManager {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_CLASS = new HashMap();
    private static final Map<Class, LuaToJavaConverter> LUA_NULL_MAP;
    private final Map<Class, Map<Class, LuaToJavaConverter>> luaToJava = new HashMap<Class, Map<Class, LuaToJavaConverter>>();
    private final Map<Class, Map<Class, LuaToJavaConverter>> luatoJavaCache = new HashMap<Class, Map<Class, LuaToJavaConverter>>();
    private static final JavaToLuaConverter NULL_CONVERTER;
    private final Map<Class, JavaToLuaConverter> javaToLua = new HashMap<Class, JavaToLuaConverter>();
    private final Map<Class, JavaToLuaConverter> javaToLuaCache = new HashMap<Class, JavaToLuaConverter>();

    public void addLuaConverter(LuaToJavaConverter converter) {
        Class javaType;
        Map<Class, LuaToJavaConverter> map = this.getOrCreate(this.luaToJava, converter.getLuaType());
        LuaToJavaConverter current = map.get(javaType = converter.getJavaType());
        if (current != null) {
            if (current instanceof MultiLuaToJavaConverter) {
                MultiLuaToJavaConverter luaToJavaConverter = (MultiLuaToJavaConverter)current;
                luaToJavaConverter.add(converter);
            } else {
                MultiLuaToJavaConverter multiLuaToJavaConverter = new MultiLuaToJavaConverter(converter.getLuaType(), javaType);
                multiLuaToJavaConverter.add(current);
                multiLuaToJavaConverter.add(converter);
                map.put(javaType, multiLuaToJavaConverter);
            }
        } else {
            map.put(javaType, converter);
        }
        this.luatoJavaCache.clear();
    }

    public void addJavaConverter(JavaToLuaConverter converter) {
        Class javaType = converter.getJavaType();
        JavaToLuaConverter current = this.javaToLua.get(javaType);
        if (current != null) {
            if (current instanceof MultiJavaToLuaConverter) {
                MultiJavaToLuaConverter multiJavaToLuaConverter = (MultiJavaToLuaConverter)current;
                multiJavaToLuaConverter.add(converter);
            } else {
                MultiJavaToLuaConverter multiConverter = new MultiJavaToLuaConverter(javaType);
                multiConverter.add(current);
                multiConverter.add(converter);
                this.javaToLua.put(javaType, multiConverter);
            }
        } else {
            this.javaToLua.put(javaType, converter);
        }
        this.javaToLuaCache.clear();
    }

    private Map<Class, LuaToJavaConverter> getOrCreate(Map<Class, Map<Class, LuaToJavaConverter>> luaToJava2, Class luaType) {
        Map<Class, LuaToJavaConverter> map = luaToJava2.get(luaType);
        if (map == null) {
            map = new HashMap<Class, LuaToJavaConverter>();
            luaToJava2.put(luaType, map);
        }
        return map;
    }

    public <T> T fromLuaToJava(Object luaObject, Class<T> javaClass) {
        if (luaObject == null) {
            return null;
        }
        if (javaClass.isPrimitive()) {
            javaClass = PRIMITIVE_CLASS.get(javaClass);
        }
        if (javaClass.isInstance(luaObject)) {
            return (T)luaObject;
        }
        Class<?> luaClass = luaObject.getClass();
        Map<Class, LuaToJavaConverter> map = this.getLuaCache(luaClass);
        for (Class<Object> scanClass = javaClass; scanClass != null; scanClass = scanClass.getSuperclass()) {
            Object o;
            LuaToJavaConverter converter = map.get(scanClass);
            if (converter == null || (o = converter.fromLuaToJava(luaObject, javaClass)) == null) continue;
            return (T)o;
        }
        return this.tryInterfaces(map, javaClass, luaObject);
    }

    private <T> T tryInterfaces(Map<Class, LuaToJavaConverter> map, Class<T> javaClass, Object luaObject) {
        T o;
        if (javaClass == null) {
            return null;
        }
        LuaToJavaConverter converter = map.get(javaClass);
        if (converter != null && (o = converter.fromLuaToJava(luaObject, javaClass)) != null) {
            return o;
        }
        for (Class<?> aClass : javaClass.getInterfaces()) {
            Object o2 = this.tryInterfaces(map, aClass, luaObject);
            if (o2 == null) continue;
            return (T)o2;
        }
        return this.tryInterfaces(map, javaClass.getSuperclass(), luaObject);
    }

    private Map<Class, LuaToJavaConverter> createLuaCache(Class<?> luaClass) {
        HashMap<Class, LuaToJavaConverter> map = new HashMap<Class, LuaToJavaConverter>();
        this.luatoJavaCache.put(luaClass, map);
        map.putAll(this.getLuaCache(luaClass.getSuperclass()));
        for (Class<?> clazz : luaClass.getInterfaces()) {
            map.putAll(this.getLuaCache(clazz));
        }
        Map<Class, LuaToJavaConverter> directMap = this.luaToJava.get(luaClass);
        if (directMap != null) {
            map.putAll(directMap);
        }
        return map;
    }

    private Map<Class, LuaToJavaConverter> getLuaCache(Class<?> clazz) {
        if (clazz == null) {
            return LUA_NULL_MAP;
        }
        Map<Class, LuaToJavaConverter> map = this.luatoJavaCache.get(clazz);
        if (map == null) {
            map = this.createLuaCache(clazz);
        }
        return map;
    }

    public Object fromJavaToLua(Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        Class<?> clazz = javaObject.getClass();
        JavaToLuaConverter converter = this.getJavaCache(clazz);
        try {
            Object o = converter.fromJavaToLua(javaObject);
            if (o == null) {
                return javaObject;
            }
            return o;
        }
        catch (StackOverflowError e) {
            throw new RuntimeException("Could not convert " + String.valueOf(javaObject) + ": it contained recursive elements.");
        }
    }

    private JavaToLuaConverter getJavaCache(Class clazz) {
        if (clazz == null) {
            return NULL_CONVERTER;
        }
        JavaToLuaConverter converter = this.javaToLuaCache.get(clazz);
        if (converter == null) {
            converter = this.createJavaCache(clazz);
            this.javaToLuaCache.put(clazz, converter);
        }
        return converter;
    }

    private JavaToLuaConverter createJavaCache(Class javaClass) {
        JavaToLuaConverter converter = this.javaToLua.get(javaClass);
        if (converter != null) {
            return converter;
        }
        for (Class<?> clazz : javaClass.getInterfaces()) {
            converter = this.getJavaCache(clazz);
            if (converter == NULL_CONVERTER) continue;
            return converter;
        }
        return this.getJavaCache(javaClass.getSuperclass());
    }

    static {
        PRIMITIVE_CLASS.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_CLASS.put(Byte.TYPE, Byte.class);
        PRIMITIVE_CLASS.put(Character.TYPE, Character.class);
        PRIMITIVE_CLASS.put(Short.TYPE, Short.TYPE);
        PRIMITIVE_CLASS.put(Integer.TYPE, Integer.class);
        PRIMITIVE_CLASS.put(Long.TYPE, Long.class);
        PRIMITIVE_CLASS.put(Float.TYPE, Float.class);
        PRIMITIVE_CLASS.put(Double.TYPE, Double.class);
        LUA_NULL_MAP = new HashMap<Class, LuaToJavaConverter>();
        NULL_CONVERTER = new JavaToLuaConverter<Object>(){

            @Override
            public Object fromJavaToLua(Object javaObject) {
                return null;
            }

            @Override
            public Class<Object> getJavaType() {
                return Object.class;
            }
        };
    }
}

