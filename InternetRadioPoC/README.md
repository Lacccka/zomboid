# Internet Vehicle Radio — registered sound parser fix 0.5.1

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

## Result from 0.5.0

Build 42 removed `LCCInternetRadioTest` while parsing its sound script because
`distanceMin` and `distanceMax` were written as decimal values. These fields
are parsed as integers in B42. F10 therefore received an empty automatically
created sound and F11 returned handle `0`.

The runtime clip experiment also established that `GameSoundClip` can be read
from Lua, but its public Java field cannot be assigned with `clip.file = ...`.
Version 0.5.1 removes that blocked write and cannot produce that error.

## What 0.5.1 changes

The WAV remains registered as the normal `GameSound`
`LCCInternetRadioTest`, but its distances now use the B42-compatible integers
`3` and `30`.

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
   `GameSoundClip` read-only and calls `vehicleEmitter:playClip(clip, vehicle)`.

F9 must no longer perform or report `clip.file = absolute path`. It is a safe
control that confirms whether the exposed `playClip` method works with a
properly parsed registered clip.

The client log prefix is:

```text
[LCC Internet Radio PoC]
```

Please report whether each key was audible and include the complete labelled
blocks. The important startup line is
`GameSounds.isKnownSound(LCCInternetRadioTest): OK; returned=true`.
