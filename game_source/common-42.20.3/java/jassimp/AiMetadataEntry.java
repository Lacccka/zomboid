/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiVector;

public class AiMetadataEntry {
    private AiMetadataType mType;
    private Object mData;

    public AiMetadataType getMetaDataType() {
        return this.mType;
    }

    public Object getData() {
        return this.mData;
    }

    public static boolean getAiBoolAsBoolean(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_BOOL);
        return (Boolean)aiMetadataEntry.mData;
    }

    public static int getAiInt32AsInteger(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_INT32);
        return (Integer)aiMetadataEntry.mData;
    }

    public static long getAiUint64AsLong(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_UINT64);
        return (Long)aiMetadataEntry.mData;
    }

    public static float getAiFloatAsFloat(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_FLOAT);
        return ((Float)aiMetadataEntry.mData).floatValue();
    }

    public static double getAiDoubleAsDouble(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_DOUBLE);
        return (Double)aiMetadataEntry.mData;
    }

    public static String getAiStringAsString(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_AISTRING);
        return (String)aiMetadataEntry.mData;
    }

    public static AiVector getAiAiVector3DAsAiVector(AiMetadataEntry aiMetadataEntry) {
        AiMetadataEntry.checkTypeBeforeCasting(aiMetadataEntry, AiMetadataType.AI_AIVECTOR3D);
        return (AiVector)aiMetadataEntry.mData;
    }

    private static void checkTypeBeforeCasting(AiMetadataEntry aiMetadataEntry, AiMetadataType aiMetadataType) {
        if (aiMetadataEntry.mType != aiMetadataType) {
            throw new RuntimeException("Cannot cast entry of type " + aiMetadataEntry.mType.name() + " to " + aiMetadataType.name());
        }
    }

    public static enum AiMetadataType {
        AI_BOOL,
        AI_INT32,
        AI_UINT64,
        AI_FLOAT,
        AI_DOUBLE,
        AI_AISTRING,
        AI_AIVECTOR3D;

    }
}

