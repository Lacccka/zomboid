/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.vehicle;

import zombie.characters.Capability;
import zombie.network.PacketSetting;
import zombie.network.anticheats.AntiCheat;
import zombie.network.packets.vehicle.VehiclePhysicsPacket;

@PacketSetting(ordering=8, priority=2, reliability=1, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.Speed})
public class VehiclePhysicsUnreliablePacket
extends VehiclePhysicsPacket {
}

