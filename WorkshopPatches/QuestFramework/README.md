# Lacccka Quest Framework

Early Build 42.20 multiplayer prototype for server-authoritative NPC interaction and dialogue.

## Scope of v0.1.0

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- stable NPC identity through `BanditBrain.key` rather than the transient zombie object;
- nearby-NPC detection on the client;
- `[E] Поговорить` interaction prompt;
- server-side NPC existence and distance validation before a dialogue may open;
- temporary server `DialogueSession` id;
- simple dialogue UI with branching local test pages;
- admin/debug context-menu action for spawning the test NPC;
- no quests, rewards, reputation, journal, vendors, audio or persistence owned by this framework yet.

Bandits2 remains the NPC runtime. This project does not copy or patch Bandits2 source files.

## Test deployment

1. Sync/copy `LaccckaQuestFramework` like the other projects under `WorkshopPatches`.
2. Add `LaccckaQuestFramework` to `Mods=` after `Bandits2` (and preferably after `LaccckaB4220NPCFixes` in the current test stack).
3. Join the dedicated server with an admin account or run a debug client.
4. Right-click the world and choose `[Quest Framework] Создать тестового NPC`.
5. Approach Алексей within about 3 tiles. The interaction prompt should appear.
6. Press `E`. The server must validate the request and only then send `OpenDialogue` to that client.
7. Exercise all dialogue buttons, close the window, reconnect, and verify the permanent NPC is still usable.
8. Repeat with two clients standing near the same NPC; both should be able to open independent dialogue sessions without changing NPC state.

## Expected log markers

Server/client messages use the prefix:

`[LCCQF]`

For the first runtime pass, collect both server and client logs so interaction rejection, spawn/profile failures and UI errors can be correlated.

## Next milestone

After this vertical slice is accepted, extend the existing `DialogueSession` rather than replacing it: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
