/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.character;

import java.io.IOException;
import zombie.Lua.LuaEventManager;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.characters.animals.AnimalTracks;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.inventory.InventoryItem;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoAnimalTrack;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.fields.character.PlayerID;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class AnimalTracksPacket
implements INetworkPacket {
    @JSONField
    PlayerID player = new PlayerID();
    @JSONField
    AnimalTracks tracks;
    @JSONField
    InventoryItem item;

    @Override
    public void setData(Object ... values2) {
        if (values2.length == 3) {
            this.set((IsoPlayer)values2[0], (AnimalTracks)values2[1], (InventoryItem)values2[2]);
        } else if (values2.length == 2) {
            this.set((IsoPlayer)values2[0], (AnimalTracks)values2[1]);
        } else if (values2.length == 1) {
            this.set((IsoPlayer)values2[0]);
        } else {
            DebugType.Multiplayer.warn(this.getClass().getSimpleName() + ".set get invalid arguments");
        }
    }

    public void set(IsoPlayer character) {
        this.player.set(character);
    }

    public void set(IsoPlayer character, AnimalTracks tracks) {
        this.player.set(character);
        this.tracks = tracks;
    }

    public void set(IsoPlayer character, AnimalTracks tracks, InventoryItem item) {
        this.player.set(character);
        this.tracks = tracks;
        this.item = item;
    }

    @Override
    public void write(ByteBufferWriter b) {
        this.player.write(b);
        if (GameServer.server && this.tracks != null) {
            try {
                this.tracks.save(b.bb);
                if (this.tracks.isItem()) {
                    b.putInt(this.item.getID());
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.player.parse(b, connection);
        if (GameClient.client) {
            this.tracks = new AnimalTracks();
            try {
                this.tracks.load(b.bb, 0);
                if (this.tracks != null && this.tracks.getSquare() != null) {
                    if (this.tracks.isItem()) {
                        int itemId = b.getInt();
                        IsoGridSquare sq = this.tracks.getSquare();
                        for (int i = 0; i < sq.getObjects().size(); ++i) {
                            IsoWorldInventoryObject isoWorldInventoryObject;
                            IsoObject obj = sq.getObjects().get(i);
                            if (!(obj instanceof IsoWorldInventoryObject) || (isoWorldInventoryObject = (IsoWorldInventoryObject)obj).getItem().getID() != itemId) continue;
                            this.item = isoWorldInventoryObject.getItem();
                            this.tracks.setItem(this.item);
                            this.item.setAnimalTracks(this.tracks);
                            break;
                        }
                    }
                    if (this.tracks.getIsoAnimalTrack() == null) {
                        new IsoAnimalTrack(this.tracks.getSquare(), this.tracks.getTrackSprite(), this.tracks);
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        AnimalTracks.getAndFindNearestTracks(this.player.getPlayer());
    }

    @Override
    public void processClient(UdpConnection connection) {
        if (this.tracks != null && this.tracks.getSquare() != null) {
            LuaEventManager.triggerEvent("OnAnimalTracks", this.player.getPlayer(), this.tracks);
        }
    }
}

