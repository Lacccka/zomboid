# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.7

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- replaceable `NPCRuntime` contract with Bandits2 isolated in the server runtime adapter;
- server-synchronized interaction anchors (`runtimeId + npcId + x/y/z`) for stationary quest NPC prompt discovery;
- no client-side Bandits physical-object lookup for prompt eligibility;
- `[E] Поговорить` interaction prompt refreshed from synchronized anchor state;
- `E` uses the standard `Events.OnKeyPressed` path and re-evaluates the target immediately on input;
- server-side registry, runtime-id, same-Z, physical NPC and distance validation before dialogue opens;
- server-owned `DialogueSession`, current node and allowed choices;
- compact `RequestDialogue / ChooseDialogue / CloseDialogue` protocol;
- admin/debug context-menu action for spawning the test NPC;
- UTF-8-safe client localization;
- no quests, rewards, reputation, journal, vendors, audio or framework-owned persistence yet.

## Why v0.2.7 changes the interaction architecture

The 0.2.6 multiplayer acceptance log proved that the previous client-discovery approach was solving the wrong problem.

For runtime id `7340111`, the server successfully synchronized the Quest Framework binding and the client successfully observed the physical Bandits object through `Bandit.ApplyVisuals`. Despite both facts, no `interaction target acquired` marker was ever produced. The 0.2.6 adapter also emitted neither its `physical object rejected` nor its delayed `physical object unresolved` diagnostic. In other words, continuing to add more ways for the client to rediscover a Bandits `IsoZombie` was not giving us a stable interaction contract.

The same log contains the more useful fact: when the spawn action was invoked again, the server returned `AlreadyNearby`. That response is produced only after the server-side Bandits adapter successfully resolves the physical NPC near the requesting player. The server physical-resolution path therefore already works; duplicating it on the client is unnecessary.

v0.2.7 separates the two responsibilities:

```text
SERVER
Bandits brain / physical NPC
        |
        +--> runtimeId + npcId + interaction anchor (x/y/z)
                          |
                          v
CLIENT                    prompt / nearest-target arithmetic only
                          |
                     player presses E
                          |
                          v
SERVER             resolve real Bandits NPC again
                   validate exact runtimeId + range
                          |
                          v
                     DialogueSession
```

For the current stationary NPC, the interaction anchor is taken from the live server object when available and otherwise from Bandits' `brain.bornCoords`. The anchor is presentation/discovery data only. It is not trusted to open dialogue: pressing `E` still sends only `npcId` and `runtimeId`, and the server must resolve the real physical Bandits object inside `SERVER_INTERACTION_RANGE` before a session can open.

This is also cleaner for the future framework. Quest identity no longer depends on `Bandit.ApplyVisuals`, `BanditZombie.Cache`, `CacheLightB`, `getVariableBoolean("Bandit")`, or a client `getMovingObjects()` scan. A future runtime provider only needs to expose an authoritative interaction anchor and implement server-side physical validation. Moving NPCs will require explicit anchor updates; v0.2.7 intentionally treats the current `stationary=true` NPC as the supported slice.

## Runtime binding lifecycle

`NPCRuntime` now stores two related pieces of state:

- `runtimeId -> npcId` identity binding;
- `runtimeId -> {x,y,z}` interaction anchor.

Full binding synchronization replaces both maps atomically. The Bandits server adapter rebuilds the binding set from the current `BanditClusters` snapshot instead of appending into an old Lua global table. This prevents stale test-runtime ids from surviving a Lua reset and being sent back to clients.

## Client update/input path

The prompt no longer relies on `Events.OnTick` polling. `OnPostUIDraw` performs a cheap throttled nearest-anchor calculation every 100 ms, and `Events.OnKeyPressed` forces one immediate calculation before handling `E`. The working Chat With Me reference also uses `Events.OnKeyPressed` for its interaction trigger; v0.2.7 follows that proven input event while retaining our server-authoritative validation model.

## Test deployment

1. Sync/copy `LaccckaQuestFramework`.
2. Confirm both sides load `0.2.7`.
3. Join the dedicated server on foot.
4. The full runtime sync should include at least one anchor-bearing binding when an existing test NPC is registered.
5. If there is no current NPC, spawn one through the Quest Framework context action.
6. The expected sequence is:

```text
[LCCQF][RUNTIME:BANDITS] client discovery=server-anchor physicalLookup=false
[LCCQF][CLIENT] loaded version=0.2.7 ... discovery=server-anchor
[LCCQF][SERVER] runtime binding broadcast npcId=lccq_test_npc_01 runtimeId=... anchor=x,y,z
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=... anchor=x,y,z
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=... distance=...
```

7. Press `E`. Expected:

```text
[LCCQF][CLIENT] interaction requested npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][SERVER] dialogue opened session=... player=... npcId=lccq_test_npc_01
[LCCQF][CLIENT] dialogue state session=... node=...
```

8. Walk farther than four tiles and verify the server rejects/ends further interaction.
9. Kill and respawn the NPC and verify the same framework `npcId` receives a new runtime id and anchor.
10. Repeat with two clients.

## Validation status

- 0.2.6 was active on both client and server in the latest multiplayer log;
- full runtime binding synchronization worked;
- the client observed the exact physical Bandits object for the synchronized runtime id;
- nevertheless the client physical-discovery layer produced no target and no decisive rejection result;
- the same run proved server-side physical resolution works because a repeated spawn request resolved the existing NPC as `AlreadyNearby`;
- v0.2.7 removes client physical-NPC rediscovery from the interaction contract and uses server-owned anchors instead;
- fresh dedicated-server/client acceptance is still required before a final report is created.

## Next milestone

Do not add quests, trading or journal state until this vertical slice passes dedicated-server acceptance. After acceptance, extend the existing `DialogueSession`: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
