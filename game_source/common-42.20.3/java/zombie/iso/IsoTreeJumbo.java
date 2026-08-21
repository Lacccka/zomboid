/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso;

import java.util.HashMap;
import java.util.Map;
import zombie.debug.DebugType;

public class IsoTreeJumbo {
    public static final TreeDescription NULL = new TreeDescription(null, null, null, null, null, null);
    public static final Map<String, TreeDescription> Jumbos = new HashMap<String, TreeDescription>();

    static {
        GenericTree[] trees = new GenericTree[]{new GenericTree("e_redmaple", false), new GenericTree("e_easternredbud", false), new GenericTree("e_dogwood", false), new GenericTree("e_cockspurhawthorn", false), new GenericTree("e_carolinasilverbell", false), new GenericTree("e_americanlinden", false), new GenericTree("e_canadianhemlock", true), new GenericTree("e_americanholly", true), new GenericTree("e_yellowwood", false), new GenericTree("e_virginiapine", true), new GenericTree("e_riverbirch", false)};
        String[] types = new String[]{"JUMBOXL", "JUMBOXXL"};
        String trunksName = "e_stumps";
        for (int itree = 0; itree < trees.length; ++itree) {
            String baseName = trees[itree].name();
            int ntrees = trees[itree].keepLeaves() ? 2 : 6;
            int section = itree / 8;
            int column = itree % 8;
            for (String type : types) {
                String advName = baseName + type + "_1_";
                String advTrunksName = "e_stumps_" + type + "_";
                for (int i = 0; i < ntrees; ++i) {
                    int snowny = i == 1 ? 1 : 0;
                    int trunk = switch (i) {
                        case 0 -> 0;
                        case 1 -> 1;
                        default -> 2;
                    };
                    Jumbos.put(advName + i, new TreeDescription(advName + i, advName + (i + 6), advName + (trunk + 12), advTrunksName + (column + 8 + snowny * 32 + section * 64), advName + "15", advName + "16"));
                    DebugType.WorldGen.debugln(Jumbos.get(advName + i));
                }
            }
        }
    }

    public record TreeDescription(String main, String treetop, String trunk, String stump, String burned, String burnedFrozen) {
    }

    private record GenericTree(String name, boolean keepLeaves) {
    }
}

