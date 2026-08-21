/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;
import astar.datastructures.IClosedSet;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.util.Comparator;

public class ClosedSetHash
implements IClosedSet {
    private final TIntObjectHashMap<ISearchNode> hashMap = new TIntObjectHashMap();
    private final Comparator<ISearchNode> comp;
    private static final MinNodeProc minNodeProc = new MinNodeProc();

    public ClosedSetHash(Comparator<ISearchNode> comp) {
        this.comp = comp;
    }

    @Override
    public boolean contains(ISearchNode node) {
        return this.hashMap.containsKey(node.keyCode());
    }

    @Override
    public void add(ISearchNode node) {
        this.hashMap.put(node.keyCode(), node);
    }

    @Override
    public ISearchNode min() {
        ClosedSetHash.minNodeProc.comp = this.comp;
        ClosedSetHash.minNodeProc.candidate = null;
        this.hashMap.forEachValue(minNodeProc);
        return ClosedSetHash.minNodeProc.candidate;
    }

    @Override
    public void clear() {
        this.hashMap.clear();
    }

    private static final class MinNodeProc
    implements TObjectProcedure<ISearchNode> {
        Comparator<ISearchNode> comp;
        ISearchNode candidate;

        private MinNodeProc() {
        }

        @Override
        public boolean execute(ISearchNode iSearchNode) {
            if (this.candidate == null) {
                this.candidate = iSearchNode;
                return true;
            }
            if (this.comp.compare(iSearchNode, this.candidate) < 0) {
                this.candidate = iSearchNode;
            }
            return true;
        }
    }
}

