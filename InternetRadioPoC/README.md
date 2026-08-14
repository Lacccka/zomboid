# Internet Vehicle Radio — WIVK-FM PoC 0.2.0

Project Zomboid Build 42 proof of concept for a real internet stream attached to
vehicle sound emitters. It is a separate mod and does not change
`LaccckaCompatibilityPatch` or subscribed Workshop mods.

## Why version 0.2 uses a bridge

B42's public Lua emitter API treats an HTTPS URL as a `GameSound` name. The
0.1.x runtime logs confirmed that no network stream or playable handle is
created. Version 0.2 therefore removes every `playSound(URL)` call.

The included client-only Leaf mixin adds six narrowly named methods to the
current game's `FMODSoundEmitter`. It calls PZ's bundled `javafmod` wrapper to:

- create the HTTPS AAC stream with `FMOD_CREATESTREAM`;
- play it as a 3D channel at the vehicle emitter position;
- update position and radio volume;
- query playback state; and
- stop the channel and release its FMOD sound.

It does **not** replace `FMODSoundEmitter.class`. That matters on frequently
updated B42 clients: a full class copied from another game build can overwrite
vanilla changes and collide with unrelated Java mods.

## Station and multiplayer state

- Frequency: `104.6 MHz` (`104600` internally)
- Name: `WIVK-FM`
- UUID: `dea0ad58-9bd8-4a2c-b4e5-ca6f3714ae7e`
- Stream: `https://playerservices.streamtheworld.com/api/livestream-redirect/WIVKFMAAC.aac`

Vanilla vehicle `DeviceData` remains authoritative for power, frequency and
volume. Each nearby client opens the stream directly; the dedicated server
does not download, decode or relay audio.

The Lua controller scans the loaded cell through B42's concrete Java
`ArrayList` API (`size()` / `get()`). This replaces the previous generic
collection probing that produced nil-call errors. Every loaded vehicle within
60 tiles is handled, so a player does not need to enter a vehicle before hearing
its radio.

## Required client setup

Leaf Loader must be installed once on **every client that should hear internet
radio**. The Workshop package already places the compiled bridge JAR at Leaf's
per-mod production path:

`Contents/mods/LaccckaInternetRadioPoC/leaf/mods/LaccckaInternetRadioBridge.jar`

The dedicated server still enables the ordinary PZ mod/Workshop item, but does
not need to load the client audio bridge. Follow the current installer and
releases at `https://github.com/aoqia194/leaf-installer` and restart the game
after installation.

## Two-client test

1. Update Workshop item `3783046891` from this directory.
2. Install Leaf Loader on both clients and fully restart both games.
3. Join the B42.20.2 server; tune one vehicle radio to `104.6 MHz` and turn it on.
4. In each client `console.txt`, confirm:
   - `[LCC Internet Radio PoC] detected audio bridge 0.2.0`
   - `[LCC Internet Radio Bridge] javafmod API initialized`
   - `[LCC Internet Radio Bridge] stream started on FMOD channel ...`
5. Confirm players inside and near the car hear it, attenuation works, and the
   source follows a moving vehicle.
6. Tune away, mute, switch the radio off, walk beyond 60 tiles, and unload the
   vehicle; each case must produce one stop line and no lingering sound.

If the bridge is absent, Lua logs one clear installation message and does not
attempt a nil method call. If FMOD rejects the AAC URL, the bridge returns zero
and writes the root Java/FMOD error without throwing into `Events.OnTick`.

## Rebuilding the bridge JAR

The bridge source and compile-only Mixin annotation stubs live in `leaf-src`.
Run `leaf-src/build.sh` with a JDK 17+ runtime. The stubs are not packaged; at
runtime Leaf supplies the real Mixin classes. The script writes the JAR directly
to the Workshop mod's `leaf/mods` directory.

This remains a PoC until the above checks pass on two B42.20.2 clients. Stream
pooling and the station discovery API belong in the next stage, after the bridge
has proved AAC playback and lifecycle cleanup.
