/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

public class PoolIDGenerator {
    private static int currentID = 1;
    private static final Object currentIDLock = new Object();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getNewID() {
        Object object = currentIDLock;
        synchronized (object) {
            return currentID++;
        }
    }
}

