# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.5

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- replaceable `NPCRuntime` contract with Bandits2 isolated in `BanditsRuntime`;
- nearby-NPC detection on the client through Bandits2's physical `BanditZombie.Cache`, with a bounded nearby-square fallback;
- runtime-id matching accepts Bandits2's persisted `brain.id`/raw persistent outfit id and its normalized cache id, while the server-synchronized LCCQF binding is the authoritative quest-identity gate;
- no dependency on the transient `getVariableBoolean("Bandit")` classifier for interaction eligibility;
- `[E] Поговорить` interaction prompt;
- server-side registry, runtime-id, same-Z and distance validation;
- server-owned `DialogueSession`, current node and allowed choices;
- compact `RequestDialogue / ChooseDialogue / CloseDialogue` protocol with identifier limits and per-command throttling;
- admin/debug context-menu action for spawning the test NPC;
- UTF-8-safe client localization: the server sends ASCII translation keys, while Russian and English text is resolved from `Translate` files on each client;
- no quests, rewards, reputation, journal, vendors, audio or framework-owned persistence yet.

Bandits2 remains the current physical NPC runtime, but interaction, dialogue and UI code do not import `Bandit`, `BanditBrain` or `BanditCustom`. Those APIs and the current `bandit.cid` compatibility workaround exist only inside the Bandits adapter. Framework identity is stored in the namespaced `brain.lccqNpcId`; Bandits2's numeric `brain.key` field is never used for `npcId`. The project does not copy or patch Bandits2 source files.

## Why v0.2.5 changed client discovery

The 0.2.4 fresh multiplayer acceptance log removed the remaining ambiguity. The server spawned `lccq_test_npc_01` and broadcast runtime ids; the client received the exact bindings. On the same client, the NPCFixes clothing-restoration path then saw physical Bandits NPCs with those exact ids (`9502826` and later `7340147`). Despite that, QuestFramework never logged `interaction target acquired`.

That means spawning, server authority, binding synchronization, physical zombie synchronization and Bandits brain identity were already functioning. The remaining failure was inside the client discovery filter itself.

Bandits2's `BanditZombie.lua` stores every valid physical `IsoZombie` in `BanditZombie.Cache` before it classifies the object with `getVariableBoolean("Bandit")`. Only after that transient classification does the object enter `CacheLightB`. On a multiplayer client that variable can therefore lag behind a valid synchronized physical object and its framework binding. v0.2.5 no longer treats that animation/runtime classifier as proof of quest ownership.

The authoritative interaction gate is now the exact runtime id synchronized by the server: the adapter gathers `brain.id`, raw `getPersistentOutfitID()`, the Bandits cache id and `BanditUtils.GetZombieID()`, then accepts a physical object only when one of those ids resolves through `LCCQF.NPCRuntime.GetBoundNPCId()`. The primary provider scan uses `BanditZombie.Cache`; the fallback scans only nearby squares' moving objects. There is no whole-cell `getZombieList()` interaction scan.

A second compatibility detail remains handled at the same boundary: Bandits2's server stores `brain.id` from `getPersistentOutfitID()`, while `BanditUtils.GetZombieID()` can clear the outfit hat bit for cache keys. The client adapter tests all relevant runtime-id forms and returns the exact id that matched the framework binding, preserving server validation.

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
2. Confirm the client log says `[LCCQF][CLIENT] loaded version=0.2.5` and the server log says `[LCCQF][SERVER] loaded version=0.2.5`.
3. Add `LaccckaQuestFramework` to `Mods=` after `Bandits2` (and preferably after `LaccckaB4220NPCFixes` in the current test stack).
4. Join the dedicated server with an admin account or run a debug client.
5. Right-click the world and choose `[Quest Framework] Создать тестового NPC`.
6. Confirm the server logs `spawned npcId=lccq_test_npc_01 runtimeId=...` and the client receives the same runtime binding.
7. Approach Алексей within about 3 tiles. The client must log `interaction target acquired npcId=lccq_test_npc_01 runtimeId=...` and show the interaction prompt.
8. Press `E`. The client must log `interaction requested ...`; the server must validate the request and only then send `DialogueState` to that client.
9. Exercise all dialogue buttons. Every button sends only its `choiceId`; the server must return the next `DialogueState` or `DialogueClosed`.
10. Walk beyond 4 tiles before choosing a reply. The server must close the session instead of accepting the transition.
11. Kill Алексей and confirm Bandits death cleanup completes without `expected argument of type int, got String`.
12. Spawn him again after death and verify the new runtime id is accepted under the same framework `npcId`.
13. Repeat with two clients standing near the same NPC; both should have independent server sessions.

Bandits2 42.20 currently returns immediately from `BanditPermanent.Check`, so unload/restart restoration is not part of v0.2.5 acceptance. Framework-owned NPC persistence remains explicitly deferred; the adapter must not claim restart survival or create a duplicate merely because physical exposure is delayed.

## Expected log markers

Server/client messages use the prefix:

`[LCCQF]`

Useful markers include `[LCCQF][SERVER]`, `[LCCQF][CLIENT]` and `[LCCQF][RUNTIME:BANDITS]`. For v0.2.5 specifically, the decisive client sequence is:

```text
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][CLIENT] interaction requested npcId=lccq_test_npc_01 runtimeId=...
```

## Validation status

- the 0.2.4 fresh MP log proved the updated Workshop package was active on both server and client;
- the same log proved exact runtime bindings and exact physical Bandits NPC ids existed on the client while QuestFramework still returned no interaction target;
- Bandits2 42.20 `BanditZombie`, `BanditBrain` and `BanditUtils` source was re-read against the repository snapshot;
- `Chat with Me` remains only an interaction/proximity architecture comparison; its weak DS authority model is not copied;
- v0.2.5 removes the transient Bandit-variable/`CacheLightB` eligibility gate and uses exact server-synchronized runtime bindings instead;
- the fallback scan remains bounded to nearby squares; the audit rejects broad `getZombieList()` scans and the old transient-variable identity gate;
- focused fresh dedicated-server/client runtime acceptance is still required before this change is promoted to a final report.

## Next milestone

Do not add quests, trading or journal state until this v0.2.5 vertical slice passes dedicated-server acceptance. After acceptance, extend the existing `DialogueSession`: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
