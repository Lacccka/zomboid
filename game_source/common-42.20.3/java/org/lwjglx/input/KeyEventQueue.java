/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import org.lwjglx.Sys;
import org.lwjglx.input.EventQueue;
import org.lwjglx.input.KeyCodes;
import org.lwjglx.input.Keyboard;

public final class KeyEventQueue {
    public static final int MAX_EVENTS = 32;
    private final EventQueue queue = new EventQueue(32);
    private final int[] keyEvents = new int[32];
    private final boolean[] keyEventStates = new boolean[32];
    private final long[] nanoTimeEvents = new long[32];
    private final char[] keyEventChars = new char[256];

    public void addKeyEvent(int key, int status) {
        switch (status) {
            case 2: {
                if (!Keyboard.isRepeatEvent()) break;
            }
            case 0: 
            case 1: {
                this.keyEvents[this.queue.getNextPos()] = KeyCodes.toLwjglKey(key);
                this.keyEventStates[this.queue.getNextPos()] = status == 1 || status == 2;
                this.keyEventChars[this.queue.getNextPos()] = '\u0000';
                this.nanoTimeEvents[this.queue.getNextPos()] = Sys.getNanoTime();
                this.queue.add();
            }
        }
    }

    public void addCharEvent(char c) {
        this.keyEvents[this.queue.getNextPos()] = 0;
        this.keyEventStates[this.queue.getNextPos()] = true;
        this.keyEventChars[this.queue.getNextPos()] = c;
        this.nanoTimeEvents[this.queue.getNextPos()] = Sys.getNanoTime();
        this.queue.add();
    }

    public boolean next() {
        return this.queue.next();
    }

    public int getEventKey() {
        return this.keyEvents[this.queue.getCurrentPos()];
    }

    public char getEventCharacter() {
        return this.keyEventChars[this.queue.getCurrentPos()];
    }

    public boolean getEventKeyState() {
        return this.keyEventStates[this.queue.getCurrentPos()];
    }

    public long getEventNanoseconds() {
        return this.nanoTimeEvents[this.queue.getCurrentPos()];
    }
}

