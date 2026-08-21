/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.NativeType;

public class EXTMeshShader {
    public static final int GL_MESH_SHADER_EXT = 38233;
    public static final int GL_TASK_SHADER_EXT = 38234;
    public static final int GL_MAX_TASK_UNIFORM_BLOCKS_EXT = 36456;
    public static final int GL_MAX_TASK_TEXTURE_IMAGE_UNITS_EXT = 36457;
    public static final int GL_MAX_TASK_IMAGE_UNIFORMS_EXT = 36458;
    public static final int GL_MAX_TASK_UNIFORM_COMPONENTS_EXT = 36459;
    public static final int GL_MAX_TASK_ATOMIC_COUNTER_BUFFERS_EXT = 36460;
    public static final int GL_MAX_TASK_ATOMIC_COUNTERS_EXT = 36461;
    public static final int GL_MAX_TASK_SHADER_STORAGE_BLOCKS_EXT = 36462;
    public static final int GL_MAX_COMBINED_TASK_UNIFORM_COMPONENTS_EXT = 36463;
    public static final int GL_MAX_MESH_UNIFORM_BLOCKS_EXT = 36448;
    public static final int GL_MAX_MESH_TEXTURE_IMAGE_UNITS_EXT = 36449;
    public static final int GL_MAX_MESH_IMAGE_UNIFORMS_EXT = 36450;
    public static final int GL_MAX_MESH_UNIFORM_COMPONENTS_EXT = 36451;
    public static final int GL_MAX_MESH_ATOMIC_COUNTER_BUFFERS_EXT = 36452;
    public static final int GL_MAX_MESH_ATOMIC_COUNTERS_EXT = 36453;
    public static final int GL_MAX_MESH_SHADER_STORAGE_BLOCKS_EXT = 36454;
    public static final int GL_MAX_COMBINED_MESH_UNIFORM_COMPONENTS_EXT = 36455;
    public static final int GL_MAX_TASK_WORK_GROUP_TOTAL_COUNT_EXT = 38720;
    public static final int GL_MAX_MESH_WORK_GROUP_TOTAL_COUNT_EXT = 38721;
    public static final int GL_MAX_MESH_WORK_GROUP_INVOCATIONS_EXT = 38743;
    public static final int GL_MAX_TASK_WORK_GROUP_INVOCATIONS_EXT = 38745;
    public static final int GL_MAX_TASK_PAYLOAD_SIZE_EXT = 38722;
    public static final int GL_MAX_TASK_SHARED_MEMORY_SIZE_EXT = 38723;
    public static final int GL_MAX_MESH_SHARED_MEMORY_SIZE_EXT = 38724;
    public static final int GL_MAX_TASK_PAYLOAD_AND_SHARED_MEMORY_SIZE_EXT = 38725;
    public static final int GL_MAX_MESH_PAYLOAD_AND_SHARED_MEMORY_SIZE_EXT = 38726;
    public static final int GL_MAX_MESH_OUTPUT_MEMORY_SIZE_EXT = 38727;
    public static final int GL_MAX_MESH_PAYLOAD_AND_OUTPUT_MEMORY_SIZE_EXT = 38728;
    public static final int GL_MAX_MESH_OUTPUT_VERTICES_EXT = 38200;
    public static final int GL_MAX_MESH_OUTPUT_PRIMITIVES_EXT = 38742;
    public static final int GL_MAX_MESH_OUTPUT_COMPONENTS_EXT = 38729;
    public static final int GL_MAX_MESH_OUTPUT_LAYERS_EXT = 38730;
    public static final int GL_MAX_MESH_MULTIVIEW_VIEW_COUNT_EXT = 38231;
    public static final int GL_MESH_OUTPUT_PER_VERTEX_GRANULARITY_EXT = 37599;
    public static final int GL_MESH_OUTPUT_PER_PRIMITIVE_GRANULARITY_EXT = 38211;
    public static final int GL_MAX_PREFERRED_TASK_WORK_GROUP_INVOCATIONS_EXT = 38731;
    public static final int GL_MAX_PREFERRED_MESH_WORK_GROUP_INVOCATIONS_EXT = 38732;
    public static final int GL_MESH_PREFERS_LOCAL_INVOCATION_VERTEX_OUTPUT_EXT = 38733;
    public static final int GL_MESH_PREFERS_LOCAL_INVOCATION_PRIMITIVE_OUTPUT_EXT = 38734;
    public static final int GL_MESH_PREFERS_COMPACT_VERTEX_OUTPUT_EXT = 38735;
    public static final int GL_MESH_PREFERS_COMPACT_PRIMITIVE_OUTPUT_EXT = 38736;
    public static final int GL_MAX_TASK_WORK_GROUP_COUNT_EXT = 38737;
    public static final int GL_MAX_MESH_WORK_GROUP_COUNT_EXT = 38738;
    public static final int GL_MAX_TASK_WORK_GROUP_SIZE_EXT = 38746;
    public static final int GL_MAX_MESH_WORK_GROUP_SIZE_EXT = 38744;
    public static final int GL_TASK_WORK_GROUP_SIZE_EXT = 38207;
    public static final int GL_MESH_WORK_GROUP_SIZE_EXT = 38206;
    public static final int GL_MESH_VERTICES_OUT_EXT = 38265;
    public static final int GL_MESH_PRIMITIVES_OUT_EXT = 38266;
    public static final int GL_MESH_OUTPUT_TYPE_EXT = 38267;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_MESH_SHADER_EXT = 38300;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_TASK_SHADER_EXT = 38301;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_MESH_SHADER_EXT = 38302;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TASK_SHADER_EXT = 38303;
    public static final int GL_REFERENCED_BY_MESH_SHADER_EXT = 38304;
    public static final int GL_REFERENCED_BY_TASK_SHADER_EXT = 38305;
    public static final int GL_MESH_SUBROUTINE_EXT = 38268;
    public static final int GL_TASK_SUBROUTINE_EXT = 38269;
    public static final int GL_MESH_SUBROUTINE_UNIFORM_EXT = 38270;
    public static final int GL_TASK_SUBROUTINE_UNIFORM_EXT = 38271;
    public static final int GL_TASK_SHADER_INVOCATIONS_EXT = 38739;
    public static final int GL_MESH_SHADER_INVOCATIONS_EXT = 38740;
    public static final int GL_MESH_PRIMITIVES_GENERATED_EXT = 38741;
    public static final int GL_MESH_SHADER_BIT_EXT = 64;
    public static final int GL_TASK_SHADER_BIT_EXT = 128;

    protected EXTMeshShader() {
        throw new UnsupportedOperationException();
    }

    public static native void glDrawMeshTasksEXT(@NativeType(value="GLuint") int var0, @NativeType(value="GLuint") int var1, @NativeType(value="GLuint") int var2);

    public static native void glDrawMeshTasksIndirectEXT(@NativeType(value="GLintptr") long var0);

    public static native void glMultiDrawMeshTasksIndirectEXT(@NativeType(value="GLintptr") long var0, @NativeType(value="GLsizei") int var2, @NativeType(value="GLsizei") int var3);

    public static native void glMultiDrawMeshTasksIndirectCountEXT(@NativeType(value="GLintptr") long var0, @NativeType(value="GLintptr") long var2, @NativeType(value="GLsizei") int var4, @NativeType(value="GLsizei") int var5);

    static {
        GL.initialize();
    }
}

