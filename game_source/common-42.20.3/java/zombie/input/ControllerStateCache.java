/*
 * Decompiled with CFR 0.152.
 */
package zombie.input;

import org.lwjglx.input.Controller;
import org.lwjglx.input.Controllers;
import zombie.input.ControllerState;

public class ControllerStateCache {
    private final Object lock = "ControllerStateCache Lock";
    private int stateIndexUsing;
    private int stateIndexPolling = 1;
    private final ControllerState[] states = new ControllerState[]{new ControllerState(), new ControllerState()};
    private final Controller[] controllers = new Controller[16];

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void poll() {
        Object object = this.lock;
        synchronized (object) {
            if (!Controllers.isCreated()) {
                return;
            }
            ControllerState statePolling = this.getStatePolling();
            if (statePolling.wasPolled()) {
                return;
            }
            for (int i = 0; i < 16; ++i) {
                this.controllers[i] = Controllers.getController(i);
            }
            statePolling.poll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void swap() {
        Object object = this.lock;
        synchronized (object) {
            ControllerState prevStatePolling = this.getStatePolling();
            if (!prevStatePolling.wasPolled()) {
                return;
            }
            this.stateIndexUsing = this.stateIndexPolling;
            this.stateIndexPolling = this.stateIndexPolling == 1 ? 0 : 1;
            ControllerState stateActive = this.getState();
            stateActive.onStateActive(this);
            ControllerState statePolling = this.getStatePolling();
            statePolling.onStatePolling(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ControllerState getState() {
        Object object = this.lock;
        synchronized (object) {
            return this.states[this.stateIndexUsing];
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ControllerState getStatePolling() {
        Object object = this.lock;
        synchronized (object) {
            return this.states[this.stateIndexPolling];
        }
    }

    public void quit() {
        this.states[0].quit();
        this.states[1].quit();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Controller getController(int index) {
        Object object = this.lock;
        synchronized (object) {
            return this.controllers[index];
        }
    }
}

