/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiBlendMode;
import jassimp.AiShadingMode;
import jassimp.AiTextureInfo;
import jassimp.AiTextureMapMode;
import jassimp.AiTextureOp;
import jassimp.AiTextureType;
import jassimp.AiWrapperProvider;
import jassimp.Jassimp;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AiMaterial {
    private final List<Property> m_properties = new ArrayList<Property>();
    private final Map<AiTextureType, Integer> m_numTextures = new EnumMap<AiTextureType, Integer>(AiTextureType.class);
    private Map<PropertyKey, Object> m_defaults = new EnumMap<PropertyKey, Object>(PropertyKey.class);

    AiMaterial() {
        this.setDefault(PropertyKey.NAME, "");
        this.setDefault(PropertyKey.TWO_SIDED, 0);
        this.setDefault(PropertyKey.SHADING_MODE, (Object)AiShadingMode.FLAT);
        this.setDefault(PropertyKey.WIREFRAME, 0);
        this.setDefault(PropertyKey.BLEND_MODE, (Object)AiBlendMode.DEFAULT);
        this.setDefault(PropertyKey.OPACITY, Float.valueOf(1.0f));
        this.setDefault(PropertyKey.BUMP_SCALING, Float.valueOf(1.0f));
        this.setDefault(PropertyKey.SHININESS, Float.valueOf(1.0f));
        this.setDefault(PropertyKey.REFLECTIVITY, Float.valueOf(0.0f));
        this.setDefault(PropertyKey.SHININESS_STRENGTH, Float.valueOf(0.0f));
        this.setDefault(PropertyKey.REFRACTI, Float.valueOf(0.0f));
        this.m_defaults.put(PropertyKey.COLOR_DIFFUSE, null);
        this.m_defaults.put(PropertyKey.COLOR_AMBIENT, null);
        this.m_defaults.put(PropertyKey.COLOR_SPECULAR, null);
        this.m_defaults.put(PropertyKey.COLOR_EMISSIVE, null);
        this.m_defaults.put(PropertyKey.COLOR_TRANSPARENT, null);
        this.m_defaults.put(PropertyKey.COLOR_REFLECTIVE, null);
        this.setDefault(PropertyKey.GLOBAL_BACKGROUND_IMAGE, "");
        this.setDefault(PropertyKey.TEX_FILE, "");
        this.setDefault(PropertyKey.TEX_UV_INDEX, 0);
        this.setDefault(PropertyKey.TEX_BLEND, Float.valueOf(1.0f));
        this.setDefault(PropertyKey.TEX_OP, (Object)AiTextureOp.ADD);
        this.setDefault(PropertyKey.TEX_MAP_MODE_U, (Object)AiTextureMapMode.CLAMP);
        this.setDefault(PropertyKey.TEX_MAP_MODE_V, (Object)AiTextureMapMode.CLAMP);
        this.setDefault(PropertyKey.TEX_MAP_MODE_W, (Object)AiTextureMapMode.CLAMP);
        for (PropertyKey propertyKey : PropertyKey.values()) {
            if (this.m_defaults.containsKey((Object)propertyKey)) continue;
            throw new IllegalStateException("missing default for: " + propertyKey);
        }
    }

    public boolean hasProperties(Set<PropertyKey> set) {
        for (PropertyKey propertyKey : set) {
            if (null != this.getProperty(propertyKey.m_key)) continue;
            return false;
        }
        return true;
    }

    public void setDefault(PropertyKey propertyKey, Object object) {
        if (null == object) {
            throw new IllegalArgumentException("defaultValue may not be null");
        }
        if (propertyKey.m_type != object.getClass()) {
            throw new IllegalArgumentException("defaultValue has wrong type, expected: " + propertyKey.m_type + ", found: " + object.getClass());
        }
        this.m_defaults.put(propertyKey, object);
    }

    public String getName() {
        return this.getTyped(PropertyKey.NAME, String.class);
    }

    public int getTwoSided() {
        return this.getTyped(PropertyKey.TWO_SIDED, Integer.class);
    }

    public AiShadingMode getShadingMode() {
        Property property = this.getProperty(PropertyKey.SHADING_MODE.m_key);
        if (null == property || null == property.getData()) {
            return (AiShadingMode)((Object)this.m_defaults.get((Object)PropertyKey.SHADING_MODE));
        }
        return AiShadingMode.fromRawValue((Integer)property.getData());
    }

    public int getWireframe() {
        return this.getTyped(PropertyKey.WIREFRAME, Integer.class);
    }

    public AiBlendMode getBlendMode() {
        Property property = this.getProperty(PropertyKey.BLEND_MODE.m_key);
        if (null == property || null == property.getData()) {
            return (AiBlendMode)((Object)this.m_defaults.get((Object)PropertyKey.BLEND_MODE));
        }
        return AiBlendMode.fromRawValue((Integer)property.getData());
    }

    public float getOpacity() {
        return this.getTyped(PropertyKey.OPACITY, Float.class).floatValue();
    }

    public float getBumpScaling() {
        return this.getTyped(PropertyKey.BUMP_SCALING, Float.class).floatValue();
    }

    public float getShininess() {
        return this.getTyped(PropertyKey.SHININESS, Float.class).floatValue();
    }

    public float getReflectivity() {
        return this.getTyped(PropertyKey.REFLECTIVITY, Float.class).floatValue();
    }

    public float getShininessStrength() {
        return this.getTyped(PropertyKey.SHININESS_STRENGTH, Float.class).floatValue();
    }

    public float getRefractIndex() {
        return this.getTyped(PropertyKey.REFRACTI, Float.class).floatValue();
    }

    public <V3, M4, C, N, Q> C getDiffuseColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_DIFFUSE.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_DIFFUSE);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public <V3, M4, C, N, Q> C getAmbientColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_AMBIENT.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_AMBIENT);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public <V3, M4, C, N, Q> C getSpecularColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_SPECULAR.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_SPECULAR);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public <V3, M4, C, N, Q> C getEmissiveColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_EMISSIVE.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_EMISSIVE);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public <V3, M4, C, N, Q> C getTransparentColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_TRANSPARENT.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_TRANSPARENT);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public <V3, M4, C, N, Q> C getReflectiveColor(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        Property property = this.getProperty(PropertyKey.COLOR_REFLECTIVE.m_key);
        if (null == property || null == property.getData()) {
            Object object = this.m_defaults.get((Object)PropertyKey.COLOR_REFLECTIVE);
            if (object == null) {
                return (C)Jassimp.wrapColor4(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return (C)object;
        }
        return (C)property.getData();
    }

    public String getGlobalBackgroundImage() {
        return this.getTyped(PropertyKey.GLOBAL_BACKGROUND_IMAGE, String.class);
    }

    public int getNumTextures(AiTextureType aiTextureType) {
        return this.m_numTextures.get((Object)aiTextureType);
    }

    public String getTextureFile(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        return this.getTyped(PropertyKey.TEX_FILE, aiTextureType, n, String.class);
    }

    public int getTextureUVIndex(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        return this.getTyped(PropertyKey.TEX_UV_INDEX, aiTextureType, n, Integer.class);
    }

    public float getBlendFactor(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        return this.getTyped(PropertyKey.TEX_BLEND, aiTextureType, n, Float.class).floatValue();
    }

    public AiTextureOp getTextureOp(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        Property property = this.getProperty(PropertyKey.TEX_OP.m_key);
        if (null == property || null == property.getData()) {
            return (AiTextureOp)((Object)this.m_defaults.get((Object)PropertyKey.TEX_OP));
        }
        return AiTextureOp.fromRawValue((Integer)property.getData());
    }

    public AiTextureMapMode getTextureMapModeU(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        Property property = this.getProperty(PropertyKey.TEX_MAP_MODE_U.m_key);
        if (null == property || null == property.getData()) {
            return (AiTextureMapMode)((Object)this.m_defaults.get((Object)PropertyKey.TEX_MAP_MODE_U));
        }
        return AiTextureMapMode.fromRawValue((Integer)property.getData());
    }

    public AiTextureMapMode getTextureMapModeV(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        Property property = this.getProperty(PropertyKey.TEX_MAP_MODE_V.m_key);
        if (null == property || null == property.getData()) {
            return (AiTextureMapMode)((Object)this.m_defaults.get((Object)PropertyKey.TEX_MAP_MODE_V));
        }
        return AiTextureMapMode.fromRawValue((Integer)property.getData());
    }

    public AiTextureMapMode getTextureMapModeW(AiTextureType aiTextureType, int n) {
        this.checkTexRange(aiTextureType, n);
        Property property = this.getProperty(PropertyKey.TEX_MAP_MODE_W.m_key);
        if (null == property || null == property.getData()) {
            return (AiTextureMapMode)((Object)this.m_defaults.get((Object)PropertyKey.TEX_MAP_MODE_W));
        }
        return AiTextureMapMode.fromRawValue((Integer)property.getData());
    }

    public AiTextureInfo getTextureInfo(AiTextureType aiTextureType, int n) {
        return new AiTextureInfo(aiTextureType, n, this.getTextureFile(aiTextureType, n), this.getTextureUVIndex(aiTextureType, n), this.getBlendFactor(aiTextureType, n), this.getTextureOp(aiTextureType, n), this.getTextureMapModeW(aiTextureType, n), this.getTextureMapModeW(aiTextureType, n), this.getTextureMapModeW(aiTextureType, n));
    }

    public Property getProperty(String string) {
        for (Property property : this.m_properties) {
            if (!property.getKey().equals(string)) continue;
            return property;
        }
        return null;
    }

    public Property getProperty(String string, int n, int n2) {
        for (Property property : this.m_properties) {
            if (!property.getKey().equals(string) || property.m_semantic != n || property.m_index != n2) continue;
            return property;
        }
        return null;
    }

    public List<Property> getProperties() {
        return this.m_properties;
    }

    private <T> T getTyped(PropertyKey propertyKey, Class<T> clazz) {
        Property property = this.getProperty(propertyKey.m_key);
        if (null == property || null == property.getData()) {
            return clazz.cast(this.m_defaults.get((Object)propertyKey));
        }
        return clazz.cast(property.getData());
    }

    private <T> T getTyped(PropertyKey propertyKey, AiTextureType aiTextureType, int n, Class<T> clazz) {
        Property property = this.getProperty(propertyKey.m_key, AiTextureType.toRawValue(aiTextureType), n);
        if (null == property || null == property.getData()) {
            return clazz.cast(this.m_defaults.get((Object)propertyKey));
        }
        return clazz.cast(property.getData());
    }

    private void checkTexRange(AiTextureType aiTextureType, int n) {
        if (n < 0 || n > this.m_numTextures.get((Object)aiTextureType)) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Size: " + this.m_numTextures.get((Object)aiTextureType));
        }
    }

    private void setTextureNumber(int n, int n2) {
        this.m_numTextures.put(AiTextureType.fromRawValue(n), n2);
    }

    public static enum PropertyKey {
        NAME("?mat.name", String.class),
        TWO_SIDED("$mat.twosided", Integer.class),
        SHADING_MODE("$mat.shadingm", AiShadingMode.class),
        WIREFRAME("$mat.wireframe", Integer.class),
        BLEND_MODE("$mat.blend", AiBlendMode.class),
        OPACITY("$mat.opacity", Float.class),
        BUMP_SCALING("$mat.bumpscaling", Float.class),
        SHININESS("$mat.shininess", Float.class),
        REFLECTIVITY("$mat.reflectivity", Float.class),
        SHININESS_STRENGTH("$mat.shinpercent", Float.class),
        REFRACTI("$mat.refracti", Float.class),
        COLOR_DIFFUSE("$clr.diffuse", Object.class),
        COLOR_AMBIENT("$clr.ambient", Object.class),
        COLOR_SPECULAR("$clr.specular", Object.class),
        COLOR_EMISSIVE("$clr.emissive", Object.class),
        COLOR_TRANSPARENT("$clr.transparent", Object.class),
        COLOR_REFLECTIVE("$clr.reflective", Object.class),
        GLOBAL_BACKGROUND_IMAGE("?bg.global", String.class),
        TEX_FILE("$tex.file", String.class),
        TEX_UV_INDEX("$tex.uvwsrc", Integer.class),
        TEX_BLEND("$tex.blend", Float.class),
        TEX_OP("$tex.op", AiTextureOp.class),
        TEX_MAP_MODE_U("$tex.mapmodeu", AiTextureMapMode.class),
        TEX_MAP_MODE_V("$tex.mapmodev", AiTextureMapMode.class),
        TEX_MAP_MODE_W("$tex.mapmodew", AiTextureMapMode.class);

        private final String m_key;
        private final Class<?> m_type;

        private PropertyKey(String string2, Class<?> clazz) {
            this.m_key = string2;
            this.m_type = clazz;
        }
    }

    public static final class Property {
        private final String m_key;
        private final int m_semantic;
        private final int m_index;
        private final PropertyType m_type;
        private final Object m_data;

        Property(String string, int n, int n2, int n3, Object object) {
            this.m_key = string;
            this.m_semantic = n;
            this.m_index = n2;
            this.m_type = PropertyType.fromRawValue(n3);
            this.m_data = object;
        }

        Property(String string, int n, int n2, int n3, int n4) {
            this.m_key = string;
            this.m_semantic = n;
            this.m_index = n2;
            this.m_type = PropertyType.fromRawValue(n3);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(n4);
            byteBuffer.order(ByteOrder.nativeOrder());
            this.m_data = byteBuffer;
        }

        public String getKey() {
            return this.m_key;
        }

        public int getSemantic() {
            return this.m_semantic;
        }

        public int getIndex() {
            return this.m_index;
        }

        public PropertyType getType() {
            return this.m_type;
        }

        public Object getData() {
            return this.m_data;
        }
    }

    public static enum PropertyType {
        FLOAT(1),
        STRING(3),
        INTEGER(4),
        BUFFER(5);

        private final int m_rawValue;

        static PropertyType fromRawValue(int n) {
            for (PropertyType propertyType : PropertyType.values()) {
                if (propertyType.m_rawValue != n) continue;
                return propertyType;
            }
            throw new IllegalArgumentException("unexptected raw value: " + n);
        }

        private PropertyType(int n2) {
            this.m_rawValue = n2;
        }
    }
}

