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
    public void setX(float value) { }
    public void setY(float value) { }
    public void setZ(float value) { }
}
