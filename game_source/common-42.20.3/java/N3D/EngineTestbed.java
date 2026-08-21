/*
 * Decompiled with CFR 0.152.
 */
package N3D;

import java.io.IOException;
import org.lwjglx.LWJGLException;
import org.lwjglx.opengl.Display;
import zombie.GameWindow;
import zombie.core.random.RandLua;
import zombie.core.random.RandStandard;
import zombie.input.KeyboardState;
import zombie.input.MouseState;

public class EngineTestbed {
    static MouseState mouse = new MouseState();
    static KeyboardState keyboard = new KeyboardState();

    static void main(String[] args2) throws InterruptedException {
        RandStandard.INSTANCE.init();
        RandLua.INSTANCE.init();
        try {
            GameWindow.InitDisplay();
        }
        catch (IOException | LWJGLException e) {
            e.printStackTrace();
        }
        while (!Display.isCloseRequested()) {
            mouse.poll();
            keyboard.poll();
            Display.update(true);
        }
    }
}

