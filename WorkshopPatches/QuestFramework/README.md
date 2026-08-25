# Lacccka Quest Framework

Build 42.20 multiplayer foundation for server-authoritative NPC interaction and dialogue.

## Scope of v0.2.6

- one Bandits2-backed permanent test NPC (`lccq_test_npc_01`);
- framework-owned logical identity in `NPCRegistry`;
- replaceable `NPCRuntime` contract with Bandits2 isolated in `BanditsRuntime`;
- client physical-NPC discovery driven by the server-synchronized runtime binding and Bandits2's own live object exposure (`Bandit.ApplyVisuals` observation plus exact `BanditZombie.Cache` lookup);
- bounded nearby-square scanning retained only as a last-resort fallback;
- runtime-id matching accepts Bandits2's persisted `brain.id`, raw persistent outfit id and normalized `BanditUtils.GetZombieID()` form;
- no dependency on the transient `getVariableBoolean("Bandit")` classifier or `CacheLightB` membership for interaction eligibility;
- `[E] Поговорить` interaction prompt;
- server-side registry, runtime-id, same-Z and distance validation;
- server-owned `DialogueSession`, current node and allowed choices;
- compact `RequestDialogue / ChooseDialogue / CloseDialogue` protocol with identifier limits and per-command throttling;
- admin/debug context-menu action for spawning the test NPC;
- UTF-8-safe client localization;
- no quests, rewards, reputation, journal, vendors, audio or framework-owned persistence yet.

Bandits2 remains the current physical NPC runtime. Bandits-specific APIs are restricted to the runtime adapter; interaction, dialogue and UI remain provider-neutral. Framework identity is the framework `npcId`; Bandits2's runtime object and `brain.id` are transient physical handles.

## Why v0.2.6 changed client discovery again

The fresh 0.2.5 multiplayer log is decisive:

- server loaded QuestFramework 0.2.5 and spawned `lccq_test_npc_01` with runtime id `10354978`;
- client loaded QuestFramework 0.2.5 and received the exact same runtime binding;
- 38 ms later the already-working NPCFixes clothing path executed `Bandit.ApplyVisuals` for a live physical Bandits object with the exact same `brain.id=10354978`;
- QuestFramework still never logged `interaction target acquired`.

This proves that spawn, networking, the logical binding, Bandits brain data and the physical client `IsoZombie` all existed. The failing boundary was specifically QuestFramework's attempt to rediscover that object through a fresh `square:getMovingObjects()` enumeration.

v0.2.6 therefore stops treating square membership as the primary discovery contract. It observes the physical object at the provider boundary that the fresh log proves is executing (`Bandit.ApplyVisuals`) and keeps weak runtime-id -> `IsoZombie` references. It also performs an O(number-of-framework-bindings) exact lookup in `BanditZombie.Cache`, which Bandits2 populates for every updated zombie before splitting them into bandit/zombie light caches. Only if neither provider-native source has exposed the object does the adapter use the old bounded nearby-square fallback.

This design is both cheaper and more reliable than scanning Bandits2's entire zombie cache or the cell-wide zombie list. It also preserves authority: a physical object becomes interactive only when one of its Bandits runtime ids resolves through `LCCQF.NPCRuntime.GetBoundNPCId()`.

## Current boundaries

```text
NPCRegistry                 framework-owned npcId and definition
    |
NPCRuntime                  provider-neutral dispatch
    |
BanditsRuntime              physical observation + Bandits id mapping

InteractionServer
    |
DialogueSession             server-owned node and transition validation
    |
DialoguePanel               renders only server state
```

## Test deployment

1. Sync/copy `LaccckaQuestFramework`.
2. Confirm client and server both log `loaded version=0.2.6`.
3. Join the dedicated server on foot.
4. Spawn the test NPC through the Quest Framework context action.
5. Expected client sequence is:

```text
[LCCQF][CLIENT] runtime binding received npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][RUNTIME:BANDITS] physical object observed source=Bandit.ApplyVisuals npcId=lccq_test_npc_01 runtimeId=...
[LCCQF][CLIENT] interaction target acquired npcId=lccq_test_npc_01 runtimeId=...
```

6. Press `E`; expect `interaction requested ...` and server `dialogue opened ...`.
7. Exercise all dialogue choices and distance rejection.
8. Kill and respawn the NPC and verify a new runtime id binds to the same framework `npcId`.
9. Repeat with two clients.

If provider exposure somehow fails, v0.2.6 emits only one diagnostic line per binding:

```text
[LCCQF][RUNTIME:BANDITS] physical object unresolved npcId=... runtimeId=...
```

That line is intentionally one-shot rather than per-tick noise.

## Validation status

- 0.2.5 runtime acceptance reproduced the interaction failure with the intended version active;
- the same run proved the exact physical Bandits object existed client-side with the exact server-broadcast runtime id;
- Bandits2 source confirms `BanditZombie.Cache` stores the raw `IsoZombie` before the later bandit classification/light-cache split;
- NPCFixes source confirms its successful clothing repair receives the physical `bandit` object and `brain` through `Bandit.ApplyVisuals`;
- 0.2.6 uses those provider-native paths directly and leaves nearby-square scanning as fallback only;
- the audit rejects whole-cell `getZombieList()` scans and the transient `Bandit` variable as an identity gate;
- fresh in-game acceptance is still required before a final report is created.

## Next milestone

Do not add quests, trading or journal state until this vertical slice passes dedicated-server acceptance. After acceptance, extend the existing `DialogueSession`: nearby-player discovery, invitations, participant acceptance, synchronized narration/subtitles, then quest-instance creation.
