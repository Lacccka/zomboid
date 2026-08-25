# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction, dialogue and future quest/faction state.

## Scope of v0.2.9

v0.2.9 is the runtime-lifecycle and concurrent-client stabilization pass before the first real `QuestInstance` implementation.

It keeps the accepted v0.2.8 architecture intact:

- framework-owned logical NPC identity in `NPCRegistry`;
- provider-neutral client discovery from synchronized `runtimeId + npcId + x/y/z` anchors;
- Bandits2 only behind the server runtime adapter for physical spawn/resolve work;
- exact server validation of logical NPC, active runtime id, physical NPC, Z and distance;
- server-owned `DialogueSession` state and dialogue transitions.

v0.2.9 adds:

- explicit `RuntimeBindingRemove` synchronization from server to all clients;
- immediate client prompt removal when a Quest Framework NPC dies or is confirmed unloaded;
- immediate local dialogue-window invalidation when its runtime disappears or is replaced;
- server-side `DialogueSession.InvalidateRuntime()` so invalid physical NPCs cannot leave live logical dialogue sessions behind;
- Bandits runtime death invalidation through `Events.OnZombieDead`;
- a lightweight 250 ms server lifecycle reconciliation pass for loaded Quest Framework NPC handles;
- unload invalidation after a short grace interval rather than on a single missing-square observation;
- rematerialization discovery for inactive Bandits-backed runtime handles when players return near the last framework anchor;
- stale-runtime rejection before dialogue open/advance;
- a multi-client safety correction: an out-of-range resolve for one player no longer discards the shared cached physical handle used by another player.

Framework-owned persistence is still intentionally deferred.

## Runtime boundaries

```text
SERVER
Bandits physical NPC / brain
        |
        +--> BanditsServerRuntime
        |        spawn + resolve + lifecycle reconciliation
        |        death/unload/rematerialization
        |
        +--> framework binding + interaction anchor
                         |
                         v
SHARED NPCRuntime
runtimeId -> npcId
runtimeId -> anchor
npcId -> one active runtimeId
                         |
            +------------+------------+
            |                         |
            v                         v
CLIENT discovery               SERVER validation
prompt + E only                exact active runtime
                               physical NPC + distance
                                      |
                                      v
                               DialogueSession
```

A synchronized anchor remains discovery/presentation data only. It never authorizes a dialogue by itself.

## Binding lifecycle

The expected lifecycle is now explicit:

```text
physical NPC appears
    -> RuntimeBindingUpsert

physical NPC dies
    -> server UnbindRuntime
    -> RuntimeBindingRemove(reason=death)
    -> client prompt disappears
    -> matching dialogue session is invalidated

physical NPC unloads
    -> missing-square grace period
    -> server UnbindRuntime
    -> RuntimeBindingRemove(reason=unload)

same Bandits runtime rematerializes near its last anchor
    -> physical runtime is resolved again
    -> RuntimeBindingUpsert(reason=rematerialized internally)
    -> clients may discover it again
```

Binding a different runtime id for the same logical `npcId` still evicts the old shared runtime entry. Client-side upsert handling also closes a dialogue that still references the replaced runtime id.

## Important two-client correction

Before v0.2.9, `BanditsServerRuntime.ResolveForPlayer()` could clear its shared cached handle whenever one player's resolve attempt failed, including a normal out-of-range attempt. That behavior was unsafe once two clients interacted with the same logical NPC.

v0.2.9 only drops the cached entity when its physical identity is no longer valid. A player being too far away simply receives no handle; it does not mutate another player's shared runtime state.

Each player still owns an independent server `DialogueSession`, keyed per player. No dialogue node is client-authoritative.

## Test deployment

1. Sync/copy `LaccckaQuestFramework` and confirm server plus both clients load `0.2.9`.
2. Dedicated-server startup should include:

```text
[LCCQF][RUNTIME:BANDITS:SERVER] adapter registered module=LCCQFBanditsServerRuntime
[LCCQF][SERVER] loaded version=0.2.9 ... lifecycleReconcileMs=250
```

3. Both clients join and receive the same current runtime binding.
4. Put both players near Alexey. Both clients must independently log `interaction target acquired` for the same `npcId/runtimeId`.
5. Player A opens dialogue and moves between several nodes. Player B opens dialogue and chooses different nodes. Neither session may alter or close the other.
6. Move Player A outside server dialogue range and attempt another choice. A's session should close/reject while B remains able to continue.
7. Reconnect Player A. Full binding sync should reacquire the same currently active NPC without affecting B.
8. With both clients near the NPC, kill Alexey. Expected server sequence includes:

```text
[LCCQF][RUNTIME:BANDITS:SERVER] runtime invalidated reason=death npcId=... runtimeId=...
[LCCQF][SERVER] runtime binding removed npcId=... runtimeId=... reason=death closedSessions=...
```

Both clients should immediately log a runtime-binding removal and lose the interaction prompt. Any dialogue window referencing that runtime should close without waiting for another choice.
9. Spawn a replacement NPC. Both clients should acquire only the new runtime id; the old id must not reappear.
10. Exercise chunk unload/reload by leaving the area far enough for the physical NPC to unload, then returning. If Bandits materializes the same current-process runtime again, expect a removal on unload followed by a fresh upsert/reacquisition on rematerialization.

## Acceptance status

**v0.2.9 is implemented but not yet accepted in-game.**

The previous v0.2.8 one-client interaction/dialogue vertical slice remains accepted on Build 42.20.3. v0.2.9 now needs the two-client lifecycle run above before quest-state work begins.

The v0.2.8 acceptance report is stored in:

`docs/final-reports/quest-framework-interaction-dialogue-acceptance-2026-08-25.md`

## Regression constraints

Do not reintroduce:

- provider-specific physical lookup into generic client prompt discovery;
- client `getZombieList()` scans;
- identical relative Lua module names for different client/server runtime implementations;
- multiple active runtime ids for one logical framework `npcId`;
- clearing shared provider cache merely because one player is out of range;
- client-authoritative dialogue transitions;
- dialogue opening from an anchor without exact server physical validation;
- resurrection of framework identity from Bandits `brain.key`.

## Next milestone after v0.2.9 acceptance

Introduce the first minimal server-owned quest vertical slice:

```text
Dialogue offer
    -> QuestDefinition
    -> QuestInstance
    -> accept
    -> ReachArea objective
    -> objective complete
    -> return to NPC
    -> quest complete/cancel
```

The first quest kernel should remain independent of Bandits2 and should not yet include trading, rewards, a full journal or faction simulation. After that vertical slice works, add the first lightweight floating Quest Tracker so synchronized quest state can be inspected directly in-game before persistence is introduced.
