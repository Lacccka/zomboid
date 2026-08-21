/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.fields;

import zombie.characters.Faction;
import zombie.core.Color;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.fields.INetworkPacketField;

public class FactionTag
implements INetworkPacketField {
    private static final int UNSET_COLOR = 0;
    @JSONField
    private String tag;
    @JSONField
    private int color;

    public void set(Faction faction) {
        this.tag = null;
        this.color = 0;
        if (faction == null) {
            return;
        }
        String factionTag = faction.getTag();
        if (factionTag == null) {
            return;
        }
        this.tag = factionTag;
        this.color = Color.colorToABGR(faction.getTagColor());
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.tag = null;
        this.color = 0;
        if (b.getBoolean()) {
            this.tag = b.getUTF();
            this.color = b.getInt();
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        if (b.putBoolean(this.tag != null)) {
            b.putUTF(this.tag);
            b.putInt(this.color);
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return this.tag != null;
    }

    public String getTag() {
        return this.tag;
    }

    public int getColor() {
        return this.color;
    }
}

