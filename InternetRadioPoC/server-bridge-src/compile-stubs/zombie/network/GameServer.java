package zombie.network;

import java.util.ArrayList;
import java.util.HashMap;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpEngine;

public final class GameServer {
    public static String checksum;
    public static UdpEngine udpEngine;
    public static final ArrayList<IsoPlayer> Players = null;
    public static final HashMap<Short, IsoPlayer> IDToPlayerMap = null;
    public static void sendPlayerConnected(IsoPlayer player, IConnection connection) { }
}
