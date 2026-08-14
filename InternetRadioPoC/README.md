# Internet Vehicle Radio — local WAV PoC 0.4.0

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

## Tests

After updating Workshop item `3783046891`, fully restart the client and join the
server. Run the tests one at a time:

- `F8` — resolve the absolute mod path, call `CacheSound(path)`, then
  `PlaySoundWav(path, false, 1.0)`;
- `F9` — call `CacheSound("media/sound/test.wav")`, then
  `PlaySoundWav("test", false, 1.0)`;
- `F10` — play the same unregistered name at the player's current square with
  `PlayWorldSoundWav`, radius 30.

The client log begins every line with:

```text
[LCC Internet Radio PoC]
```

For each key, record whether the WAV was audible and provide the complete block
from `test started` through `test finished`. A method returning `nil` is not by
itself proof of failure: audible output and subsequent engine log messages are
the decisive observations.

## Decision after the test

- If `F8` works, an arbitrary downloaded file can be passed to the sound system.
- If only `F9` works, direct named WAV loading exists, but runtime cache/reload
  behavior must be tested before implementing HLS chunks.
- If `F10` works, the same path supports positional audio and can advance to a
  vehicle-emitter experiment.
- If none work, the ordinary Workshop Lua environment has no usable dynamic
  local-file playback path and HLS downloading cannot solve the audio-backend
  restriction.
