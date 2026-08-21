/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiBone;
import jassimp.AiPrimitiveType;
import jassimp.AiWrapperProvider;
import jassimp.Jassimp;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class AiMesh {
    private final int SIZEOF_FLOAT = Jassimp.NATIVE_FLOAT_SIZE;
    private final int SIZEOF_INT = Jassimp.NATIVE_INT_SIZE;
    private final int SIZEOF_V3D = Jassimp.NATIVE_AIVEKTOR3D_SIZE;
    private final Set<AiPrimitiveType> m_primitiveTypes = EnumSet.noneOf(AiPrimitiveType.class);
    private int m_numVertices = 0;
    private int m_numFaces = 0;
    private int m_materialIndex = -1;
    private String m_name = "";
    private ByteBuffer m_vertices = null;
    private ByteBuffer m_faces = null;
    private ByteBuffer m_faceOffsets = null;
    private ByteBuffer m_normals = null;
    private ByteBuffer m_tangents = null;
    private ByteBuffer m_bitangents = null;
    private ByteBuffer[] m_colorsets = new ByteBuffer[8];
    private int[] m_numUVComponents = new int[8];
    private ByteBuffer[] m_texcoords = new ByteBuffer[8];
    private final List<AiBone> m_bones = new ArrayList<AiBone>();
    private static final int NORMALS = 0;
    private static final int TANGENTS = 1;
    private static final int BITANGENTS = 2;
    private static final int COLORSET = 3;
    private static final int TEXCOORDS_1D = 4;
    private static final int TEXCOORDS_2D = 5;
    private static final int TEXCOORDS_3D = 6;

    private AiMesh() {
    }

    public Set<AiPrimitiveType> getPrimitiveTypes() {
        return this.m_primitiveTypes;
    }

    public boolean isPureTriangle() {
        return this.m_primitiveTypes.contains((Object)AiPrimitiveType.TRIANGLE) && this.m_primitiveTypes.size() == 1;
    }

    public boolean hasPositions() {
        return this.m_vertices != null;
    }

    public boolean hasFaces() {
        return this.m_faces != null;
    }

    public boolean hasNormals() {
        return this.m_normals != null;
    }

    public boolean hasTangentsAndBitangents() {
        return this.m_tangents != null && this.m_tangents != null;
    }

    public boolean hasColors(int n) {
        return this.m_colorsets[n] != null;
    }

    public boolean hasVertexColors() {
        for (ByteBuffer byteBuffer : this.m_colorsets) {
            if (byteBuffer == null) continue;
            return true;
        }
        return false;
    }

    public boolean hasTexCoords(int n) {
        return this.m_texcoords[n] != null;
    }

    public boolean hasTexCoords() {
        for (ByteBuffer byteBuffer : this.m_texcoords) {
            if (byteBuffer == null) continue;
            return true;
        }
        return false;
    }

    public boolean hasBones() {
        return !this.m_bones.isEmpty();
    }

    public List<AiBone> getBones() {
        return this.m_bones;
    }

    public int getNumVertices() {
        return this.m_numVertices;
    }

    public int getNumFaces() {
        return this.m_numFaces;
    }

    public int getFaceNumIndices(int n) {
        if (null == this.m_faceOffsets) {
            if (n >= this.m_numFaces || n < 0) {
                throw new IndexOutOfBoundsException("Index: " + n + ", Size: " + this.m_numFaces);
            }
            return 3;
        }
        if (n == this.m_numFaces - 1) {
            return this.m_faces.capacity() / 4 - this.m_faceOffsets.getInt(n * 4);
        }
        return this.m_faceOffsets.getInt((n + 1) * 4) - this.m_faceOffsets.getInt(n * 4);
    }

    public int getNumUVComponents(int n) {
        return this.m_numUVComponents[n];
    }

    public int getMaterialIndex() {
        return this.m_materialIndex;
    }

    public String getName() {
        return this.m_name;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Mesh(").append(this.m_numVertices).append(" vertices, ").append(this.m_numFaces).append(" faces");
        if (this.hasNormals()) {
            stringBuilder.append(", normals");
        }
        if (this.hasTangentsAndBitangents()) {
            stringBuilder.append(", (bi-)tangents");
        }
        if (this.hasVertexColors()) {
            stringBuilder.append(", colors");
        }
        if (this.hasTexCoords()) {
            stringBuilder.append(", texCoords");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public FloatBuffer getPositionBuffer() {
        if (this.m_vertices == null) {
            return null;
        }
        return this.m_vertices.asFloatBuffer();
    }

    public IntBuffer getFaceBuffer() {
        if (this.m_faces == null) {
            return null;
        }
        return this.m_faces.asIntBuffer();
    }

    public IntBuffer getFaceOffsets() {
        if (this.m_faceOffsets == null) {
            return null;
        }
        return this.m_faceOffsets.asIntBuffer();
    }

    public IntBuffer getIndexBuffer() {
        if (!this.isPureTriangle()) {
            throw new UnsupportedOperationException("mesh is not a pure triangle mesh");
        }
        return this.getFaceBuffer();
    }

    public FloatBuffer getNormalBuffer() {
        if (this.m_normals == null) {
            return null;
        }
        return this.m_normals.asFloatBuffer();
    }

    public FloatBuffer getTangentBuffer() {
        if (this.m_tangents == null) {
            return null;
        }
        return this.m_tangents.asFloatBuffer();
    }

    public FloatBuffer getBitangentBuffer() {
        if (this.m_bitangents == null) {
            return null;
        }
        return this.m_bitangents.asFloatBuffer();
    }

    public FloatBuffer getColorBuffer(int n) {
        if (this.m_colorsets[n] == null) {
            return null;
        }
        return this.m_colorsets[n].asFloatBuffer();
    }

    public FloatBuffer getTexCoordBuffer(int n) {
        if (this.m_texcoords[n] == null) {
            return null;
        }
        return this.m_texcoords[n].asFloatBuffer();
    }

    public float getPositionX(int n) {
        if (!this.hasPositions()) {
            throw new IllegalStateException("mesh has no positions");
        }
        this.checkVertexIndexBounds(n);
        return this.m_vertices.getFloat(n * 3 * this.SIZEOF_FLOAT);
    }

    public float getPositionY(int n) {
        if (!this.hasPositions()) {
            throw new IllegalStateException("mesh has no positions");
        }
        this.checkVertexIndexBounds(n);
        return this.m_vertices.getFloat((n * 3 + 1) * this.SIZEOF_FLOAT);
    }

    public float getPositionZ(int n) {
        if (!this.hasPositions()) {
            throw new IllegalStateException("mesh has no positions");
        }
        this.checkVertexIndexBounds(n);
        return this.m_vertices.getFloat((n * 3 + 2) * this.SIZEOF_FLOAT);
    }

    public int getFaceVertex(int n, int n2) {
        if (!this.hasFaces()) {
            throw new IllegalStateException("mesh has no faces");
        }
        if (n >= this.m_numFaces || n < 0) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Size: " + this.m_numFaces);
        }
        if (n2 >= this.getFaceNumIndices(n) || n2 < 0) {
            throw new IndexOutOfBoundsException("Index: " + n2 + ", Size: " + this.getFaceNumIndices(n));
        }
        int n3 = 0;
        n3 = this.m_faceOffsets == null ? 3 * n * this.SIZEOF_INT : this.m_faceOffsets.getInt(n * this.SIZEOF_INT) * this.SIZEOF_INT;
        return this.m_faces.getInt(n3 + n2 * this.SIZEOF_INT);
    }

    public float getNormalX(int n) {
        if (!this.hasNormals()) {
            throw new IllegalStateException("mesh has no normals");
        }
        this.checkVertexIndexBounds(n);
        return this.m_normals.getFloat(n * 3 * this.SIZEOF_FLOAT);
    }

    public float getNormalY(int n) {
        if (!this.hasNormals()) {
            throw new IllegalStateException("mesh has no normals");
        }
        this.checkVertexIndexBounds(n);
        return this.m_normals.getFloat((n * 3 + 1) * this.SIZEOF_FLOAT);
    }

    public float getNormalZ(int n) {
        if (!this.hasNormals()) {
            throw new IllegalStateException("mesh has no normals");
        }
        this.checkVertexIndexBounds(n);
        return this.m_normals.getFloat((n * 3 + 2) * this.SIZEOF_FLOAT);
    }

    public float getTangentX(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no tangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_tangents.getFloat(n * 3 * this.SIZEOF_FLOAT);
    }

    public float getTangentY(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no bitangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_tangents.getFloat((n * 3 + 1) * this.SIZEOF_FLOAT);
    }

    public float getTangentZ(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no tangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_tangents.getFloat((n * 3 + 2) * this.SIZEOF_FLOAT);
    }

    public float getBitangentX(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no bitangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_bitangents.getFloat(n * 3 * this.SIZEOF_FLOAT);
    }

    public float getBitangentY(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no bitangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_bitangents.getFloat((n * 3 + 1) * this.SIZEOF_FLOAT);
    }

    public float getBitangentZ(int n) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no bitangents");
        }
        this.checkVertexIndexBounds(n);
        return this.m_bitangents.getFloat((n * 3 + 2) * this.SIZEOF_FLOAT);
    }

    public float getColorR(int n, int n2) {
        if (!this.hasColors(n2)) {
            throw new IllegalStateException("mesh has no colorset " + n2);
        }
        this.checkVertexIndexBounds(n);
        return this.m_colorsets[n2].getFloat(n * 4 * this.SIZEOF_FLOAT);
    }

    public float getColorG(int n, int n2) {
        if (!this.hasColors(n2)) {
            throw new IllegalStateException("mesh has no colorset " + n2);
        }
        this.checkVertexIndexBounds(n);
        return this.m_colorsets[n2].getFloat((n * 4 + 1) * this.SIZEOF_FLOAT);
    }

    public float getColorB(int n, int n2) {
        if (!this.hasColors(n2)) {
            throw new IllegalStateException("mesh has no colorset " + n2);
        }
        this.checkVertexIndexBounds(n);
        return this.m_colorsets[n2].getFloat((n * 4 + 2) * this.SIZEOF_FLOAT);
    }

    public float getColorA(int n, int n2) {
        if (!this.hasColors(n2)) {
            throw new IllegalStateException("mesh has no colorset " + n2);
        }
        this.checkVertexIndexBounds(n);
        return this.m_colorsets[n2].getFloat((n * 4 + 3) * this.SIZEOF_FLOAT);
    }

    public float getTexCoordU(int n, int n2) {
        if (!this.hasTexCoords(n2)) {
            throw new IllegalStateException("mesh has no texture coordinate set " + n2);
        }
        this.checkVertexIndexBounds(n);
        return this.m_texcoords[n2].getFloat(n * this.m_numUVComponents[n2] * this.SIZEOF_FLOAT);
    }

    public float getTexCoordV(int n, int n2) {
        if (!this.hasTexCoords(n2)) {
            throw new IllegalStateException("mesh has no texture coordinate set " + n2);
        }
        this.checkVertexIndexBounds(n);
        if (this.getNumUVComponents(n2) < 2) {
            throw new IllegalArgumentException("coordinate set " + n2 + " does not contain 2D texture coordinates");
        }
        return this.m_texcoords[n2].getFloat((n * this.m_numUVComponents[n2] + 1) * this.SIZEOF_FLOAT);
    }

    public float getTexCoordW(int n, int n2) {
        if (!this.hasTexCoords(n2)) {
            throw new IllegalStateException("mesh has no texture coordinate set " + n2);
        }
        this.checkVertexIndexBounds(n);
        if (this.getNumUVComponents(n2) < 3) {
            throw new IllegalArgumentException("coordinate set " + n2 + " does not contain 3D texture coordinates");
        }
        return this.m_texcoords[n2].getFloat((n * this.m_numUVComponents[n2] + 1) * this.SIZEOF_FLOAT);
    }

    public <V3, M4, C, N, Q> V3 getWrappedPosition(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasPositions()) {
            throw new IllegalStateException("mesh has no positions");
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapVector3f(this.m_vertices, n * 3 * this.SIZEOF_FLOAT, 3);
    }

    public <V3, M4, C, N, Q> V3 getWrappedNormal(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasNormals()) {
            throw new IllegalStateException("mesh has no positions");
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapVector3f(this.m_normals, n * 3 * this.SIZEOF_FLOAT, 3);
    }

    public <V3, M4, C, N, Q> V3 getWrappedTangent(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no tangents");
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapVector3f(this.m_tangents, n * 3 * this.SIZEOF_FLOAT, 3);
    }

    public <V3, M4, C, N, Q> V3 getWrappedBitangent(int n, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasTangentsAndBitangents()) {
            throw new IllegalStateException("mesh has no bitangents");
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapVector3f(this.m_bitangents, n * 3 * this.SIZEOF_FLOAT, 3);
    }

    public <V3, M4, C, N, Q> C getWrappedColor(int n, int n2, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasColors(n2)) {
            throw new IllegalStateException("mesh has no colorset " + n2);
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapColor(this.m_colorsets[n2], n * 4 * this.SIZEOF_FLOAT);
    }

    public <V3, M4, C, N, Q> V3 getWrappedTexCoords(int n, int n2, AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        if (!this.hasTexCoords(n2)) {
            throw new IllegalStateException("mesh has no texture coordinate set " + n2);
        }
        this.checkVertexIndexBounds(n);
        return aiWrapperProvider.wrapVector3f(this.m_texcoords[n2], n * 3 * this.SIZEOF_FLOAT, this.getNumUVComponents(n2));
    }

    private void checkVertexIndexBounds(int n) {
        if (n >= this.m_numVertices || n < 0) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Size: " + this.m_numVertices);
        }
    }

    private void setPrimitiveTypes(int n) {
        AiPrimitiveType.fromRawValue(this.m_primitiveTypes, n);
    }

    private void allocateBuffers(int n, int n2, boolean bl, int n3) {
        if (bl && !this.isPureTriangle()) {
            throw new IllegalArgumentException("mesh is not purely triangular");
        }
        this.m_numVertices = n;
        this.m_numFaces = n2;
        if (this.m_numVertices > 0) {
            this.m_vertices = ByteBuffer.allocateDirect(n * 3 * this.SIZEOF_FLOAT);
            this.m_vertices.order(ByteOrder.nativeOrder());
        }
        if (this.m_numFaces > 0) {
            if (bl) {
                this.m_faces = ByteBuffer.allocateDirect(n2 * 3 * this.SIZEOF_INT);
                this.m_faces.order(ByteOrder.nativeOrder());
            } else {
                this.m_faces = ByteBuffer.allocateDirect(n3);
                this.m_faces.order(ByteOrder.nativeOrder());
                this.m_faceOffsets = ByteBuffer.allocateDirect(n2 * this.SIZEOF_INT);
                this.m_faceOffsets.order(ByteOrder.nativeOrder());
            }
        }
    }

    private void allocateDataChannel(int n, int n2) {
        switch (n) {
            case 0: {
                this.m_normals = ByteBuffer.allocateDirect(this.m_numVertices * 3 * this.SIZEOF_FLOAT);
                this.m_normals.order(ByteOrder.nativeOrder());
                break;
            }
            case 1: {
                this.m_tangents = ByteBuffer.allocateDirect(this.m_numVertices * 3 * this.SIZEOF_FLOAT);
                this.m_tangents.order(ByteOrder.nativeOrder());
                break;
            }
            case 2: {
                this.m_bitangents = ByteBuffer.allocateDirect(this.m_numVertices * 3 * this.SIZEOF_FLOAT);
                this.m_bitangents.order(ByteOrder.nativeOrder());
                break;
            }
            case 3: {
                this.m_colorsets[n2] = ByteBuffer.allocateDirect(this.m_numVertices * 4 * this.SIZEOF_FLOAT);
                this.m_colorsets[n2].order(ByteOrder.nativeOrder());
                break;
            }
            case 4: {
                this.m_numUVComponents[n2] = 1;
                this.m_texcoords[n2] = ByteBuffer.allocateDirect(this.m_numVertices * 1 * this.SIZEOF_FLOAT);
                this.m_texcoords[n2].order(ByteOrder.nativeOrder());
                break;
            }
            case 5: {
                this.m_numUVComponents[n2] = 2;
                this.m_texcoords[n2] = ByteBuffer.allocateDirect(this.m_numVertices * 2 * this.SIZEOF_FLOAT);
                this.m_texcoords[n2].order(ByteOrder.nativeOrder());
                break;
            }
            case 6: {
                this.m_numUVComponents[n2] = 3;
                this.m_texcoords[n2] = ByteBuffer.allocateDirect(this.m_numVertices * 3 * this.SIZEOF_FLOAT);
                this.m_texcoords[n2].order(ByteOrder.nativeOrder());
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported channel type");
            }
        }
    }
}

