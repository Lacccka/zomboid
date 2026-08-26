# Quest Framework 0.3.3 objective runtime acceptance — 2026-08-26

## Result

**PASS — single-client dedicated multiplayer objective-runtime acceptance.**

This report scopes the accepted gameplay result to Quest Framework runtime revision:

`5757d741fcb06351b239f050e44aa3241fe51548`

Test evidence came from the user-provided dedicated/client log bundle:

`ZomboidLogs_2026-08-26_11-56-52.zip`

Post-test quest-giver non-combat hardening is documented separately below and is **not** claimed runtime-tested by this report.

## What was validated

The second test quest `lccq_test_supply_run` completed end to end on the dedicated server.

Authoritative server progression observed in the logs:

```text
Accept Supply by the Road
    -> Kill 3 infected
    -> Fetch 2 sheets
    -> Clear roadside area
    -> Deliver 2 sheets to Alexey
    -> Quest completed
```

Concrete server evidence included:

- quest accepted for durable `characterId=2d4b0570-42f8-41f7-802a-0b3937992216`;
- `kill_zombies` received zombie-death progress and completed;
- `fetch_sheets` received inventory progress and completed;
- `clear_roadside` completed from authoritative area evaluation;
- `deliver_sheets` received inventory progress and completed during validated interaction with Alexey;
- the quest transitioned to `completed`.

The client received the matching sanitized quest projections and objective transitions. The `ClearArea` objective produced a map marker and the marker was removed when the objective completed.

No Quest Framework Lua stack failure was observed in the accepted quest chain.

## Objective handlers accepted by this run

The run validates the first practical server-authoritative use of:

- `Kill`;
- `Fetch`;
- `ClearArea`;
- `Deliver`.

Together with the previously accepted `ReachArea` and `TalkToNPC`, the framework now has six runtime objective types exercised across the two test quests.

## Physical quest-giver behavior — not accepted

The same run exposed a remaining Bandits2 physical-role problem for Alexey:

- ordinary zombies still aggroed and attacked the quest giver;
- invulnerability prevented death;
- player gunfire also could not kill him;
- however hit/knockdown state could leave the physical NPC lying on the ground.

The old server policy attempted post-acquisition zombie target clearing. That did not prevent the attack/hit state early enough and was not sufficient for a non-combat quest giver.

This does **not** invalidate the accepted quest objective runtime. Dialogue and final `Deliver` turn-in still completed against the same framework NPC identity.

## Post-test hardening

After this gameplay run the Bandits quest-giver policy was changed to model essential quest givers as non-combat presentation/interaction entities rather than merely immortal combat targets.

Post-test changes include:

- B42 `ZOMBIES_DONT_ATTACK` cheat flag applied directly to the physical NPC cheat state, bypassing the public capability-gated setter;
- `setShootable(false)` for essential quest givers;
- `setInvulnerable(true)` retained as a final safety layer;
- `setIgnoreStaggerBack(true)`;
- recovery from `isKnockedDown()` / `isOnFloor()` states;
- Bandits `stationary` and `LCCQFQuestGiver` program enforcement;
- no active `setTarget(nil)` target-clearing loop;
- explicit loading of the protection policy together with the Bandits server adapter.

These changes are post-acceptance hardening and require a focused smoke test before being considered runtime accepted.

## Focused follow-up smoke test

A full quest replay is not required to validate the post-test physical-role fix. The useful checks are:

1. spawn or rematerialize a fresh Alexey;
2. confirm server log contains `QUEST-GIVER-PROTECTION:SERVER` and reports the protected runtime;
3. bring normal zombies next to Alexey and verify they do not continue attacking him;
4. shoot at Alexey and verify he is not a valid gun target / is not left knocked down;
5. shove him and verify he returns to a stable standing stationary role;
6. confirm `[E] Talk` and dialogue still work.

## Remaining acceptance gaps

- post-test non-combat quest-giver hardening runtime smoke test;
- two-client concurrent quest/dialogue acceptance;
- broader reconnect/restart/death lifecycle regression after the latest client-life fix;
- future Character Knowledge / discovery layer is not implemented yet.

## Accepted architectural invariants reinforced by this run

- objective state is server-owned;
- client quest UI is a sanitized projection only;
- `QuestTarget != QuestMarker`;
- framework NPC identity is independent of one physical Bandits runtime;
- provider-specific Bandits classification remains outside generic quest objective code;
- durable quest ownership uses framework `characterId`, not `onlineID` or username.
