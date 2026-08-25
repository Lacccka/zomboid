# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.4

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- replaceable `NPCRuntime` contract with Bandits2 isolated in `BanditsRuntime`;
- nearby-NPC detection on the client through Bandits2's own `BanditZombie.CacheLightB` runtime view, with a `cell:getZombieList()` fallback for initial/late-join cache warmup;
- runtime-id matching accepts Bandits2's persisted `brain.id`/raw persistent outfit id and its normalized cache id, while the server-synchronized LCCQF binding remains the quest-identity authority;
- `[E] Поговорить` interaction prompt;
- server-side registry, runtime-id, same-Z and distance validation;
- server-owned `DialogueSession`, current node and allowed choices;
- compact `RequestDialogue / ChooseDialogue / CloseDialogue` protocol with identifier limits and per-command throttling;
- admin/debug context-menu action for spawning the test NPC;
- UTF-8-safe client localization: the server sends ASCII translation keys, while Russian and English text is resolved from `Translate` files on each client;
- no quests, rewards, reputation, journal, vendors, audio or framework-owned persistence yet.

Bandits2 remains the current physical NPC runtime, but interaction, dialogue and UI code no longer imports `Bandit`, `BanditBrain` or `BanditCustom`. Those APIs and the current `bandit.cid` compatibility workaround exist only inside the Bandits adapter. Framework identity is stored in the namespaced `brain.lccqNpcId`; Bandits2's numeric `brain.key` field is never used for `npcId`. The project does not copy or patch Bandits2 source files.

## Why v0.2.4 changed client discovery

The 0.2.3 dedicated-server log proved that spawning and network bindings were already working: the server created `lccq_test_npc_01`, broadcast its runtime id, and the client received that binding, but the client never logged `interaction target acquired`.

The defect was the client adapter's assumption that every nearby Bandits2 zombie already had `zombie:getModData().brain`. Bandits2 42.20 does not use that as its primary client discovery contract: `BanditZombie.lua` classifies physical bandits through `getVariableBoolean("Bandit")`, maintains `BanditZombie.Cache` / `CacheLightB`, and rebuilds those caches from `cell:getZombieList()`. v0.2.4 follows that provider-native runtime view and uses `BanditBrain.Get()` only as optional metadata/legacy fallback.

A second compatibility detail is handled at the same boundary: Bandits2's server stores `brain.id` from `getPersistentOutfitID()`, while `BanditUtils.GetZombieID()` can clear the outfit hat bit for cache keys. The client adapter therefore tests all relevant runtime-id forms and returns the exact id that matched the framework binding, preserving server validation.

## Current boundaries

```text
NPCRegistry                 framework-owned npcId and definition
    |
NPCRuntime                  provider-neutral dispatch
    |
BanditsRuntime              namespaced identity, spawn and state enforcement

InteractionServer
    |
DialogueSession             server-owned node and transition validation
    |
DialoguePanel               renders only the state sent to that client
```

The Bandits `brain.id` and zombie object are transient runtime handles. Dialogue sessions and future quest state must refer to `npcId`; replacing BanditsRuntime with NativeRuntime must not change dialogue or quest data.

## Test deployment

1. Sync/copy `LaccckaQuestFramework` like the other projects under `WorkshopPatches`.
2. Confirm the client log says `[LCCQF][CLIENT] loaded version=0.2.4` and the server log says `[LCCQF][SERVER] loaded version=0.2.4`.
3. Add `LaccckaQuestFramework` to `Mods=` after `Bandits2` (and preferably after `LaccckaB4220NPCFixes` in the current test stack).
4. Join the dedicated server with an admin account or run a debug client.
5. Right-click the world and choose `[Quest Framework] Создать тестового NPC`.
6. Confirm the server logs `spawned npcId=lccq_test_npc_01 runtimeId=...` and the client receives the same runtime binding.
7. Approach Алексей within about 3 tiles. The client must now log `interaction target acquired npcId=lccq_test_npc_01 runtimeId=...` and show the interaction prompt.
8. Press `E`. The client must log `interaction requested ...`; the server must validate the request and only then send `DialogueState` to that client.
9. Exercise all dialogue buttons. Every button sends only its `choiceId`; the server must return the next `DialogueState` or `DialogueClosed`.
10. Walk beyond 4 tiles before choosing a reply. The server must close the session instead of accepting the transition.
11. Kill Алексей and confirm Bandits death cleanup completes without `expected argument of type int, got String`.
12. Spawn him again after death and verify the new runtime id is accepted under the same framework `npcId`.
13. Repeat with two clients standing near the same NPC; both should have independent server sessions.

Bandits2 42.20 currently returns immediately from `BanditPermanent.Check`, so unload/restart restoration is not part of v0.2.4 acceptance. Framework-owned NPC persistence remains explicitly deferred; the adapter must not claim restart survival or create a duplicate merely because physical exposure is delayed.

## Expected log markers

Server/client messages use the prefix:

`[LCCQF]`

Useful markers include `[LCCQF][SERVER]`, `[LCCQF][CLIENT]` and `[LCCQF][RUNTIME:BANDITS]`. For v0.2.4 specifically, the decisive client sequence is:

```text
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][CLIENT] interaction requested npcId=lccq_test_npc_01 runtimeId=...
```

## Validation status

- the 0.2.3 fresh MP log isolated the failure to client-side physical NPC discovery; spawn and runtime-binding synchronization succeeded;
- Bandits2 42.20 `BanditZombie`, `BanditBrain` and `BanditUtils` source was re-read against the repository snapshot;
- `Chat with Me` was used only as an interaction/proximity architecture comparison; its weak DS authority model was not copied;
- v0.2.4 removes the client-brain synchronization prerequisite and keeps dialogue authority on the server;
- repository audit rules were updated so client use of Bandits2's own `getZombieList()` cache fallback is permitted while server-wide zombie scans remain forbidden;
- focused fresh dedicated-server/client runtime acceptance is still required before this change is promoted to a final report.

## Next milestone

Do not add quests, trading or journal state until this v0.2.4 vertical slice passes dedicated-server acceptance. After acceptance, extend the existing `DialogueSession`: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
