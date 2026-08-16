# Internet Vehicle Radio — RadioBot Stage 1 (0.10.0)

Server-only transport proof of concept for Project Zomboid Build 42.20.2.

Workshop ID: `3783046891`  
Mod ID: `LaccckaInternetRadioPoC`

## Current milestone

Version 0.10.0 implements the first isolated RadioBot experiment. The Leaf
bridge remains inside the dedicated-server JVM, but starts the vanilla
`FakeClientManager` in a separate child JVM after the first ordinary player
joins.

This version tests only:

```text
child process -> 127.0.0.1:16261 -> authenticate -> create player -> GUID/onlineID
```

It deliberately does not initialize client VOIP, send PCM, or play Internet
audio yet. A successful Stage 1 is required before enabling client-side
`RakVoice` in Stage 2.

## Safety boundary

Version 0.9.1 proved that `RakVoice.SendFrame` from the dedicated-server native
context can terminate the whole server process. Version 0.10.0 contains no
reference to `RakVoice.SendFrame` and does not load client native libraries in
the server JVM.

The child process loads `RakNet64` and `ZNetNoSteam64`. If that client bootstrap
is incompatible with a Steam dedicated server, only the child exits; the main
server remains running and logs the result.

## Automatic behavior

1. Leaf starts the bridge with the dedicated server.
2. The bridge waits for one real, fully connected player.
3. It waits another five seconds and uses that player's coordinates as the bot
   spawn point.
4. It reads the server's current Lua and script checksums.
5. It writes `.leaf/radio-bot/stage1-scenario.json`.
6. It starts a child JVM using the server's own Java runtime,
   `projectzomboid.jar`, native directory, and bridge JAR.
7. The child invokes the vanilla `FakeClientManager` with movement ID `1046`.
8. On normal server shutdown, the bridge also terminates the child process.

Clients do not install Leaf, Java, a decoder, DLLs, or a custom launcher.

## Expected log

Before joining:

```text
[InternetRadioBridge][BOOT] version=0.10.0; ... stage=connection-only
[InternetRadioBridge][SUPERVISOR_OK] ...
[InternetRadioBridge][WAIT] join one ordinary player to start RadioBot Stage 1
```

After joining:

```text
[InternetRadioBridge][PLAYER_READY] ...
[InternetRadioBridge][CHECKSUM] luaPresent=true; scriptPresent=true
[InternetRadioBridge][BOT_START] ...
[InternetRadioBridge][BOT_PROCESS] pid=...; started=true
[InternetRadioBot] [BOOT] ...
[InternetRadioBot] [FAKECLIENT_ENTER] ...
[InternetRadioBot] [CONNECTION_STATE] guid=...; onlineId=...
```

Success is:

```text
[InternetRadioBot] [CONNECTED] authenticated=true; playerCreated=true; ...
```

Important failure markers are `BOT_START_FAIL`, `FAKECLIENT_FAIL`,
`CONNECTION_ABORTED`, `CONNECT_TIMEOUT`, and `BOT_EXIT`. Send both server and
client logs after the test even if the server stays running.

## One-client test procedure

1. Update Workshop item `3783046891` and fully restart the server.
2. Confirm Leaf loads `lcc-internet-radio-server-bridge 0.10.0`.
3. Join with the single ordinary client and remain in the world for at least
   two minutes.
4. No radio tuning is required in Stage 1 and no sound is expected.
5. Confirm whether `[CONNECTED]` appears, then shut down normally with `quit`.

F9/F10/F11 remain local packaged-audio diagnostics and are unrelated to this
connection test.

## Installation path

The Workshop source contains:

```text
Contents/mods/LaccckaInternetRadioPoC/leaf/mods/
  LaccckaInternetRadioServerBridge.jar
```

The existing server batch file may continue copying that JAR into:

```text
.leaf/runtime-mods/LaccckaInternetRadioServerBridge.jar
```

## Decision after Stage 1

- If `[CONNECTED]` appears, Stage 2 enables VOIP in the child and tests
  `440 Hz -> client RakVoice -> server` before adding radio routing on 104.6.
- If the no-Steam fake client cannot authenticate to the Steam server, the
  failure log identifies the boundary for a Steam-compatible client bootstrap.

Only after the transport works will the project add the HLS/AAC decoder,
station sessions, distance management, and multiple frequencies.

## Build

```bash
bash server-bridge-src/build.sh
```

The build uses narrow compile-time stubs. No Project Zomboid or Leaf classes are
bundled in the resulting JAR.
