# Quest Framework v0.3.1 — quest / RPG Hub / map-marker acceptance

Date: 2026-08-26

## Status

**PASS — single-client dedicated multiplayer acceptance.**

The accepted runtime revision is:

`915b250992ccb5d6938428c09489ebda1c7f554d`

Test environment:

- Project Zomboid `42.20.3` (`70207f62e0`, Steam);
- multiplayer / dedicated-server flow;
- Lua checksum enabled;
- `LaccckaQuestFramework` v0.3.1;
- Bandits2 as the physical NPC provider.

Two-client concurrency remains a separate pending acceptance item because a second player was not available for this run.

## Accepted vertical slice

The complete first quest flow was exercised in game:

```text
Alexey dialogue
    -> server-authoritative quest accept
    -> QuestInstance ACTIVE
    -> sanitized client quest view
    -> RPG Hub / Quests page
    -> EXACT world-map marker
    -> server ReachArea completion
    -> marker removal
    -> current objective becomes return_to_alexey
    -> validated TalkToNPC interaction
    -> QuestInstance COMPLETED
```

The player successfully completed `lccq_test_checkpoint` / `Old Checkpoint` / `Старый блокпост` end to end.

## Confirmed behavior

### Quest authority

Quest acceptance and progression remained server-owned.

The client received synchronized quest views and presentation events only. No client command was required or accepted for objective completion.

`reach_checkpoint` was completed by the server from the server-side player position through the `ReachArea` objective handler.

`return_to_alexey` was completed only after the existing logical NPC / active runtime / physical provider / same-Z / interaction-range validation path succeeded.

### RPG Hub

The v0.3.1 RPG Hub correctly displayed the active quest and objective state from `LCCQFQuestClientState`.

The Hub remained presentation-only and did not own quest transitions.

The generic page-host architecture remains suitable for later non-quest pages such as factions, known people and relationship/world-state information.

### Quest map marker

The active `ReachArea` produced a visible world-map marker in game.

The important renderer/API fixes validated by this run were:

- `getSymbolsAPIv2()` / `WorldMapSymbolsV2` uses the four-argument `addUntranslatedText(text, layer, x, y)` overload;
- visual properties are applied on the returned symbol object;
- the framework marker uses the engine user-defined visibility path because B42.20.3 hides non-user-defined symbols when the `PlaceNames` renderer layer is disabled;
- framework-owned marker cleanup/integrity remains separate from quest authority.

Observed lifecycle:

```text
ReachArea active   -> marker service rebuilt count=1
ReachArea complete -> marker service rebuilt count=0
```

The marker therefore appeared for the spatial objective and disappeared after the server advanced the quest.

`QuestTarget != QuestMarker` remains a hard invariant: the map symbol is presentation data and cannot complete the quest.

### Moving NPC interaction anchor

The movement-replication fix was confirmed in game.

While the physical Bandits NPC moved, server runtime reconciliation continuously refreshed its physical coordinates and published thresholded runtime-binding upserts. The client interaction anchor therefore followed the NPC instead of remaining at the spawn position.

The server interaction-distance check was not weakened.

### Logical NPC identity survived physical runtime replacement

The run supplied particularly useful lifecycle evidence.

The first physical Alexey had runtime id:

`7930073`

That physical NPC died before the quest was fully completed. Quest Framework invalidated and removed that physical runtime binding.

A later physical Alexey appeared with a different runtime id:

`12582980`

The already-active quest remained valid, continued through the replacement physical runtime, and was successfully turned in through the same framework-owned logical NPC identity `lccq_test_npc_01`.

This confirms the intended boundary:

```text
framework npcId = persistent logical identity
Bandits runtimeId / IsoZombie = replaceable physical materialization
```

Quest state did not become coupled to one Bandits zombie object or runtime id.

## Error review

No Quest Framework Lua exception was observed in the accepted run.

In particular, the previously fixed marker errors did not recur:

- no `addUntranslatedText` overload exception;
- no `marker creation failed` loop;
- no Quest Framework stack trace from marker rebuilding.

An unrelated generic `NetworkZombieMind: Canceled procedure not found` exception was present in the broader modded runtime, but it did not contain a Quest Framework stack and did not prevent the accepted quest flow.

## Residual discovered during acceptance: physical quest-giver role

The accepted revision still used the Bandits2 `Defend` program for Alexey.

Runtime testing showed poor quest-giver UX: Alexey could travel far from his original area and could be killed by zombies, making turn-in unnecessarily difficult.

Source review after the accepted run confirmed that this is expected Bandits2 behavior rather than a Quest Framework anchor defect:

- `ZombiePrograms.Defend.Prepare` explicitly executes `Bandit.ForceStationary(bandit, false)`;
- `ZombiePrograms.Defend.Wait` switches an outdoor defender to the `Looter` program;
- therefore `stationary=true` in the framework NPC definition is not stable while the provider continues running `Defend`.

This residual does **not** invalidate the accepted quest / Hub / marker architecture. In fact, the NPC death during the test additionally validated runtime-replacement independence.

## Post-acceptance hardening

After the accepted run, a provider-specific quest-giver role was added on top of the accepted revision:

- `LCCQFBanditsQuestGiverProgram.lua`;
- Alexey now requests Bandits program `LCCQFQuestGiver` instead of `Defend`.

The role is deliberately scoped to essential anchored quest givers:

- keeps Bandits stationary mode enabled;
- clears move tasks;
- does not transition to `Looter` or generic combat programs;
- generates only a small idle animation task;
- uses B42 `IsoGameCharacter:setInvulnerable(true)` for this essential role.

This is **post-acceptance hardening**, not part of the runtime-tested revision recorded above. It requires only a focused smoke test that a freshly spawned Alexey stays anchored and survives nearby zombies. The full quest flow does not need to be repeated solely for this provider-role change unless a regression is observed.

The current post-acceptance hardening HEAD at the time of this report is based on commit:

`2aef247c14a7b56151d6e01734794a4814f231b4`

The dedicated Quest Framework static audit passed on that HEAD.

## Accepted invariants

Do not regress the following:

- dialogue and quest state are server-authoritative;
- the client never reports objective completion as authority;
- `QuestTarget` and `QuestMarker` are separate concepts;
- marker loss or local marker manipulation cannot progress a quest;
- generic client interaction discovery does not scan Bandits / zombies;
- physical NPC movement is synchronized as framework runtime-anchor presentation data;
- exact dialogue validation still resolves the physical NPC on the server;
- logical `npcId` must survive physical Bandits runtime replacement;
- Bandits `brain.id`, runtime id and `IsoZombie` object must never become framework NPC identity;
- provider-specific quest-giver behavior stays behind the Bandits/runtime boundary;
- essential/invulnerable behavior is a role policy, not a global rule for every future NPC.

## Closure

`LaccckaQuestFramework` v0.3.1 has a confirmed single-client multiplayer vertical slice for:

- NPC interaction;
- dialogue;
- quest acceptance;
- server-owned sequential objectives;
- RPG Hub quest presentation;
- exact world-map target presentation;
- ReachArea progression;
- return-to-NPC completion;
- moving physical NPC anchors;
- physical NPC death/replacement without logical quest identity loss.

**Single-client v0.3.1 quest / Hub / marker acceptance is closed.**

Remaining separate work:

1. smoke-test the post-acceptance anchored/essential quest-giver role;
2. two-client concurrency acceptance when another player is available;
3. framework-owned persistence and durable character identity before expanding deeply into additional quest types.
