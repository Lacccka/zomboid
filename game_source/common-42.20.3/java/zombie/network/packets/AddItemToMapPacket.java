/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import zombie.GameTime;
import zombie.Lua.LuaEventManager;
import zombie.MapCollisionData;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.PerformanceSettings;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoGridOcclusionData;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.LosUtil;
import zombie.iso.objects.IsoFeedingTrough;
import zombie.iso.objects.IsoGenerator;
import zombie.iso.objects.IsoLightSwitch;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.WorldItemTypes;
import zombie.network.packets.INetworkPacket;
import zombie.pathfind.PolygonalMap2;

@PacketSetting(ordering=1, priority=1, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=2)
public class AddItemToMapPacket
implements INetworkPacket {
    @JSONField
    IsoObject obj;

    public void set(IsoObject o) {
        this.obj = o;
    }

    @Override
    public void setData(Object ... values2) {
        this.obj = (IsoObject)values2[0];
    }

    @Override
    public void write(ByteBufferWriter b) {
        this.obj.writeToRemoteBuffer(b);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.obj = WorldItemTypes.createFromBuffer(b);
        this.obj.loadFromRemoteBuffer(b);
    }

    @Override
    public void processClient(UdpConnection connection) {
        if (this.obj.square != null) {
            IsoObject isoObject = this.obj;
            if (isoObject instanceof IsoLightSwitch) {
                IsoLightSwitch isoLightSwitch = (IsoLightSwitch)isoObject;
                isoLightSwitch.addLightSourceFromSprite();
            }
            this.obj.addToWorld();
            this.obj.square.RecalcProperties();
            this.obj.square.RecalcAllWithNeighbours(true);
            IsoWorld.instance.currentCell.checkHaveRoof(this.obj.square.getX(), this.obj.square.getY());
            if (!(this.obj instanceof IsoWorldInventoryObject)) {
                for (int pn = 0; pn < IsoPlayer.numPlayers; ++pn) {
                    LosUtil.cachecleared[pn] = true;
                }
                IsoGridSquare.setRecalcLightTime(-1.0f);
                GameTime.instance.lightSourceUpdate = 100.0f;
                MapCollisionData.instance.squareChanged(this.obj.square);
                PolygonalMap2.instance.squareChanged(this.obj.square);
                if (this.obj == this.obj.square.getPlayerBuiltFloor()) {
                    IsoGridOcclusionData.SquareChanged();
                }
                IsoGenerator.updateGenerator(this.obj.getSquare());
            }
            if ((isoObject = this.obj) instanceof IsoFeedingTrough) {
                IsoFeedingTrough isoFeedingTrough = (IsoFeedingTrough)isoObject;
                isoFeedingTrough.checkOverlayAfterAnimalEat();
            }
            if (this.obj instanceof IsoWorldInventoryObject || this.obj.getContainer() != null) {
                LuaEventManager.triggerEvent("OnContainerUpdate", this.obj);
            }
            LuaEventManager.triggerEvent("OnObjectAdded", this.obj);
            if (PerformanceSettings.fboRenderChunk) {
                ++Core.dirtyGlobalLightsCount;
            }
            this.obj.invalidateRenderChunkLevel(64L);
        }
    }
}

