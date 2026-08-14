# Internet Vehicle Radio — WIVK-FM PoC

Minimal Project Zomboid Build 42 proof of concept for an internet stream attached to a vehicle sound emitter.

This is a separate mod and does not modify `LaccckaCompatibilityPatch` or subscribed Workshop content.

## Scope

- Adds a local `WIVK-FM` preset at `104.6 MHz` to loaded vehicle radios.
- Uses the vanilla vehicle radio state as the multiplayer source of truth:
  power, channel and volume are already synchronized by Project Zomboid.
- On each client, attempts the exact direct path that must be proven first:
  `vehicle:getEmitter():playSound(https AAC URL)`.
- Starts only within 60 tiles, follows the vehicle emitter, mirrors radio volume,
  and stops when the player moves away, the vehicle unloads, the radio is switched
  off/muted, or another frequency is selected.
- Keeps at most one stream handle per vehicle on each client.
- This PoC intentionally tracks only the current or last-entered vehicle on each
  client. It does not scan every vehicle in the world before HTTP/AAC support is proven.

Station used by the PoC:

- UUID: `dea0ad58-9bd8-4a2c-b4e5-ca6f3714ae7e`
- Name: `WIVK-FM`
- Stream: `https://playerservices.streamtheworld.com/api/livestream-redirect/WIVKFMAAC.aac`

The discovery API is intentionally not called in this first test. The returned
stream URL is fixed in the client script so the test isolates FMOD/HTTP/AAC support.

## Important limitation

The exposed B42 API documents `BaseSoundEmitter:playSound(String file)`, but normal
Project Zomboid use resolves registered local `GameSound` resources. The public API
does not document HTTP stream creation, codec buffering, reconnects or a PCM feed.
Therefore this mod is an instrumented compatibility test, not a claim that direct
streaming already works.

Existing music mods do not prove URL support: they register local `.ogg` files as
named `GameSound` resources and pass those names to the emitter. This PoC passes an
HTTPS AAC URL, which is a different FMOD input path.

Expected client-log outcomes:

- `FMOD reports the HTTPS handle as playing...` — FMOD kept the handle alive;
  confirm that decoded AAC is actually audible in-game before accepting the direct path.
- `returned no handle` or `direct HTTPS AAC was not playable` — direct streaming is
  unavailable and the next stage requires a native/client decoder bridge.

## Install for a local/dedicated test

Copy `Contents/mods/LaccckaInternetRadioPoC` into the server/client `mods` directory,
or publish this folder as a new Workshop item. Add this mod ID to the server:

```ini
Mods=...;LaccckaInternetRadioPoC
```

After publishing, add its new numeric ID to `WorkshopItems=`. This mod has no
dependency on `LaccckaB4220Compat` and its load order is not significant.

## Two-client test checklist

1. Start a B42.20.x dedicated server with the mod enabled on server and both clients.
2. On each client, briefly enter the test vehicle once; then open the radio UI.
3. Turn it on, choose `104.6 MHz WIVK-FM`, and set volume above zero.
4. Confirm only one start line appears per vehicle in each client's `console.txt`.
5. Check the log for either the live-handle line or the explicit bridge-required result.
6. If audio plays, let one player exit and verify the vehicle remains the 3D source.
7. Drive the vehicle and verify that the source moves with it.
8. Walk beyond 60 tiles, return, switch frequency, mute, and turn the radio off.
9. Despawn/unload the vehicle and confirm that the handle is stopped.

## If the direct test fails

Do not add server-side audio transport. The viable next stage is a client-native
bridge that:

1. resolves the station API;
2. opens and decodes the AAC stream;
3. feeds PCM into an FMOD user/open-user sound or a custom native plugin;
4. exposes start/stop/volume/3D-position controls to Lua;
5. leaves the dedicated server responsible only for vehicle radio state.

One decoder connection cannot automatically be reused by multiple positional FMOD
emitters through the public Lua API. Pooling one station stream across several cars
must be designed inside that bridge after the PoC result is known.
