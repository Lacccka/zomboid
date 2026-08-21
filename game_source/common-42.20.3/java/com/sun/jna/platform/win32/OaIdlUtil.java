/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import java.lang.reflect.Array;

public abstract class OaIdlUtil {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object toPrimitiveArray(OaIdl.SAFEARRAY sa, boolean destruct) {
        Pointer dataPointer = sa.accessData();
        try {
            Object[] sourceArray;
            int i;
            int dimensions = sa.getDimensionCount();
            int[] elements = new int[dimensions];
            int[] cumElements = new int[dimensions];
            int varType = sa.getVarType().intValue();
            for (i = 0; i < dimensions; ++i) {
                elements[i] = sa.getUBound(i) - sa.getLBound(i) + 1;
            }
            for (i = dimensions - 1; i >= 0; --i) {
                cumElements[i] = i == dimensions - 1 ? 1 : cumElements[i + 1] * elements[i + 1];
            }
            if (dimensions == 0) {
                throw new IllegalArgumentException("Supplied Array has no dimensions.");
            }
            int elementCount = cumElements[0] * elements[0];
            switch (varType) {
                case 16: 
                case 17: {
                    sourceArray = dataPointer.getByteArray(0L, elementCount);
                    break;
                }
                case 2: 
                case 11: 
                case 18: {
                    sourceArray = dataPointer.getShortArray(0L, elementCount);
                    break;
                }
                case 3: 
                case 10: 
                case 19: 
                case 22: 
                case 23: {
                    sourceArray = dataPointer.getIntArray(0L, elementCount);
                    break;
                }
                case 4: {
                    sourceArray = dataPointer.getFloatArray(0L, elementCount);
                    break;
                }
                case 5: 
                case 7: {
                    sourceArray = dataPointer.getDoubleArray(0L, elementCount);
                    break;
                }
                case 8: {
                    sourceArray = dataPointer.getPointerArray(0L, elementCount);
                    break;
                }
                case 12: {
                    Variant.VARIANT variant = new Variant.VARIANT(dataPointer);
                    sourceArray = variant.toArray(elementCount);
                    break;
                }
                default: {
                    throw new IllegalStateException("Type not supported: " + varType);
                }
            }
            Object targetArray = Array.newInstance(Object.class, elements);
            OaIdlUtil.toPrimitiveArray(sourceArray, targetArray, elements, cumElements, varType, new int[0]);
            Object object = targetArray;
            return object;
        }
        finally {
            sa.unaccessData();
            if (destruct) {
                sa.destroy();
            }
        }
    }

    private static void toPrimitiveArray(Object dataArray, Object targetArray, int[] elements, int[] cumElements, int varType, int[] currentIdx) {
        int dimIdx = currentIdx.length;
        int[] subIdx = new int[currentIdx.length + 1];
        System.arraycopy(currentIdx, 0, subIdx, 0, dimIdx);
        for (int i = 0; i < elements[dimIdx]; ++i) {
            subIdx[dimIdx] = i;
            if (dimIdx == elements.length - 1) {
                int offset = 0;
                for (int j = 0; j < dimIdx; ++j) {
                    offset += cumElements[j] * currentIdx[j];
                }
                offset += subIdx[dimIdx];
                int targetPos = subIdx[dimIdx];
                block0 : switch (varType) {
                    case 11: {
                        Array.set(targetArray, targetPos, Array.getShort(dataArray, offset) != 0);
                        break;
                    }
                    case 16: 
                    case 17: {
                        Array.set(targetArray, targetPos, Array.getByte(dataArray, offset));
                        break;
                    }
                    case 2: 
                    case 18: {
                        Array.set(targetArray, targetPos, Array.getShort(dataArray, offset));
                        break;
                    }
                    case 3: 
                    case 19: 
                    case 22: 
                    case 23: {
                        Array.set(targetArray, targetPos, Array.getInt(dataArray, offset));
                        break;
                    }
                    case 10: {
                        Array.set(targetArray, targetPos, new WinDef.SCODE((long)Array.getInt(dataArray, offset)));
                        break;
                    }
                    case 4: {
                        Array.set(targetArray, targetPos, Float.valueOf(Array.getFloat(dataArray, offset)));
                        break;
                    }
                    case 5: {
                        Array.set(targetArray, targetPos, Array.getDouble(dataArray, offset));
                        break;
                    }
                    case 7: {
                        Array.set(targetArray, targetPos, new OaIdl.DATE(Array.getDouble(dataArray, offset)).getAsJavaDate());
                        break;
                    }
                    case 8: {
                        Array.set(targetArray, targetPos, new WTypes.BSTR((Pointer)Array.get(dataArray, offset)).getValue());
                        break;
                    }
                    case 12: {
                        Variant.VARIANT holder = (Variant.VARIANT)Array.get(dataArray, offset);
                        switch (holder.getVarType().intValue()) {
                            case 0: 
                            case 1: {
                                Array.set(targetArray, targetPos, null);
                                break block0;
                            }
                            case 11: {
                                Array.set(targetArray, targetPos, holder.booleanValue());
                                break block0;
                            }
                            case 16: 
                            case 17: {
                                Array.set(targetArray, targetPos, holder.byteValue());
                                break block0;
                            }
                            case 2: 
                            case 18: {
                                Array.set(targetArray, targetPos, holder.shortValue());
                                break block0;
                            }
                            case 3: 
                            case 19: 
                            case 22: 
                            case 23: {
                                Array.set(targetArray, targetPos, holder.intValue());
                                break block0;
                            }
                            case 10: {
                                Array.set(targetArray, targetPos, new WinDef.SCODE((long)holder.intValue()));
                                break block0;
                            }
                            case 4: {
                                Array.set(targetArray, targetPos, Float.valueOf(holder.floatValue()));
                                break block0;
                            }
                            case 5: {
                                Array.set(targetArray, targetPos, holder.doubleValue());
                                break block0;
                            }
                            case 7: {
                                Array.set(targetArray, targetPos, holder.dateValue());
                                break block0;
                            }
                            case 8: {
                                Array.set(targetArray, targetPos, holder.stringValue());
                                break block0;
                            }
                        }
                        throw new IllegalStateException("Type not supported: " + holder.getVarType().intValue());
                    }
                    default: {
                        throw new IllegalStateException("Type not supported: " + varType);
                    }
                }
                continue;
            }
            OaIdlUtil.toPrimitiveArray(dataArray, Array.get(targetArray, i), elements, cumElements, varType, subIdx);
        }
    }
}

