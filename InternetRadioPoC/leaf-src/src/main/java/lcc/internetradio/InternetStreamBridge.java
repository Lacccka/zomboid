package lcc.internetradio;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal reflection bridge to Project Zomboid's bundled javafmod wrapper. */
public final class InternetStreamBridge {
    public static final String VERSION = "0.2.0";
    private static final long FMOD_CREATESTREAM = 0x00000080L;
    private static final long FMOD_3D = 0x00000010L;
    private static final Map<Long, Long> SOUND_BY_CHANNEL = new ConcurrentHashMap<>();
    private static volatile Api api;
    private static volatile String lastError = "not initialized";

    private InternetStreamBridge() {}

    public static long start(String url, float x, float y, float z,
                             float minDistance, float maxDistance, float volume) {
        long sound = 0L;
        long channel = 0L;
        try {
            if (url == null || !url.startsWith("https://")) {
                return fail("only HTTPS stream URLs are accepted");
            }
            float safeMin = Math.max(0.1f, minDistance);
            float safeMax = Math.max(safeMin, maxDistance);
            Api fmod = api();

            sound = (Long) fmod.createSound.invoke(null, url, FMOD_CREATESTREAM);
            if (sound == 0L) return fail("FMOD_System_CreateSound returned 0");

            channel = (Long) fmod.playSound.invoke(null, sound, true);
            if (channel == 0L) {
                releaseSound(fmod, sound);
                return fail("FMOD_System_PlaySound returned 0");
            }
            // Register before configuring the channel so every later failure
            // can stop the channel and release its sound through one path.
            SOUND_BY_CHANNEL.put(channel, sound);

            fmod.setMode.invoke(null, channel, FMOD_3D);
            setPosition(fmod, channel, x, y, z);
            fmod.setMinMaxDistance.invoke(null, channel, safeMin, safeMax);
            fmod.setVolume.invoke(null, channel, clamp01(volume));
            fmod.setPaused.invoke(null, channel, false);

            lastError = "none";
            System.out.println("[LCC Internet Radio Bridge] stream started on FMOD channel " + channel);
            return channel;
        } catch (Throwable error) {
            if (channel != 0L) stopQuietly(channel);
            else if (sound != 0L) releaseSoundQuietly(sound);
            return fail("start failed: " + rootMessage(error));
        }
    }

    public static boolean update(long channel, float x, float y, float z, float volume) {
        if (channel == 0L || !SOUND_BY_CHANNEL.containsKey(channel)) return false;
        try {
            Api fmod = api();
            setPosition(fmod, channel, x, y, z);
            fmod.setVolume.invoke(null, channel, clamp01(volume));
            return true;
        } catch (Throwable error) {
            fail("update failed: " + rootMessage(error));
            return false;
        }
    }

    public static boolean isPlaying(long channel) {
        if (channel == 0L || !SOUND_BY_CHANNEL.containsKey(channel)) return false;
        try {
            return (Boolean) api().isPlaying.invoke(null, channel);
        } catch (Throwable error) {
            fail("isPlaying failed: " + rootMessage(error));
            return false;
        }
    }

    public static void stop(long channel) {
        if (channel == 0L) return;
        stopQuietly(channel);
        System.out.println("[LCC Internet Radio Bridge] stream stopped on FMOD channel " + channel);
    }

    public static String lastError() { return lastError; }

    private static void stopQuietly(long channel) {
        Long sound = SOUND_BY_CHANNEL.remove(channel);
        try { api().stop.invoke(null, channel); }
        catch (Throwable ignored) { /* Never escape into the Lua tick event. */ }
        if (sound != null && sound != 0L) releaseSoundQuietly(sound);
    }

    private static void releaseSoundQuietly(long sound) {
        try { releaseSound(api(), sound); }
        catch (Throwable ignored) { /* Best-effort failed-start cleanup. */ }
    }

    private static void releaseSound(Api fmod, long sound) throws Exception {
        fmod.releaseSound.invoke(null, sound);
    }

    private static void setPosition(Api fmod, long channel, float x, float y, float z)
            throws Exception {
        // Vanilla FMODSoundEmitter uses a 3x vertical scale for world sounds.
        fmod.set3DAttributes.invoke(null, channel, x, y, z * 3.0f, 0.0f, 0.0f, 0.0f);
    }

    private static Api api() throws Exception {
        Api current = api;
        if (current != null) return current;
        synchronized (InternetStreamBridge.class) {
            if (api == null) {
                api = new Api(Class.forName("fmod.javafmod"));
                System.out.println("[LCC Internet Radio Bridge] javafmod API initialized");
            }
            return api;
        }
    }

    private static long fail(String message) {
        lastError = message;
        System.out.println("[LCC Internet Radio Bridge] " + message);
        return 0L;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return current.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    private static final class Api {
        private final Method createSound;
        private final Method playSound;
        private final Method setMode;
        private final Method set3DAttributes;
        private final Method setMinMaxDistance;
        private final Method setVolume;
        private final Method setPaused;
        private final Method isPlaying;
        private final Method stop;
        private final Method releaseSound;

        private Api(Class<?> javafmod) throws NoSuchMethodException {
            createSound = javafmod.getMethod("FMOD_System_CreateSound", String.class, long.class);
            playSound = javafmod.getMethod("FMOD_System_PlaySound", long.class, boolean.class);
            setMode = javafmod.getMethod("FMOD_Channel_SetMode", long.class, long.class);
            set3DAttributes = javafmod.getMethod("FMOD_Channel_Set3DAttributes",
                    long.class, float.class, float.class, float.class,
                    float.class, float.class, float.class);
            setMinMaxDistance = javafmod.getMethod("FMOD_Channel_Set3DMinMaxDistance",
                    long.class, float.class, float.class);
            setVolume = javafmod.getMethod("FMOD_Channel_SetVolume", long.class, float.class);
            setPaused = javafmod.getMethod("FMOD_Channel_SetPaused", long.class, boolean.class);
            isPlaying = javafmod.getMethod("FMOD_Channel_IsPlaying", long.class);
            stop = javafmod.getMethod("FMOD_Channel_Stop", long.class);
            releaseSound = javafmod.getMethod("FMOD_Sound_Release", long.class);
        }
    }
}
