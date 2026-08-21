/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileElement;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CompoundElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCReturnTransform;
import org.lwjgl.system.ffm.FFM;
import org.lwjgl.system.ffm.FFMCharset;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.FFMName;
import org.lwjgl.system.ffm.FFMNoPrefix;
import org.lwjgl.system.ffm.FFMNullable;
import org.lwjgl.system.ffm.FFMPointer;
import org.lwjgl.system.ffm.FFMPrefix;

final class BCUtil {
    static final int JAVA_VERSION;
    static final long NATIVE_THRESHOLD_FILL;
    static final long NATIVE_THRESHOLD_COPY;
    static final Linker.Option[] EMPTY_OPTIONS;
    static final Object EMPTY_SLOT;
    private static final Pattern NULLABLE_PATTERN;
    private static final String PROPERTY_PATH = "java.lang.foreign.native.threshold.power.";

    private BCUtil() {
    }

    static ClassBuilder startHiddenClass(ClassBuilder classBuilder) {
        return classBuilder.withVersion(ClassFile.latestMajorVersion(), ClassFile.latestMinorVersion()).withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL).withSuperclass(ConstantDescs.CD_Object).withMethod("<init>", ConstantDescs.MTD_void, 1, mb -> mb.withCode(cb -> cb.aload(cb.receiverSlot()).invokespecial(ConstantDescs.CD_Object, "<init>", ConstantDescs.MTD_void, false).return_()));
    }

    static MethodTypeDesc getMethodTypeDesc(Method method) {
        return MethodTypeDesc.of(method.getReturnType().describeConstable().orElseThrow(), BCUtil.getParameterDescs(method));
    }

    private static ClassDesc[] getParameterDescs(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        ClassDesc[] parameterDescs = new ClassDesc[parameterTypes.length];
        for (int p = 0; p < parameterTypes.length; ++p) {
            parameterDescs[p] = parameterTypes[p].describeConstable().orElseThrow();
        }
        return parameterDescs;
    }

    private static ClassDesc[] getParameterDescsNative(Method method, BCReturnTransform returnTransform) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        ClassDesc[] parameterDescs = new ClassDesc[parameterTypes.length];
        for (int p = 0; p < parameterTypes.length; ++p) {
            parameterDescs[p] = BCUtil.getClassDescNative(parameterTypes[p]);
        }
        return parameterDescs;
    }

    private static ClassDesc getClassDescNative(Class<?> type) {
        Class nativeType = type == String.class ? MemorySegment.class : type;
        return nativeType.describeConstable().orElseThrow();
    }

    static String getNativeName(Class<?> type) {
        FFMName nativeName = type.getAnnotation(FFMName.class);
        return nativeName != null ? nativeName.value() : type.getSimpleName();
    }

    static String getNativeName(Method method) {
        Object name;
        FFMName nativeName = method.getAnnotation(FFMName.class);
        if (nativeName != null) {
            name = nativeName.value();
        } else {
            name = method.getName();
            FFMPrefix nativePrefix = method.getDeclaringClass().getAnnotation(FFMPrefix.class);
            if (nativePrefix != null && !method.isAnnotationPresent(FFMNoPrefix.class)) {
                name = nativePrefix.value() + (String)name;
            }
        }
        return name;
    }

    private static void checkFFMNullableOnPrimitive(AnnotatedElement element, Class<?> type) {
        if (Checks.DEBUG && (!element.isAnnotationPresent(FFMPointer.class) || type != Long.TYPE)) {
            throw new IllegalStateException("The FFMNullable annotation can be applied to @FFMPointer long types only");
        }
    }

    private static void checkFFMNullableOnReference(AnnotatedElement element) {
        if (Checks.DEBUG && element.isAnnotationPresent(FFMNullable.class)) {
            throw new IllegalStateException("The FFMNullable annotation can be applied to @FFMPointer long parameters only");
        }
    }

    private static void checkAnnotations(AnnotatedElement element, Class<? extends AnnotatedElement> type) {
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getPackage() == FFM.class.getPackage()) continue;
            APIUtil.apiLog("Unsupported annotation found on " + type.getSimpleName().toLowerCase() + ": " + String.valueOf(element));
            if (!NULLABLE_PATTERN.matcher(annotationType.getSimpleName()).find()) continue;
            APIUtil.apiLog("\tUse FFMConfigBuilder::withNullableAnnotation if applicable.");
        }
    }

    private static <T extends AnnotatedElement> void checkAnnotations(T element, Function<T, AnnotatedType> annotatedTypeProvider) {
        if (Checks.DEBUG) {
            Class<?> type = element.getClass();
            BCUtil.checkAnnotations(element, type);
            BCUtil.checkAnnotations(annotatedTypeProvider.apply(element), type);
        }
    }

    static <T extends AnnotatedElement> boolean isNullable(FFMConfig config, T element, Class<?> type, Function<T, AnnotatedType> annotatedTypeProvider) {
        if (type.isPrimitive()) {
            BCUtil.checkFFMNullableOnPrimitive(element, type);
            return element.isAnnotationPresent(FFMNullable.class);
        }
        Class<? extends Annotation> nullableAnnotation = config.nullableAnnotation;
        BCUtil.checkFFMNullableOnReference(element);
        if (nullableAnnotation != null) {
            return config.nullableAnnotationOnType ? annotatedTypeProvider.apply(element).isAnnotationPresent(nullableAnnotation) : element.isAnnotationPresent(nullableAnnotation);
        }
        BCUtil.checkAnnotations(element, annotatedTypeProvider);
        return false;
    }

    static boolean isNullable(FFMConfig config, Method method) {
        return BCUtil.isNullable(config, method, method.getReturnType(), Method::getAnnotatedReturnType);
    }

    static boolean isNullable(FFMConfig config, Parameter parameter) {
        return BCUtil.isNullable(config, parameter, parameter.getType(), Parameter::getAnnotatedType);
    }

    static <T extends CodeBuilder> T buildPointer64to32(T cb) {
        cb.l2i();
        return cb;
    }

    static <T extends CodeBuilder> T buildPointer32to64(T cb) {
        cb.i2l().loadConstant(0xFFFFFFFFL).land();
        return cb;
    }

    static <T extends CodeBuilder> T buildGetString(T cb, Method method) {
        cb.lconst_0();
        BCUtil.buildCharsetInstance(cb, BCUtil.getCharset(method)).invokeinterface(BCDescriptors.CD_MemorySegment, "getString", BCDescriptors.MTD_String_long_Charset);
        return cb;
    }

    static <T extends CodeBuilder> T buildCharsetInstance(T cb, FFMCharset.Type type) {
        cb.getstatic(BCDescriptors.CD_StandardCharsets, type.charset, BCDescriptors.CD_Charset);
        return cb;
    }

    static <T extends CodeBuilder> T buildCharsetShift(T cb, FFMCharset.Type type, TypeKind kind) {
        int byteSize = (int)type.layout.byteSize();
        if (byteSize == 1) {
            return cb;
        }
        switch (byteSize) {
            case 2: {
                cb.iconst_1();
                break;
            }
            case 4: {
                cb.iconst_2();
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        if (kind != TypeKind.LONG) {
            cb.ishl();
        } else {
            cb.lshl();
        }
        return cb;
    }

    static <T> DynamicConstantDesc<T> condyCData(ClassDesc constantType) {
        return DynamicConstantDesc.ofNamed(ConstantDescs.BSM_CLASS_DATA, "_", constantType, new ConstantDesc[0]);
    }

    static <T> DynamicConstantDesc<T> condyCDataAt(ClassDesc constantType, int index) {
        return DynamicConstantDesc.ofNamed(ConstantDescs.BSM_CLASS_DATA_AT, "_", constantType, Integer.valueOf(index));
    }

    static FFMCharset.Type getCharset(Method method) {
        FFMCharset annotation = method.getAnnotation(FFMCharset.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(FFMCharset.class);
        }
        return annotation != null ? annotation.value() : FFMCharset.DEFAULT;
    }

    static FFMCharset.Type getCharset(Parameter parameter) {
        FFMCharset annotation = parameter.getAnnotation(FFMCharset.class);
        if (annotation == null) {
            annotation = parameter.getDeclaringExecutable().getDeclaringClass().getAnnotation(FFMCharset.class);
        }
        return annotation != null ? annotation.value() : FFMCharset.DEFAULT;
    }

    static boolean isPointerType(Parameter parameter, Class<?> type) {
        return type == MemorySegment.class || type == Long.TYPE && parameter.isAnnotationPresent(FFMPointer.class);
    }

    private static String getParameterName(Parameter parameter, int index) {
        return parameter.isNamePresent() ? "<" + parameter.getName() + ">" : "#" + index;
    }

    static String getExceptionTextNULL(Parameter parameter, int index) {
        return parameter.getType().getSimpleName() + " argument " + BCUtil.getParameterName(parameter, index) + " cannot be NULL";
    }

    static void printModel(CompoundElement<?> model) {
        APIUtil.DEBUG_STREAM.println(model);
        BCUtil.printModel(model, 0);
    }

    private static void printModel(CompoundElement<?> model, int depth) {
        String indent = "\t".repeat(depth);
        int bci = 0;
        for (ClassFileElement el : model) {
            if (el instanceof Instruction) {
                Instruction i = (Instruction)el;
                APIUtil.DEBUG_STREAM.println(indent + bci + ": " + String.valueOf(i));
                bci += i.sizeInBytes();
            } else {
                APIUtil.DEBUG_STREAM.println(indent + " ".repeat(Integer.toString(bci).length()) + "* " + String.valueOf(el));
            }
            if (!(el instanceof CompoundElement)) continue;
            CompoundElement ce = (CompoundElement)el;
            BCUtil.printModel(ce, depth + 1);
        }
    }

    private static long powerOfPropertyOr(String name, int defaultPower) {
        int power = Integer.getInteger(PROPERTY_PATH + name, defaultPower);
        return 1L << Math.clamp((long)power, 0, 30);
    }

    static {
        String javaVersion = System.getProperty("java.version");
        Matcher matcher = Pattern.compile("^([1-9][0-9]*)(?:(?:\\.0)*\\.[1-9][0-9]*)*(?:-[a-zA-Z0-9]+)?").matcher(javaVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Failed to parse java.version: " + javaVersion);
        }
        JAVA_VERSION = Integer.parseInt(matcher.group(1));
        NATIVE_THRESHOLD_FILL = BCUtil.powerOfPropertyOr("fill", 5);
        NATIVE_THRESHOLD_COPY = BCUtil.powerOfPropertyOr("copy", 6);
        EMPTY_OPTIONS = new Linker.Option[0];
        EMPTY_SLOT = new Object();
        NULLABLE_PATTERN = Pattern.compile("null", 2);
    }
}

