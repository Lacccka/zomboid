/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMOD_GUID;
import fmod.fmod.FMOD_STUDIO_EVENT_DESCRIPTION;
import fmod.fmod.FMOD_STUDIO_PARAMETER_DESCRIPTION;
import fmod.fmod.FMOD_STUDIO_PARAMETER_FLAGS;
import fmod.fmod.FMOD_STUDIO_PARAMETER_ID;
import fmod.fmod.SoundListener;
import fmod.javafmod;
import fmod.javafmodJNI;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import zombie.ZomboidFileSystem;
import zombie.audio.FMODGlobalParameter;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.logger.ExceptionLogger;
import zombie.core.random.Rand;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.iso.Vector2;

public class FMODManager {
    public static FMODManager instance = new FMODManager();
    public static final int FMOD_STUDIO_INIT_NORMAL = 0;
    public static final int FMOD_STUDIO_INIT_LIVEUPDATE = 1;
    public static final int FMOD_STUDIO_INIT_ALLOW_MISSING_PLUGINS = 2;
    public static final int FMOD_STUDIO_INIT_SYNCHRONOUS_UPDATE = 4;
    public static final int FMOD_STUDIO_INIT_DEFERRED_CALLBACKS = 8;
    public static final int FMOD_INIT_NORMAL = 0;
    public static final int FMOD_INIT_STREAM_FROM_UPDATE = 1;
    public static final int FMOD_INIT_MIX_FROM_UPDATE = 2;
    public static final int FMOD_INIT_3D_RIGHTHANDED = 4;
    public static final int FMOD_INIT_CHANNEL_LOWPASS = 256;
    public static final int FMOD_INIT_CHANNEL_DISTANCEFILTER = 512;
    public static final int FMOD_INIT_PROFILE_ENABLE = 65536;
    public static final int FMOD_INIT_VOL0_BECOMES_VIRTUAL = 131072;
    public static final int FMOD_INIT_GEOMETRY_USECLOSEST = 262144;
    public static final int FMOD_INIT_PREFER_DOLBY_DOWNMIX = 524288;
    public static final int FMOD_INIT_THREAD_UNSAFE = 0x100000;
    public static final int FMOD_INIT_PROFILE_METER_ALL = 0x200000;
    public static final int FMOD_DEFAULT = 0;
    public static final int FMOD_LOOP_OFF = 1;
    public static final int FMOD_LOOP_NORMAL = 2;
    public static final int FMOD_LOOP_BIDI = 4;
    public static final int FMOD_2D = 8;
    public static final int FMOD_3D = 16;
    public static final int FMOD_HARDWARE = 32;
    public static final int FMOD_SOFTWARE = 64;
    public static final int FMOD_CREATESTREAM = 128;
    public static final int FMOD_CREATESAMPLE = 256;
    public static final int FMOD_CREATECOMPRESSEDSAMPLE = 512;
    public static final int FMOD_OPENUSER = 1024;
    public static final int FMOD_OPENMEMORY = 2048;
    public static final int FMOD_OPENMEMORY_POINT = 0x10000000;
    public static final int FMOD_OPENRAW = 4096;
    public static final int FMOD_OPENONLY = 8192;
    public static final int FMOD_ACCURATETIME = 16384;
    public static final int FMOD_MPEGSEARCH = 32768;
    public static final int FMOD_NONBLOCKING = 65536;
    public static final int FMOD_UNIQUE = 131072;
    public static final int FMOD_3D_HEADRELATIVE = 262144;
    public static final int FMOD_3D_WORLDRELATIVE = 524288;
    public static final int FMOD_3D_INVERSEROLLOFF = 0x100000;
    public static final int FMOD_3D_LINEARROLLOFF = 0x200000;
    public static final int FMOD_3D_LINEARSQUAREROLLOFF = 0x400000;
    public static final int FMOD_3D_INVERSETAPEREDROLLOFF = 0x800000;
    public static final int FMOD_3D_CUSTOMROLLOFF = 0x4000000;
    public static final int FMOD_3D_IGNOREGEOMETRY = 0x40000000;
    public static final int FMOD_IGNORETAGS = 0x2000000;
    public static final int FMOD_LOWMEM = 0x8000000;
    public static final int FMOD_LOADSECONDARYRAM = 0x20000000;
    public static final int FMOD_VIRTUAL_PLAYFROMSTART = Integer.MIN_VALUE;
    public static final int FMOD_PRESET_OFF = 0;
    public static final int FMOD_PRESET_GENERIC = 1;
    public static final int FMOD_PRESET_PADDEDCELL = 2;
    public static final int FMOD_PRESET_ROOM = 3;
    public static final int FMOD_PRESET_BATHROOM = 4;
    public static final int FMOD_PRESET_LIVINGROOM = 5;
    public static final int FMOD_PRESET_STONEROOM = 6;
    public static final int FMOD_PRESET_AUDITORIUM = 7;
    public static final int FMOD_PRESET_CONCERTHALL = 8;
    public static final int FMOD_PRESET_CAVE = 9;
    public static final int FMOD_PRESET_ARENA = 10;
    public static final int FMOD_PRESET_HANGAR = 11;
    public static final int FMOD_PRESET_CARPETTEDHALLWAY = 12;
    public static final int FMOD_PRESET_HALLWAY = 13;
    public static final int FMOD_PRESET_STONECORRIDOR = 14;
    public static final int FMOD_PRESET_ALLEY = 15;
    public static final int FMOD_PRESET_FOREST = 16;
    public static final int FMOD_PRESET_CITY = 17;
    public static final int FMOD_PRESET_MOUNTAINS = 18;
    public static final int FMOD_PRESET_QUARRY = 19;
    public static final int FMOD_PRESET_PLAIN = 20;
    public static final int FMOD_PRESET_PARKINGLOT = 21;
    public static final int FMOD_PRESET_SEWERPIPE = 22;
    public static final int FMOD_PRESET_UNDERWATER = 23;
    public static final int FMOD_TIMEUNIT_MS = 1;
    public static final int FMOD_TIMEUNIT_PCM = 2;
    public static final int FMOD_TIMEUNIT_PCMBYTES = 4;
    public static final int FMOD_STUDIO_PLAYBACK_PLAYING = 0;
    public static final int FMOD_STUDIO_PLAYBACK_SUSTAINING = 1;
    public static final int FMOD_STUDIO_PLAYBACK_STOPPED = 2;
    public static final int FMOD_STUDIO_PLAYBACK_STARTING = 3;
    public static final int FMOD_STUDIO_PLAYBACK_STOPPING = 4;
    public static final int FMOD_SOUND_FORMAT_NONE = 0;
    public static final int FMOD_SOUND_FORMAT_PCM8 = 1;
    public static final int FMOD_SOUND_FORMAT_PCM16 = 2;
    public static final int FMOD_SOUND_FORMAT_PCM24 = 3;
    public static final int FMOD_SOUND_FORMAT_PCM32 = 4;
    public static final int FMOD_SOUND_FORMAT_PCMFLOAT = 5;
    public static final int FMOD_SOUND_FORMAT_BITSTREAM = 6;
    ArrayList<Long> sounds = new ArrayList();
    ArrayList<Long> instances = new ArrayList();
    public long channelGroupInGameNonBankSounds;
    private final HashMap<String, FMOD_STUDIO_PARAMETER_DESCRIPTION> parameterDescriptionMap = new HashMap();
    private final HashMap<String, FMOD_STUDIO_EVENT_DESCRIPTION> eventDescriptionMap = new HashMap();
    private final ArrayList<String> eventPathList = new ArrayList();
    private final ArrayList<FMODGlobalParameter> globalParameterList = new ArrayList();
    private final HashMap<String, FMODGlobalParameter> globalParameterMap = new HashMap();
    int c;
    Vector2 move = new Vector2(0.0f, 0.0f);
    Vector2 pos = new Vector2(-400.0f, -400.0f);
    float x;
    float y;
    float z;
    float vx = 0.02f;
    float vy = 0.01f;
    float vz;
    private int numListeners = 1;
    private final HashMap<String, Long> fileToSoundMap = new HashMap();
    private final int[] reverbPreset = new int[]{-1, -1, -1, -1};

    public void init() {
        int profileFlags;
        javafmodJNI.init();
        int result = javafmod.FMOD_System_Create();
        if (result != 0) {
            Core.soundDisabled = true;
            return;
        }
        int studioFlags = Core.debug ? 1 : 0;
        result = javafmod.FMOD_System_Init(1024, 0 | studioFlags, 0x20300 | (profileFlags = Core.debug ? 0x210000 : 0));
        if (result != 0) {
            Core.soundDisabled = true;
            return;
        }
        javafmod.FMOD_System_Set3DSettings(1.0f, 1.0f, 1.0f);
        this.loadBank("ZomboidSound.strings");
        this.loadBank("ZomboidMusic");
        this.loadBank("ZomboidSound");
        this.loadBank("ZomboidSoundMP");
        this.loadBank("Animal");
        this.loadBank("Broadcast");
        this.loadBank("Character");
        this.loadBank("Object");
        this.loadBank("Vehicle");
        this.loadBank("Weapon");
        this.loadBank("World");
        this.loadBank("Zombie");
        this.channelGroupInGameNonBankSounds = javafmod.FMOD_System_CreateChannelGroup("InGameNonBank");
        this.initGlobalParameters();
        this.initEvents();
        this.initVCA();
    }

    private String bankPath(String bankName) throws IOException {
        return new File("./media/sound/banks/Desktop/" + bankName + ".bank").getCanonicalFile().getPath();
    }

    private void loadBank(String bankName) {
        try {
            javafmod.FMOD_Studio_System_LoadBankFile(this.bankPath(bankName));
        }
        catch (Exception ex) {
            ExceptionLogger.logException(ex);
        }
    }

    /*
     * Unable to fully structure code
     */
    private void loadZombieTest() {
        bankFile = javafmod.FMOD_Studio_System_LoadBankFile("media/sound/banks/chopper.bank");
        bankFile2 = javafmod.FMOD_Studio_System_LoadBankFile("media/sound/banks/chopper.strings.bank");
        javafmod.FMOD_Studio_LoadSampleData(bankFile);
        event = javafmod.FMOD_Studio_System_GetEvent("{5deff1b6-984c-42e0-98ec-c133a83d0513}");
        event2 = javafmod.FMOD_Studio_System_GetEvent("{c00fed39-230a-4c6a-b9c0-b0924876f53a}");
        javafmod.FMOD_Studio_LoadEventSampleData(event);
        javafmod.FMOD_Studio_LoadEventSampleData(event2);
        x = 2000;
        y = 2000;
        listener = new SoundListener(0);
        listener.x = 2000.0f;
        listener.y = 2000.0f;
        listener.tick();
        bDone = false;
        instances = new ArrayList<TestZombieInfo>();
        attacking = new ArrayList<TestZombieInfo>();
        z = new TestZombieInfo(event, 1995.0f, 1995.0f);
        javafmod.FMOD_Studio_EventInstance_SetParameterByName(z.inst, "Pitch", (float)Rand.Next(200) / 100.0f - 1.0f);
        javafmod.FMOD_Studio_EventInstance_SetParameterByName(z.inst, "Aggitation", 0.0f);
        instances.add(z);
        ++this.c;
        while (true) lbl-1000:
        // 5 sources

        {
            for (n = 0; n < instances.size(); ++n) {
                inst = (TestZombieInfo)instances.get(n);
                if (Rand.Next(1000) != 0) continue;
                --this.c;
                attacking.add(inst);
                instances.remove(inst);
                a = (float)(Rand.Next(40) + 60) / 100.0f;
                javafmod.FMOD_Studio_EventInstance_SetParameterByName(inst.inst, "Aggitation", a);
                --n;
            }
            for (n = 0; n < attacking.size(); ++n) {
                testZombieInfo = (TestZombieInfo)attacking.get(n);
                vec = new Vector2(2000.0f - testZombieInfo.x, 2000.0f - testZombieInfo.y);
                if (vec.getLength() < 2.0f) {
                    attacking.remove(testZombieInfo);
                    javafmod.FMOD_Studio_EventInstance_Stop(testZombieInfo.inst, false);
                }
                vec.setLength(0.01f);
                testZombieInfo.x += vec.x;
                testZombieInfo.y += vec.y;
                javafmod.FMOD_Studio_EventInstance3D(testZombieInfo.inst, testZombieInfo.x, testZombieInfo.y, 0.0f);
            }
            javafmod.FMOD_System_Update();
            try {
                Thread.sleep(10L);
                ** continue;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    private void loadTestEvent() {
        long bankFile = javafmod.FMOD_Studio_System_LoadBankFile("media/sound/banks/chopper.bank");
        javafmod.FMOD_Studio_LoadSampleData(bankFile);
        long event = javafmod.FMOD_Studio_System_GetEvent("{47d0c496-7d0a-48e9-9bad-1c8fcf292986}");
        javafmod.FMOD_Studio_LoadEventSampleData(event);
        long inst = javafmod.FMOD_Studio_System_CreateEventInstance(event);
        javafmod.FMOD_Studio_EventInstance3D(inst, this.pos.x, this.pos.y, 100.0f);
        javafmod.FMOD_Studio_Listener3D(0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        javafmod.FMOD_Studio_StartEvent(inst);
        int count = 0;
        boolean bDone = false;
        while (true) {
            if (count > 200) {
                this.pos.x = Rand.Next(400) - 200;
                this.pos.y = Rand.Next(400) - 200;
                if (Rand.Next(3) == 0) {
                    this.pos.x /= 4.0f;
                    this.pos.y /= 4.0f;
                }
                javafmod.FMOD_Studio_StartEvent(inst);
                javafmod.FMOD_Studio_EventInstance3D(inst, this.pos.x, this.pos.y, 0.0f);
                count = 0;
            }
            ++count;
            javafmod.FMOD_System_Update();
            try {
                Thread.sleep(10L);
                continue;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    public void loadTest() {
        long sound = javafmod.FMOD_System_CreateSound("media/sound/PZ_FemaleBeingEaten_Death.wav", 16L);
        javafmod.FMOD_Sound_Set3DMinMaxDistance(sound, 0.005f, 100.0f);
        this.sounds.add(sound);
        this.playTest();
    }

    public void playTest() {
        long sound = this.sounds.get(0);
        long channel = javafmod.FMOD_System_PlaySound(sound, true);
        javafmod.FMOD_Channel_Set3DAttributes(channel, 10.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        javafmod.FMOD_Channel_SetPaused(channel, false);
        this.instances.add(channel);
    }

    public void applyDSP() {
        javafmod.FMOD_System_PlayDSP();
    }

    public void tick() {
        if (Rand.Next(100) == 0) {
            this.vx = -this.vx;
        }
        if (Rand.Next(100) == 0) {
            this.vy = -this.vy;
        }
        float lx = this.x;
        float ly = this.y;
        float lz = this.z;
        this.x += this.vx;
        this.y += this.vy;
        this.z += this.vz;
        for (int n = 0; n < this.instances.size(); ++n) {
            long channel = this.instances.get(n);
            javafmod.FMOD_Channel_Set3DAttributes(channel, this.x, this.y, this.z, this.x - lx, this.y - ly, this.z - lz);
            float maxrange = 40.0f;
            float reverb = (Math.abs(this.x) + Math.abs(this.y)) / 40.0f;
            if (reverb < 0.1f) {
                reverb = 0.1f;
            }
            if (reverb > 1.0f) {
                reverb = 1.0f;
            }
            reverb *= reverb;
            javafmod.FMOD_Channel_SetReverbProperties(channel, 0, reverb *= 40.0f);
        }
        int numListeners = 0;
        for (int i = 0; i < IsoPlayer.numPlayers; ++i) {
            IsoPlayer player = IsoPlayer.players[i];
            if (player == null) continue;
            ++numListeners;
        }
        if (numListeners > 0) {
            if (numListeners != this.numListeners) {
                javafmod.FMOD_Studio_SetNumListeners(numListeners);
            }
        } else if (this.numListeners != 1) {
            javafmod.FMOD_Studio_SetNumListeners(1);
        }
        this.numListeners = numListeners;
        this.updateReverbPreset();
        javafmod.FMOD_System_Update();
    }

    public int getNumListeners() {
        return this.numListeners;
    }

    public long loadSound(String filename) {
        if ((filename = ZomboidFileSystem.instance.getAbsolutePath(filename)) == null) {
            return 0L;
        }
        Long sound = this.fileToSoundMap.get(filename);
        if (sound != null) {
            return sound;
        }
        sound = javafmod.FMOD_System_CreateSound(filename, 16L);
        if (Core.debug && sound == 0L) {
            DebugLog.log("ERROR: failed to load sound " + filename);
        }
        this.fileToSoundMap.put(filename, sound);
        return sound;
    }

    public void stopSound(long soundEffect) {
        if (soundEffect == 0L) {
            return;
        }
        javafmod.FMOD_Channel_Stop(soundEffect);
    }

    public boolean isPlaying(long sound) {
        return javafmod.FMOD_Channel_IsPlaying(sound);
    }

    public long loadSound(String filename, boolean looped) {
        if ((filename = ZomboidFileSystem.instance.getAbsolutePath(filename)) == null) {
            return 0L;
        }
        Long sound = this.fileToSoundMap.get(filename);
        if (sound != null) {
            return sound;
        }
        sound = !looped ? Long.valueOf(javafmod.FMOD_System_CreateSound(filename, 16L)) : Long.valueOf(javafmod.FMOD_System_CreateSound(filename, 18L));
        this.fileToSoundMap.put(filename, sound);
        return sound;
    }

    public void updateReverbPreset() {
        int preset0 = -1;
        int preset1 = -1;
        int preset2 = -1;
        preset1 = 16;
        preset2 = 17;
        preset0 = 0;
        preset1 = 0;
        preset2 = 0;
        if (this.reverbPreset[0] != preset0) {
            javafmod.FMOD_System_SetReverbDefault(0, preset0);
            this.reverbPreset[0] = preset0;
        }
        if (this.reverbPreset[1] != preset1) {
            javafmod.FMOD_System_SetReverbDefault(1, preset1);
            this.reverbPreset[1] = preset1;
        }
        if (this.reverbPreset[2] != preset2) {
            javafmod.FMOD_System_SetReverbDefault(2, preset2);
            this.reverbPreset[2] = preset2;
        }
    }

    private void initGlobalParameters() {
        int parameterCount = javafmodJNI.FMOD_Studio_System_GetParameterDescriptionCount();
        if (parameterCount <= 0) {
            return;
        }
        long[] parameterArray = new long[parameterCount];
        parameterCount = javafmodJNI.FMOD_Studio_System_GetParameterDescriptionList(parameterArray);
        for (int i = 0; i < parameterCount; ++i) {
            long parameterDesc = parameterArray[i];
            this.initParameterDescription(parameterDesc);
            javafmodJNI.FMOD_Studio_ParameterDescription_Free(parameterDesc);
        }
    }

    private FMOD_STUDIO_PARAMETER_DESCRIPTION initParameterDescription(long parameterDesc) {
        String name = javafmodJNI.FMOD_Studio_ParameterDescription_GetName(parameterDesc);
        FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription = this.parameterDescriptionMap.get(name);
        if (parameterDescription == null) {
            int flags = javafmodJNI.FMOD_Studio_ParameterDescription_GetFlags(parameterDesc);
            long address = javafmodJNI.FMOD_Studio_ParameterDescription_GetID(parameterDesc);
            FMOD_STUDIO_PARAMETER_ID parameterId = new FMOD_STUDIO_PARAMETER_ID(address);
            parameterDescription = new FMOD_STUDIO_PARAMETER_DESCRIPTION(name, parameterId, flags, this.parameterDescriptionMap.size());
            this.parameterDescriptionMap.put(parameterDescription.name, parameterDescription);
            if ((parameterDescription.flags & FMOD_STUDIO_PARAMETER_FLAGS.FMOD_STUDIO_PARAMETER_GLOBAL.bit) != 0) {
                boolean bl = true;
            } else {
                boolean bl = true;
            }
        }
        return parameterDescription;
    }

    private void initEvents() {
        int bankCount = javafmodJNI.FMOD_Studio_System_GetBankCount();
        if (bankCount <= 0) {
            return;
        }
        long[] bankList = new long[bankCount];
        bankCount = javafmodJNI.FMOD_Studio_System_GetBankList(bankList);
        for (int i = 0; i < bankCount; ++i) {
            int eventDescCount = javafmodJNI.FMOD_Studio_Bank_GetEventCount(bankList[i]);
            if (eventDescCount <= 0) continue;
            long[] eventDescList = new long[eventDescCount];
            eventDescCount = javafmodJNI.FMOD_Studio_Bank_GetEventList(bankList[i], eventDescList);
            for (int j = 0; j < eventDescCount; ++j) {
                this.initEvent(eventDescList[j]);
            }
        }
        this.eventPathList.sort(String::compareToIgnoreCase);
    }

    private void initEvent(long eventDesc) {
        String path = javafmodJNI.FMOD_Studio_EventDescription_GetPath(eventDesc);
        long address = javafmodJNI.FMOD_Studio_EventDescription_GetID(eventDesc);
        FMOD_GUID guid = new FMOD_GUID(address);
        boolean bHasSustainPoints = javafmodJNI.FMOD_Studio_EventDescription_HasSustainPoint(eventDesc);
        long length = javafmodJNI.FMOD_Studio_EventDescription_GetLength(eventDesc);
        FMOD_STUDIO_EVENT_DESCRIPTION eventDescription = new FMOD_STUDIO_EVENT_DESCRIPTION(eventDesc, path, guid, bHasSustainPoints, length);
        this.eventDescriptionMap.put(path.toLowerCase(Locale.ENGLISH), eventDescription);
        this.eventPathList.add(path);
        if (!path.startsWith("event:/World/Ambi")) {
            javafmodJNI.FMOD_Studio_LoadEventSampleData(eventDesc);
        }
        int parameterCount = javafmodJNI.FMOD_Studio_EventDescription_GetParameterDescriptionCount(eventDesc);
        for (int k = 0; k < parameterCount; ++k) {
            long parameterDesc = javafmodJNI.FMOD_Studio_EventDescription_GetParameterDescriptionByIndex(eventDesc, k);
            FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription = this.initParameterDescription(parameterDesc);
            eventDescription.parameters.add(parameterDescription);
            javafmodJNI.FMOD_Studio_ParameterDescription_Free(parameterDesc);
        }
    }

    public FMOD_STUDIO_EVENT_DESCRIPTION getEventDescription(String path) {
        return this.eventDescriptionMap.get(path.toLowerCase(Locale.ENGLISH));
    }

    public FMOD_STUDIO_PARAMETER_DESCRIPTION getParameterDescription(String name) {
        return this.parameterDescriptionMap.get(name);
    }

    public FMOD_STUDIO_PARAMETER_ID getParameterID(String name) {
        FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription = this.getParameterDescription(name);
        return parameterDescription == null ? null : parameterDescription.id;
    }

    public int getParameterCount() {
        return this.parameterDescriptionMap.size();
    }

    public ArrayList<String> getEventPathList() {
        return this.eventPathList;
    }

    public void addGlobalParameter(FMODGlobalParameter globalParameter) {
        this.globalParameterList.add(globalParameter);
        this.globalParameterMap.put(globalParameter.getName(), globalParameter);
    }

    public FMODGlobalParameter getGlobalParameter(String parameterName) {
        return this.globalParameterMap.get(parameterName);
    }

    private void initVCA() {
        int bankCount = javafmodJNI.FMOD_Studio_System_GetBankCount();
        if (bankCount <= 0) {
            return;
        }
        long[] bankList = new long[bankCount];
        bankCount = javafmodJNI.FMOD_Studio_System_GetBankList(bankList);
        for (int i = 0; i < bankCount; ++i) {
            int vcaCount = javafmodJNI.FMOD_Studio_Bank_GetVCACount(bankList[i]);
            if (vcaCount <= 0) continue;
            long[] vcaList = new long[vcaCount];
            vcaCount = javafmodJNI.FMOD_Studio_Bank_GetVCAList(bankList[i], vcaList);
            for (int j = 0; j < vcaCount; ++j) {
                long vca = vcaList[j];
                String vcaPath = javafmodJNI.FMOD_Studio_VCA_GetPath(vca);
                DebugType.Sound.debugln("FMOD VCA %d / %d : %s", j + 1, vcaCount, vcaPath);
            }
        }
    }

    public static class TestZombieInfo {
        public float x;
        public float y;
        public long event;
        public long inst;

        public TestZombieInfo(long event, float x, float y) {
            this.createZombieInstance(event, x, y);
        }

        public long createZombieInstance(long event, float x, float y) {
            long inst = javafmod.FMOD_Studio_System_CreateEventInstance(event);
            javafmod.FMOD_Studio_EventInstance3D(inst, x, y, 0.0f);
            javafmod.FMOD_Studio_StartEvent(inst);
            this.x = x;
            this.y = y;
            this.event = event;
            this.inst = inst;
            return inst;
        }
    }
}

