# Internet Vehicle Radio — Safe Server Bridge 0.9.2

Server-only transport proof of concept for Project Zomboid Build 42.20.2.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Current milestone

Version 0.9.2 is a stability release. The 0.9.1 runtime test proved that direct
server injection is unsafe in Project Zomboid B42.20.2:

```text
IDENTITY_RETURN
  -> RADIO_ROUTE_RETURN
  -> SEND_ENTER
  -> native server process termination
```

There was no Java exception and no `SEND_RETURN`, so the failure occurred
inside the native RakVoice call. Version 0.9.2 physically removes the
`RakVoice.SendFrame` call from the bridge and logs `DIRECT_TEST_BLOCKED`
instead. It is safe to keep loaded while the next transport is implemented.

The next transport is a server-managed RadioBot running in a genuine client
RakVoice context. It remains server-only: ordinary players will not install
Leaf, Java, a decoder, or a custom launcher.

## Why 0.9.1 differs from 0.8.7

Inspection of the actual B42.20.2 `projectzomboid.jar` established that:

- `RakVoice.SendFrame` has descriptor `(long, long, byte[], long)`;
- `UdpConnection.players` is the stable runtime player array;
- the server converts client `SyncRadioData` into
  `RakVoice.SetChannelsRouting`;
- the client only calls `RakVoice.ReceiveFrame(onlineID, buffer)` for remote
  players known to `GameClient`;
- one-client self injection is therefore expected to be suppressed or ignored.

Version 0.9.1 announced a minimal remote `IsoPlayer` with reserved online ID
`3000`, sends radio metadata for that ID, and then injects PCM using the same
ID. This makes the one-client test meaningful without requiring a full bot
login.

The first 0.9.0 server run also exposed two dedicated-server differences that
0.9.1 corrects:

- B42.20.2 coordinate setters have descriptor `(float) -> float`, not
  `(float) -> void`;
- `RakVoice.GetBufferSizeBytes()` returns zero in the server voice context,
  because the server has no FMOD microphone record buffer.

When the native size is zero, the bridge derives one mono signed PCM16 frame
from the authoritative voice parameters. With the current server settings this
is `24000 samples/s * 20 ms * 2 bytes = 960 bytes`.

## Runtime behavior

For every fully connected client the bridge:

1. waits five seconds for the game connection to settle;
2. verifies that online ID `3000` does not collide with a real player;
3. sends a vanilla `ConnectedPlayer` representation named
   `[Radio] WIVK-FM 104.6`;
4. sends `SyncRadioData` containing one route:
   `104600, 30000, x, y`;
5. waits two seconds;
6. sends a six-second 440 Hz PCM tone;
7. repeats the tone every 30 seconds so a single tester cannot miss it.

The radio metadata contains four integer values:

```text
frequency, transmitDistance, x, y
```

The bridge queries sample rate, frame period and PCM buffer size from
`RakVoice` at runtime instead of hard-coding a frame size.

## Expected server log

```text
[InternetRadioBridge][BOOT] version=0.9.2; transport=synthetic-radio-sender; frequency=104.6; onlineId=3000
[InternetRadioBridge][MONITOR_OK] ...
[InternetRadioBridge][VOICE_STATE] serverEnabled=true; ...
[InternetRadioBridge][DIRECT_TEST_BLOCKED] ... nextTransport=server-radio-bot
```

Interpretation:

- no `BOOT`: Leaf did not execute the bridge entrypoint;
- `IDENTITY FAIL`: the minimal synthetic player could not be serialized;
- `RADIO_ROUTE FAIL`: the vanilla radio metadata packet was rejected;
- `SEND_ENTER` without `SEND_RETURN`: the native send call failed;
- `SYNTHETIC_RESULT` with silence on 104.6: native RakVoice requires a real
  connected peer, so the next transport must be a minimal server-side RadioBot.

## Test procedure

1. Publish/update Workshop item `3783046891` and fully restart the server.
2. Confirm Leaf loads `lcc-internet-radio-server-bridge 0.9.2`.
3. Join with the ordinary client.
4. Confirm `VOICE_STATE` and `DIRECT_TEST_BLOCKED` appear.
5. Leave the server running for several minutes and confirm it remains stable.

F9/F10/F11 remain local packaged-audio controls and are independent of the
server radio test.

## Installation boundary

- Players install only the normal Steam Workshop mod.
- Players do not install Leaf, JARs, DLLs, ffmpeg, a browser extension, or a
  custom launcher.
- Leaf and the Java bridge are server-only.

The Workshop source contains the bridge here:

```text
Contents/mods/LaccckaInternetRadioPoC/leaf/mods/
  LaccckaInternetRadioServerBridge.jar
```

Steam installs the contents at the Workshop item root:

```text
steamapps/workshop/content/108600/3783046891/
  mods/LaccckaInternetRadioPoC/leaf/mods/
    LaccckaInternetRadioServerBridge.jar
```

When the server lives under `steamapps/common/Project Zomboid Dedicated
Server`, its global Workshop directory is reached with `..\..\workshop`, not
with a nested `Dedicated Server\steamapps\workshop` copy:

```bat
set "BRIDGE_SOURCE=%~dp0..\..\workshop\content\108600\3783046891\mods\LaccckaInternetRadioPoC\leaf\mods\LaccckaInternetRadioServerBridge.jar"
set "BRIDGE_TARGET=%~dp0.leaf\runtime-mods\LaccckaInternetRadioServerBridge.jar"
```

## Next step after the test

- Implement a minimal connected RadioBot launched automatically on the server.
- Prove `440 Hz -> client RakVoice -> server -> radio 104.6`.
- Only after that succeeds, attach `HlsPcmSource`, listener-driven station
  sessions and additional frequencies.

## Build

The JAR compiles against narrow local stubs. No Project Zomboid or Leaf classes
are bundled:

```bash
bash server-bridge-src/build.sh
```
