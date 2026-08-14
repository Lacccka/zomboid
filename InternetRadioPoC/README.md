# Internet Vehicle Radio — WIVK-FM PoC 0.3.1

Compatibility proof of concept for Project Zomboid Build 42.20.2 multiplayer.

## Result

Real HTTP/HTTPS AAC radio cannot be implemented with the APIs exposed to a
normal Workshop-only Lua mod in the tested client:

- `vehicle:getEmitter():playSound(URL)` treats the URL as a local `GameSound`
  name instead of a network media source;
- the multiplayer client does not expose `luajava`, so Lua cannot bind the
  game's internal `fmod.javafmod` wrapper;
- therefore no client-side decoder can receive the WIVK-FM live stream without
  code installed outside the ordinary Workshop Lua mod.

The client log supplied for 0.3.0 confirms the decisive condition:

```text
[LCC Internet Radio PoC] luajava is unavailable; this B42 client cannot expose the built-in FMOD wrapper
```

## What 0.3.1 changes

Version 0.3.0 incorrectly left its vehicle-scanning `OnTick` callback active
after the FMOD binding had failed. It then called indexed `get()` on B42's
vehicle collection and emitted a nil-call error every 15 ticks.

Version 0.3.1 removes the tick callback and all unreachable streaming code. It
loads once, writes two diagnostic lines, and remains inert. It does not play
audio, change radio state, scan vehicles, or generate recurring errors.

## Installation contract

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

No Leaf, Java mod loader, executable, copied class, altered game file, or Steam
launch option is required. This safety update can continue to be distributed by
the server through the existing Workshop and mod lists.

## Viable Workshop-only direction

The supported alternative is a conventional vehicle-radio mod whose audio is
packaged locally with the Workshop item and registered as `GameSound` content.
It can provide positional vehicle audio, attenuation, movement, multiplayer
state, and automatic client distribution, but it cannot be the changing live
WIVK-FM internet broadcast.
