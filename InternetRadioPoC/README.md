# Internet Vehicle Radio — Synthetic Radio Sender 0.9.0

Server-only transport proof of concept for Project Zomboid Build 42.20.2.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Current milestone

Version 0.9.0 tests the lightest architecture compatible with an unmodified
Project Zomboid client:

```text
server-generated PCM
  -> synthetic remote onlineID
  -> vanilla SyncRadioData on 104.6 MHz
  -> RakVoice.SendFrame(recipientGuid, syntheticOnlineID, PCM)
  -> ordinary client tuned to 104.6 MHz
```

No Steam-authenticated Virtual Client is created in this phase. The synthetic
identity does not open its own network connection and does not consume a
normal player slot.

The test source is still a deterministic 440 Hz tone. HLS, AAC and the WIVK-FM
API are intentionally behind the `PcmSource` boundary and are not enabled
until this transport reaches the normal client.

## Why 0.9.0 differs from 0.8.7

Inspection of the actual B42.20.2 `projectzomboid.jar` established that:

- `RakVoice.SendFrame` has descriptor `(long, long, byte[], long)`;
- `UdpConnection.players` is the stable runtime player array;
- the server converts client `SyncRadioData` into
  `RakVoice.SetChannelsRouting`;
- the client only calls `RakVoice.ReceiveFrame(onlineID, buffer)` for remote
  players known to `GameClient`;
- one-client self injection is therefore expected to be suppressed or ignored.

Version 0.9.0 announces a minimal remote `IsoPlayer` with reserved online ID
`3000`, sends radio metadata for that ID, and then injects PCM using the same
ID. This makes the one-client test meaningful without requiring a full bot
login.

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
[InternetRadioBridge][BOOT] version=0.9.0; transport=synthetic-radio-sender; frequency=104.6; onlineId=3000
[InternetRadioBridge][MONITOR_OK] ...
[InternetRadioBridge][VOICE_STATE] serverEnabled=true; ...
[InternetRadioBridge][SYNTHETIC_ID] reserved onlineId=3000; collision=false
[InternetRadioBridge][IDENTITY_ENTER] ...
[InternetRadioBridge][IDENTITY_RETURN] GameServer.sendPlayerConnected returned
[InternetRadioBridge][RADIO_ROUTE_RETURN] SyncRadioData sent; values=4
[InternetRadioBridge][IDENTITY_READY] ... frequency=104.6
[InternetRadioBridge][SYNTHETIC_TEST] ... expectedFrequency=104.6
[InternetRadioBridge][SEND_ENTER] ... onlineId=3000; ...
[InternetRadioBridge][SEND_RETURN] first synthetic frame returned without exception
[InternetRadioBridge][SYNTHETIC_RESULT] ... listenOn=104.6
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
2. Confirm Leaf loads `lcc-internet-radio-server-bridge 0.9.0`.
3. Join with the single ordinary client and enable VOIP.
4. Sit in a vehicle with a working radio.
5. Turn the radio on and tune it to exactly `104.6 MHz`.
6. Set radio volume above zero and make sure no cassette/media is playing.
7. Wait up to 40 seconds. The six-second tone repeats every 30 seconds.
8. Also test a nearby powered handheld radio on `104.6` if available.
9. Save both the client and server logs.

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

- If the tone is heard: keep the synthetic sender and implement
  `HlsPcmSource`, listener-driven station sessions and additional frequencies.
- If identity and send calls succeed but no tone is received: retain the same
  `PcmSource` and station manager interfaces, replacing only the transport with
  a minimal connected RadioBot.

## Build

The JAR compiles against narrow local stubs. No Project Zomboid or Leaf classes
are bundled:

```bash
bash server-bridge-src/build.sh
```
