package zombie.characters;

import zombie.iso.IsoCell;

public class IsoPlayer {
    public short onlineId;
    public boolean remote;
    public String username;

    public IsoPlayer(IsoCell cell) { }
    public short getOnlineID() { return onlineId; }
    public float getX() { return 0.0f; }
    public float getY() { return 0.0f; }
    public float getZ() { return 0.0f; }
    public float setX(float value) { return value; }
    public float setY(float value) { return value; }
    public float setZ(float value) { return value; }
}
