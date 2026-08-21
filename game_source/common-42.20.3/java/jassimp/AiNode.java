/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiMetadataEntry;
import jassimp.AiWrapperProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AiNode {
    private final AiNode m_parent;
    private final int[] m_meshReferences;
    private final List<AiNode> m_children = new ArrayList<AiNode>();
    private final Map<String, AiMetadataEntry> m_metaData = new HashMap<String, AiMetadataEntry>();
    private final Object m_transformationMatrix;
    private final String m_name;

    AiNode(AiNode aiNode, Object object, int[] nArray, String string) {
        this.m_parent = aiNode;
        this.m_transformationMatrix = object;
        this.m_meshReferences = nArray;
        this.m_name = string;
        if (null != this.m_parent) {
            this.m_parent.addChild(this);
        }
    }

    public String getName() {
        return this.m_name;
    }

    public int getNumChildren() {
        return this.getChildren().size();
    }

    public <V3, M4, C, N, Q> M4 getTransform(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (M4)this.m_transformationMatrix;
    }

    public List<AiNode> getChildren() {
        return this.m_children;
    }

    public AiNode getParent() {
        return this.m_parent;
    }

    public AiNode findNode(String string) {
        if (this.m_name.equals(string)) {
            return this;
        }
        for (AiNode aiNode : this.m_children) {
            if (null == aiNode.findNode(string)) continue;
            return aiNode;
        }
        return null;
    }

    public int getNumMeshes() {
        return this.m_meshReferences.length;
    }

    public int[] getMeshes() {
        return this.m_meshReferences;
    }

    public Map<String, AiMetadataEntry> getMetadata() {
        return this.m_metaData;
    }

    void addChild(AiNode aiNode) {
        this.m_children.add(aiNode);
    }
}

