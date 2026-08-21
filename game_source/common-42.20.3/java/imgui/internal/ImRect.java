/*
 * Decompiled with CFR 0.152.
 */
package imgui.internal;

import imgui.ImVec2;
import java.util.Objects;

public final class ImRect
implements Cloneable {
    public final ImVec2 min = new ImVec2();
    public final ImVec2 max = new ImVec2();

    public ImRect() {
    }

    public ImRect(ImVec2 min, ImVec2 max) {
        this.min.x = min.x;
        this.min.y = min.y;
        this.max.x = max.x;
        this.max.y = max.y;
    }

    public ImRect(float x1, float y1, float x2, float y2) {
        this.min.x = x1;
        this.min.y = y1;
        this.max.x = x2;
        this.max.y = y2;
    }

    public ImRect(ImRect imRect) {
        this(imRect.min, imRect.max);
    }

    public void set(ImRect value) {
        this.min.x = value.min.x;
        this.min.y = value.min.y;
        this.max.x = value.max.x;
        this.max.y = value.max.y;
    }

    public String toString() {
        return "ImRect{min=" + this.min + ", max=" + this.max + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImRect imRect = (ImRect)o;
        return Objects.equals(this.min, imRect.min) && Objects.equals(this.max, imRect.max);
    }

    public int hashCode() {
        return Objects.hash(this.min, this.max);
    }

    public ImRect clone() {
        return new ImRect(this);
    }
}

