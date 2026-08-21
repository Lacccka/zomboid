/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleNetworkSound.server;

import zombie.audio.parameters.ParameterVehicleRoadMaterial;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;
import zombie.util.StringUtils;
import zombie.vehicleNetworkSound.SharedVehicleState;
import zombie.vehicleNetworkSound.VehicleStateFlags;
import zombie.vehicles.BaseVehicle;

final class VehicleState
extends SharedVehicleState {
    VehicleState() {
    }

    void sendNew(UdpConnection udpConnection) {
        INetworkPacket.send(udpConnection, PacketTypes.PacketType.VehicleSoundAddVehicle, this);
    }

    void update(BaseVehicle vehicle, UdpConnection udpConnection) {
        ParameterVehicleRoadMaterial.Material roadMaterial1;
        short vehicleStateFlags1;
        int changeBits = 0;
        if (!vehicle.getScriptName().equals(this.scriptName)) {
            this.scriptName = vehicle.getScriptName();
            changeBits |= 1;
        }
        if (this.x != vehicle.getX() || this.y != vehicle.getY() || this.z != vehicle.getZ()) {
            this.x = vehicle.getX();
            this.y = vehicle.getY();
            this.z = vehicle.getZ();
            changeBits |= 2;
        }
        if (this.engineState != vehicle.getEngineState()) {
            this.engineState = vehicle.getEngineState();
            changeBits |= 4;
        }
        if (this.vehicleStateFlags != (vehicleStateFlags1 = VehicleStateFlags.fromVehicle(vehicle))) {
            this.vehicleStateFlags = vehicleStateFlags1;
            changeBits |= 8;
        }
        if (this.engineSpeed != this.engineSpeedToShort(vehicle)) {
            this.engineSpeed = this.engineSpeedToShort(vehicle);
            changeBits |= 0x10;
        }
        if (this.currentSpeedKmHour != (byte)vehicle.getCurrentSpeedKmHour()) {
            this.currentSpeedKmHour = (byte)vehicle.getCurrentSpeedKmHour();
            changeBits |= 0x20;
        }
        if (this.gear != (byte)vehicle.getTransmissionNumber()) {
            this.gear = (byte)vehicle.getTransmissionNumber();
            changeBits |= 0x40;
        }
        if (this.engineCondition != (byte)vehicle.getEngineCondition() || this.engineQuality != (byte)vehicle.getEngineQuality()) {
            this.engineCondition = (byte)vehicle.getEngineCondition();
            this.engineQuality = (byte)vehicle.getEngineQuality();
            changeBits |= 0x80;
        }
        if (!StringUtils.equals(this.chosenAlarmSound, vehicle.getChosenAlarmSound())) {
            this.chosenAlarmSound = vehicle.getChosenAlarmSound();
            changeBits |= 0x100;
        }
        if (this.lightbarSirenMode.get() != vehicle.getLightbarSirenMode()) {
            this.lightbarSirenMode.set(vehicle.getLightbarSirenMode());
            changeBits |= 0x200;
        }
        if (this.minWheelSkid != (byte)(vehicle.getMinWheelSkid() * 100.0f)) {
            this.minWheelSkid = (byte)(vehicle.getMinWheelSkid() * 100.0f);
            changeBits |= 0x400;
        }
        if (this.steering != (byte)(vehicle.getMaxWheelSteering() * 100.0f)) {
            this.steering = (byte)(vehicle.getMaxWheelSteering() * 100.0f);
            changeBits |= 0x800;
        }
        if (this.roadMaterial != (roadMaterial1 = vehicle.getRoadMaterial())) {
            this.roadMaterial = roadMaterial1;
            changeBits |= 0x1000;
        }
        if (changeBits == 0) {
            return;
        }
        INetworkPacket.send(udpConnection, PacketTypes.PacketType.VehicleSoundUpdateVehicle, this, changeBits);
    }

    void remove(UdpConnection udpConnection) {
        INetworkPacket.send(udpConnection, PacketTypes.PacketType.VehicleSoundRemoveVehicle, this.id);
    }

    short engineSpeedToShort(BaseVehicle vehicle) {
        double engineSpeed = Math.floor(vehicle.getEngineSpeed() / 100.0) * 100.0;
        return (short)engineSpeed;
    }
}

