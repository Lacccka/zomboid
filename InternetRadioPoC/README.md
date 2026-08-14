# Internet Vehicle Radio — Direct Server Transport Probe 0.8.7

Proof of concept for Project Zomboid Build 42.20.2 multiplayer.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Current milestone

Version 0.8.7 tests one narrow question:

> Can `RakVoice.SendFrame()` be called after Dedicated Server VOIP
> initialization and deliver generated PCM to an ordinary Project Zomboid
> client?

It does not claim that radio routing, frequency 104.6, a virtual sender, HTTP,
AAC decoding, buffering, or vehicle integration work yet.

Version 0.8.2 targeted a client lifecycle method. Version 0.8.4 proved that the
Leaf entrypoint runs, but also proved that `ServerMap.preupdate()` is not called
by this Dedicated Server lifecycle. Version 0.8.5 removed the lifecycle mixin
and started a lightweight daemon monitor from the proven entrypoint, but its
direct access to `UdpConnection.playerIDs` was incompatible with the runtime
class and stopped the probe before `SendFrame()`. Version 0.8.6 then established
that the documented `GameServer.getAnyPlayerFromConnection()` method is also
absent from the actual B42.20.2 server JAR. Version 0.8.7 removes hard bytecode
links to both unstable members and resolves the online ID reflectively.

## Probe architecture

```text
Leaf main entrypoint
  -> [BOOT]
Server-only daemon monitor
  -> [MONITOR_OK]
GameServer.udpEngine.connections
  -> reflect connection.players / connection.playerIDs
  -> single-connection GameServer player collections as fallback
  -> fully connected GUID + runtime player onlineID
RakVoice server state
  -> sample rate + frame period + buffer size
440 Hz mono S16LE generator
  -> RakVoice.SendFrame(recipientGuid, sourceOnlineId, frame, size)
  -> ordinary Project Zomboid client
```

The probe waits for at least one fully connected client, then waits five
seconds, sends a four-second tone once, and never retries during the same
server process.

With one client, that connection supplies both the temporary source onlineID
and recipient GUID. Logs mark this as `mode=self-target` and
`selfSuppressionPossible=true`. Silence in this mode is inconclusive because
the client may suppress audio attributed to its own player. With two clients,
the first connection is the source identity and the second is the recipient.

`SEND_RETURN` proves only that the Java/native call returned without an
exception. Audible delivery must still be confirmed in game.

## Expected server log

```text
[InternetRadioBridge][BOOT] version=0.8.7; ...
[InternetRadioBridge][MONITOR_OK] daemon polling started; ...
[InternetRadioBridge][WAIT] no fully-connected player ...
[InternetRadioBridge][TARGET_RESOLVE] strategy=...; onlineId=...; ...
[InternetRadioBridge][VOICE_STATE] serverEnabled=true; sampleRate=...; ...
[InternetRadioBridge][TARGET] sourceGuid=...; sourceOnlineId=...; ...
[InternetRadioBridge][DIRECT_TEST] guid=...; onlineId=...; bytes=...; ...
[InternetRadioBridge][SEND_ENTER] guid=...; onlineId=...; bytes=...
[InternetRadioBridge][SEND_RETURN] first frame returned ...
[InternetRadioBridge][DIRECT_RESULT] sendFrameReturned=true; framesSent=...;
```

Interpretation:

- no `BOOT`: Leaf found metadata but did not run the entrypoint;
- `BOOT` without `MONITOR_OK`: the entrypoint could not start its daemon;
- `MONITOR_POLL FAIL`: target discovery failed before the VOIP test;
- `TARGET_RESOLVE unresolved`: no supported runtime player representation was
  found; the same line includes the actual connection field inventory;
- `serverEnabled=false`: VOIP is disabled in server settings;
- `SEND_ENTER` without `SEND_RETURN`: native call failed or blocked;
- `DIRECT_RESULT` without audible sound: the call was accepted but delivery,
  source identity, client loopback, or routing rejected the frame.

## Installation boundary

- Players install only the normal Steam Workshop mod.
- Players do not install Leaf, JARs, DLLs, ffmpeg, or a custom launcher.
- Leaf and this Java bridge are server-only.

The Workshop upload source contains the bridge here:

```text
Contents/mods/LaccckaInternetRadioPoC/leaf/mods/
  LaccckaInternetRadioServerBridge.jar
```

Steam installs the contents of `Contents` at the item root, so the actual
Dedicated Server path is:

```text
steamapps/workshop/content/108600/3783046891/
  mods/LaccckaInternetRadioPoC/leaf/mods/
    LaccckaInternetRadioServerBridge.jar
```

For a standard Dedicated Server installation, load the exact JAR before `-cp`:

```bat
"-Dleaf.addMods=%CD%\steamapps\workshop\content\108600\3783046891\mods\LaccckaInternetRadioPoC\leaf\mods\LaccckaInternetRadioServerBridge.jar"
"-Dleaf.gameWorkshopPath=%CD%\steamapps\workshop\content\108600"
```

## Test procedure

1. Update Workshop item `3783046891` and fully restart the server.
2. Confirm Leaf reports `lcc-internet-radio-server-bridge 0.8.7`.
3. Confirm `[BOOT]` and `[MONITOR_OK]` appear.
4. Connect the available ordinary client with VOIP enabled.
5. Wait at least ten seconds after it finishes loading.
6. Record whether it hears a four-second 440 Hz tone, but do not treat silence
   in `self-target` mode as proof that direct injection failed.
7. Save both client logs and the server log.

No radio or 104.6 tuning is used in this phase. Frequency routing is the next
test only if direct delivery works.

## Existing client controls

- F8: report client HTTP availability only.
- F9: packaged `GameSoundClip` through the vehicle emitter.
- F10: packaged positional world sound.
- F11: packaged sound through the vehicle emitter.

F11 remains the confirmed control for packaged audio and moving vehicle
positioning; it is independent of this RakVoice probe.

## Decision after 0.8.7

- If direct delivery works, the next probe adds radio routing on 104.6 MHz.
- If `SendFrame()` returns but clients consistently receive no audio with valid
  cross-client identities, stop expanding direct injection and move to a
  separate Virtual Client transport.
- HTTP/AAC work starts only after `synthetic PCM -> PZ transport -> ordinary
  client` succeeds.

## Build

The JAR compiles against narrow local stubs. No Project Zomboid or Leaf classes
are bundled into it:

```bash
bash server-bridge-src/build.sh
```
