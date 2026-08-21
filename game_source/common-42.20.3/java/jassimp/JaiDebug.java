/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiMaterial;
import jassimp.AiMesh;
import jassimp.AiNodeAnim;
import jassimp.Jassimp;
import java.nio.ByteBuffer;

public final class JaiDebug {
    private JaiDebug() {
    }

    public static void dumpPositions(AiMesh aiMesh) {
        if (!aiMesh.hasPositions()) {
            System.out.println("mesh has no vertex positions");
            return;
        }
        for (int i = 0; i < aiMesh.getNumVertices(); ++i) {
            System.out.println("[" + aiMesh.getPositionX(i) + ", " + aiMesh.getPositionY(i) + ", " + aiMesh.getPositionZ(i) + "]");
        }
    }

    public static void dumpFaces(AiMesh aiMesh) {
        if (!aiMesh.hasFaces()) {
            System.out.println("mesh has no faces");
            return;
        }
        for (int i = 0; i < aiMesh.getNumFaces(); ++i) {
            int n = aiMesh.getFaceNumIndices(i);
            System.out.print(n + ": ");
            for (int j = 0; j < n; ++j) {
                int n2 = aiMesh.getFaceVertex(i, j);
                System.out.print("[" + aiMesh.getPositionX(n2) + ", " + aiMesh.getPositionY(n2) + ", " + aiMesh.getPositionZ(n2) + "] ");
            }
            System.out.println();
        }
    }

    public static void dumpColorset(AiMesh aiMesh, int n) {
        if (!aiMesh.hasColors(n)) {
            System.out.println("mesh has no vertex color set " + n);
            return;
        }
        for (int i = 0; i < aiMesh.getNumVertices(); ++i) {
            System.out.println("[" + aiMesh.getColorR(i, n) + ", " + aiMesh.getColorG(i, n) + ", " + aiMesh.getColorB(i, n) + ", " + aiMesh.getColorA(i, n) + "]");
        }
    }

    public static void dumpTexCoords(AiMesh aiMesh, int n) {
        if (!aiMesh.hasTexCoords(n)) {
            System.out.println("mesh has no texture coordinate set " + n);
            return;
        }
        for (int i = 0; i < aiMesh.getNumVertices(); ++i) {
            int n2 = aiMesh.getNumUVComponents(n);
            System.out.print("[" + aiMesh.getTexCoordU(i, n));
            if (n2 > 1) {
                System.out.print(", " + aiMesh.getTexCoordV(i, n));
            }
            if (n2 > 2) {
                System.out.print(", " + aiMesh.getTexCoordW(i, n));
            }
            System.out.println("]");
        }
    }

    public static void dumpMaterialProperty(AiMaterial.Property property) {
        System.out.print(property.getKey() + " " + property.getSemantic() + " " + property.getIndex() + ": ");
        Object object = property.getData();
        if (object instanceof ByteBuffer) {
            ByteBuffer byteBuffer = (ByteBuffer)object;
            for (int i = 0; i < byteBuffer.capacity(); ++i) {
                System.out.print(Integer.toHexString(byteBuffer.get(i) & 0xFF) + " ");
            }
            System.out.println();
        } else {
            System.out.println(object.toString());
        }
    }

    public static void dumpMaterial(AiMaterial aiMaterial) {
        for (AiMaterial.Property property : aiMaterial.getProperties()) {
            JaiDebug.dumpMaterialProperty(property);
        }
    }

    public static void dumpNodeAnim(AiNodeAnim aiNodeAnim) {
        for (int i = 0; i < aiNodeAnim.getNumPosKeys(); ++i) {
            System.out.println(i + ": " + aiNodeAnim.getPosKeyTime(i) + " ticks, " + aiNodeAnim.getPosKeyVector(i, Jassimp.BUILTIN));
        }
    }
}

