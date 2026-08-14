# Internet Vehicle Radio — registered sound and runtime clip PoC 0.5.0

Workshop-only playback probe for Project Zomboid Build 42.20.2 multiplayer.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Confirmed by 0.4.1

- `test.wav` plays by its short name.
- `vehicle:getEmitter():playSound("test")` returns a non-zero handle.
- The sound source moves with the vehicle.
- Passing an absolute WAV path directly to the vehicle emitter returns handle
  `0` and does not play.

The vehicle and 3D-audio portion is therefore working. The remaining problem is
how to make a file obtained at runtime visible to FMOD under a usable sound
name.

## What 0.5.0 changes

The WAV is now registered in a normal `media/scripts` sound definition as
`LCCInternetRadioTest`. This is the same supported mechanism used by ordinary
music and sound mods, and removes the engine warning about an unknown sound.

There is no `OnTick`, vehicle enumeration, HTTP request, AAC/HLS decoder,
`luajava`, Leaf, or external client installation. Every experimental Java/Lua
boundary call is isolated with `pcall`, so an unsupported operation produces a
single labelled log line instead of a recurring error flood.

## Test order

After the Workshop item updates, fully restart the game and join the server.

1. Press `F10` on foot. The WAV should play at the player's square through the
   registered sound name.
2. Sit inside a vehicle and press `F11`. The WAV should play from the vehicle
   and move with it. This is the registered-name control.
3. Still inside the vehicle, press `F9`. The mod obtains the registered
   `GameSoundClip`, changes its `file` field to the fully resolved path of the
   same Workshop WAV, and calls `vehicleEmitter:playClip(clip, vehicle)`.

`F9` is the critical new result:

- if it is audible and returns a non-zero handle, a runtime-downloaded WAV can
  be inserted into this clip slot next;
- if field assignment or `playClip` is unavailable, the labelled log output
  identifies exactly which boundary B42 blocks.

The client log prefix is:

```text
[LCC Internet Radio PoC]
```

Please report whether each key was audible and include the complete labelled
block for `F9`. F12 is no longer used, so Steam's screenshot binding is not
triggered by this version.
