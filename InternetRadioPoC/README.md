# Internet Vehicle Radio — Low-Level Server Voice Bridge 0.8.0

Proof of concept for Project Zomboid Build 42.20.2 multiplayer.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Milestone

Version 0.8.0 tests one question only:

> Can a server-only Java component inject generated audio through RakVoice so
> that an unmodified Project Zomboid client hears it?

It does not download WIVK, parse HLS, decode AAC, create a virtual source, or
change vehicle radio state yet. Those features remain blocked until this tone
test passes.

## Architecture of this probe

```text
Dedicated Server
  -> generate 440 Hz mono S16LE PCM
  -> RakVoice.SendFrame(recipientGuid, sourcePlayerId, frame, size)
  -> ordinary Project Zomboid client
```

The server observes a fully connected client after vanilla
`VoiceManager.UpdateChannelsRoaming`. Five seconds later it sends that client a
three-second tone. For this first transport test, the receiving player's own
online id is used as the temporary source id. A virtual/vehicle source is not
claimed to work yet.

## Installation boundary

- Players install only the normal Steam Workshop mod.
- Players do not install Leaf, Java modules, DLLs, ffmpeg, or a custom launcher.
- The low-level loader and bridge are server-only components.
- The server administrator must install a compatible server-side Leaf Loader.
  A normal Workshop Lua mod cannot load Java bytecode by itself.

The built server plugin is:

```text
Contents/mods/LaccckaInternetRadioPoC/leaf/mods/
  LaccckaInternetRadioServerBridge.jar
```

Its `leaf.mod.json` uses `environment: server`, so it must not load on clients.
The earlier client FMOD bridge JAR is retained only as historical source and is
not part of this architecture.

## Expected server log

On a successful mixin load and player connection:

```text
[InternetRadioBridge][VOICE] RakVoice initialized; enabled=...; sampleRate=...
[InternetRadioBridge][TEST] tone worker started; version=0.8.0
[InternetRadioBridge][TEST] 440Hz generation started; ...
[InternetRadioBridge][TEST] tone finished; framesSent=...; ...
```

The final success criterion is not merely `framesSent`: the connected player
must actually hear the tone. If no `[InternetRadioBridge]` lines appear, the
server loader or mixin did not load. If the lines appear but there is no sound,
the log values distinguish invalid voice format from client rejection/routing.

Any Java/native error disables only this probe and is caught before it can
repeat in the server tick.

## Existing client controls

The client Lua remains deliberately unchanged except for its version log:

- F8: report that client HTTP is unavailable.
- F9: packaged `GameSoundClip` through vehicle emitter.
- F10: packaged positional world sound.
- F11: packaged sound through the vehicle emitter.

F11 remains the confirmed control proving that packaged audio and moving
vehicle positioning work independently of RakVoice.

## What a successful tone unlocks

Only after the tone is heard:

1. determine whether a source can be independent of a real `IsoPlayer`;
2. route and position a virtual source;
3. attach its coordinates to a vehicle;
4. synchronize radio state on 104.6 MHz;
5. add server-side WIVK HLS fetch and AAC decode;
6. reuse one decoded station stream for multiple vehicle sources.

If the ordinary client rejects server-injected frames or requires a real remote
player source, the RakVoice approach is closed without implementing HLS.

## Build

The JAR is compiled against narrow local stubs; no Project Zomboid classes are
bundled into it:

```bash
bash server-bridge-src/build.sh
```
