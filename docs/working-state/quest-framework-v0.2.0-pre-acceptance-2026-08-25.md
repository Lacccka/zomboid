# Quest Framework v0.2.0 pre-acceptance state

Date: 2026-08-25

Target: Project Zomboid Build 42.20.x, Dedicated Server / Multiplayer

Mod ID: `LaccckaQuestFramework`

## Status

The architecture pass after the 35 mod-research reports is implemented, but it is not yet a final in-game acceptance. This document belongs in `working-state`; it must move to `final-reports` only after a clean dedicated-server run with server and client logs.

## Implemented boundaries

- `LCCQFNPCRegistry` owns stable logical `npcId` definitions.
- `LCCQFNPCRuntime` dispatches provider-neutral runtime operations.
- client and server `LCCQFBanditsRuntime` files contain every direct `BanditBrain`, `BanditCustom`, `Bandit` and `BanditServer` dependency.
- interaction and dialogue layers no longer see a Bandits brain or zombie object.
- the server resolves the client-supplied transient runtime id only near the requesting player; the previous server-wide `getZombieList()` scan and minute-wide enforcement loop are removed.
- Bandits2's `bandit.cid` / `bandit.general.cid` mismatch is handled temporarily and restored inside the server adapter.
- persistent Bandits brains are checked before admin spawn so an unloaded logical NPC is not duplicated.

## Dialogue authority

The old UI advanced `choice.next` locally. In v0.2.0:

1. client requests `npcId + runtimeId`;
2. server validates registry membership, adapter resolution and distance;
3. server creates a `DialogueSession` containing the current node;
4. client receives only display text and `choiceId + text` pairs;
5. client sends only `sessionId + choiceId`;
6. server verifies that the choice belongs to the current node, revalidates NPC distance and selects the next node;
7. server sends the next state or closes the session.

Unknown commands are ignored before rate-limit bookkeeping. All accepted identifiers are non-empty strings capped at 96 characters. Each player/command pair is throttled.

## Source verification completed

- Bandits2 42.20 `BanditBrain.Get/Update`, `Bandit.ForceStationary`, `BanditServer.Spawner.Individual` and cluster transmit APIs were checked against the current repository source.
- The known Bandits2 custom-profile CID mismatch is present in the current source and is not hypothetical.
- B42.20.3 `TextManager.DrawStringCentre`, `ISRichTextPanel:paginate`, `setYScroll`, `ISButton:new/setTitle`, and `ISUIElement` instantiate/createChildren flow match the calls used by the UI.
- Every Quest Framework Lua file passes a Lua 5.4 parse check.
- An isolated `DialogueSession` logic test passes: open, valid transition, invalid current-node choice rejection, return transition and close.
- `git diff --check` passes.

## Required dedicated-server acceptance

1. Start with `Bandits2,LaccckaB4220NPCFixes,LaccckaQuestFramework` in that order.
2. Confirm v0.2.0 client and server load markers.
3. Spawn Алексей once as admin and verify a second spawn request does not duplicate him.
4. Open all dialogue branches and close them normally.
5. Move beyond server range before selecting a choice; the session must close.
6. Repeat with two clients; node selection and closure must remain session-local.
7. Reconnect, interact again and record old/new Bandits runtime ids for the same `npcId`.
8. Restart the server and verify restoration without duplicate spawn.
9. Collect both server and client logs and confirm there are no Lua exceptions under the `[LCCQF]` markers.

## Explicitly deferred

- framework-owned NPC persistence;
- quest instances, rewards and reputation;
- parties and group dialogue;
- journal and trading UI;
- audio and subtitles;
- NativeRuntime.

These features must not be added before this vertical slice is accepted.
