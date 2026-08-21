/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso.fboRenderChunk;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import zombie.popman.ObjectPool;

public final class DepthStagger {
    private final TIntObjectHashMap<Billboard> billboards = new TIntObjectHashMap();
    private final TIntArrayList overlap = new TIntArrayList();
    private final ObjectPool<Billboard> pool = new ObjectPool<Billboard>(Billboard::new, "DepthStagger.pool");

    public DepthStagger() {
        this.billboards.setAutoCompactionFactor(0.0f);
    }

    private int calculateOffset(Billboard billboard, Billboard head) {
        this.overlap.clear();
        Billboard walk = head;
        while (walk != null) {
            if (billboard.screenX < walk.screenX + walk.width && billboard.screenX + billboard.width > walk.screenX) {
                this.overlap.add(walk.offset);
            }
            walk = walk.next;
        }
        for (int i = 0; i < 10; ++i) {
            if (!this.overlap.contains(i)) {
                return i;
            }
            if (this.overlap.contains(-i)) continue;
            return -i;
        }
        throw new RuntimeException("too much overlap");
    }

    public void startFrame() {
        this.billboards.forEachValue(billboard -> {
            billboard.release(this.pool);
            return true;
        });
        this.billboards.clear();
    }

    public int addBillboard(int depth, int screenX, int width) {
        Billboard billboard = this.pool.alloc();
        billboard.set(screenX, width);
        Billboard head = this.billboards.get(depth);
        billboard.offset = this.calculateOffset(billboard, head);
        billboard.next = head;
        this.billboards.put(depth, billboard);
        return billboard.offset;
    }

    private static final class Billboard {
        int screenX;
        int width;
        int offset;
        Billboard next;

        private Billboard() {
        }

        Billboard set(int screenX, int width) {
            this.screenX = screenX;
            this.width = width;
            this.offset = 0;
            this.next = null;
            return this;
        }

        void release(ObjectPool<Billboard> pool) {
            if (this.next != null) {
                this.next.release(pool);
                this.next = null;
            }
            pool.release(this);
        }
    }
}

