/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImVec4;
import java.awt.Color;

public final class ImColor {
    private ImColor() {
    }

    public static int rgba(int r, int g, int b, int a) {
        return ImColor.intToColor(r, g, b, a);
    }

    public static int rgb(int r, int g, int b) {
        return ImColor.intToColor(r, g, b);
    }

    public static int rgba(float r, float g, float b, float a) {
        return ImColor.floatToColor(r, g, b, a);
    }

    public static int rgb(float r, float g, float b) {
        return ImColor.floatToColor(r, g, b);
    }

    public static int rgba(String hex) {
        return ImColor.rgbaToColor(hex);
    }

    public static int rgb(String hex) {
        return ImColor.rgbToColor(hex);
    }

    public static int rgba(ImVec4 color) {
        return ImColor.rgba(color.x, color.y, color.z, color.w);
    }

    public static int rgb(ImVec4 color) {
        return ImColor.rgb(color.x, color.y, color.z);
    }

    public static int rgba(Color color) {
        return ImColor.rgba(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int rgb(Color color) {
        return ImColor.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int hsla(float h, float s, float l, float a) {
        return ImColor.hslToColor(h, s, l, a);
    }

    public static int hsl(float h, float s, float l) {
        return ImColor.hslToColor(h, s, l);
    }

    public static int hsla(int h, int s, int l, int a) {
        return ImColor.hslToColor(h, s, l, (float)a);
    }

    public static int hsl(int h, int s, int l) {
        return ImColor.hslToColor(h, s, l);
    }

    @Deprecated
    public static int intToColor(int r, int g, int b, int a) {
        return a << 24 | b << 16 | g << 8 | r;
    }

    @Deprecated
    public static int intToColor(int r, int g, int b) {
        return ImColor.intToColor(r, g, b, 255);
    }

    @Deprecated
    public static int floatToColor(float r, float g, float b, float a) {
        return ImColor.intToColor((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f), (int)(a * 255.0f));
    }

    @Deprecated
    public static int floatToColor(float r, float g, float b) {
        return ImColor.floatToColor(r, g, b, 1.0f);
    }

    @Deprecated
    public static int rgbToColor(String hex) {
        return ImColor.intToColor(Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16), Integer.parseInt(hex.substring(5, 7), 16));
    }

    @Deprecated
    public static int rgbaToColor(String hex) {
        return ImColor.intToColor(Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16), Integer.parseInt(hex.substring(5, 7), 16), Integer.parseInt(hex.substring(7, 9), 16));
    }

    @Deprecated
    public static int hslToColor(int h, int s, int l) {
        return ImColor.hslToColor(h, s, l, 1.0f);
    }

    @Deprecated
    public static int hslToColor(int h, int s, int l, float a) {
        return ImColor.hslToColor((float)h / 360.0f, (float)s / 100.0f, (float)l / 100.0f, a);
    }

    @Deprecated
    public static int hslToColor(float h, float s, float l) {
        return ImColor.hslToColor(h, s, l, 1.0f);
    }

    @Deprecated
    public static int hslToColor(float h, float s, float l, float a) {
        float b;
        float g;
        float r;
        if (s == 0.0f) {
            r = l;
            g = l;
            b = l;
        } else {
            float q = (double)l < 0.5 ? l * (1.0f + s) : l + s - l * s;
            float p = 2.0f * l - q;
            r = ImColor.hue2rgb(p, q, h + 0.33333334f);
            g = ImColor.hue2rgb(p, q, h);
            b = ImColor.hue2rgb(p, q, h - 0.33333334f);
        }
        return ImColor.floatToColor(r, g, b, a);
    }

    private static float hue2rgb(float p, float q, float hue) {
        float h = hue;
        if (h < 0.0f) {
            h += 1.0f;
        }
        if (h > 1.0f) {
            h -= 1.0f;
        }
        if (6.0f * h < 1.0f) {
            return p + (q - p) * 6.0f * h;
        }
        if (2.0f * h < 1.0f) {
            return q;
        }
        if (3.0f * h < 2.0f) {
            return p + (q - p) * 6.0f * (0.6666667f - h);
        }
        return p;
    }
}

