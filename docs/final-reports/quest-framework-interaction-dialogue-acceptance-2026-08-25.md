# Quest Framework interaction/dialogue acceptance — 2026-08-25

## Status

**CLOSED / ACCEPTED IN-GAME**

The Project Zomboid Build 42.20.3 dedicated-server vertical slice for Quest Framework NPC discovery, interaction and server-authoritative dialogue is confirmed working with `LaccckaQuestFramework` **0.2.8**.

Acceptance archive: `ZomboidLogs_2026-08-25_20-13-21.zip`.

This report closes the interaction failure that prevented the `[E] Talk` prompt and dialogue request from being produced even when the client already had a valid quest-NPC runtime binding.

## Affected build and versions

- Project Zomboid: Build 42.20.3
- Lacccka Quest Framework: 0.2.8 accepted
- NPC provider used for the test slice: Bandits2
- Test NPC: `lccq_test_npc_01` / Alexey

## Original symptom

Earlier builds successfully spawned or synchronized the Bandits-backed test NPC, but the client never promoted the valid runtime binding into an interaction target. The player therefore saw no Quest Framework interaction prompt and pressing `E` produced no dialogue request.

The failed 0.2.7 run had already proven that the client possessed all of the generic eligibility data: registered `npcId`, runtime id, anchor coordinates, matching Z level and an in-range distance. Despite that, the old provider-adapter dispatch returned no target.

## Confirmed root cause

There were two structural problems rather than one missing Bandits lookup.

### 1. Provider-specific client target discovery was redundant and inconsistent

The framework already had authoritative synchronized interaction data:

- `runtimeId -> npcId`;
- `runtimeId -> {x,y,z}`;
- registry metadata for the logical NPC.

The old client path still delegated nearest-target selection through a Bandits-specific adapter. That added a second set of eligibility rules between already-valid framework state and the UI target. The diagnostics and the actual target-selection path could therefore disagree.

The accepted design makes `NPCRuntime.FindNearestInteractive()` provider-neutral. It selects the nearest eligible framework binding directly from registry state and the synchronized interaction anchor. Bandits is not consulted to decide whether the client may draw the prompt.

### 2. Client/server runtime modules previously shared one require path

The 0.2.7 implementation had separate client and server files named:

```text
LCCQF/Runtime/LCCQFBanditsRuntime.lua
```

Build 42.20.3 registers Lua search paths in a way that made this same-name split unsafe. The failed dedicated-server log directly showed the client implementation being resolved during server startup.

The server provider implementation was therefore moved to the unique module path:

```text
LCCQF/Runtime/LCCQFBanditsServerRuntime
```

The client Bandits runtime module was removed. The old server module name remains only as a compatibility forwarding wrapper.

## Additional runtime-state correction

0.2.7 could retain several runtime ids for one logical `npcId`. The shared runtime now also maintains:

```text
npcId -> active runtimeId
```

Binding a new runtime id for the same logical NPC evicts the previous runtime id and its anchor. Only one active physical handle is advertised for one framework NPC.

Framework-owned persistence is intentionally not implemented yet; historical Bandits cluster entries are not reconstructed as Quest Framework bindings at startup.

## Final implementation boundary

```text
SERVER
Bandits physical NPC / brain
        |
        +--> BanditsServerRuntime
        |      spawn + authoritative physical validation
        |
        +--> runtimeId + npcId + interaction anchor
                          |
                          v
SHARED NPCRuntime
provider-neutral nearest-anchor selection
                          |
                          v
CLIENT
prompt + E request
                          |
                          v
SERVER
resolve exact physical NPC again
validate runtimeId + Z + distance
                          |
                          v
DialogueSession
```

The interaction anchor is discovery/presentation data. It is not sufficient to open a dialogue. The server resolves and validates the actual Bandits-backed NPC before creating or advancing a dialogue session.

## Acceptance evidence

### Correct 0.2.8 startup

Dedicated server:

```text
[LCCQF][RUNTIME:BANDITS:SERVER] adapter registered module=LCCQFBanditsServerRuntime
[LCCQF][SERVER] loaded version=0.2.8 runtime=Bandits serverRange=4 restoredBindings=0
```

Client:

```text
[LCCQF][CLIENT] loaded version=0.2.8 runtime=Bandits interactKey=E range=3.25 discovery=framework-anchor
```

The initial full synchronization correctly returned `count=0` because framework-owned persistence is still deferred.

### Binding became a real client interaction target

The first bound NPC was advertised as runtime id `11141215`:

```text
runtime binding received npcId=lccq_test_npc_01 runtimeId=11141215
... distance=0.5277414588255136 ... registered=true interactive=true ... eligible=true
interaction target acquired npcId=lccq_test_npc_01 runtimeId=11141215 distance=0.5277414588255136
```

This is the marker that was absent in the failed architecture and confirms that the prompt-discovery path now completes.

### `E` opened an authoritative server dialogue

Client:

```text
interaction requested npcId=lccq_test_npc_01 runtimeId=11141215
dialogue state session=866d6f63-a63c-42f7-9bdb-c7f056faebf4 node=start
```

Server:

```text
dialogue opened session=866d6f63-a63c-42f7-9bdb-c7f056faebf4 player=Noxis npcId=lccq_test_npc_01
```

Several additional sessions opened, closed and finished normally.

### Server-owned node transitions worked

One accepted session traversed multiple states without a client-side transition failure:

```text
start -> who -> start -> work -> start -> who
```

Later after reconnect another session traversed:

```text
start -> work -> start -> who -> start
```

### NPC death invalidated authoritative interaction

Runtime id `11141215` was killed during the test. The next request was rejected by the server:

```text
[LCCQF][SERVER] dialogue rejected: unresolved npcId=lccq_test_npc_01 runtimeId=11141215
```

The client received `IGUI_LCCQF_Status_NPCUnavailable` instead of opening a dialogue against the dead entity.

A new NPC then spawned as runtime id `10223856`, was broadcast as the active binding, immediately became the client target and successfully opened dialogue.

### Reconnect/full synchronization worked

After the client reloaded/reconnected, the server sent one current binding:

```text
[LCCQF][SERVER] runtime bindings sent player=Noxis count=1
```

The client reconstructed the target from the full sync without requiring a new spawn:

```text
runtime bindings synchronized count=1
... runtimeId=10223856 ... distance=2.4088928769412155 ... eligible=true
interaction target acquired npcId=lccq_test_npc_01 runtimeId=10223856
```

Dialogue then opened successfully again.

### Range enforcement worked

When the player moved away, client prompt eligibility dropped at distances such as `7.15` and `11.55`, and an active dialogue produced `IGUI_LCCQF_Status_DialogueTooFar` rather than continuing outside the authoritative range.

## Residual observations / follow-up work

These do not invalidate the accepted vertical slice, but should be addressed before the framework grows substantially.

1. **Death/unload binding lifecycle:** after runtime `11141215` died, its client anchor was not proactively removed. The server correctly rejected interaction, but an explicit runtime-binding removal/invalidation message would make the prompt disappear immediately instead of relying on replacement, distance change or a failed request.

2. **Range-edge transient:** after reconnect the client reacquired runtime `10223856` at anchor distance `3.037`. One request was rejected as `NPCUnavailable`, and a request about 0.76 seconds later succeeded. This is consistent with a short client/server position or physical-entity resolution mismatch near the interaction boundary. It is not a persistent failure, but should be instrumented or smoothed before more complex interactions depend on the same edge.

3. **Two-client behavior is not covered by this archive.** The accepted result proves dedicated-server/client interaction for one connected player, including reconnect, but not concurrent sessions from two clients.

4. Client logs still show server-side Lua being executed during parts of Build 42.20 world/Lua initialization, including the server runtime registration marker. Client prompt discovery is now independent of the provider adapter, so this did not break acceptance. Future provider modules should avoid unsafe client-side side effects and should not assume directory placement alone creates a process boundary.

## Regression constraints

Do not reintroduce any of the following:

- provider-specific physical NPC lookup into generic client prompt discovery;
- client `getZombieList()` scans;
- same relative Lua module path for different client/server runtime implementations;
- more than one active runtime id for one logical framework `npcId`;
- client-authoritative dialogue node transitions;
- dialogue opening based only on the synchronized anchor without server physical validation.

## Closure

The original missing-interaction problem is resolved and confirmed in-game on Build 42.20.3 with Quest Framework 0.2.8.

The interaction/dialogue vertical slice may now be treated as the stable base for the next multiplayer/lifecycle milestone.
