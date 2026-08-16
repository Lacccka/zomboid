package zombie.core.raknet;

public final class RakVoice {
    public static boolean GetServerVOIPEnable() { return false; }
    public static int GetSampleRate() { return 0; }
    public static int GetBufferSizeBytes() { return 0; }
    public static int GetSendFramePeriod() { return 0; }
    public static int GetBuffering() { return 0; }
    public static boolean GetIs3D() { return false; }
    public static float GetMinDistance() { return 0; }
    public static float GetMaxDistance() { return 0; }
    public static void SendFrame(long uuid, long playerId, byte[] buffer, long size) { }
}
