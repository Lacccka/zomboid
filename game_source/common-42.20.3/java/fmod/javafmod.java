/*
 * Decompiled with CFR 0.152.
 */
package fmod;

import fmod.FMODRecordPosition;
import fmod.FMODSoundData;
import fmod.FMOD_DriverInfo;
import fmod.SWIGTYPE_p_FMOD_3D_ROLLOFF_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_ADVANCEDSETTINGS;
import fmod.SWIGTYPE_p_FMOD_BOOL;
import fmod.SWIGTYPE_p_FMOD_CHANNEL;
import fmod.SWIGTYPE_p_FMOD_CHANNELCONTROL_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_CHANNELGROUP;
import fmod.SWIGTYPE_p_FMOD_CHANNELMASK;
import fmod.SWIGTYPE_p_FMOD_CODEC_DESCRIPTION;
import fmod.SWIGTYPE_p_FMOD_CREATESOUNDEXINFO;
import fmod.SWIGTYPE_p_FMOD_DEBUG_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_DEBUG_FLAGS;
import fmod.SWIGTYPE_p_FMOD_DEBUG_MODE;
import fmod.SWIGTYPE_p_FMOD_DSP;
import fmod.SWIGTYPE_p_FMOD_DSPCONNECTION;
import fmod.SWIGTYPE_p_FMOD_DSPCONNECTION_TYPE;
import fmod.SWIGTYPE_p_FMOD_DSP_DESCRIPTION;
import fmod.SWIGTYPE_p_FMOD_DSP_METERING_INFO;
import fmod.SWIGTYPE_p_FMOD_DSP_TYPE;
import fmod.SWIGTYPE_p_FMOD_FILE_ASYNCCANCEL_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_FILE_ASYNCREAD_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_FILE_CLOSE_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_FILE_OPEN_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_FILE_READ_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_FILE_SEEK_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_GEOMETRY;
import fmod.SWIGTYPE_p_FMOD_GUID;
import fmod.SWIGTYPE_p_FMOD_MEMORY_ALLOC_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_MEMORY_FREE_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_MEMORY_REALLOC_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_MEMORY_TYPE;
import fmod.SWIGTYPE_p_FMOD_MODE;
import fmod.SWIGTYPE_p_FMOD_OPENSTATE;
import fmod.SWIGTYPE_p_FMOD_OUTPUTTYPE;
import fmod.SWIGTYPE_p_FMOD_PLUGINTYPE;
import fmod.SWIGTYPE_p_FMOD_PORT_INDEX;
import fmod.SWIGTYPE_p_FMOD_PORT_TYPE;
import fmod.SWIGTYPE_p_FMOD_REVERB3D;
import fmod.SWIGTYPE_p_FMOD_REVERB_PROPERTIES;
import fmod.SWIGTYPE_p_FMOD_SOUND;
import fmod.SWIGTYPE_p_FMOD_SOUNDGROUP;
import fmod.SWIGTYPE_p_FMOD_SOUNDGROUP_BEHAVIOR;
import fmod.SWIGTYPE_p_FMOD_SOUND_FORMAT;
import fmod.SWIGTYPE_p_FMOD_SOUND_TYPE;
import fmod.SWIGTYPE_p_FMOD_SPEAKER;
import fmod.SWIGTYPE_p_FMOD_SPEAKERMODE;
import fmod.SWIGTYPE_p_FMOD_SYNCPOINT;
import fmod.SWIGTYPE_p_FMOD_SYSTEM;
import fmod.SWIGTYPE_p_FMOD_SYSTEM_CALLBACK;
import fmod.SWIGTYPE_p_FMOD_SYSTEM_CALLBACK_TYPE;
import fmod.SWIGTYPE_p_FMOD_TAG;
import fmod.SWIGTYPE_p_FMOD_TIMEUNIT;
import fmod.SWIGTYPE_p_FMOD_VECTOR;
import fmod.SWIGTYPE_p_float;
import fmod.SWIGTYPE_p_int;
import fmod.SWIGTYPE_p_p_FMOD_CHANNEL;
import fmod.SWIGTYPE_p_p_FMOD_CHANNELGROUP;
import fmod.SWIGTYPE_p_p_FMOD_DSP;
import fmod.SWIGTYPE_p_p_FMOD_DSPCONNECTION;
import fmod.SWIGTYPE_p_p_FMOD_DSP_DESCRIPTION;
import fmod.SWIGTYPE_p_p_FMOD_DSP_PARAMETER_DESC;
import fmod.SWIGTYPE_p_p_FMOD_GEOMETRY;
import fmod.SWIGTYPE_p_p_FMOD_REVERB3D;
import fmod.SWIGTYPE_p_p_FMOD_SOUND;
import fmod.SWIGTYPE_p_p_FMOD_SOUNDGROUP;
import fmod.SWIGTYPE_p_p_FMOD_SYNCPOINT;
import fmod.SWIGTYPE_p_p_FMOD_SYSTEM;
import fmod.SWIGTYPE_p_p_FMOD_VECTOR;
import fmod.SWIGTYPE_p_p_void;
import fmod.SWIGTYPE_p_unsigned_int;
import fmod.SWIGTYPE_p_unsigned_long_long;
import fmod.SWIGTYPE_p_void;
import fmod.fmod.FMOD_STUDIO_EVENT_CALLBACK;
import fmod.fmod.FMOD_STUDIO_PARAMETER_ID;
import fmod.javafmodJNI;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import zombie.core.Core;
import zombie.debug.DebugLog;

public class javafmod {
    static HashMap<String, Long> map = new HashMap();
    private static final int[] reverb = new int[4];

    public static int FMOD_Memory_Initialize(SWIGTYPE_p_void poolmem, int poollen, SWIGTYPE_p_FMOD_MEMORY_ALLOC_CALLBACK useralloc, SWIGTYPE_p_FMOD_MEMORY_REALLOC_CALLBACK userrealloc, SWIGTYPE_p_FMOD_MEMORY_FREE_CALLBACK userfree, SWIGTYPE_p_FMOD_MEMORY_TYPE memtypeflags) {
        return javafmodJNI.FMOD_Memory_Initialize(SWIGTYPE_p_void.getCPtr(poolmem), poollen, SWIGTYPE_p_FMOD_MEMORY_ALLOC_CALLBACK.getCPtr(useralloc), SWIGTYPE_p_FMOD_MEMORY_REALLOC_CALLBACK.getCPtr(userrealloc), SWIGTYPE_p_FMOD_MEMORY_FREE_CALLBACK.getCPtr(userfree), SWIGTYPE_p_FMOD_MEMORY_TYPE.getCPtr(memtypeflags));
    }

    public static int FMOD_Memory_GetStats(SWIGTYPE_p_int currentalloced, SWIGTYPE_p_int maxalloced, SWIGTYPE_p_FMOD_BOOL blocking) {
        return javafmodJNI.FMOD_Memory_GetStats(SWIGTYPE_p_int.getCPtr(currentalloced), SWIGTYPE_p_int.getCPtr(maxalloced), SWIGTYPE_p_FMOD_BOOL.getCPtr(blocking));
    }

    public static int FMOD_Debug_Initialize(SWIGTYPE_p_FMOD_DEBUG_FLAGS flags, SWIGTYPE_p_FMOD_DEBUG_MODE mode, SWIGTYPE_p_FMOD_DEBUG_CALLBACK callback, String filename) {
        return javafmodJNI.FMOD_Debug_Initialize(SWIGTYPE_p_FMOD_DEBUG_FLAGS.getCPtr(flags), SWIGTYPE_p_FMOD_DEBUG_MODE.getCPtr(mode), SWIGTYPE_p_FMOD_DEBUG_CALLBACK.getCPtr(callback), filename);
    }

    public static int FMOD_File_SetDiskBusy(int busy) {
        return javafmodJNI.FMOD_File_SetDiskBusy(busy);
    }

    public static int FMOD_File_GetDiskBusy(SWIGTYPE_p_int busy) {
        return javafmodJNI.FMOD_File_GetDiskBusy(SWIGTYPE_p_int.getCPtr(busy));
    }

    public static int FMOD_System_Create() {
        return javafmodJNI.FMOD_System_Create();
    }

    public static int FMOD_System_Release(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_Release(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_SetOutput(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_OUTPUTTYPE output) {
        return javafmodJNI.FMOD_System_SetOutput(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_OUTPUTTYPE.getCPtr(output));
    }

    public static int FMOD_System_GetOutput(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_OUTPUTTYPE output) {
        return javafmodJNI.FMOD_System_GetOutput(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_OUTPUTTYPE.getCPtr(output));
    }

    public static int FMOD_System_GetNumDrivers(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int numdrivers) {
        return javafmodJNI.FMOD_System_GetNumDrivers(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(numdrivers));
    }

    public static int FMOD_System_GetDriverInfo(SWIGTYPE_p_FMOD_SYSTEM system, int id, String name, int namelen, SWIGTYPE_p_FMOD_GUID guid, SWIGTYPE_p_int systemrate, SWIGTYPE_p_FMOD_SPEAKERMODE speakermode, SWIGTYPE_p_int speakermodechannels) {
        return javafmodJNI.FMOD_System_GetDriverInfo(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), id, name, namelen, SWIGTYPE_p_FMOD_GUID.getCPtr(guid), SWIGTYPE_p_int.getCPtr(systemrate), SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(speakermode), SWIGTYPE_p_int.getCPtr(speakermodechannels));
    }

    public static int FMOD_System_SetDriver(SWIGTYPE_p_FMOD_SYSTEM system, int driver) {
        return javafmodJNI.FMOD_System_SetDriver(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), driver);
    }

    public static int FMOD_System_GetDriver(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int driver) {
        return javafmodJNI.FMOD_System_GetDriver(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(driver));
    }

    public static int FMOD_System_SetSoftwareChannels(SWIGTYPE_p_FMOD_SYSTEM system, int numsoftwarechannels) {
        return javafmodJNI.FMOD_System_SetSoftwareChannels(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), numsoftwarechannels);
    }

    public static int FMOD_System_GetSoftwareChannels(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int numsoftwarechannels) {
        return javafmodJNI.FMOD_System_GetSoftwareChannels(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(numsoftwarechannels));
    }

    public static int FMOD_System_SetSoftwareFormat(SWIGTYPE_p_FMOD_SYSTEM system, int samplerate, SWIGTYPE_p_FMOD_SPEAKERMODE speakermode, int numrawspeakers) {
        return javafmodJNI.FMOD_System_SetSoftwareFormat(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), samplerate, SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(speakermode), numrawspeakers);
    }

    public static int FMOD_System_GetSoftwareFormat(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int samplerate, SWIGTYPE_p_FMOD_SPEAKERMODE speakermode, SWIGTYPE_p_int numrawspeakers) {
        return javafmodJNI.FMOD_System_GetSoftwareFormat(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(samplerate), SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(speakermode), SWIGTYPE_p_int.getCPtr(numrawspeakers));
    }

    public static int FMOD_System_SetDSPBufferSize(SWIGTYPE_p_FMOD_SYSTEM system, long bufferlength, int numbuffers) {
        return javafmodJNI.FMOD_System_SetDSPBufferSize(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), bufferlength, numbuffers);
    }

    public static int FMOD_System_GetDSPBufferSize(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_unsigned_int bufferlength, SWIGTYPE_p_int numbuffers) {
        return javafmodJNI.FMOD_System_GetDSPBufferSize(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_unsigned_int.getCPtr(bufferlength), SWIGTYPE_p_int.getCPtr(numbuffers));
    }

    public static int FMOD_System_SetFileSystem(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_FILE_OPEN_CALLBACK useropen, SWIGTYPE_p_FMOD_FILE_CLOSE_CALLBACK userclose, SWIGTYPE_p_FMOD_FILE_READ_CALLBACK userread, SWIGTYPE_p_FMOD_FILE_SEEK_CALLBACK userseek, SWIGTYPE_p_FMOD_FILE_ASYNCREAD_CALLBACK userasyncread, SWIGTYPE_p_FMOD_FILE_ASYNCCANCEL_CALLBACK userasynccancel, int blockalign) {
        return javafmodJNI.FMOD_System_SetFileSystem(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_FILE_OPEN_CALLBACK.getCPtr(useropen), SWIGTYPE_p_FMOD_FILE_CLOSE_CALLBACK.getCPtr(userclose), SWIGTYPE_p_FMOD_FILE_READ_CALLBACK.getCPtr(userread), SWIGTYPE_p_FMOD_FILE_SEEK_CALLBACK.getCPtr(userseek), SWIGTYPE_p_FMOD_FILE_ASYNCREAD_CALLBACK.getCPtr(userasyncread), SWIGTYPE_p_FMOD_FILE_ASYNCCANCEL_CALLBACK.getCPtr(userasynccancel), blockalign);
    }

    public static int FMOD_System_AttachFileSystem(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_FILE_OPEN_CALLBACK useropen, SWIGTYPE_p_FMOD_FILE_CLOSE_CALLBACK userclose, SWIGTYPE_p_FMOD_FILE_READ_CALLBACK userread, SWIGTYPE_p_FMOD_FILE_SEEK_CALLBACK userseek) {
        return javafmodJNI.FMOD_System_AttachFileSystem(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_FILE_OPEN_CALLBACK.getCPtr(useropen), SWIGTYPE_p_FMOD_FILE_CLOSE_CALLBACK.getCPtr(userclose), SWIGTYPE_p_FMOD_FILE_READ_CALLBACK.getCPtr(userread), SWIGTYPE_p_FMOD_FILE_SEEK_CALLBACK.getCPtr(userseek));
    }

    public static int FMOD_System_SetAdvancedSettings(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_ADVANCEDSETTINGS settings) {
        return javafmodJNI.FMOD_System_SetAdvancedSettings(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_ADVANCEDSETTINGS.getCPtr(settings));
    }

    public static int FMOD_System_GetAdvancedSettings(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_ADVANCEDSETTINGS settings) {
        return javafmodJNI.FMOD_System_GetAdvancedSettings(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_ADVANCEDSETTINGS.getCPtr(settings));
    }

    public static int FMOD_System_SetCallback(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_SYSTEM_CALLBACK callback, SWIGTYPE_p_FMOD_SYSTEM_CALLBACK_TYPE callbackmask) {
        return javafmodJNI.FMOD_System_SetCallback(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_SYSTEM_CALLBACK.getCPtr(callback), SWIGTYPE_p_FMOD_SYSTEM_CALLBACK_TYPE.getCPtr(callbackmask));
    }

    public static int FMOD_System_SetPluginPath(SWIGTYPE_p_FMOD_SYSTEM system, String path) {
        return javafmodJNI.FMOD_System_SetPluginPath(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), path);
    }

    public static int FMOD_System_LoadPlugin(SWIGTYPE_p_FMOD_SYSTEM system, String filename, SWIGTYPE_p_unsigned_int handle, long priority) {
        return javafmodJNI.FMOD_System_LoadPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), filename, SWIGTYPE_p_unsigned_int.getCPtr(handle), priority);
    }

    public static int FMOD_System_UnloadPlugin(SWIGTYPE_p_FMOD_SYSTEM system, long handle) {
        return javafmodJNI.FMOD_System_UnloadPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), handle);
    }

    public static int FMOD_System_GetNumPlugins(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_PLUGINTYPE plugintype, SWIGTYPE_p_int numplugins) {
        return javafmodJNI.FMOD_System_GetNumPlugins(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_PLUGINTYPE.getCPtr(plugintype), SWIGTYPE_p_int.getCPtr(numplugins));
    }

    public static int FMOD_System_GetPluginHandle(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_PLUGINTYPE plugintype, int index, SWIGTYPE_p_unsigned_int handle) {
        return javafmodJNI.FMOD_System_GetPluginHandle(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_PLUGINTYPE.getCPtr(plugintype), index, SWIGTYPE_p_unsigned_int.getCPtr(handle));
    }

    public static int FMOD_System_GetPluginInfo(SWIGTYPE_p_FMOD_SYSTEM system, long handle, SWIGTYPE_p_FMOD_PLUGINTYPE plugintype, String name, int namelen, SWIGTYPE_p_unsigned_int version) {
        return javafmodJNI.FMOD_System_GetPluginInfo(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), handle, SWIGTYPE_p_FMOD_PLUGINTYPE.getCPtr(plugintype), name, namelen, SWIGTYPE_p_unsigned_int.getCPtr(version));
    }

    public static int FMOD_System_SetOutputByPlugin(SWIGTYPE_p_FMOD_SYSTEM system, long handle) {
        return javafmodJNI.FMOD_System_SetOutputByPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), handle);
    }

    public static int FMOD_System_GetOutputByPlugin(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_unsigned_int handle) {
        return javafmodJNI.FMOD_System_GetOutputByPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_unsigned_int.getCPtr(handle));
    }

    public static int FMOD_System_CreateDSPByPlugin(SWIGTYPE_p_FMOD_SYSTEM system, long handle, SWIGTYPE_p_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_System_CreateDSPByPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), handle, SWIGTYPE_p_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_System_GetDSPInfoByPlugin(SWIGTYPE_p_FMOD_SYSTEM system, long handle, SWIGTYPE_p_p_FMOD_DSP_DESCRIPTION description) {
        return javafmodJNI.FMOD_System_GetDSPInfoByPlugin(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), handle, SWIGTYPE_p_p_FMOD_DSP_DESCRIPTION.getCPtr(description));
    }

    public static int FMOD_System_RegisterCodec(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_CODEC_DESCRIPTION description, SWIGTYPE_p_unsigned_int handle, long priority) {
        return javafmodJNI.FMOD_System_RegisterCodec(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_CODEC_DESCRIPTION.getCPtr(description), SWIGTYPE_p_unsigned_int.getCPtr(handle), priority);
    }

    public static int FMOD_System_RegisterDSP(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_DSP_DESCRIPTION description, SWIGTYPE_p_unsigned_int handle) {
        return javafmodJNI.FMOD_System_RegisterDSP(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_DSP_DESCRIPTION.getCPtr(description), SWIGTYPE_p_unsigned_int.getCPtr(handle));
    }

    public static int FMOD_System_Init(int maxchannels, long studioFlags, long systemFlags) {
        return javafmodJNI.FMOD_System_Init(maxchannels, studioFlags, systemFlags);
    }

    public static int FMOD_System_Close(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_Close(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_Update() {
        return javafmodJNI.FMOD_System_Update();
    }

    public static int FMOD_System_SetSpeakerPosition(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_SPEAKER speaker, float x, float y, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_System_SetSpeakerPosition(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_SPEAKER.getCPtr(speaker), x, y, SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_System_GetSpeakerPosition(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_SPEAKER speaker, SWIGTYPE_p_float x, SWIGTYPE_p_float y, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_System_GetSpeakerPosition(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_SPEAKER.getCPtr(speaker), SWIGTYPE_p_float.getCPtr(x), SWIGTYPE_p_float.getCPtr(y), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_System_SetStreamBufferSize(SWIGTYPE_p_FMOD_SYSTEM system, long filebuffersize, SWIGTYPE_p_FMOD_TIMEUNIT filebuffersizetype) {
        return javafmodJNI.FMOD_System_SetStreamBufferSize(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), filebuffersize, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(filebuffersizetype));
    }

    public static int FMOD_System_GetStreamBufferSize(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_unsigned_int filebuffersize, SWIGTYPE_p_FMOD_TIMEUNIT filebuffersizetype) {
        return javafmodJNI.FMOD_System_GetStreamBufferSize(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_unsigned_int.getCPtr(filebuffersize), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(filebuffersizetype));
    }

    public static int FMOD_System_Set3DSettings(float dopplerscale, float distancefactor, float rolloffscale) {
        return javafmodJNI.FMOD_System_Set3DSettings(dopplerscale, distancefactor, rolloffscale);
    }

    public static int FMOD_System_Get3DSettings(SWIGTYPE_p_float dopplerscale, SWIGTYPE_p_float distancefactor, SWIGTYPE_p_float rolloffscale) {
        return javafmodJNI.FMOD_System_Get3DSettings(SWIGTYPE_p_float.getCPtr(dopplerscale), SWIGTYPE_p_float.getCPtr(distancefactor), SWIGTYPE_p_float.getCPtr(rolloffscale));
    }

    public static int FMOD_System_Set3DNumListeners(int numlisteners) {
        return javafmodJNI.FMOD_System_Set3DNumListeners(numlisteners);
    }

    public static int FMOD_System_Get3DNumListeners(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int numlisteners) {
        return javafmodJNI.FMOD_System_Get3DNumListeners(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(numlisteners));
    }

    public static int FMOD_System_Set3DListenerAttributes(int listener, float x, float y, float z, float vx, float vy, float vz, float fx, float fy, float fz, float ux, float uy, float uz) {
        return javafmodJNI.FMOD_System_Set3DListenerAttributes(listener, x, y, z, vx, vy, vz, fx, fy, fz, ux, uy, uz);
    }

    public static int FMOD_System_Get3DListenerAttributes(SWIGTYPE_p_FMOD_SYSTEM system, int listener, SWIGTYPE_p_FMOD_VECTOR pos, SWIGTYPE_p_FMOD_VECTOR vel, SWIGTYPE_p_FMOD_VECTOR forward, SWIGTYPE_p_FMOD_VECTOR up) {
        return javafmodJNI.FMOD_System_Get3DListenerAttributes(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), listener, SWIGTYPE_p_FMOD_VECTOR.getCPtr(pos), SWIGTYPE_p_FMOD_VECTOR.getCPtr(vel), SWIGTYPE_p_FMOD_VECTOR.getCPtr(forward), SWIGTYPE_p_FMOD_VECTOR.getCPtr(up));
    }

    public static int FMOD_System_Set3DRolloffCallback(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_3D_ROLLOFF_CALLBACK callback) {
        return javafmodJNI.FMOD_System_Set3DRolloffCallback(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_3D_ROLLOFF_CALLBACK.getCPtr(callback));
    }

    public static int FMOD_System_MixerSuspend(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_MixerSuspend(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_MixerResume(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_MixerResume(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_GetVersion(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_unsigned_int version) {
        return javafmodJNI.FMOD_System_GetVersion(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_unsigned_int.getCPtr(version));
    }

    public static int FMOD_System_GetOutputHandle(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_p_void handle) {
        return javafmodJNI.FMOD_System_GetOutputHandle(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_p_void.getCPtr(handle));
    }

    public static int FMOD_System_GetChannelsPlaying(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int channels) {
        return javafmodJNI.FMOD_System_GetChannelsPlaying(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(channels));
    }

    public static int FMOD_System_GetCPUUsage(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_float dsp, SWIGTYPE_p_float stream, SWIGTYPE_p_float geometry, SWIGTYPE_p_float update, SWIGTYPE_p_float total) {
        return javafmodJNI.FMOD_System_GetCPUUsage(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_float.getCPtr(dsp), SWIGTYPE_p_float.getCPtr(stream), SWIGTYPE_p_float.getCPtr(geometry), SWIGTYPE_p_float.getCPtr(update), SWIGTYPE_p_float.getCPtr(total));
    }

    public static long FMOD_System_CreateSound(String name_or_data, long mode) {
        return javafmodJNI.FMOD_System_CreateSound(name_or_data, mode);
    }

    public static long FMOD_System_CreateRecordSound(long deviceindex, long mode, long format, long frequency, int agcMode) {
        return javafmodJNI.FMOD_System_CreateRecordSound(deviceindex, mode, format, frequency, agcMode);
    }

    public static long FMOD_System_SetVADMode(int mode) {
        return javafmodJNI.FMOD_System_SetVADMode(mode);
    }

    public static long FMOD_System_SetRecordVolume(long vol) {
        return javafmodJNI.FMOD_System_SetRecordVolume((int)vol);
    }

    public static long FMOD_System_CreateRAWPlaySound(long mode, long format, long frequency) {
        return javafmodJNI.FMOD_System_CreateRAWPlaySound(mode, format, frequency);
    }

    public static long FMOD_System_SetRawPlayBufferingPeriod(long delay_pcm) {
        return javafmodJNI.FMOD_System_SetRawPlayBufferingPeriod(delay_pcm);
    }

    public static int FMOD_System_RAWPlayData(long sound, short[] data, long size) {
        return javafmodJNI.FMOD_System_RAWPlayData(sound, data, size);
    }

    public static int FMOD_System_RAWPlayData(long sound, byte[] data, long size) {
        short[] intdata = new short[data.length / 2];
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(intdata);
        return javafmodJNI.FMOD_System_RAWPlayData(sound, intdata, size / 2L);
    }

    public static int FMOD_Studio_LoadSampleData(long bank) {
        return javafmodJNI.FMOD_Studio_LoadSampleData(bank);
    }

    public static void FMOD_Studio_LoadEventSampleData(long event) {
        javafmodJNI.FMOD_Studio_LoadEventSampleData(event);
    }

    public static int FMOD_System_CreateStream(SWIGTYPE_p_FMOD_SYSTEM system, String name_or_data, SWIGTYPE_p_FMOD_MODE mode, SWIGTYPE_p_FMOD_CREATESOUNDEXINFO exinfo, SWIGTYPE_p_p_FMOD_SOUND sound) {
        return javafmodJNI.FMOD_System_CreateStream(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), name_or_data, SWIGTYPE_p_FMOD_MODE.getCPtr(mode), SWIGTYPE_p_FMOD_CREATESOUNDEXINFO.getCPtr(exinfo), SWIGTYPE_p_p_FMOD_SOUND.getCPtr(sound));
    }

    public static int FMOD_System_CreateDSP(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_DSP_DESCRIPTION description, SWIGTYPE_p_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_System_CreateDSP(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_DSP_DESCRIPTION.getCPtr(description), SWIGTYPE_p_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_System_CreateDSPByType(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_DSP_TYPE type, SWIGTYPE_p_p_FMOD_DSP dsp) {
        return 0;
    }

    public static long FMOD_System_CreateChannelGroup(String name) {
        try {
            return javafmodJNI.FMOD_System_CreateChannelGroup(name);
        }
        catch (Throwable t) {
            DebugLog.log("ERROR: FMOD_System_CreateChannelGroup exception:" + t.getMessage());
            return 0L;
        }
    }

    public static int FMOD_System_CreateSoundGroup(SWIGTYPE_p_FMOD_SYSTEM system, String name, SWIGTYPE_p_p_FMOD_SOUNDGROUP soundgroup) {
        return javafmodJNI.FMOD_System_CreateSoundGroup(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), name, SWIGTYPE_p_p_FMOD_SOUNDGROUP.getCPtr(soundgroup));
    }

    public static int FMOD_System_CreateReverb3D(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_p_FMOD_REVERB3D reverb) {
        return javafmodJNI.FMOD_System_CreateReverb3D(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_p_FMOD_REVERB3D.getCPtr(reverb));
    }

    public static long FMOD_System_PlaySound(long sound, boolean paused) {
        return javafmodJNI.FMOD_System_PlaySound(sound, paused ? 1L : 0L);
    }

    public static int FMOD_System_PlayDSP() {
        return javafmodJNI.FMOD_System_PlayDSP();
    }

    public static int FMOD_System_GetChannel(SWIGTYPE_p_FMOD_SYSTEM system, int channelid, SWIGTYPE_p_p_FMOD_CHANNEL channel) {
        return javafmodJNI.FMOD_System_GetChannel(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), channelid, SWIGTYPE_p_p_FMOD_CHANNEL.getCPtr(channel));
    }

    public static long FMOD_System_GetMasterChannelGroup() {
        return javafmodJNI.FMOD_System_GetMasterChannelGroup();
    }

    public static int FMOD_System_GetMasterSoundGroup(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_p_FMOD_SOUNDGROUP soundgroup) {
        return javafmodJNI.FMOD_System_GetMasterSoundGroup(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_p_FMOD_SOUNDGROUP.getCPtr(soundgroup));
    }

    public static int FMOD_System_AttachChannelGroupToPort(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_PORT_TYPE portType, SWIGTYPE_p_FMOD_PORT_INDEX portIndex, SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL passThru) {
        return javafmodJNI.FMOD_System_AttachChannelGroupToPort(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_PORT_TYPE.getCPtr(portType), SWIGTYPE_p_FMOD_PORT_INDEX.getCPtr(portIndex), SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(passThru));
    }

    public static int FMOD_System_DetachChannelGroupFromPort(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup) {
        return javafmodJNI.FMOD_System_DetachChannelGroupFromPort(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup));
    }

    public static int FMOD_System_SetReverbProperties(SWIGTYPE_p_FMOD_SYSTEM system, int instance, SWIGTYPE_p_FMOD_REVERB_PROPERTIES prop) {
        return javafmodJNI.FMOD_System_SetReverbProperties(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), instance, SWIGTYPE_p_FMOD_REVERB_PROPERTIES.getCPtr(prop));
    }

    public static int FMOD_System_GetReverbProperties(SWIGTYPE_p_FMOD_SYSTEM system, int instance, SWIGTYPE_p_FMOD_REVERB_PROPERTIES prop) {
        return javafmodJNI.FMOD_System_GetReverbProperties(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), instance, SWIGTYPE_p_FMOD_REVERB_PROPERTIES.getCPtr(prop));
    }

    public static int FMOD_System_LockDSP(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_LockDSP(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_UnlockDSP(SWIGTYPE_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_System_UnlockDSP(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_System_GetRecordNumDrivers() {
        return javafmodJNI.FMOD_System_GetRecordNumDrivers();
    }

    public static int FMOD_System_GetRecordDriverInfo(int id, FMOD_DriverInfo info) {
        return javafmodJNI.FMOD_System_GetRecordDriverInfo(id, info);
    }

    public static int FMOD_System_GetRecordPosition(int id, FMODRecordPosition data) {
        return javafmodJNI.FMOD_System_GetRecordPosition(id, data);
    }

    public static int FMOD_System_RecordStart(int id, long sound, boolean loop) {
        return javafmodJNI.FMOD_System_RecordStart(id, sound, loop);
    }

    public static int FMOD_System_RecordStop(int id) {
        return javafmodJNI.FMOD_System_RecordStop(id);
    }

    public static int FMOD_System_IsRecording(SWIGTYPE_p_FMOD_SYSTEM system, int id, SWIGTYPE_p_FMOD_BOOL recording) {
        return javafmodJNI.FMOD_System_IsRecording(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), id, SWIGTYPE_p_FMOD_BOOL.getCPtr(recording));
    }

    public static int FMOD_System_CreateGeometry(SWIGTYPE_p_FMOD_SYSTEM system, int maxpolygons, int maxvertices, SWIGTYPE_p_p_FMOD_GEOMETRY geometry) {
        return javafmodJNI.FMOD_System_CreateGeometry(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), maxpolygons, maxvertices, SWIGTYPE_p_p_FMOD_GEOMETRY.getCPtr(geometry));
    }

    public static int FMOD_System_SetGeometrySettings(SWIGTYPE_p_FMOD_SYSTEM system, float maxworldsize) {
        return javafmodJNI.FMOD_System_SetGeometrySettings(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), maxworldsize);
    }

    public static int FMOD_System_GetGeometrySettings(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_float maxworldsize) {
        return javafmodJNI.FMOD_System_GetGeometrySettings(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_float.getCPtr(maxworldsize));
    }

    public static int FMOD_System_LoadGeometry(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_void data, int datasize, SWIGTYPE_p_p_FMOD_GEOMETRY geometry) {
        return javafmodJNI.FMOD_System_LoadGeometry(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_void.getCPtr(data), datasize, SWIGTYPE_p_p_FMOD_GEOMETRY.getCPtr(geometry));
    }

    public static int FMOD_System_GetGeometryOcclusion(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_FMOD_VECTOR listener, SWIGTYPE_p_FMOD_VECTOR source2, SWIGTYPE_p_float direct, SWIGTYPE_p_float reverb) {
        return javafmodJNI.FMOD_System_GetGeometryOcclusion(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_FMOD_VECTOR.getCPtr(listener), SWIGTYPE_p_FMOD_VECTOR.getCPtr(source2), SWIGTYPE_p_float.getCPtr(direct), SWIGTYPE_p_float.getCPtr(reverb));
    }

    public static int FMOD_System_SetNetworkProxy(SWIGTYPE_p_FMOD_SYSTEM system, String proxy) {
        return javafmodJNI.FMOD_System_SetNetworkProxy(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), proxy);
    }

    public static int FMOD_System_GetNetworkProxy(SWIGTYPE_p_FMOD_SYSTEM system, String proxy, int proxylen) {
        return javafmodJNI.FMOD_System_GetNetworkProxy(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), proxy, proxylen);
    }

    public static int FMOD_System_SetNetworkTimeout(SWIGTYPE_p_FMOD_SYSTEM system, int timeout2) {
        return javafmodJNI.FMOD_System_SetNetworkTimeout(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), timeout2);
    }

    public static int FMOD_System_GetNetworkTimeout(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_int timeout2) {
        return javafmodJNI.FMOD_System_GetNetworkTimeout(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_int.getCPtr(timeout2));
    }

    public static int FMOD_System_SetUserData(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_System_SetUserData(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_System_GetUserData(SWIGTYPE_p_FMOD_SYSTEM system, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_System_GetUserData(SWIGTYPE_p_FMOD_SYSTEM.getCPtr(system), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_Sound_Release(long sound) {
        return javafmodJNI.FMOD_Sound_Release(sound);
    }

    public static int FMOD_RAWPlaySound_Release(long sound) {
        return javafmodJNI.FMOD_RAWPlaySound_Release(sound);
    }

    public static int FMOD_RecordSound_Release(long sound) {
        return javafmodJNI.FMOD_RecordSound_Release(sound);
    }

    public static int FMOD_Sound_GetSystemObject(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_Sound_GetSystemObject(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_Sound_Lock(long sound, long offset, long length, byte[] ptr1, byte[] ptr2, Long size1, Long size2, long[] data) {
        return javafmodJNI.FMOD_Sound_Lock(sound, offset, length, ptr1, ptr2, size1, size2, data);
    }

    public static int FMOD_Sound_GetData(long sound, byte[] ptr, FMODSoundData data) {
        return javafmodJNI.FMOD_Sound_GetData(sound, ptr, data);
    }

    public static int FMOD_Sound_Unlock(long sound, long[] data) {
        return javafmodJNI.FMOD_Sound_Unlock(sound, data);
    }

    public static int FMOD_Sound_SetDefaults(SWIGTYPE_p_FMOD_SOUND sound, float frequency, int priority) {
        return javafmodJNI.FMOD_Sound_SetDefaults(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), frequency, priority);
    }

    public static int FMOD_Sound_GetDefaults(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_float frequency, SWIGTYPE_p_int priority) {
        return javafmodJNI.FMOD_Sound_GetDefaults(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_float.getCPtr(frequency), SWIGTYPE_p_int.getCPtr(priority));
    }

    public static int FMOD_Sound_Set3DMinMaxDistance(long sound, float min, float max) {
        return javafmodJNI.FMOD_Sound_Set3DMinMaxDistance(sound, min, max);
    }

    public static int FMOD_Sound_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_float min, SWIGTYPE_p_float max) {
        return javafmodJNI.FMOD_Sound_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_float.getCPtr(min), SWIGTYPE_p_float.getCPtr(max));
    }

    public static long FMOD_Studio_System_Create() {
        return javafmodJNI.FMOD_Studio_Create();
    }

    public static void FMOD_Studio_StartEvent(long event) {
        javafmodJNI.FMOD_Studio_StartEvent(event);
    }

    public static long FMOD_Studio_GetTimelinePosition(long inst) {
        return javafmodJNI.FMOD_Studio_GetTimelinePosition(inst);
    }

    public static long FMOD_Studio_System_GetBus(String path) {
        return javafmodJNI.FMOD_Studio_System_GetBus(path);
    }

    public static long FMOD_Studio_System_GetEvent(String path) {
        if (map.containsKey(path)) {
            return map.get(path);
        }
        long l = javafmodJNI.FMOD_Studio_GetEvent(path);
        map.put(path, l);
        return l;
    }

    public static long FMOD_Studio_System_CreateEventInstance(long desc) {
        return javafmodJNI.FMOD_Studio_CreateEventInstance(desc);
    }

    public static long FMOD_Studio_System_LoadBankFile(String file) {
        return javafmodJNI.FMOD_Studio_LoadBankFile(file);
    }

    public static int FMOD_Studio_System_SetParameterByID(FMOD_STUDIO_PARAMETER_ID id, float value, boolean ignoreseekspeed) {
        if (id == null) {
            return 0;
        }
        return javafmodJNI.FMOD_Studio_System_SetParameterByID(id.address(), value, ignoreseekspeed);
    }

    public static int FMOD_Studio_System_SetParameterByName(String name, float value, boolean ignoreseekspeed) {
        return javafmodJNI.FMOD_Studio_System_SetParameterByName(name, value, ignoreseekspeed);
    }

    public static boolean FMOD_Studio_Bus_GetMute(long bus) {
        if (bus == 0L) {
            return false;
        }
        return javafmodJNI.FMOD_Studio_Bus_GetMute(bus);
    }

    public static boolean FMOD_Studio_Bus_GetPaused(long bus) {
        if (bus == 0L) {
            return false;
        }
        return javafmodJNI.FMOD_Studio_Bus_GetMute(bus);
    }

    public static float FMOD_Studio_Bus_GetVolume(long bus) {
        if (bus == 0L) {
            return 0.0f;
        }
        return javafmodJNI.FMOD_Studio_Bus_GetVolume(bus);
    }

    public static int FMOD_Studio_Bus_SetMute(long bus, boolean mute) {
        if (bus == 0L) {
            return -1;
        }
        return javafmodJNI.FMOD_Studio_Bus_SetMute(bus, mute);
    }

    public static int FMOD_Studio_Bus_SetPaused(long bus, boolean paused) {
        if (bus == 0L) {
            return -1;
        }
        return javafmodJNI.FMOD_Studio_Bus_SetPaused(bus, paused);
    }

    public static int FMOD_Studio_Bus_SetVolume(long bus, float volume) {
        if (bus == 0L) {
            return -1;
        }
        return javafmodJNI.FMOD_Studio_Bus_SetVolume(bus, volume);
    }

    public static int FMOD_Studio_Bus_StopAllEvents(long bus, boolean immediate) {
        if (bus == 0L) {
            return -1;
        }
        return javafmodJNI.FMOD_Studio_Bus_StopAllEvents(bus, immediate);
    }

    public static int FMOD_Sound_Set3DConeSettings(SWIGTYPE_p_FMOD_SOUND sound, float insideconeangle, float outsideconeangle, float outsidevolume) {
        return javafmodJNI.FMOD_Sound_Set3DConeSettings(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), insideconeangle, outsideconeangle, outsidevolume);
    }

    public static int FMOD_Sound_Get3DConeSettings(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_float insideconeangle, SWIGTYPE_p_float outsideconeangle, SWIGTYPE_p_float outsidevolume) {
        return javafmodJNI.FMOD_Sound_Get3DConeSettings(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_float.getCPtr(insideconeangle), SWIGTYPE_p_float.getCPtr(outsideconeangle), SWIGTYPE_p_float.getCPtr(outsidevolume));
    }

    public static int FMOD_Sound_Set3DCustomRolloff(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_VECTOR points, int numpoints) {
        return javafmodJNI.FMOD_Sound_Set3DCustomRolloff(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_VECTOR.getCPtr(points), numpoints);
    }

    public static int FMOD_Sound_Get3DCustomRolloff(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_p_FMOD_VECTOR points, SWIGTYPE_p_int numpoints) {
        return javafmodJNI.FMOD_Sound_Get3DCustomRolloff(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_p_FMOD_VECTOR.getCPtr(points), SWIGTYPE_p_int.getCPtr(numpoints));
    }

    public static int FMOD_Sound_SetSubSound(SWIGTYPE_p_FMOD_SOUND sound, int index, SWIGTYPE_p_FMOD_SOUND subsound) {
        return javafmodJNI.FMOD_Sound_SetSubSound(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), index, SWIGTYPE_p_FMOD_SOUND.getCPtr(subsound));
    }

    public static int FMOD_Sound_GetSubSound(SWIGTYPE_p_FMOD_SOUND sound, int index, SWIGTYPE_p_p_FMOD_SOUND subsound) {
        return javafmodJNI.FMOD_Sound_GetSubSound(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), index, SWIGTYPE_p_p_FMOD_SOUND.getCPtr(subsound));
    }

    public static int FMOD_Sound_GetSubSoundParent(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_p_FMOD_SOUND parentsound) {
        return javafmodJNI.FMOD_Sound_GetSubSoundParent(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_p_FMOD_SOUND.getCPtr(parentsound));
    }

    public static int FMOD_Sound_GetName(SWIGTYPE_p_FMOD_SOUND sound, String name, int namelen) {
        return javafmodJNI.FMOD_Sound_GetName(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), name, namelen);
    }

    public static long FMOD_Sound_GetLength(long sound, long timeunit) {
        return javafmodJNI.FMOD_Sound_GetLength(sound, timeunit);
    }

    public static int FMOD_Sound_GetFormat(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_SOUND_TYPE type, SWIGTYPE_p_FMOD_SOUND_FORMAT format, SWIGTYPE_p_int channels, SWIGTYPE_p_int bits) {
        return javafmodJNI.FMOD_Sound_GetFormat(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_SOUND_TYPE.getCPtr(type), SWIGTYPE_p_FMOD_SOUND_FORMAT.getCPtr(format), SWIGTYPE_p_int.getCPtr(channels), SWIGTYPE_p_int.getCPtr(bits));
    }

    public static int FMOD_Sound_GetNumSubSounds(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_int numsubsounds) {
        return javafmodJNI.FMOD_Sound_GetNumSubSounds(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_int.getCPtr(numsubsounds));
    }

    public static int FMOD_Sound_GetNumTags(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_int numtags, SWIGTYPE_p_int numtagsupdated) {
        return javafmodJNI.FMOD_Sound_GetNumTags(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_int.getCPtr(numtags), SWIGTYPE_p_int.getCPtr(numtagsupdated));
    }

    public static int FMOD_Sound_GetTag(SWIGTYPE_p_FMOD_SOUND sound, String name, int index, SWIGTYPE_p_FMOD_TAG tag) {
        return javafmodJNI.FMOD_Sound_GetTag(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), name, index, SWIGTYPE_p_FMOD_TAG.getCPtr(tag));
    }

    public static int FMOD_Sound_GetOpenState(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_OPENSTATE openstate, SWIGTYPE_p_unsigned_int percentbuffered, SWIGTYPE_p_FMOD_BOOL starving, SWIGTYPE_p_FMOD_BOOL diskbusy) {
        return javafmodJNI.FMOD_Sound_GetOpenState(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_OPENSTATE.getCPtr(openstate), SWIGTYPE_p_unsigned_int.getCPtr(percentbuffered), SWIGTYPE_p_FMOD_BOOL.getCPtr(starving), SWIGTYPE_p_FMOD_BOOL.getCPtr(diskbusy));
    }

    public static int FMOD_Sound_ReadData(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_void buffer, long lenbytes, SWIGTYPE_p_unsigned_int read) {
        return javafmodJNI.FMOD_Sound_ReadData(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_void.getCPtr(buffer), lenbytes, SWIGTYPE_p_unsigned_int.getCPtr(read));
    }

    public static int FMOD_Sound_SeekData(SWIGTYPE_p_FMOD_SOUND sound, long pcm) {
        return javafmodJNI.FMOD_Sound_SeekData(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), pcm);
    }

    public static int FMOD_Sound_SetSoundGroup(long sound, long soundgroup) {
        return javafmodJNI.FMOD_Sound_SetSoundGroup(sound, soundgroup);
    }

    public static int FMOD_Sound_GetSoundGroup(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_p_FMOD_SOUNDGROUP soundgroup) {
        return javafmodJNI.FMOD_Sound_GetSoundGroup(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_p_FMOD_SOUNDGROUP.getCPtr(soundgroup));
    }

    public static int FMOD_Sound_GetNumSyncPoints(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_int numsyncpoints) {
        return javafmodJNI.FMOD_Sound_GetNumSyncPoints(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_int.getCPtr(numsyncpoints));
    }

    public static int FMOD_Sound_GetSyncPoint(SWIGTYPE_p_FMOD_SOUND sound, int index, SWIGTYPE_p_p_FMOD_SYNCPOINT point) {
        return javafmodJNI.FMOD_Sound_GetSyncPoint(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), index, SWIGTYPE_p_p_FMOD_SYNCPOINT.getCPtr(point));
    }

    public static int FMOD_Sound_GetSyncPointInfo(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_SYNCPOINT point, String name, int namelen, SWIGTYPE_p_unsigned_int offset, SWIGTYPE_p_FMOD_TIMEUNIT offsettype) {
        return javafmodJNI.FMOD_Sound_GetSyncPointInfo(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_SYNCPOINT.getCPtr(point), name, namelen, SWIGTYPE_p_unsigned_int.getCPtr(offset), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(offsettype));
    }

    public static int FMOD_Sound_AddSyncPoint(SWIGTYPE_p_FMOD_SOUND sound, long offset, SWIGTYPE_p_FMOD_TIMEUNIT offsettype, String name, SWIGTYPE_p_p_FMOD_SYNCPOINT point) {
        return javafmodJNI.FMOD_Sound_AddSyncPoint(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), offset, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(offsettype), name, SWIGTYPE_p_p_FMOD_SYNCPOINT.getCPtr(point));
    }

    public static int FMOD_Sound_DeleteSyncPoint(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_SYNCPOINT point) {
        return javafmodJNI.FMOD_Sound_DeleteSyncPoint(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_SYNCPOINT.getCPtr(point));
    }

    public static int FMOD_Sound_SetMode(long sound, int mode) {
        return javafmodJNI.FMOD_Sound_SetMode(sound, mode);
    }

    public static int FMOD_Sound_GetMode(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_FMOD_MODE mode) {
        return javafmodJNI.FMOD_Sound_GetMode(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_FMOD_MODE.getCPtr(mode));
    }

    public static int FMOD_Sound_SetLoopCount(long sound, int loopcount) {
        return javafmodJNI.FMOD_Sound_SetLoopCount(sound, loopcount);
    }

    public static int FMOD_Sound_GetLoopCount(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_int loopcount) {
        return javafmodJNI.FMOD_Sound_GetLoopCount(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_int.getCPtr(loopcount));
    }

    public static int FMOD_Sound_SetLoopPoints(SWIGTYPE_p_FMOD_SOUND sound, long loopstart, SWIGTYPE_p_FMOD_TIMEUNIT loopstarttype, long loopend, SWIGTYPE_p_FMOD_TIMEUNIT loopendtype) {
        return javafmodJNI.FMOD_Sound_SetLoopPoints(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), loopstart, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopstarttype), loopend, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopendtype));
    }

    public static int FMOD_Sound_GetLoopPoints(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_unsigned_int loopstart, SWIGTYPE_p_FMOD_TIMEUNIT loopstarttype, SWIGTYPE_p_unsigned_int loopend, SWIGTYPE_p_FMOD_TIMEUNIT loopendtype) {
        return javafmodJNI.FMOD_Sound_GetLoopPoints(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_unsigned_int.getCPtr(loopstart), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopstarttype), SWIGTYPE_p_unsigned_int.getCPtr(loopend), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopendtype));
    }

    public static int FMOD_Sound_GetMusicNumChannels(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_int numchannels) {
        return javafmodJNI.FMOD_Sound_GetMusicNumChannels(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_int.getCPtr(numchannels));
    }

    public static int FMOD_Sound_SetMusicChannelVolume(SWIGTYPE_p_FMOD_SOUND sound, int channel, float volume) {
        return javafmodJNI.FMOD_Sound_SetMusicChannelVolume(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), channel, volume);
    }

    public static int FMOD_Sound_GetMusicChannelVolume(SWIGTYPE_p_FMOD_SOUND sound, int channel, SWIGTYPE_p_float volume) {
        return javafmodJNI.FMOD_Sound_GetMusicChannelVolume(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), channel, SWIGTYPE_p_float.getCPtr(volume));
    }

    public static int FMOD_Sound_SetMusicSpeed(SWIGTYPE_p_FMOD_SOUND sound, float speed) {
        return javafmodJNI.FMOD_Sound_SetMusicSpeed(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), speed);
    }

    public static int FMOD_Sound_GetMusicSpeed(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_float speed) {
        return javafmodJNI.FMOD_Sound_GetMusicSpeed(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_float.getCPtr(speed));
    }

    public static int FMOD_Sound_SetUserData(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_Sound_SetUserData(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_Sound_GetUserData(SWIGTYPE_p_FMOD_SOUND sound, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_Sound_GetUserData(SWIGTYPE_p_FMOD_SOUND.getCPtr(sound), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_Channel_GetSystemObject(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_Channel_GetSystemObject(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_Channel_Stop(long channel) {
        return javafmodJNI.FMOD_Channel_Stop(channel);
    }

    public static int FMOD_Channel_SetPaused(long channel, boolean paused) {
        return javafmodJNI.FMOD_Channel_SetPaused(channel, paused ? 1L : 0L);
    }

    public static int FMOD_Channel_GetPaused(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL paused) {
        return javafmodJNI.FMOD_Channel_GetPaused(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(paused));
    }

    public static int FMOD_Channel_SetVolume(long channel, float volume) {
        return javafmodJNI.FMOD_Channel_SetVolume(channel, volume);
    }

    public static int FMOD_Channel_GetVolume(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float volume) {
        return javafmodJNI.FMOD_Channel_GetVolume(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(volume));
    }

    public static int FMOD_Channel_SetVolumeRamp(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL ramp) {
        return javafmodJNI.FMOD_Channel_SetVolumeRamp(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(ramp));
    }

    public static int FMOD_Channel_GetVolumeRamp(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL ramp) {
        return javafmodJNI.FMOD_Channel_GetVolumeRamp(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(ramp));
    }

    public static float FMOD_Channel_GetAudibility(long channel) {
        return javafmodJNI.FMOD_Channel_GetAudibility(channel);
    }

    public static int FMOD_Channel_SetPitch(long channel, float pitch) {
        return javafmodJNI.FMOD_Channel_SetPitch(channel, pitch);
    }

    public static int FMOD_Channel_SetPitch(SWIGTYPE_p_FMOD_CHANNEL channel, float pitch) {
        return javafmodJNI.FMOD_Channel_SetPitch(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), pitch);
    }

    public static int FMOD_Channel_GetPitch(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float pitch) {
        return javafmodJNI.FMOD_Channel_GetPitch(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(pitch));
    }

    public static int FMOD_Channel_SetMute(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL mute) {
        return javafmodJNI.FMOD_Channel_SetMute(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(mute));
    }

    public static int FMOD_Channel_GetMute(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL mute) {
        return javafmodJNI.FMOD_Channel_GetMute(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(mute));
    }

    public static int FMOD_Channel_SetReverbProperties(long channel, int instance, float wet) {
        return javafmodJNI.FMOD_Channel_SetReverbProperties(channel, instance, wet);
    }

    public static int FMOD_Channel_GetReverbProperties(SWIGTYPE_p_FMOD_CHANNEL channel, int instance, SWIGTYPE_p_float wet) {
        return javafmodJNI.FMOD_Channel_GetReverbProperties(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), instance, SWIGTYPE_p_float.getCPtr(wet));
    }

    public static int FMOD_Channel_SetLowPassGain(long channel, float gain) {
        return javafmodJNI.FMOD_Channel_SetLowPassGain(channel, gain);
    }

    public static int FMOD_Channel_GetLowPassGain(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float gain) {
        return javafmodJNI.FMOD_Channel_GetLowPassGain(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(gain));
    }

    public static int FMOD_Channel_SetMode(long channel, long mode) {
        return javafmodJNI.FMOD_Channel_SetMode(channel, mode);
    }

    public static int FMOD_Channel_GetMode(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_MODE mode) {
        return javafmodJNI.FMOD_Channel_GetMode(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_MODE.getCPtr(mode));
    }

    public static int FMOD_Channel_SetCallback(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_CHANNELCONTROL_CALLBACK callback) {
        return javafmodJNI.FMOD_Channel_SetCallback(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_CHANNELCONTROL_CALLBACK.getCPtr(callback));
    }

    public static boolean FMOD_Channel_IsPlaying(long channel) {
        return javafmodJNI.FMOD_Channel_IsPlaying(channel);
    }

    public static int FMOD_Channel_SetPan(SWIGTYPE_p_FMOD_CHANNEL channel, float pan) {
        return javafmodJNI.FMOD_Channel_SetPan(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), pan);
    }

    public static int FMOD_Channel_SetMixLevelsOutput(SWIGTYPE_p_FMOD_CHANNEL channel, float frontleft, float frontright, float center, float lfe, float surroundleft, float surroundright, float backleft, float backright) {
        return javafmodJNI.FMOD_Channel_SetMixLevelsOutput(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), frontleft, frontright, center, lfe, surroundleft, surroundright, backleft, backright);
    }

    public static int FMOD_Channel_SetMixLevelsInput(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float levels, int numlevels) {
        return javafmodJNI.FMOD_Channel_SetMixLevelsInput(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(levels), numlevels);
    }

    public static int FMOD_Channel_SetMixMatrix(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float matrix, int outchannels, int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_Channel_SetMixMatrix(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(matrix), outchannels, inchannels, inchannel_hop);
    }

    public static int FMOD_Channel_GetMixMatrix(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float matrix, SWIGTYPE_p_int outchannels, SWIGTYPE_p_int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_Channel_GetMixMatrix(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(matrix), SWIGTYPE_p_int.getCPtr(outchannels), SWIGTYPE_p_int.getCPtr(inchannels), inchannel_hop);
    }

    public static int FMOD_Channel_GetDSPClock(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_unsigned_long_long dspclock, SWIGTYPE_p_unsigned_long_long parentclock) {
        return javafmodJNI.FMOD_Channel_GetDSPClock(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock), SWIGTYPE_p_unsigned_long_long.getCPtr(parentclock));
    }

    public static int FMOD_Channel_SetDelay(SWIGTYPE_p_FMOD_CHANNEL channel, BigInteger dspclock_start, BigInteger dspclock_end, SWIGTYPE_p_FMOD_BOOL stopchannels) {
        return javafmodJNI.FMOD_Channel_SetDelay(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), dspclock_start, dspclock_end, SWIGTYPE_p_FMOD_BOOL.getCPtr(stopchannels));
    }

    public static int FMOD_Channel_GetDelay(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_unsigned_long_long dspclock_start, SWIGTYPE_p_unsigned_long_long dspclock_end, SWIGTYPE_p_FMOD_BOOL stopchannels) {
        return javafmodJNI.FMOD_Channel_GetDelay(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock_start), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock_end), SWIGTYPE_p_FMOD_BOOL.getCPtr(stopchannels));
    }

    public static int FMOD_Channel_AddFadePoint(SWIGTYPE_p_FMOD_CHANNEL channel, BigInteger dspclock, float volume) {
        return javafmodJNI.FMOD_Channel_AddFadePoint(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), dspclock, volume);
    }

    public static int FMOD_Channel_RemoveFadePoints(SWIGTYPE_p_FMOD_CHANNEL channel, BigInteger dspclock_start, BigInteger dspclock_end) {
        return javafmodJNI.FMOD_Channel_RemoveFadePoints(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), dspclock_start, dspclock_end);
    }

    public static int FMOD_Channel_GetFadePoints(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_unsigned_int numpoints, SWIGTYPE_p_unsigned_long_long point_dspclock, SWIGTYPE_p_float point_volume) {
        return javafmodJNI.FMOD_Channel_GetFadePoints(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_unsigned_int.getCPtr(numpoints), SWIGTYPE_p_unsigned_long_long.getCPtr(point_dspclock), SWIGTYPE_p_float.getCPtr(point_volume));
    }

    public static int FMOD_Channel_GetDSP(SWIGTYPE_p_FMOD_CHANNEL channel, int index, SWIGTYPE_p_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_Channel_GetDSP(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), index, SWIGTYPE_p_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_Channel_AddDSP(SWIGTYPE_p_FMOD_CHANNEL channel, int index, SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_Channel_AddDSP(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), index, SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_Channel_RemoveDSP(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_Channel_RemoveDSP(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_Channel_GetNumDSPs(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_int numdsps) {
        return 0;
    }

    public static int FMOD_Channel_SetDSPIndex(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_DSP dsp, int index) {
        return javafmodJNI.FMOD_Channel_SetDSPIndex(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index);
    }

    public static int FMOD_Channel_GetDSPIndex(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_int index) {
        return javafmodJNI.FMOD_Channel_GetDSPIndex(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_int.getCPtr(index));
    }

    public static int FMOD_Channel_OverridePanDSP(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_DSP pan) {
        return javafmodJNI.FMOD_Channel_OverridePanDSP(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_DSP.getCPtr(pan));
    }

    public static int FMOD_Channel_Set3DAttributes(long channel, float x, float y, float z, float vx, float vy, float vz) {
        return javafmodJNI.FMOD_Channel_Set3DAttributes(channel, x, y, z, vx, vy, vz);
    }

    public static int FMOD_Channel_Get3DAttributes(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_VECTOR pos, SWIGTYPE_p_FMOD_VECTOR vel) {
        return javafmodJNI.FMOD_Channel_Get3DAttributes(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_VECTOR.getCPtr(pos), SWIGTYPE_p_FMOD_VECTOR.getCPtr(vel));
    }

    public static int FMOD_Channel_Set3DMinMaxDistance(long channel, float mindistance, float maxdistance) {
        return javafmodJNI.FMOD_Channel_Set3DMinMaxDistance(channel, mindistance, maxdistance);
    }

    public static int FMOD_Channel_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float mindistance, SWIGTYPE_p_float maxdistance) {
        return javafmodJNI.FMOD_Channel_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(mindistance), SWIGTYPE_p_float.getCPtr(maxdistance));
    }

    public static int FMOD_Channel_Set3DConeSettings(SWIGTYPE_p_FMOD_CHANNEL channel, float insideconeangle, float outsideconeangle, float outsidevolume) {
        return javafmodJNI.FMOD_Channel_Set3DConeSettings(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), insideconeangle, outsideconeangle, outsidevolume);
    }

    public static int FMOD_Channel_Get3DConeSettings(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float insideconeangle, SWIGTYPE_p_float outsideconeangle, SWIGTYPE_p_float outsidevolume) {
        return javafmodJNI.FMOD_Channel_Get3DConeSettings(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(insideconeangle), SWIGTYPE_p_float.getCPtr(outsideconeangle), SWIGTYPE_p_float.getCPtr(outsidevolume));
    }

    public static int FMOD_Channel_Set3DConeOrientation(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_VECTOR orientation) {
        return javafmodJNI.FMOD_Channel_Set3DConeOrientation(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_VECTOR.getCPtr(orientation));
    }

    public static int FMOD_Channel_Get3DConeOrientation(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_VECTOR orientation) {
        return javafmodJNI.FMOD_Channel_Get3DConeOrientation(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_VECTOR.getCPtr(orientation));
    }

    public static int FMOD_Channel_Set3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_VECTOR points, int numpoints) {
        return javafmodJNI.FMOD_Channel_Set3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_VECTOR.getCPtr(points), numpoints);
    }

    public static int FMOD_Channel_Get3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_p_FMOD_VECTOR points, SWIGTYPE_p_int numpoints) {
        return javafmodJNI.FMOD_Channel_Get3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_p_FMOD_VECTOR.getCPtr(points), SWIGTYPE_p_int.getCPtr(numpoints));
    }

    public static int FMOD_Channel_Set3DOcclusion(long channel, float directocclusion, float reverbocclusion) {
        return javafmodJNI.FMOD_Channel_Set3DOcclusion(channel, directocclusion, reverbocclusion);
    }

    public static int FMOD_Channel_Get3DOcclusion(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float directocclusion, SWIGTYPE_p_float reverbocclusion) {
        return javafmodJNI.FMOD_Channel_Get3DOcclusion(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(directocclusion), SWIGTYPE_p_float.getCPtr(reverbocclusion));
    }

    public static int FMOD_Channel_Set3DSpread(SWIGTYPE_p_FMOD_CHANNEL channel, float angle) {
        return javafmodJNI.FMOD_Channel_Set3DSpread(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), angle);
    }

    public static int FMOD_Channel_Get3DSpread(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float angle) {
        return javafmodJNI.FMOD_Channel_Get3DSpread(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(angle));
    }

    public static int FMOD_Channel_Set3DLevel(SWIGTYPE_p_FMOD_CHANNEL channel, float level) {
        return javafmodJNI.FMOD_Channel_Set3DLevel(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), level);
    }

    public static int FMOD_Channel_Get3DLevel(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float level) {
        return javafmodJNI.FMOD_Channel_Get3DLevel(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(level));
    }

    public static int FMOD_Channel_Set3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNEL channel, float level) {
        return javafmodJNI.FMOD_Channel_Set3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), level);
    }

    public static int FMOD_Channel_Get3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float level) {
        return javafmodJNI.FMOD_Channel_Get3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(level));
    }

    public static int FMOD_Channel_Set3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL custom, float customLevel, float centerFreq) {
        return javafmodJNI.FMOD_Channel_Set3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(custom), customLevel, centerFreq);
    }

    public static int FMOD_Channel_Get3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_FMOD_BOOL custom, SWIGTYPE_p_float customLevel, SWIGTYPE_p_float centerFreq) {
        return javafmodJNI.FMOD_Channel_Get3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_FMOD_BOOL.getCPtr(custom), SWIGTYPE_p_float.getCPtr(customLevel), SWIGTYPE_p_float.getCPtr(centerFreq));
    }

    public static int FMOD_Channel_SetUserData(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_Channel_SetUserData(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_Channel_GetUserData(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_Channel_GetUserData(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_Channel_SetFrequency(SWIGTYPE_p_FMOD_CHANNEL channel, float frequency) {
        return javafmodJNI.FMOD_Channel_SetFrequency(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), frequency);
    }

    public static int FMOD_Channel_GetFrequency(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_float frequency) {
        return javafmodJNI.FMOD_Channel_GetFrequency(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_float.getCPtr(frequency));
    }

    public static int FMOD_Channel_SetPriority(long channel, int priority) {
        return javafmodJNI.FMOD_Channel_SetPriority(channel, priority);
    }

    public static int FMOD_Channel_GetPriority(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_int priority) {
        return javafmodJNI.FMOD_Channel_GetPriority(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_int.getCPtr(priority));
    }

    public static int FMOD_Channel_SetPosition(long channel, long position) {
        return javafmodJNI.FMOD_Channel_SetPosition(channel, position);
    }

    public static long FMOD_Channel_GetPosition(long channel, int postype) {
        return javafmodJNI.FMOD_Channel_GetPosition(channel, postype);
    }

    public static int FMOD_Channel_SetChannelGroup(long channel, long channelgroup) {
        return javafmodJNI.FMOD_Channel_SetChannelGroup(channel, channelgroup);
    }

    public static int FMOD_Channel_GetChannelGroup(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_p_FMOD_CHANNELGROUP channelgroup) {
        return javafmodJNI.FMOD_Channel_GetChannelGroup(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_p_FMOD_CHANNELGROUP.getCPtr(channelgroup));
    }

    public static int FMOD_Channel_SetLoopCount(long channel, int loopcount) {
        return javafmodJNI.FMOD_Channel_SetLoopCount(channel, loopcount);
    }

    public static int FMOD_Channel_GetLoopCount(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_int loopcount) {
        return javafmodJNI.FMOD_Channel_GetLoopCount(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_int.getCPtr(loopcount));
    }

    public static int FMOD_Channel_SetLoopPoints(SWIGTYPE_p_FMOD_CHANNEL channel, long loopstart, SWIGTYPE_p_FMOD_TIMEUNIT loopstarttype, long loopend, SWIGTYPE_p_FMOD_TIMEUNIT loopendtype) {
        return javafmodJNI.FMOD_Channel_SetLoopPoints(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), loopstart, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopstarttype), loopend, SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopendtype));
    }

    public static int FMOD_Channel_GetLoopPoints(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_unsigned_int loopstart, SWIGTYPE_p_FMOD_TIMEUNIT loopstarttype, SWIGTYPE_p_unsigned_int loopend, SWIGTYPE_p_FMOD_TIMEUNIT loopendtype) {
        return javafmodJNI.FMOD_Channel_GetLoopPoints(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_unsigned_int.getCPtr(loopstart), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopstarttype), SWIGTYPE_p_unsigned_int.getCPtr(loopend), SWIGTYPE_p_FMOD_TIMEUNIT.getCPtr(loopendtype));
    }

    public static boolean FMOD_Channel_IsVirtual(long channel) {
        return javafmodJNI.FMOD_Channel_IsVirtual(channel);
    }

    public static int FMOD_Channel_GetCurrentSound(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_p_FMOD_SOUND sound) {
        return javafmodJNI.FMOD_Channel_GetCurrentSound(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_p_FMOD_SOUND.getCPtr(sound));
    }

    public static int FMOD_Channel_GetIndex(SWIGTYPE_p_FMOD_CHANNEL channel, SWIGTYPE_p_int index) {
        return javafmodJNI.FMOD_Channel_GetIndex(SWIGTYPE_p_FMOD_CHANNEL.getCPtr(channel), SWIGTYPE_p_int.getCPtr(index));
    }

    public static int FMOD_ChannelGroup_GetSystemObject(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_ChannelGroup_GetSystemObject(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_ChannelGroup_Stop(long channelgroup) {
        return javafmodJNI.FMOD_ChannelGroup_Stop(channelgroup);
    }

    public static int FMOD_ChannelGroup_SetPaused(long channelgroup, boolean paused) {
        return javafmodJNI.FMOD_ChannelGroup_SetPaused(channelgroup, paused);
    }

    public static int FMOD_ChannelGroup_GetPaused(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL paused) {
        return javafmodJNI.FMOD_ChannelGroup_GetPaused(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(paused));
    }

    public static int FMOD_ChannelGroup_SetVolume(long channelgroup, float volume) {
        return javafmodJNI.FMOD_ChannelGroup_SetVolume(channelgroup, volume);
    }

    public static int FMOD_ChannelGroup_GetVolume(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float volume) {
        return javafmodJNI.FMOD_ChannelGroup_GetVolume(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(volume));
    }

    public static int FMOD_ChannelGroup_SetVolumeRamp(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL ramp) {
        return javafmodJNI.FMOD_ChannelGroup_SetVolumeRamp(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(ramp));
    }

    public static int FMOD_ChannelGroup_GetVolumeRamp(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL ramp) {
        return javafmodJNI.FMOD_ChannelGroup_GetVolumeRamp(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(ramp));
    }

    public static int FMOD_ChannelGroup_GetAudibility(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float audibility) {
        return javafmodJNI.FMOD_ChannelGroup_GetAudibility(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(audibility));
    }

    public static int FMOD_ChannelGroup_SetPitch(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float pitch) {
        return javafmodJNI.FMOD_ChannelGroup_SetPitch(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), pitch);
    }

    public static int FMOD_ChannelGroup_GetPitch(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float pitch) {
        return javafmodJNI.FMOD_ChannelGroup_GetPitch(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(pitch));
    }

    public static int FMOD_ChannelGroup_SetMute(long channelgroup, boolean mute) {
        return javafmodJNI.FMOD_ChannelGroup_SetMute(channelgroup, mute);
    }

    public static int FMOD_ChannelGroup_GetMute(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL mute) {
        return javafmodJNI.FMOD_ChannelGroup_GetMute(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(mute));
    }

    public static int FMOD_ChannelGroup_SetReverbProperties(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int instance, float wet) {
        return javafmodJNI.FMOD_ChannelGroup_SetReverbProperties(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), instance, wet);
    }

    public static int FMOD_ChannelGroup_GetReverbProperties(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int instance, SWIGTYPE_p_float wet) {
        return javafmodJNI.FMOD_ChannelGroup_GetReverbProperties(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), instance, SWIGTYPE_p_float.getCPtr(wet));
    }

    public static int FMOD_ChannelGroup_SetLowPassGain(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float gain) {
        return javafmodJNI.FMOD_ChannelGroup_SetLowPassGain(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), gain);
    }

    public static int FMOD_ChannelGroup_GetLowPassGain(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float gain) {
        return javafmodJNI.FMOD_ChannelGroup_GetLowPassGain(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(gain));
    }

    public static int FMOD_ChannelGroup_SetMode(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_MODE mode) {
        return javafmodJNI.FMOD_ChannelGroup_SetMode(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_MODE.getCPtr(mode));
    }

    public static int FMOD_ChannelGroup_GetMode(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_MODE mode) {
        return javafmodJNI.FMOD_ChannelGroup_GetMode(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_MODE.getCPtr(mode));
    }

    public static int FMOD_ChannelGroup_SetCallback(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_CHANNELCONTROL_CALLBACK callback) {
        return javafmodJNI.FMOD_ChannelGroup_SetCallback(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_CHANNELCONTROL_CALLBACK.getCPtr(callback));
    }

    public static int FMOD_ChannelGroup_IsPlaying(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL isplaying) {
        return javafmodJNI.FMOD_ChannelGroup_IsPlaying(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(isplaying));
    }

    public static int FMOD_ChannelGroup_SetPan(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float pan) {
        return javafmodJNI.FMOD_ChannelGroup_SetPan(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), pan);
    }

    public static int FMOD_ChannelGroup_SetMixLevelsOutput(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float frontleft, float frontright, float center, float lfe, float surroundleft, float surroundright, float backleft, float backright) {
        return javafmodJNI.FMOD_ChannelGroup_SetMixLevelsOutput(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), frontleft, frontright, center, lfe, surroundleft, surroundright, backleft, backright);
    }

    public static int FMOD_ChannelGroup_SetMixLevelsInput(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float levels, int numlevels) {
        return javafmodJNI.FMOD_ChannelGroup_SetMixLevelsInput(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(levels), numlevels);
    }

    public static int FMOD_ChannelGroup_SetMixMatrix(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float matrix, int outchannels, int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_ChannelGroup_SetMixMatrix(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(matrix), outchannels, inchannels, inchannel_hop);
    }

    public static int FMOD_ChannelGroup_GetMixMatrix(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float matrix, SWIGTYPE_p_int outchannels, SWIGTYPE_p_int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_ChannelGroup_GetMixMatrix(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(matrix), SWIGTYPE_p_int.getCPtr(outchannels), SWIGTYPE_p_int.getCPtr(inchannels), inchannel_hop);
    }

    public static int FMOD_ChannelGroup_GetDSPClock(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_unsigned_long_long dspclock, SWIGTYPE_p_unsigned_long_long parentclock) {
        return javafmodJNI.FMOD_ChannelGroup_GetDSPClock(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock), SWIGTYPE_p_unsigned_long_long.getCPtr(parentclock));
    }

    public static int FMOD_ChannelGroup_SetDelay(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, BigInteger dspclock_start, BigInteger dspclock_end, SWIGTYPE_p_FMOD_BOOL stopchannels) {
        return javafmodJNI.FMOD_ChannelGroup_SetDelay(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), dspclock_start, dspclock_end, SWIGTYPE_p_FMOD_BOOL.getCPtr(stopchannels));
    }

    public static int FMOD_ChannelGroup_GetDelay(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_unsigned_long_long dspclock_start, SWIGTYPE_p_unsigned_long_long dspclock_end, SWIGTYPE_p_FMOD_BOOL stopchannels) {
        return javafmodJNI.FMOD_ChannelGroup_GetDelay(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock_start), SWIGTYPE_p_unsigned_long_long.getCPtr(dspclock_end), SWIGTYPE_p_FMOD_BOOL.getCPtr(stopchannels));
    }

    public static int FMOD_ChannelGroup_AddFadePoint(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, BigInteger dspclock, float volume) {
        return javafmodJNI.FMOD_ChannelGroup_AddFadePoint(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), dspclock, volume);
    }

    public static int FMOD_ChannelGroup_RemoveFadePoints(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, BigInteger dspclock_start, BigInteger dspclock_end) {
        return javafmodJNI.FMOD_ChannelGroup_RemoveFadePoints(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), dspclock_start, dspclock_end);
    }

    public static int FMOD_ChannelGroup_GetFadePoints(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_unsigned_int numpoints, SWIGTYPE_p_unsigned_long_long point_dspclock, SWIGTYPE_p_float point_volume) {
        return javafmodJNI.FMOD_ChannelGroup_GetFadePoints(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_unsigned_int.getCPtr(numpoints), SWIGTYPE_p_unsigned_long_long.getCPtr(point_dspclock), SWIGTYPE_p_float.getCPtr(point_volume));
    }

    public static int FMOD_ChannelGroup_GetDSP(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int index, SWIGTYPE_p_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_ChannelGroup_GetDSP(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), index, SWIGTYPE_p_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_ChannelGroup_AddDSP(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int index, SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_ChannelGroup_AddDSP(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), index, SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_ChannelGroup_RemoveDSP(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_ChannelGroup_RemoveDSP(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_ChannelGroup_GetNumDSPs(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_int numdsps) {
        return 0;
    }

    public static int FMOD_ChannelGroup_SetDSPIndex(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_DSP dsp, int index) {
        return javafmodJNI.FMOD_ChannelGroup_SetDSPIndex(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index);
    }

    public static int FMOD_ChannelGroup_GetDSPIndex(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_int index) {
        return javafmodJNI.FMOD_ChannelGroup_GetDSPIndex(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_int.getCPtr(index));
    }

    public static int FMOD_ChannelGroup_OverridePanDSP(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_DSP pan) {
        return javafmodJNI.FMOD_ChannelGroup_OverridePanDSP(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_DSP.getCPtr(pan));
    }

    public static int FMOD_ChannelGroup_Set3DAttributes(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_VECTOR pos, SWIGTYPE_p_FMOD_VECTOR vel) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DAttributes(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_VECTOR.getCPtr(pos), SWIGTYPE_p_FMOD_VECTOR.getCPtr(vel));
    }

    public static int FMOD_ChannelGroup_Get3DAttributes(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_VECTOR pos, SWIGTYPE_p_FMOD_VECTOR vel) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DAttributes(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_VECTOR.getCPtr(pos), SWIGTYPE_p_FMOD_VECTOR.getCPtr(vel));
    }

    public static int FMOD_ChannelGroup_Set3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float mindistance, float maxdistance) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), mindistance, maxdistance);
    }

    public static int FMOD_ChannelGroup_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float mindistance, SWIGTYPE_p_float maxdistance) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DMinMaxDistance(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(mindistance), SWIGTYPE_p_float.getCPtr(maxdistance));
    }

    public static int FMOD_ChannelGroup_Set3DConeSettings(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float insideconeangle, float outsideconeangle, float outsidevolume) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DConeSettings(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), insideconeangle, outsideconeangle, outsidevolume);
    }

    public static int FMOD_ChannelGroup_Get3DConeSettings(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float insideconeangle, SWIGTYPE_p_float outsideconeangle, SWIGTYPE_p_float outsidevolume) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DConeSettings(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(insideconeangle), SWIGTYPE_p_float.getCPtr(outsideconeangle), SWIGTYPE_p_float.getCPtr(outsidevolume));
    }

    public static int FMOD_ChannelGroup_Set3DConeOrientation(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_VECTOR orientation) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DConeOrientation(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_VECTOR.getCPtr(orientation));
    }

    public static int FMOD_ChannelGroup_Get3DConeOrientation(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_VECTOR orientation) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DConeOrientation(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_VECTOR.getCPtr(orientation));
    }

    public static int FMOD_ChannelGroup_Set3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_VECTOR points, int numpoints) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_VECTOR.getCPtr(points), numpoints);
    }

    public static int FMOD_ChannelGroup_Get3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_p_FMOD_VECTOR points, SWIGTYPE_p_int numpoints) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DCustomRolloff(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_p_FMOD_VECTOR.getCPtr(points), SWIGTYPE_p_int.getCPtr(numpoints));
    }

    public static int FMOD_ChannelGroup_Set3DOcclusion(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float directocclusion, float reverbocclusion) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DOcclusion(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), directocclusion, reverbocclusion);
    }

    public static int FMOD_ChannelGroup_Get3DOcclusion(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float directocclusion, SWIGTYPE_p_float reverbocclusion) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DOcclusion(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(directocclusion), SWIGTYPE_p_float.getCPtr(reverbocclusion));
    }

    public static int FMOD_ChannelGroup_Set3DSpread(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float angle) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DSpread(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), angle);
    }

    public static int FMOD_ChannelGroup_Get3DSpread(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float angle) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DSpread(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(angle));
    }

    public static int FMOD_ChannelGroup_Set3DLevel(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float level) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DLevel(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), level);
    }

    public static int FMOD_ChannelGroup_Get3DLevel(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float level) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DLevel(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(level));
    }

    public static int FMOD_ChannelGroup_Set3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, float level) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), level);
    }

    public static int FMOD_ChannelGroup_Get3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_float level) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DDopplerLevel(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_float.getCPtr(level));
    }

    public static int FMOD_ChannelGroup_Set3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL custom, float customLevel, float centerFreq) {
        return javafmodJNI.FMOD_ChannelGroup_Set3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(custom), customLevel, centerFreq);
    }

    public static int FMOD_ChannelGroup_Get3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_BOOL custom, SWIGTYPE_p_float customLevel, SWIGTYPE_p_float centerFreq) {
        return javafmodJNI.FMOD_ChannelGroup_Get3DDistanceFilter(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_BOOL.getCPtr(custom), SWIGTYPE_p_float.getCPtr(customLevel), SWIGTYPE_p_float.getCPtr(centerFreq));
    }

    public static int FMOD_ChannelGroup_SetUserData(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_ChannelGroup_SetUserData(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_ChannelGroup_GetUserData(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_ChannelGroup_GetUserData(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_ChannelGroup_Release(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup) {
        return javafmodJNI.FMOD_ChannelGroup_Release(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup));
    }

    public static int FMOD_ChannelGroup_AddGroup(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_FMOD_CHANNELGROUP group, SWIGTYPE_p_FMOD_BOOL propagatedspclock, SWIGTYPE_p_p_FMOD_DSPCONNECTION connection) {
        return javafmodJNI.FMOD_ChannelGroup_AddGroup(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(group), SWIGTYPE_p_FMOD_BOOL.getCPtr(propagatedspclock), SWIGTYPE_p_p_FMOD_DSPCONNECTION.getCPtr(connection));
    }

    public static int FMOD_ChannelGroup_GetNumGroups(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_int numgroups) {
        return javafmodJNI.FMOD_ChannelGroup_GetNumGroups(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_int.getCPtr(numgroups));
    }

    public static int FMOD_ChannelGroup_GetGroup(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int index, SWIGTYPE_p_p_FMOD_CHANNELGROUP group) {
        return javafmodJNI.FMOD_ChannelGroup_GetGroup(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), index, SWIGTYPE_p_p_FMOD_CHANNELGROUP.getCPtr(group));
    }

    public static int FMOD_ChannelGroup_GetParentGroup(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_p_FMOD_CHANNELGROUP group) {
        return javafmodJNI.FMOD_ChannelGroup_GetParentGroup(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_p_FMOD_CHANNELGROUP.getCPtr(group));
    }

    public static int FMOD_ChannelGroup_GetName(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, String name, int namelen) {
        return javafmodJNI.FMOD_ChannelGroup_GetName(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), name, namelen);
    }

    public static int FMOD_ChannelGroup_GetNumChannels(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, SWIGTYPE_p_int numchannels) {
        return javafmodJNI.FMOD_ChannelGroup_GetNumChannels(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), SWIGTYPE_p_int.getCPtr(numchannels));
    }

    public static int FMOD_ChannelGroup_GetChannel(SWIGTYPE_p_FMOD_CHANNELGROUP channelgroup, int index, SWIGTYPE_p_p_FMOD_CHANNEL channel) {
        return javafmodJNI.FMOD_ChannelGroup_GetChannel(SWIGTYPE_p_FMOD_CHANNELGROUP.getCPtr(channelgroup), index, SWIGTYPE_p_p_FMOD_CHANNEL.getCPtr(channel));
    }

    public static int FMOD_SoundGroup_Release(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup) {
        return javafmodJNI.FMOD_SoundGroup_Release(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup));
    }

    public static int FMOD_SoundGroup_GetSystemObject(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_SoundGroup_GetSystemObject(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_SoundGroup_SetMaxAudible(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, int maxaudible) {
        return javafmodJNI.FMOD_SoundGroup_SetMaxAudible(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), maxaudible);
    }

    public static int FMOD_SoundGroup_GetMaxAudible(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_int maxaudible) {
        return javafmodJNI.FMOD_SoundGroup_GetMaxAudible(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_int.getCPtr(maxaudible));
    }

    public static int FMOD_SoundGroup_SetMaxAudibleBehavior(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_FMOD_SOUNDGROUP_BEHAVIOR behavior) {
        return javafmodJNI.FMOD_SoundGroup_SetMaxAudibleBehavior(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_FMOD_SOUNDGROUP_BEHAVIOR.getCPtr(behavior));
    }

    public static int FMOD_SoundGroup_GetMaxAudibleBehavior(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_FMOD_SOUNDGROUP_BEHAVIOR behavior) {
        return javafmodJNI.FMOD_SoundGroup_GetMaxAudibleBehavior(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_FMOD_SOUNDGROUP_BEHAVIOR.getCPtr(behavior));
    }

    public static int FMOD_SoundGroup_SetMuteFadeSpeed(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, float speed) {
        return javafmodJNI.FMOD_SoundGroup_SetMuteFadeSpeed(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), speed);
    }

    public static int FMOD_SoundGroup_GetMuteFadeSpeed(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_float speed) {
        return javafmodJNI.FMOD_SoundGroup_GetMuteFadeSpeed(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_float.getCPtr(speed));
    }

    public static int FMOD_SoundGroup_SetVolume(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, float volume) {
        return javafmodJNI.FMOD_SoundGroup_SetVolume(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), volume);
    }

    public static int FMOD_SoundGroup_GetVolume(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_float volume) {
        return javafmodJNI.FMOD_SoundGroup_GetVolume(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_float.getCPtr(volume));
    }

    public static int FMOD_SoundGroup_Stop(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup) {
        return javafmodJNI.FMOD_SoundGroup_Stop(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup));
    }

    public static int FMOD_SoundGroup_GetName(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, String name, int namelen) {
        return javafmodJNI.FMOD_SoundGroup_GetName(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), name, namelen);
    }

    public static int FMOD_SoundGroup_GetNumSounds(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_int numsounds) {
        return javafmodJNI.FMOD_SoundGroup_GetNumSounds(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_int.getCPtr(numsounds));
    }

    public static int FMOD_SoundGroup_GetSound(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, int index, SWIGTYPE_p_p_FMOD_SOUND sound) {
        return javafmodJNI.FMOD_SoundGroup_GetSound(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), index, SWIGTYPE_p_p_FMOD_SOUND.getCPtr(sound));
    }

    public static int FMOD_SoundGroup_GetNumPlaying(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_int numplaying) {
        return javafmodJNI.FMOD_SoundGroup_GetNumPlaying(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_int.getCPtr(numplaying));
    }

    public static int FMOD_SoundGroup_SetUserData(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_SoundGroup_SetUserData(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_SoundGroup_GetUserData(SWIGTYPE_p_FMOD_SOUNDGROUP soundgroup, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_SoundGroup_GetUserData(SWIGTYPE_p_FMOD_SOUNDGROUP.getCPtr(soundgroup), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_DSP_Release(SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_DSP_Release(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_DSP_GetSystemObject(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_p_FMOD_SYSTEM system) {
        return javafmodJNI.FMOD_DSP_GetSystemObject(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_p_FMOD_SYSTEM.getCPtr(system));
    }

    public static int FMOD_DSP_AddInput(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_DSP input, SWIGTYPE_p_p_FMOD_DSPCONNECTION connection, SWIGTYPE_p_FMOD_DSPCONNECTION_TYPE type) {
        return javafmodJNI.FMOD_DSP_AddInput(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_DSP.getCPtr(input), SWIGTYPE_p_p_FMOD_DSPCONNECTION.getCPtr(connection), SWIGTYPE_p_FMOD_DSPCONNECTION_TYPE.getCPtr(type));
    }

    public static int FMOD_DSP_DisconnectFrom(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_DSP target, SWIGTYPE_p_FMOD_DSPCONNECTION connection) {
        return javafmodJNI.FMOD_DSP_DisconnectFrom(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_DSP.getCPtr(target), SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(connection));
    }

    public static int FMOD_DSP_DisconnectAll(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL inputs, SWIGTYPE_p_FMOD_BOOL outputs) {
        return javafmodJNI.FMOD_DSP_DisconnectAll(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(inputs), SWIGTYPE_p_FMOD_BOOL.getCPtr(outputs));
    }

    public static int FMOD_DSP_GetNumInputs(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_int numinputs) {
        return javafmodJNI.FMOD_DSP_GetNumInputs(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_int.getCPtr(numinputs));
    }

    public static int FMOD_DSP_GetNumOutputs(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_int numoutputs) {
        return javafmodJNI.FMOD_DSP_GetNumOutputs(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_int.getCPtr(numoutputs));
    }

    public static int FMOD_DSP_GetInput(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_p_FMOD_DSP input, SWIGTYPE_p_p_FMOD_DSPCONNECTION inputconnection) {
        return javafmodJNI.FMOD_DSP_GetInput(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_p_FMOD_DSP.getCPtr(input), SWIGTYPE_p_p_FMOD_DSPCONNECTION.getCPtr(inputconnection));
    }

    public static int FMOD_DSP_GetOutput(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_p_FMOD_DSP output, SWIGTYPE_p_p_FMOD_DSPCONNECTION outputconnection) {
        return javafmodJNI.FMOD_DSP_GetOutput(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_p_FMOD_DSP.getCPtr(output), SWIGTYPE_p_p_FMOD_DSPCONNECTION.getCPtr(outputconnection));
    }

    public static int FMOD_DSP_SetActive(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_DSP_SetActive(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_DSP_GetActive(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_DSP_GetActive(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_DSP_SetBypass(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL bypass) {
        return javafmodJNI.FMOD_DSP_SetBypass(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(bypass));
    }

    public static int FMOD_DSP_GetBypass(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL bypass) {
        return javafmodJNI.FMOD_DSP_GetBypass(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(bypass));
    }

    public static int FMOD_DSP_SetWetDryMix(SWIGTYPE_p_FMOD_DSP dsp, float prewet, float postwet, float dry) {
        return javafmodJNI.FMOD_DSP_SetWetDryMix(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), prewet, postwet, dry);
    }

    public static int FMOD_DSP_GetWetDryMix(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_float prewet, SWIGTYPE_p_float postwet, SWIGTYPE_p_float dry) {
        return javafmodJNI.FMOD_DSP_GetWetDryMix(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_float.getCPtr(prewet), SWIGTYPE_p_float.getCPtr(postwet), SWIGTYPE_p_float.getCPtr(dry));
    }

    public static int FMOD_DSP_SetChannelFormat(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_CHANNELMASK channelmask, int numchannels, SWIGTYPE_p_FMOD_SPEAKERMODE source_speakermode) {
        return javafmodJNI.FMOD_DSP_SetChannelFormat(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_CHANNELMASK.getCPtr(channelmask), numchannels, SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(source_speakermode));
    }

    public static int FMOD_DSP_GetChannelFormat(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_CHANNELMASK channelmask, SWIGTYPE_p_int numchannels, SWIGTYPE_p_FMOD_SPEAKERMODE source_speakermode) {
        return javafmodJNI.FMOD_DSP_GetChannelFormat(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_CHANNELMASK.getCPtr(channelmask), SWIGTYPE_p_int.getCPtr(numchannels), SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(source_speakermode));
    }

    public static int FMOD_DSP_GetOutputChannelFormat(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_CHANNELMASK inmask, int inchannels, SWIGTYPE_p_FMOD_SPEAKERMODE inspeakermode, SWIGTYPE_p_FMOD_CHANNELMASK outmask, SWIGTYPE_p_int outchannels, SWIGTYPE_p_FMOD_SPEAKERMODE outspeakermode) {
        return javafmodJNI.FMOD_DSP_GetOutputChannelFormat(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_CHANNELMASK.getCPtr(inmask), inchannels, SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(inspeakermode), SWIGTYPE_p_FMOD_CHANNELMASK.getCPtr(outmask), SWIGTYPE_p_int.getCPtr(outchannels), SWIGTYPE_p_FMOD_SPEAKERMODE.getCPtr(outspeakermode));
    }

    public static int FMOD_DSP_Reset(SWIGTYPE_p_FMOD_DSP dsp) {
        return javafmodJNI.FMOD_DSP_Reset(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp));
    }

    public static int FMOD_DSP_SetParameterFloat(SWIGTYPE_p_FMOD_DSP dsp, int index, float value) {
        return javafmodJNI.FMOD_DSP_SetParameterFloat(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, value);
    }

    public static int FMOD_DSP_SetParameterInt(SWIGTYPE_p_FMOD_DSP dsp, int index, int value) {
        return javafmodJNI.FMOD_DSP_SetParameterInt(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, value);
    }

    public static int FMOD_DSP_SetParameterBool(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_FMOD_BOOL value) {
        return javafmodJNI.FMOD_DSP_SetParameterBool(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_FMOD_BOOL.getCPtr(value));
    }

    public static int FMOD_DSP_SetParameterData(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_void data, long length) {
        return javafmodJNI.FMOD_DSP_SetParameterData(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_void.getCPtr(data), length);
    }

    public static int FMOD_DSP_GetParameterFloat(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_float value, String valuestr, int valuestrlen) {
        return javafmodJNI.FMOD_DSP_GetParameterFloat(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_float.getCPtr(value), valuestr, valuestrlen);
    }

    public static int FMOD_DSP_GetParameterInt(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_int value, String valuestr, int valuestrlen) {
        return javafmodJNI.FMOD_DSP_GetParameterInt(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_int.getCPtr(value), valuestr, valuestrlen);
    }

    public static int FMOD_DSP_GetParameterBool(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_FMOD_BOOL value, String valuestr, int valuestrlen) {
        return javafmodJNI.FMOD_DSP_GetParameterBool(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_FMOD_BOOL.getCPtr(value), valuestr, valuestrlen);
    }

    public static int FMOD_DSP_GetParameterData(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_p_void data, SWIGTYPE_p_unsigned_int length, String valuestr, int valuestrlen) {
        return javafmodJNI.FMOD_DSP_GetParameterData(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_p_void.getCPtr(data), SWIGTYPE_p_unsigned_int.getCPtr(length), valuestr, valuestrlen);
    }

    public static int FMOD_DSP_GetNumParameters(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_int numparams) {
        return javafmodJNI.FMOD_DSP_GetNumParameters(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_int.getCPtr(numparams));
    }

    public static int FMOD_DSP_GetParameterInfo(SWIGTYPE_p_FMOD_DSP dsp, int index, SWIGTYPE_p_p_FMOD_DSP_PARAMETER_DESC desc) {
        return javafmodJNI.FMOD_DSP_GetParameterInfo(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), index, SWIGTYPE_p_p_FMOD_DSP_PARAMETER_DESC.getCPtr(desc));
    }

    public static int FMOD_DSP_GetDataParameterIndex(SWIGTYPE_p_FMOD_DSP dsp, int datatype, SWIGTYPE_p_int index) {
        return javafmodJNI.FMOD_DSP_GetDataParameterIndex(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), datatype, SWIGTYPE_p_int.getCPtr(index));
    }

    public static int FMOD_DSP_ShowConfigDialog(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_void hwnd, SWIGTYPE_p_FMOD_BOOL show) {
        return javafmodJNI.FMOD_DSP_ShowConfigDialog(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_void.getCPtr(hwnd), SWIGTYPE_p_FMOD_BOOL.getCPtr(show));
    }

    public static int FMOD_DSP_GetInfo(SWIGTYPE_p_FMOD_DSP dsp, String name, SWIGTYPE_p_unsigned_int version, SWIGTYPE_p_int channels, SWIGTYPE_p_int configwidth, SWIGTYPE_p_int configheight) {
        return javafmodJNI.FMOD_DSP_GetInfo(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), name, SWIGTYPE_p_unsigned_int.getCPtr(version), SWIGTYPE_p_int.getCPtr(channels), SWIGTYPE_p_int.getCPtr(configwidth), SWIGTYPE_p_int.getCPtr(configheight));
    }

    public static int FMOD_DSP_GetType(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_DSP_TYPE type) {
        return javafmodJNI.FMOD_DSP_GetType(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_DSP_TYPE.getCPtr(type));
    }

    public static int FMOD_DSP_GetIdle(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL idle) {
        return javafmodJNI.FMOD_DSP_GetIdle(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(idle));
    }

    public static int FMOD_DSP_SetUserData(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_DSP_SetUserData(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_DSP_GetUserData(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_DSP_GetUserData(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_DSP_SetMeteringEnabled(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL inputEnabled, SWIGTYPE_p_FMOD_BOOL outputEnabled) {
        return javafmodJNI.FMOD_DSP_SetMeteringEnabled(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(inputEnabled), SWIGTYPE_p_FMOD_BOOL.getCPtr(outputEnabled));
    }

    public static int FMOD_DSP_GetMeteringEnabled(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_BOOL inputEnabled, SWIGTYPE_p_FMOD_BOOL outputEnabled) {
        return javafmodJNI.FMOD_DSP_GetMeteringEnabled(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_BOOL.getCPtr(inputEnabled), SWIGTYPE_p_FMOD_BOOL.getCPtr(outputEnabled));
    }

    public static int FMOD_DSP_GetMeteringInfo(SWIGTYPE_p_FMOD_DSP dsp, SWIGTYPE_p_FMOD_DSP_METERING_INFO inputInfo, SWIGTYPE_p_FMOD_DSP_METERING_INFO outputInfo) {
        return javafmodJNI.FMOD_DSP_GetMeteringInfo(SWIGTYPE_p_FMOD_DSP.getCPtr(dsp), SWIGTYPE_p_FMOD_DSP_METERING_INFO.getCPtr(inputInfo), SWIGTYPE_p_FMOD_DSP_METERING_INFO.getCPtr(outputInfo));
    }

    public static int FMOD_DSPConnection_GetInput(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_p_FMOD_DSP input) {
        return javafmodJNI.FMOD_DSPConnection_GetInput(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_p_FMOD_DSP.getCPtr(input));
    }

    public static int FMOD_DSPConnection_GetOutput(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_p_FMOD_DSP output) {
        return javafmodJNI.FMOD_DSPConnection_GetOutput(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_p_FMOD_DSP.getCPtr(output));
    }

    public static int FMOD_DSPConnection_SetMix(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, float volume) {
        return javafmodJNI.FMOD_DSPConnection_SetMix(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), volume);
    }

    public static int FMOD_DSPConnection_GetMix(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_float volume) {
        return javafmodJNI.FMOD_DSPConnection_GetMix(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_float.getCPtr(volume));
    }

    public static int FMOD_DSPConnection_SetMixMatrix(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_float matrix, int outchannels, int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_DSPConnection_SetMixMatrix(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_float.getCPtr(matrix), outchannels, inchannels, inchannel_hop);
    }

    public static int FMOD_DSPConnection_GetMixMatrix(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_float matrix, SWIGTYPE_p_int outchannels, SWIGTYPE_p_int inchannels, int inchannel_hop) {
        return javafmodJNI.FMOD_DSPConnection_GetMixMatrix(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_float.getCPtr(matrix), SWIGTYPE_p_int.getCPtr(outchannels), SWIGTYPE_p_int.getCPtr(inchannels), inchannel_hop);
    }

    public static int FMOD_DSPConnection_GetType(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_FMOD_DSPCONNECTION_TYPE type) {
        return javafmodJNI.FMOD_DSPConnection_GetType(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_FMOD_DSPCONNECTION_TYPE.getCPtr(type));
    }

    public static int FMOD_DSPConnection_SetUserData(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_DSPConnection_SetUserData(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_DSPConnection_GetUserData(SWIGTYPE_p_FMOD_DSPCONNECTION dspconnection, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_DSPConnection_GetUserData(SWIGTYPE_p_FMOD_DSPCONNECTION.getCPtr(dspconnection), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_Geometry_Release(SWIGTYPE_p_FMOD_GEOMETRY geometry) {
        return javafmodJNI.FMOD_Geometry_Release(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry));
    }

    public static int FMOD_Geometry_AddPolygon(SWIGTYPE_p_FMOD_GEOMETRY geometry, float directocclusion, float reverbocclusion, SWIGTYPE_p_FMOD_BOOL doublesided, int numvertices, SWIGTYPE_p_FMOD_VECTOR vertices, SWIGTYPE_p_int polygonindex) {
        return javafmodJNI.FMOD_Geometry_AddPolygon(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), directocclusion, reverbocclusion, SWIGTYPE_p_FMOD_BOOL.getCPtr(doublesided), numvertices, SWIGTYPE_p_FMOD_VECTOR.getCPtr(vertices), SWIGTYPE_p_int.getCPtr(polygonindex));
    }

    public static int FMOD_Geometry_GetNumPolygons(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_int numpolygons) {
        return javafmodJNI.FMOD_Geometry_GetNumPolygons(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_int.getCPtr(numpolygons));
    }

    public static int FMOD_Geometry_GetMaxPolygons(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_int maxpolygons, SWIGTYPE_p_int maxvertices) {
        return javafmodJNI.FMOD_Geometry_GetMaxPolygons(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_int.getCPtr(maxpolygons), SWIGTYPE_p_int.getCPtr(maxvertices));
    }

    public static int FMOD_Geometry_GetPolygonNumVertices(SWIGTYPE_p_FMOD_GEOMETRY geometry, int index, SWIGTYPE_p_int numvertices) {
        return javafmodJNI.FMOD_Geometry_GetPolygonNumVertices(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), index, SWIGTYPE_p_int.getCPtr(numvertices));
    }

    public static int FMOD_Geometry_SetPolygonVertex(SWIGTYPE_p_FMOD_GEOMETRY geometry, int index, int vertexindex, SWIGTYPE_p_FMOD_VECTOR vertex) {
        return javafmodJNI.FMOD_Geometry_SetPolygonVertex(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), index, vertexindex, SWIGTYPE_p_FMOD_VECTOR.getCPtr(vertex));
    }

    public static int FMOD_Geometry_GetPolygonVertex(SWIGTYPE_p_FMOD_GEOMETRY geometry, int index, int vertexindex, SWIGTYPE_p_FMOD_VECTOR vertex) {
        return javafmodJNI.FMOD_Geometry_GetPolygonVertex(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), index, vertexindex, SWIGTYPE_p_FMOD_VECTOR.getCPtr(vertex));
    }

    public static int FMOD_Geometry_SetPolygonAttributes(SWIGTYPE_p_FMOD_GEOMETRY geometry, int index, float directocclusion, float reverbocclusion, SWIGTYPE_p_FMOD_BOOL doublesided) {
        return javafmodJNI.FMOD_Geometry_SetPolygonAttributes(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), index, directocclusion, reverbocclusion, SWIGTYPE_p_FMOD_BOOL.getCPtr(doublesided));
    }

    public static int FMOD_Geometry_GetPolygonAttributes(SWIGTYPE_p_FMOD_GEOMETRY geometry, int index, SWIGTYPE_p_float directocclusion, SWIGTYPE_p_float reverbocclusion, SWIGTYPE_p_FMOD_BOOL doublesided) {
        return javafmodJNI.FMOD_Geometry_GetPolygonAttributes(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), index, SWIGTYPE_p_float.getCPtr(directocclusion), SWIGTYPE_p_float.getCPtr(reverbocclusion), SWIGTYPE_p_FMOD_BOOL.getCPtr(doublesided));
    }

    public static int FMOD_Geometry_SetActive(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_Geometry_SetActive(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_Geometry_GetActive(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_Geometry_GetActive(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_Geometry_SetRotation(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR forward, SWIGTYPE_p_FMOD_VECTOR up) {
        return javafmodJNI.FMOD_Geometry_SetRotation(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(forward), SWIGTYPE_p_FMOD_VECTOR.getCPtr(up));
    }

    public static int FMOD_Geometry_GetRotation(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR forward, SWIGTYPE_p_FMOD_VECTOR up) {
        return javafmodJNI.FMOD_Geometry_GetRotation(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(forward), SWIGTYPE_p_FMOD_VECTOR.getCPtr(up));
    }

    public static int FMOD_Geometry_SetPosition(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR position) {
        return javafmodJNI.FMOD_Geometry_SetPosition(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(position));
    }

    public static int FMOD_Geometry_GetPosition(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR position) {
        return javafmodJNI.FMOD_Geometry_GetPosition(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(position));
    }

    public static int FMOD_Geometry_SetScale(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR scale) {
        return javafmodJNI.FMOD_Geometry_SetScale(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(scale));
    }

    public static int FMOD_Geometry_GetScale(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_FMOD_VECTOR scale) {
        return javafmodJNI.FMOD_Geometry_GetScale(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_FMOD_VECTOR.getCPtr(scale));
    }

    public static int FMOD_Geometry_Save(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_void data, SWIGTYPE_p_int datasize) {
        return javafmodJNI.FMOD_Geometry_Save(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_void.getCPtr(data), SWIGTYPE_p_int.getCPtr(datasize));
    }

    public static int FMOD_Geometry_SetUserData(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_Geometry_SetUserData(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_Geometry_GetUserData(SWIGTYPE_p_FMOD_GEOMETRY geometry, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_Geometry_GetUserData(SWIGTYPE_p_FMOD_GEOMETRY.getCPtr(geometry), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static int FMOD_Reverb3D_Release(SWIGTYPE_p_FMOD_REVERB3D reverb3d) {
        return javafmodJNI.FMOD_Reverb3D_Release(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d));
    }

    public static int FMOD_Reverb3D_Set3DAttributes(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_VECTOR position, float mindistance, float maxdistance) {
        return javafmodJNI.FMOD_Reverb3D_Set3DAttributes(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_VECTOR.getCPtr(position), mindistance, maxdistance);
    }

    public static int FMOD_Reverb3D_Get3DAttributes(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_VECTOR position, SWIGTYPE_p_float mindistance, SWIGTYPE_p_float maxdistance) {
        return javafmodJNI.FMOD_Reverb3D_Get3DAttributes(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_VECTOR.getCPtr(position), SWIGTYPE_p_float.getCPtr(mindistance), SWIGTYPE_p_float.getCPtr(maxdistance));
    }

    public static int FMOD_Reverb3D_SetProperties(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_REVERB_PROPERTIES properties) {
        return javafmodJNI.FMOD_Reverb3D_SetProperties(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_REVERB_PROPERTIES.getCPtr(properties));
    }

    public static int FMOD_Reverb3D_GetProperties(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_REVERB_PROPERTIES properties) {
        return javafmodJNI.FMOD_Reverb3D_GetProperties(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_REVERB_PROPERTIES.getCPtr(properties));
    }

    public static int FMOD_Reverb3D_SetActive(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_Reverb3D_SetActive(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_Reverb3D_GetActive(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_FMOD_BOOL active) {
        return javafmodJNI.FMOD_Reverb3D_GetActive(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_FMOD_BOOL.getCPtr(active));
    }

    public static int FMOD_Reverb3D_SetUserData(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_void userdata) {
        return javafmodJNI.FMOD_Reverb3D_SetUserData(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_void.getCPtr(userdata));
    }

    public static int FMOD_Reverb3D_GetUserData(SWIGTYPE_p_FMOD_REVERB3D reverb3d, SWIGTYPE_p_p_void userdata) {
        return javafmodJNI.FMOD_Reverb3D_GetUserData(SWIGTYPE_p_FMOD_REVERB3D.getCPtr(reverb3d), SWIGTYPE_p_p_void.getCPtr(userdata));
    }

    public static void FMOD_System_SetReverbDefault(int reverbChannel, int preset) {
        if (reverb[reverbChannel] != preset) {
            if (Core.debug) {
                DebugLog.log("reverb instance=" + reverbChannel + " preset=" + preset);
            }
            javafmod.reverb[reverbChannel] = preset;
        }
        javafmodJNI.FMOD_System_SetReverbDefault(reverbChannel, preset);
    }

    public static int FMOD_Studio_EventInstance3D(long inst, float x, float y, float z) {
        return javafmodJNI.FMOD_Studio_EventInstance3D(inst, x, y, z);
    }

    public static int FMOD_Studio_SetNumListeners(int numListeners) {
        return javafmodJNI.FMOD_Studio_SetNumListeners(numListeners);
    }

    public static void FMOD_Studio_Listener3D(int listener, float x, float y, float z, float vx, float vy, float vz, float fx, float fy, float fz, float ux, float uy, float uz) {
        javafmodJNI.FMOD_Studio_Listener3D(listener, x, y, z, vx, vy, vz, fx, fy, fz, ux, uy, uz);
    }

    public static int FMOD_Studio_EventInstance_SetCallback(long eventinstance, FMOD_STUDIO_EVENT_CALLBACK callback, int callbackmask) {
        return javafmodJNI.FMOD_Studio_EventInstance_SetCallback(eventinstance, callback, callbackmask);
    }

    public static int FMOD_Studio_EventInstance_SetParameterByID(long inst, FMOD_STUDIO_PARAMETER_ID id, float value, boolean ignoreseekspeed) {
        if (id == null) {
            return 0;
        }
        return javafmodJNI.FMOD_Studio_EventInstance_SetParameterByID(inst, id.address(), value, ignoreseekspeed);
    }

    public static int FMOD_Studio_EventInstance_SetParameterByID(long inst, FMOD_STUDIO_PARAMETER_ID id, float value) {
        boolean ignoreseekspeed = false;
        return javafmod.FMOD_Studio_EventInstance_SetParameterByID(inst, id, value, false);
    }

    public static int FMOD_Studio_EventInstance_SetParameterByName(long inst, String param, float value) {
        return javafmodJNI.FMOD_Studio_EventInstance_SetParameterByName(inst, param, value);
    }

    public static float FMOD_Studio_GetParameter(long inst, String param) {
        return javafmodJNI.FMOD_Studio_GetParameter(inst, param);
    }

    public static int FMOD_Studio_GetPlaybackState(long inst) {
        return javafmodJNI.FMOD_Studio_GetPlaybackState(inst);
    }

    public static int FMOD_Studio_EventInstance_SetVolume(long inst, float volume) {
        return javafmodJNI.FMOD_Studio_EventInstance_SetVolume(inst, volume);
    }

    public static int FMOD_Studio_ReleaseEventInstance(long inst) {
        return javafmodJNI.FMOD_Studio_ReleaseEventInstance(inst);
    }

    public static int FMOD_Studio_EventInstance_Stop(long inst, boolean immediate) {
        return javafmodJNI.FMOD_Studio_EventInstance_Stop(inst, immediate);
    }
}

