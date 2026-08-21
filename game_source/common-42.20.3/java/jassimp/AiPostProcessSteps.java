/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.util.Set;

public enum AiPostProcessSteps {
    CALC_TANGENT_SPACE(1L),
    JOIN_IDENTICAL_VERTICES(2L),
    MAKE_LEFT_HANDED(4L),
    TRIANGULATE(8L),
    REMOVE_COMPONENT(16L),
    GEN_NORMALS(32L),
    GEN_SMOOTH_NORMALS(64L),
    SPLIT_LARGE_MESHES(128L),
    PRE_TRANSFORM_VERTICES(256L),
    LIMIT_BONE_WEIGHTS(512L),
    VALIDATE_DATA_STRUCTURE(1024L),
    IMPROVE_CACHE_LOCALITY(2048L),
    REMOVE_REDUNDANT_MATERIALS(4096L),
    FIX_INFACING_NORMALS(8192L),
    SORT_BY_PTYPE(32768L),
    FIND_DEGENERATES(65536L),
    FIND_INVALID_DATA(131072L),
    GEN_UV_COORDS(262144L),
    TRANSFORM_UV_COORDS(524288L),
    FIND_INSTANCES(0x100000L),
    OPTIMIZE_MESHES(0x200000L),
    OPTIMIZE_GRAPH(0x400000L),
    FLIP_UVS(0x800000L),
    FLIP_WINDING_ORDER(0x1000000L),
    SPLIT_BY_BONE_COUNT(0x2000000L),
    DEBONE(0x4000000L);

    private final long m_rawValue;

    static long toRawValue(Set<AiPostProcessSteps> set) {
        long l = 0L;
        for (AiPostProcessSteps aiPostProcessSteps : set) {
            l |= aiPostProcessSteps.m_rawValue;
        }
        return l;
    }

    private AiPostProcessSteps(long l) {
        this.m_rawValue = l;
    }
}

