/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

public class TimedInputHandler {
    public static final int MAX_CLICK_TIME = 400;
    private boolean keyDown;
    private boolean keyPressed;
    private boolean keyReleased;
    private boolean keyClicked;
    private boolean keyIsMouse;
    private boolean keyWasMouse;
    private boolean muted;
    private long keyDownMs;
    private final IsKeyDownCallback isKeyDownCallback;
    private final IsMouseKeyCallback isMouseKeyCallback;
    private final IsKeyMutedCallback isMutedCallback;

    public TimedInputHandler(IsKeyDownCallback isKeyDownCallback, IsMouseKeyCallback isMouseKeyCallback, IsKeyMutedCallback isMuted) {
        this.isKeyDownCallback = isKeyDownCallback;
        this.isMouseKeyCallback = isMouseKeyCallback;
        this.isMutedCallback = isMuted;
    }

    public void mute() {
        this.muted = true;
    }

    public boolean queryKeyDown() {
        return this.isKeyDownCallback.isKeyDown();
    }

    public boolean queryKeyIsMouse() {
        return this.isMouseKeyCallback != null && this.isMouseKeyCallback.isMouseKey();
    }

    public boolean queryIsMuted() {
        return this.isMutedCallback != null && this.isMutedCallback.isMuted();
    }

    public void updateState() {
        long now = System.currentTimeMillis();
        this.keyDown = this.queryKeyDown();
        this.keyIsMouse = this.queryKeyIsMouse();
        this.keyPressed = this.keyDown && this.keyDownMs == 0L;
        this.keyReleased = !this.keyDown && this.keyDownMs != 0L;
        boolean bl = this.keyClicked = this.keyReleased && now - this.keyDownMs < 400L;
        if (this.keyPressed) {
            this.keyWasMouse = this.keyIsMouse;
        }
        if (this.keyPressed) {
            this.keyDownMs = now;
        } else if (this.keyReleased) {
            this.keyDownMs = 0L;
        }
        boolean shouldMute = this.queryIsMuted();
        if (this.muted != shouldMute) {
            this.muted = this.keyDown || this.keyReleased || shouldMute;
        }
    }

    public boolean isKeyDown() {
        return !this.muted && this.keyDown;
    }

    public boolean isKeyPressed() {
        return !this.muted && this.keyPressed;
    }

    public boolean isKeyReleased() {
        return !this.muted && this.keyReleased;
    }

    public boolean isMouseKey() {
        return this.keyIsMouse;
    }

    public boolean wasMouseKey() {
        return this.keyWasMouse;
    }

    public boolean isKeyClicked() {
        return !this.muted && this.keyClicked;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public static interface IsKeyDownCallback {
        public boolean isKeyDown();
    }

    public static interface IsMouseKeyCallback {
        public boolean isMouseKey();
    }

    public static interface IsKeyMutedCallback {
        public boolean isMuted();
    }
}

