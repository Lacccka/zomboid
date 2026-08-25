# Quest Framework interaction — 0.2.8 working state

Date: 2026-08-25

Branch: `agent/b42-20-compatibility-patch`

Status: implementation changed after a failed 0.2.7 dedicated-server acceptance run; fresh 0.2.8 in-game acceptance is still required. This is not a final report.

## Decisive 0.2.7 runtime evidence

The fresh client log loaded Quest Framework 0.2.7 and synchronized two runtime bindings. For `lccq_test_npc_01` runtime `7340111`, the synchronized anchor was about 1.31 tiles from the player and the diagnostic reported `sameZ=true eligible=true`. Pressing `E` later repeatedly produced `source=key-no-target` while the same binding remained within interaction range.

A newly spawned runtime `11141215` reproduced the same state. The client received its anchor, repeatedly calculated distances below the 3.25 tile interaction range, and still never logged `interaction target acquired` or `interaction requested`. Therefore the failure was after binding/anchor synchronization and input delivery but before target selection returned from `NPCRuntime.FindNearestInteractive()`.

The client also physically received both tested Bandits NPCs through the independently working clothing-restore path. Physical NPC synchronization is therefore not the missing prerequisite.

## Build 42.20.3 loader finding

The decompiled game source shows that dedicated server initializes Lua paths as:

1. `shared`;
2. `client` with `onlyChecksum=true`;
3. `server`.

`LoadDirBase` registers each path even when `onlyChecksum=true`. Global `require()` then walks the registered paths in order and loads the first matching module name.

Quest Framework 0.2.7 had different client and server implementations at the identical relative module path:

```text
client/LCCQF/Runtime/LCCQFBanditsRuntime.lua
server/LCCQF/Runtime/LCCQFBanditsRuntime.lua
```

The failed dedicated-server log proves the consequence: it printed the client-only marker `client discovery=server-anchor physicalLookup=false` during server startup. The module boundary was therefore load-path dependent and invalid.

## 0.2.8 correction

Client prompt selection no longer dispatches through a provider adapter. `NPCRuntime.FindNearestInteractive()` directly evaluates framework-owned runtime bindings, registry definitions and synchronized interaction anchors.

The client Bandits runtime module was removed. Bandits-specific client state, `IsoZombie`, `BanditBrain`, Bandit caches and `getMovingObjects()` are not prerequisites for showing the prompt.

The actual Bandits server adapter moved to the unique module path:

`LCCQF/Runtime/LCCQFBanditsServerRuntime`

The old server module name is only a compatibility wrapper. There is no same-name client implementation anymore.

`NPCRuntime` now enforces one active runtime id per logical `npcId`. Binding a replacement runtime id evicts the previous binding and anchor. This addresses the 0.2.7 run in which multiple historical runtime ids (`7340111`, `10485949`, then `11141215`) were simultaneously synchronized for the same logical test NPC.

Framework-owned NPC persistence remains deferred. The server adapter no longer reconstructs bindings from every historical `BanditClusters` brain at startup, and legacy string identity in Bandits `brain.key` is not restored.

## 0.2.8 acceptance markers

Expected server startup:

```text
[LCCQF][RUNTIME:BANDITS:SERVER] adapter registered module=LCCQFBanditsServerRuntime
[LCCQF][SERVER] loaded version=0.2.8 ...
```

The server must not print the removed client marker.

Expected client after a binding with an in-range anchor:

```text
[LCCQF][CLIENT] loaded version=0.2.8 ... discovery=framework-anchor
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=... anchor=...
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=... distance=...
```

After pressing `E`:

```text
[LCCQF][CLIENT] interaction requested ...
[LCCQF][SERVER] dialogue opened ...
[LCCQF][CLIENT] dialogue state ...
```

Do not promote this issue to `docs/final-reports` until the full chain is confirmed in-game.
