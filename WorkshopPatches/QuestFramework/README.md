# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.1

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- replaceable `NPCRuntime` contract with Bandits2 isolated in `BanditsRuntime`;
- nearby-NPC detection on the client through the runtime adapter;
- `[E] Поговорить` interaction prompt;
- server-side registry, runtime-id, same-Z and distance validation;
- server-owned `DialogueSession`, current node and allowed choices;
- compact `RequestDialogue / ChooseDialogue / CloseDialogue` protocol with identifier limits and per-command throttling;
- admin/debug context-menu action for spawning the test NPC;
- no quests, rewards, reputation, journal, vendors, audio or framework-owned persistence yet.

Bandits2 remains the current physical NPC runtime, but interaction, dialogue and UI code no longer imports `Bandit`, `BanditBrain` or `BanditCustom`. Those APIs and the current `bandit.cid` compatibility workaround exist only inside the Bandits adapter. Framework identity is stored in the namespaced `brain.lccqNpcId`; Bandits2's numeric `brain.key` field is never used for `npcId`. The project does not copy or patch Bandits2 source files.

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
2. Add `LaccckaQuestFramework` to `Mods=` after `Bandits2` (and preferably after `LaccckaB4220NPCFixes` in the current test stack).
3. Join the dedicated server with an admin account or run a debug client.
4. Right-click the world and choose `[Quest Framework] Создать тестового NPC`.
5. Approach Алексей within about 3 tiles. The interaction prompt should appear.
6. Press `E`. The server must validate the request and only then send `DialogueState` to that client.
7. Exercise all dialogue buttons. Every button sends only its `choiceId`; the server must return the next `DialogueState` or `DialogueClosed`.
8. Walk beyond 4 tiles before choosing a reply. The server must close the session instead of accepting the transition.
9. Kill Алексей and confirm Bandits death cleanup completes without `expected argument of type int, got String`.
10. Spawn him again after death and verify the new runtime id is accepted under the same framework `npcId`.
11. Repeat with two clients standing near the same NPC; both should have independent server sessions.

Bandits2 42.20 currently returns immediately from `BanditPermanent.Check`, so unload/restart restoration is not part of v0.2.1 acceptance. Framework-owned NPC persistence remains explicitly deferred; the adapter must not claim restart survival or create a duplicate merely because physical exposure is delayed.

## Expected log markers

Server/client messages use the prefix:

`[LCCQF]`

Useful markers include `[LCCQF][SERVER]`, `[LCCQF][CLIENT]` and `[LCCQF][RUNTIME:BANDITS]`. For the first runtime pass, collect both server and client logs so interaction rejection, spawn/profile failures and UI errors can be correlated.

## Validation status

- all Lua files parse successfully with Lua 5.4;
- `DialogueSession` open, allowed transition, rejected transition and close paths pass an isolated logic test;
- B42.20.3 `TextManager`, `ISRichTextPanel`, `ISButton` and Bandits2 42.20 source signatures were checked against the repository snapshot;
- the first dedicated-server run exposed and reproduced two adapter defects; v0.2.1 fixes them and is pending a focused fresh server/client retest.

## Next milestone

Do not add quests, trading or journal state until this v0.2.1 vertical slice passes dedicated-server acceptance. After acceptance, extend the existing `DialogueSession`: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
