/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjglx.BufferUtils;
import org.lwjglx.LWJGLException;

public class Cursor {
    public static final int CURSOR_ONE_BIT_TRANSPARENCY = 1;
    public static final int CURSOR_8_BIT_ALPHA = 2;
    public static final int CURSOR_ANIMATION = 4;
    private long cursorHandle;

    public Cursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
        if (numImages != 1) {
            System.out.println("ANIMATED CURSORS NOT YET SUPPORTED IN LWJGLX");
            return;
        }
        IntBuffer flippedImages = BufferUtils.createIntBuffer(images.limit());
        Cursor.flipImages(width, height, numImages, images, flippedImages);
        ByteBuffer pixels = Cursor.convertARGBIntBuffertoRGBAByteBuffer(width, height, flippedImages);
        GLFWImage cursorImage = GLFWImage.malloc();
        cursorImage.width(width);
        cursorImage.height(height);
        cursorImage.pixels(pixels);
        this.cursorHandle = GLFW.glfwCreateCursor(cursorImage, xHotspot, yHotspot);
        if (this.cursorHandle == 0L) {
            throw new RuntimeException("Error creating GLFW cursor");
        }
    }

    private static ByteBuffer convertARGBIntBuffertoRGBAByteBuffer(int width, int height, IntBuffer imageBuffer) {
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        for (int i = 0; i < imageBuffer.limit(); ++i) {
            int argbColor = imageBuffer.get(i);
            byte alpha = (byte)(argbColor >>> 24);
            byte blue = (byte)(argbColor >>> 16);
            byte green = (byte)(argbColor >>> 8);
            byte red = (byte)argbColor;
            pixels.put(red);
            pixels.put(green);
            pixels.put(blue);
            pixels.put(alpha);
        }
        pixels.flip();
        return pixels;
    }

    public static int getMinCursorSize() {
        return 1;
    }

    public static int getMaxCursorSize() {
        return 512;
    }

    public static int getCapabilities() {
        return 2;
    }

    private static void flipImages(int width, int height, int numImages, IntBuffer images, IntBuffer images_copy) {
        for (int i = 0; i < numImages; ++i) {
            int start_index = i * width * height;
            Cursor.flipImage(width, height, start_index, images, images_copy);
        }
    }

    private static void flipImage(int width, int height, int start_index, IntBuffer images, IntBuffer images_copy) {
        for (int y = 0; y < height >> 1; ++y) {
            int index_y_1 = y * width + start_index;
            int index_y_2 = (height - y - 1) * width + start_index;
            for (int x = 0; x < width; ++x) {
                int index1 = index_y_1 + x;
                int index2 = index_y_2 + x;
                int temp_pixel = images.get(index1 + images.position());
                images_copy.put(index1, images.get(index2 + images.position()));
                images_copy.put(index2, temp_pixel);
            }
        }
    }

    public long getHandle() {
        return this.cursorHandle;
    }

    public void destroy() {
        GLFW.glfwDestroyCursor(this.cursorHandle);
    }
}

