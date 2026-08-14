# Internet Vehicle Radio — B42 HTTP boundary result 0.6.1

Workshop-only probe for Project Zomboid Build 42.20.2 multiplayer.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Confirmed working

- A packaged WAV registered as `LCCInternetRadioTest` is known by the game.
- F10 plays it positionally at the player's square.
- F11 plays it through the vehicle emitter with a non-zero handle and the
  source follows the moving vehicle.
- F9 obtains its `GameSoundClip` and plays the clip directly through the same
  emitter with a non-zero handle.
- Distance, movement, stop, and normal Workshop distribution work without a
  Java/Leaf/native installation.

## Confirmed blocked

Version 0.6.0 attempted a finite client-side WAV download. On B42.20.2
multiplayer the call failed before any network connection was attempted:

```text
getUrlInputStream: FAILED; Tried to call nil
```

The Java method still appears in generated engine documentation, but the
multiplayer client Lua environment does not export it as a callable global.
The client therefore cannot fetch the station API, playlist, AAC segment, or
converted WAV by this route.

Direct assignment to `GameSoundClip.file` is also blocked by Kahlua. Direct
absolute paths and URLs supplied as sound names return emitter handle `0`.
The stock file-sound path resolves local game/mod files before asking FMOD to
load them, so a normal registered clip does not provide an HTTP transport.

Version 0.6.1 removes every download call. F8 is now a no-error report that
prints:

```text
type(getUrlInputStream)=nil
client-side HTTP download is unavailable in B42.20.2 multiplayer Lua
```

F9, F10, and F11 remain the confirmed local-audio controls.

## Architectural consequence

Under the requirement that players install only the Steam Workshop mod, live
internet audio cannot be fetched independently by each B42.20.2 client with
the currently exposed Lua/FMOD interfaces.

The remaining implementation choices are:

1. **Dedicated-server relay** — the server downloads finite audio segments and
   sends their bytes to nearby clients. This requires no manual client install,
   but makes the server carry audio bandwidth and is different from the
   original state-only architecture.
2. **Client Java/native bridge** — clients download directly and FMOD can use a
   stream decoder, but this requires installation outside the ordinary Lua
   Workshop lifecycle and has already been rejected for this project.
3. **Packaged audio** — keep the working vehicle radio behavior with files
   included in Workshop, but it is not live internet radio.

No server-relay implementation is included until its bandwidth and security
trade-off is explicitly accepted.
