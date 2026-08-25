# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.8

- one Bandits2-backed test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- provider-neutral client prompt discovery from synchronized `runtimeId + npcId + x/y/z` state;
- Bandits2 is used only by the server runtime adapter for spawn and authoritative physical validation;
- exactly one active runtime id is retained for each logical framework `npcId`;
- `[E] Поговорить` is driven by `Events.OnKeyPressed` and the same framework-anchor query used by the prompt;
- server-side runtime-id, physical NPC, same-Z and distance validation before dialogue opens;
- server-owned `DialogueSession`, current node and allowed choices;
- no framework-owned NPC persistence yet.

## Why v0.2.8 is a structural correction

The fresh 0.2.7 multiplayer acceptance log removed the remaining uncertainty from the client side. The client received valid framework bindings and anchors and repeatedly calculated them as eligible at distances between roughly 0.2 and 2 tiles. Pressing `E` executed the diagnostic path, but `NPCRuntime.FindNearestInteractive()` still returned nil. There was no `interaction target acquired`, no `interaction requested` and therefore no server dialogue request.

This proves the failure was no longer spawn, networking, anchor data, distance arithmetic, input delivery or Bandits physical-object synchronization. It was inside the extra provider-adapter dispatch between the already-valid framework binding state and target selection.

The 0.2.7 adapter also contained a hidden `player:getVehicle()` early return that was not represented by the old `eligible=true` diagnostic. The log does not prove the player was in a vehicle, so this is not claimed as the sole cause, but it demonstrates why provider-specific gates did not belong in generic prompt selection.

A second concrete design defect was found by reading the actual Build 42.20.3 Lua loader. Dedicated Server loads Lua paths in this order: `shared`, `client` for checksum, then `server`. `require()` resolves the first matching module name from those registered paths. We had two different implementations with the same relative require path:

```text
client/LCCQF/Runtime/LCCQFBanditsRuntime.lua
server/LCCQF/Runtime/LCCQFBanditsRuntime.lua
```

The fresh server log directly exposed the collision by printing the client-only marker `client discovery=server-anchor physicalLookup=false` during dedicated-server startup. That module-name collision made adapter registration dependent on loader/path order and was invalid architecture even independently of the prompt failure.

v0.2.8 removes both problems instead of adding another Bandits lookup.

## Runtime boundaries

```text
SERVER
Bandits physical NPC / brain
        |
        +--> BanditsServerRuntime
        |        spawn + physical validation
        |
        +--> framework binding + interaction anchor
                         |
                         v
SHARED NPCRuntime
one active runtimeId per npcId
provider-neutral nearest-anchor selection
                         |
                         v
CLIENT
prompt + E request only
                         |
                         v
SERVER
resolve the real NPC again
validate runtimeId + distance + Z
                         |
                         v
DialogueSession
```

`NPCRuntime.FindNearestInteractive()` now iterates framework bindings and anchors directly. It does not dispatch through a client Bandits adapter. Consequently there is no client `LCCQFBanditsRuntime.lua` anymore.

The real Bandits server adapter now lives at the unique module path:

`LCCQF/Runtime/LCCQFBanditsServerRuntime`

The old server module path remains only as a compatibility wrapper that forwards to the uniquely named server module. There is no client file with the same relative path, so Build 42.20's path-order behavior cannot select the wrong implementation.

## Binding lifecycle

`NPCRuntime` keeps:

- `runtimeId -> npcId`;
- `runtimeId -> {x,y,z}`;
- `npcId -> active runtimeId`.

Binding a new runtime id for the same logical NPC evicts the previous id and anchor. This prevents the 0.2.7 state in which `7340111`, `10485949` and later `11141215` could all be advertised for the same `lccq_test_npc_01`.

Framework-owned NPC persistence is still deferred. The Bandits server adapter therefore no longer rebuilds Quest Framework bindings from every historical `BanditClusters` entry during startup. Old experimental `brain.key` string identity is not restored either. A current-process binding is synchronized normally; when spawning, a real framework-owned NPC physically near the player may be reused, but a far historical cluster can no longer block a fresh test spawn.

## Test deployment

1. Sync/copy `LaccckaQuestFramework` and confirm both sides load `0.2.8`.
2. On dedicated-server startup expect the server runtime marker:

```text
[LCCQF][RUNTIME:BANDITS:SERVER] adapter registered module=LCCQFBanditsServerRuntime
[LCCQF][SERVER] loaded version=0.2.8 ...
```

3. Join the server. With persistence deferred, a clean startup may synchronize `count=0` until the test NPC is spawned/rebound.
4. Spawn the test NPC. Expected client sequence:

```text
[LCCQF][CLIENT] loaded version=0.2.8 ... discovery=framework-anchor
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=... anchor=x,y,z
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=... distance=...
```

5. Press `E`. Expected:

```text
[LCCQF][CLIENT] interaction requested npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][SERVER] dialogue opened session=... player=... npcId=lccq_test_npc_01
[LCCQF][CLIENT] dialogue state session=... node=...
```

## Validation status

**0.2.8 passed the dedicated-server/client vertical-slice acceptance on Build 42.20.3.**

The accepted run confirmed:

- initial clean full sync with `count=0`;
- runtime binding broadcast and immediate client `interaction target acquired`;
- `[E]` interaction request delivery;
- server-authoritative dialogue open;
- repeated `start / who / work` node transitions;
- session close and finish paths;
- authoritative rejection after the first NPC died;
- replacement with a new runtime id and successful interaction;
- reconnect/full-sync recovery with `count=1` and automatic target reacquisition;
- client range loss and `DialogueTooFar` enforcement;
- no Quest Framework Lua runtime exception or crash during the accepted interaction sequence.

The final acceptance report is stored in:

`docs/final-reports/quest-framework-interaction-dialogue-acceptance-2026-08-25.md`

Two residual observations remain intentionally open: stale client binding/anchor state is not proactively removed when the physical NPC dies or unloads, and one near-boundary request after reconnect was transiently rejected as `NPCUnavailable` before succeeding less than a second later.

## Next milestone

Do one stabilization/multiplayer pass before adding real quest state:

1. add explicit runtime binding invalidation/removal for NPC death/unload/replacement;
2. instrument or smooth the short client/server range-edge mismatch without weakening exact runtime-id validation;
3. run a two-client acceptance: both clients discover the same NPC, maintain independent `DialogueSession` state, survive one client disconnect/reconnect, and receive binding invalidation consistently;
4. after that passes, introduce the first minimal server-owned `QuestInstance` lifecycle: offer -> accept -> objective state -> complete/cancel, with journal/trading/rewards still deferred.
