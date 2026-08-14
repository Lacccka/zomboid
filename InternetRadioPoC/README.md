# Internet Vehicle Radio — local WAV and vehicle PoC 0.4.1

Minimal Workshop-only playback probe for Project Zomboid Build 42.20.2
multiplayer. It tests the supplied `test.wav` without registering a `GameSound`.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Scope

This version deliberately contains no HTTP, AAC, HLS, vehicle enumeration,
multiplayer radio synchronization, `OnTick`, `luajava`, or external loader.
Every potentially unsupported audio call is isolated with `pcall`, so one
failed method is logged once instead of producing a recurring error.

The WAV must be present at:

```text
Contents/mods/LaccckaInternetRadioPoC/42/media/sound/test.wav
```

It is intentionally not declared in a sound script. The purpose is to determine
whether B42 can load a plain WAV at runtime without a pre-existing `GameSound`.

## Confirmed result from 0.4.0

On B42.20.2 multiplayer, `F10` successfully played the unregistered WAV with:

```text
PlayWorldSoundWav("test", ...) -> fmod.fmod.FMODAudio
```

The previous F8 path was invalid because `getDir()` returned the root mod
directory while versioned content is stored under an additional `/42` folder.
Version 0.4.1 corrects that path before drawing conclusions about arbitrary
absolute-file playback.

## Tests

After updating Workshop item `3783046891`, fully restart the client and join the
server. Run the tests one at a time:

- `F8` — use the corrected `/42/media/sound/test.wav` absolute path with
  `PlayWorldSoundWav` at the player's square;
- `F9` — use that corrected absolute path with the 2D `PlaySoundWav` method;
- `F10` — repeat the confirmed named positional-world test as a control;
- `F11` — while seated in a vehicle, call its emitter with the unregistered
  name `test`, then enable 3D and query the returned handle;
- `F12` — repeat the vehicle-emitter test with the corrected absolute path.

The client log begins every line with:

```text
[LCC Internet Radio PoC]
```

For each key, record whether the WAV was audible and provide the complete block
from `test started` through `test finished`. A method returning `nil` is not by
itself proof of failure: audible output and subsequent engine log messages are
the decisive observations.

For F11 and F12, remain seated in the vehicle until the WAV finishes. If sound
plays, drive several tiles and ask a second nearby client whether the source
moves with the car.

## Decision after the test

- If `F8` works, the positional sound method accepts an arbitrary full path;
  the next test can create or download a file outside `media/sound` at runtime.
- `F10` is already confirmed and establishes packaged positional WAV playback.
- If F11 or F12 returns a non-zero handle and is audible, the standard vehicle
  emitter can play the WAV and automatically follow the moving vehicle.
- If both emitter tests fail, named world WAV remains usable, but moving-source
  behavior will require controlled segment restarts or another exposed handle.
