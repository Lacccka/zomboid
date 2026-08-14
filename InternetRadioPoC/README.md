# Internet Vehicle Radio — WIVK-FM PoC 0.3.0

Project Zomboid Build 42 multiplayer proof of concept for a real internet
stream attached to vehicle radios.

## Installation contract

This is a normal Workshop-only mod. Clients install it through the server's
existing `WorkshopItems` / `Mods` list. It requires no Leaf, ZombieBuddy,
executable, copied class, altered game file, or Steam launch option.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Audio path

B42 Lua provides `luajava`, which can bind public classes already bundled with
the game. The client script binds `fmod.javafmod` and invokes the native FMOD
wrapper directly:

1. `FMOD_System_CreateSound(URL, FMOD_CREATESTREAM)`
2. `FMOD_System_PlaySound(sound, paused=true)`
3. configure the channel as 3D and set its vehicle position/range/volume
4. unpause, update it while the vehicle moves, and periodically verify playback
5. stop the channel and release the sound when it is no longer needed

This differs from the failed 0.1 path. `emitter:playSound(URL)` treated the URL
as a `GameSound` name. Version 0.3 bypasses `GameSound` resolution but does not
load any external Java code: `fmod.javafmod` is part of Project Zomboid itself.

## Station and multiplayer state

- Frequency: `104.6 MHz` (`104600` internally)
- Name: `WIVK-FM`
- UUID: `dea0ad58-9bd8-4a2c-b4e5-ca6f3714ae7e`
- Stream: `https://playerservices.streamtheworld.com/api/livestream-redirect/WIVKFMAAC.aac`

Vanilla vehicle `DeviceData` remains authoritative for radio power, frequency
and volume. Each client within 60 tiles opens the live stream directly. The
dedicated server synchronizes normal vehicle state and never carries audio.

Loaded vehicles are enumerated using B42's concrete Java `ArrayList`
`size()` / `get()` interface. This avoids the generic collection probing that
caused the earlier nil-call errors and lets a client hear nearby cars without
entering them first.

## Two-client test

1. Publish/update Workshop item `3783046891` from this directory.
2. Let the dedicated server distribute the update normally; do not install any
   external loader on either client.
3. Fully restart both clients and join the B42.20.2 server.
4. Confirm each client log contains:
   - `pure Workshop mode; no Leaf, Java mod loader, or manual client installation`
   - `bound built-in fmod.javafmod; pure Workshop streaming path is ready`
5. Turn on one vehicle radio and tune it to `104.6 MHz`.
6. Confirm both clients log `started WIVK-FM for vehicle ...` and hear the same
   live broadcast near the car.
7. Drive the car and verify positional movement and attenuation.
8. Tune away, mute, turn off, walk beyond 60 tiles, and unload the vehicle.
   Each action must stop the relevant client channel without lingering audio.

If class binding succeeds but stream creation returns zero, the remaining issue
is the B42.20.2 FMOD build's handling of this HTTPS/AAC endpoint—not mod loading.
That result would require changing the station transport/codec or accepting a
local-audio Workshop radio instead of requiring a client installer.
