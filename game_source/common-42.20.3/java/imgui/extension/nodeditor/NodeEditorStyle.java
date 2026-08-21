/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.nodeditor;

import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStruct;

public final class NodeEditorStyle
extends ImGuiStruct {
    public NodeEditorStyle(long ptr) {
        super(ptr);
    }

    public ImVec4 getNodePadding() {
        ImVec4 value = new ImVec4();
        this.getNodePadding(value);
        return value;
    }

    public native void getNodePadding(ImVec4 var1);

    public native void setNodePadding(float var1, float var2, float var3, float var4);

    public native float getNodeRounding();

    public native void setNodeRounding(float var1);

    public native float getNodeBorderWidth();

    public native void setNodeBorderWidth(float var1);

    public native float getHoveredNodeBorderWidth();

    public native void setHoveredNodeBorderWidth(float var1);

    public native float getSelectedNodeBorderWidth();

    public native void setSelectedNodeBorderWidth(float var1);

    public native float getPinRounding();

    public native void setPinRounding(float var1);

    public native float getPinBorderWidth();

    public native void setPinBorderWidth(float var1);

    public native float getLinkStrength();

    public native void setLinkStrength(float var1);

    public ImVec2 getSourceDirection() {
        ImVec2 value = new ImVec2();
        this.getSourceDirection(value);
        return value;
    }

    public native void getSourceDirection(ImVec2 var1);

    public native float getSourceDirectionX();

    public native float getSourceDirectionY();

    public native void setSourceDirection(float var1, float var2);

    public ImVec2 getTargetDirection() {
        ImVec2 value = new ImVec2();
        this.getTargetDirection(value);
        return value;
    }

    public native void getTargetDirection(ImVec2 var1);

    public native float getTargetDirectionX();

    public native float getTargetDirectionY();

    public native void setTargetDirection(float var1, float var2);

    public native float getScrollDuration();

    public native void setScrollDuration(float var1);

    public native float getFlowMarkerDistance();

    public native void setFlowMarkerDistance(float var1);

    public native float getFlowSpeed();

    public native void setFlowSpeed(float var1);

    public native float getFlowDuration();

    public native void setFlowDuration(float var1);

    public ImVec2 getPivotAlignment() {
        ImVec2 value = new ImVec2();
        this.getPivotAlignment(value);
        return value;
    }

    public native void getPivotAlignment(ImVec2 var1);

    public native float getPivotAlignmentX();

    public native float getPivotAlignmentY();

    public native void setPivotAlignment(float var1, float var2);

    public ImVec2 getPivotSize() {
        ImVec2 value = new ImVec2();
        this.getPivotSize(value);
        return value;
    }

    public native void getPivotSize(ImVec2 var1);

    public native float getPivotSizeX();

    public native float getPivotSizeY();

    public native void setPivotSize(float var1, float var2);

    public ImVec2 getPivotScale() {
        ImVec2 value = new ImVec2();
        this.getPivotScale(value);
        return value;
    }

    public native void getPivotScale(ImVec2 var1);

    public native float getPivotScaleX();

    public native float getPivotScaleY();

    public native void setPivotScale(float var1, float var2);

    public native float getPinCorners();

    public native void setPinCorners(float var1);

    public native float getPinRadius();

    public native void setPinRadius(float var1);

    public native float getPinArrowSize();

    public native void setPinArrowSize(float var1);

    public native float getPinArrowWidth();

    public native void setPinArrowWidth(float var1);

    public native float getGroupRounding();

    public native void setGroupRounding(float var1);

    public native float getGroupBorderWidth();

    public native void setGroupBorderWidth(float var1);

    public float[][] getColors() {
        float[][] colors = new float[18][4];
        this.getColors(colors);
        return colors;
    }

    public native void getColors(float[][] var1);

    public native void setColors(float[][] var1);

    public ImVec4 getColor(int styleColor) {
        ImVec4 value = new ImVec4();
        this.getColor(styleColor, value);
        return value;
    }

    public native void getColor(int var1, ImVec4 var2);

    public native void setColor(int var1, float var2, float var3, float var4, float var5);

    public native void setColor(int var1, int var2, int var3, int var4, int var5);

    public native void setColor(int var1, int var2);
}

