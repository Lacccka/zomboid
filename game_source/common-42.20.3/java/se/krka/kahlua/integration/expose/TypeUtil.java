/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zombie.debug.DebugLog;

public class TypeUtil {
    private static final Pattern pattern = Pattern.compile("([\\.a-z0-9]*)\\.([A-Za-z][A-Za-z0-9_]*)");

    public static String removePackages(String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("$2");
    }

    public static String getClassName(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class)type;
            if (clazz.isArray()) {
                return TypeUtil.getClassName(clazz.getComponentType()) + "[]";
            }
            return clazz.getName();
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            Type[] upper = wildcardType.getUpperBounds();
            Type[] lower = wildcardType.getLowerBounds();
            return TypeUtil.handleBounds("?", upper, lower);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            Type[] args2 = paramType.getActualTypeArguments();
            String raw = TypeUtil.getClassName(paramType.getRawType());
            if (args2.length == 0) {
                return raw;
            }
            StringBuilder builder = new StringBuilder(raw);
            builder.append("<");
            for (int i = 0; i < args2.length; ++i) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(TypeUtil.getClassName(args2[i]));
            }
            builder.append(">");
            return builder.toString();
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)type;
            return typeVariable.getName();
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType)type;
            return TypeUtil.getClassName(arrayType.getGenericComponentType()) + "[]";
        }
        DebugLog.log("got unknown: " + String.valueOf(type) + ", " + String.valueOf(type.getClass()));
        return "unknown";
    }

    static String handleBounds(String s, Type[] upper, Type[] lower) {
        if (upper != null) {
            if (upper.length == 1 && upper[0] == Object.class) {
                return s;
            }
            if (upper.length >= 1) {
                StringBuilder list = new StringBuilder();
                boolean first = true;
                for (Type typeExtends : upper) {
                    if (first) {
                        first = false;
                    } else {
                        list.append(", ");
                    }
                    list.append(TypeUtil.getClassName(typeExtends));
                }
                return s + " extends " + list.toString();
            }
        }
        if (lower != null && lower.length > 0) {
            StringBuilder list = new StringBuilder();
            boolean first = true;
            for (Type typeExtends : lower) {
                if (first) {
                    first = false;
                } else {
                    list.append(", ");
                }
                list.append(TypeUtil.getClassName(typeExtends));
            }
            return s + " super " + list.toString();
        }
        return "unknown type";
    }
}

