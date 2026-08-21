/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

class EventQueue {
    private int maxEvents = 32;
    private int currentEventPos = -1;
    private int nextEventPos;

    EventQueue(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    void add() {
        ++this.nextEventPos;
        if (this.nextEventPos == this.maxEvents) {
            this.nextEventPos = 0;
        }
        if (this.nextEventPos == this.currentEventPos) {
            ++this.currentEventPos;
            if (this.currentEventPos == this.maxEvents) {
                this.currentEventPos = 0;
            }
        }
    }

    boolean next() {
        if (this.currentEventPos == this.nextEventPos - 1) {
            return false;
        }
        if (this.nextEventPos == 0 && this.currentEventPos == this.maxEvents - 1) {
            return false;
        }
        ++this.currentEventPos;
        if (this.currentEventPos == this.maxEvents) {
            this.currentEventPos = 0;
        }
        return true;
    }

    int getMaxEvents() {
        return this.maxEvents;
    }

    int getCurrentPos() {
        return this.currentEventPos;
    }

    int getNextPos() {
        return this.nextEventPos;
    }
}

