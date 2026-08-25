# Quest Framework v0.2.1-v0.2.2 runtime fixes

Date: 2026-08-25

Target: Project Zomboid Build 42.20.3, Dedicated Server / Multiplayer

Inputs: first v0.2.0 client/server runtime logs and the repository snapshots of Bandits2 42.20 plus the decompiled B42.20.3 game classes.

## Result of the first runtime pass

Quest Framework loaded on both sides without its own Lua initialization exception. The run nevertheless exposed two reproducible integration defects:

1. `Spawner.Individual` created a brain synchronously, but the adapter immediately searched square moving-object lists before the engine exposed the zombie there. It returned `Bandits2 did not expose the spawned NPC`; retries created additional brains with runtime ids `12124480` and `10485949`.
2. v0.2.0 stored the string `lccq_test_npc_01` in Bandits2's `brain.key`. On death, Bandits2 sometimes passes that field to `InventoryItem.setKeyId(int)`, producing `expected argument of type int, got String`. The failure occurred before Bandits death cleanup removed the brain.

The earlier `NPC already exists in Bandits persistence` result was also not a reliable existence check. Bandits2 42.20 leaves `BanditPermanent.Check` disabled with an unconditional return, and the failed death cleanup left stale cluster entries behind.

## v0.2.1 changes

- framework identity moved to `brain.lccqNpcId`; `brain.key` is reserved for Bandits2's numeric door-key contract;
- live v0.2.0 brains are recognized and migrated by both runtime adapters;
- the spawner snapshots runtime ids before the call, locates the newly registered brain afterwards, assigns logical identity, transmits that cluster, and returns success without requiring immediate square exposure;
- server entity resolution falls back to the Bandits cluster because dedicated-server spawns do not run the client-side `BanditBrain.Update`; the client refreshes an initially untagged attached brain from the newer tagged cluster snapshot;
- repeated spawn requests return an existing framework-owned brain as `already registered` instead of creating another physical instance;
- the adapter supplies the missing temporary `general.bid` alias so new brains retain their Bandits custom-profile id;
- NPC Fixes 1.0.2 adds an exact source-clean guard requiring a numeric `brain.key` before Bandits2 calls `setKeyId(int)`, protecting legacy worlds and other integrations from the same crash;
- restart/unload restoration is no longer claimed as accepted behavior. Framework-owned NPC persistence remains deferred.

## Focused in-game retest

Before the retest, v0.2.2 also moved every user-facing Russian/English string into client translation JSON. Network payloads now contain only ASCII translation keys, avoiding the Unicode low-byte truncation visible in the first logs. Target discovery refreshes the attached client brain from the authoritative cluster snapshot, and `E` interaction now fires on `OnKeyStartPressed`. Client logs report one `interaction target acquired` marker and one `interaction requested` marker for diagnosis without per-tick noise.

1. Confirm server and client load `LCCQF` version `0.2.2`.
2. Confirm the NPC Fixes `BanditUpdateShim` boot marker is `source-clean-coordinate-pursuit-v2 mode=PATCHED`.
3. Use the admin action once. It must report success, produce one `[RUNTIME:BANDITS] spawned` line, and show Алексей nearby.
4. Confirm the client logs `interaction target acquired` when entering range, displays `[E] Поговорить - Алексей`, and logs `interaction requested` immediately when `E` is pressed.
5. Use the action again while he is present. It must report that he already exists, without creating a second NPC or a new runtime id.
6. Traverse every dialogue branch and close normally; every Russian character must render correctly in the prompt, statuses, NPC name, dialogue and buttons.
7. Open dialogue, walk beyond four tiles, then choose a reply. The server must close/reject the session.
8. Kill Алексей several times across respawns to exercise the random key-drop branch. There must be no `setKeyId`, `expected argument of type int`, or `OnZombieDead(BanditUpdate.lua...)` exception.
9. After each death, confirm corpse clothing remains intact and the next admin spawn obtains exactly one new runtime id.
10. Repeat the dialogue checks with two clients and collect fresh client/server logs.

Do not test or claim server-restart NPC restoration as part of this patch. That requires framework-owned persistence or a deliberate restore implementation behind `NPCRuntime`.
