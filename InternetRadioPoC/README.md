# Internet Vehicle Radio — automatic download PoC 0.6.0

Workshop-only playback probe for Project Zomboid Build 42.20.2 multiplayer.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Confirmed by 0.5.1

- The packaged `GameSound` is parsed and known by the game.
- F10 plays it positionally at the player's square.
- F11 plays it through the vehicle emitter with a non-zero handle and
  `isPlaying=true`.
- F9 retrieves the registered `GameSoundClip`, whose file is
  `media/sound/test.wav`, and plays it directly through the vehicle emitter.
- The emitter source moves with the vehicle.

This confirms the supported sound-name, clip, emitter, 3D position, and moving
vehicle portions of the architecture.

## New F8 test

F8 tests the remaining Workshop-only file bridge without requiring any manual
client installation:

1. PZ downloads a finite public test WAV with `getUrlInputStream`.
2. The returned binary bytes are saved with `getFileOutput` under
   `Zomboid/Lua/LCCInternetRadioPoC/downloaded-test.wav`.
3. Lua writes a temporary sound definition beside it.
4. `GameSounds.ReloadFile` registers the temporary sound as
   `LCCInternetRadioDownloadedTest`.
5. The vehicle emitter plays that downloaded sound by its registered name.

The external file is a small public-domain/licensed-for-reuse audio test from
the public `ArtskydJ/test-audio` repository. It is deliberately a very short
drip sound, not WIVK music. The dynamic sound loops so it is easy to hear and
stops when another test starts or the game exits.

No `OnTick`, vehicle enumeration, Java mod, Leaf loader, native library, or
manual client installation is used. The server does not download or relay the
audio.

## Test order

After uploading the Workshop update, fully restart both server and client.

1. Sit inside a vehicle.
2. Press `F8` once. A short pause while 37 KB downloads is possible.
3. Listen for the repeating downloaded drip sound and drive a few tiles.
4. Press `F11` to stop it and confirm the packaged music still works.

The decisive successful F8 lines are:

```text
getFileInput(downloaded WAV): OK; returned=java.io.DataInputStream@...
GameSounds.isKnownSound(downloaded): OK; returned=true
downloaded clip:getFile: OK; returned=.../Zomboid/Lua/LCCInternetRadioPoC/downloaded-test.wav
vehicle emitter:playSound(downloaded name): OK; returned=<non-zero handle>
vehicle emitter:isPlaying: OK; returned=true
```

If F8 fails, provide the complete block from
`automatic WAV download ... started` through its stop/finish line. F9, F10,
and F11 remain unchanged controls.

## Decision after F8

If F8 succeeds, the mod can automatically download finite HLS-derived audio
segments, generate or refresh a named sound slot, and attach that slot to a
vehicle without any client-side installation outside Steam Workshop. The next
step will replace the public test WAV with station segment acquisition and add
buffered segment rotation.
