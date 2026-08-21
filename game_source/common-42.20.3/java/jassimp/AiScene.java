/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiAnimation;
import jassimp.AiCamera;
import jassimp.AiLight;
import jassimp.AiMaterial;
import jassimp.AiMesh;
import jassimp.AiWrapperProvider;
import java.util.ArrayList;
import java.util.List;

public final class AiScene {
    private final List<AiMesh> m_meshes = new ArrayList<AiMesh>();
    private final List<AiMaterial> m_materials = new ArrayList<AiMaterial>();
    private final List<AiAnimation> m_animations = new ArrayList<AiAnimation>();
    private final List<AiLight> m_lights = new ArrayList<AiLight>();
    private final List<AiCamera> m_cameras = new ArrayList<AiCamera>();
    private Object m_sceneRoot;

    AiScene() {
    }

    public int getNumMeshes() {
        return this.m_meshes.size();
    }

    public List<AiMesh> getMeshes() {
        return this.m_meshes;
    }

    public int getNumMaterials() {
        return this.m_materials.size();
    }

    public List<AiMaterial> getMaterials() {
        return this.m_materials;
    }

    public int getNumAnimations() {
        return this.m_animations.size();
    }

    public List<AiAnimation> getAnimations() {
        return this.m_animations;
    }

    public int getNumLights() {
        return this.m_lights.size();
    }

    public List<AiLight> getLights() {
        return this.m_lights;
    }

    public int getNumCameras() {
        return this.m_cameras.size();
    }

    public List<AiCamera> getCameras() {
        return this.m_cameras;
    }

    public <V3, M4, C, N, Q> N getSceneRoot(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (N)this.m_sceneRoot;
    }

    public String toString() {
        return "AiScene (" + this.m_meshes.size() + " mesh/es)";
    }
}

