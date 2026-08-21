# Split Workshop patches

`WorkshopPatches/` contains the independent Workshop-ready Lacccka Build 42 compatibility items. There is no supported monolithic compatibility package. Each functional item has a narrow responsibility and may be installed only when that responsibility is needed.

`Bandits-LCC-Dev` is an internal repository test stand. It is deliberately excluded from the published-package audit and must never be copied into a normal split-patch installation.

## Public naming

Workshop-facing names are deliberately generic. Public descriptions state that an item contains targeted Build 42 compatibility/fix files; they do not imply ownership of or bundle upstream mods.

Every Workshop item must keep this warning:

> Do not use this patch unless you are sure it is required by your setup. This item contains patch files only; the original mods are not included and must be installed separately.

## Items

| Folder | Mod ID | Workshop ID | Public title | Responsibility |
| --- | --- | ---: | --- | --- |
| `PatchCore` | `LaccckaB4220PatchCore` | `3786175901` | Lacccka B42 Patch Core | Shared guarded-patch helper used by functional patches. |
| `RuntimeFixes` | `LaccckaB4220RuntimeFixes` | `3786175979` | Lacccka B42 Runtime Fixes | Low-level runtime, dedicated-server, cache and API compatibility hooks. |
| `NPCFixes` | `LaccckaB4220NPCFixes` | staging `0` | Lacccka B42 NPC Fixes | Validated NPC combat, terminal-death and corpse/clothing lifecycle fixes. |
| `NPCCombatExperimental` | `LaccckaB4220NPCCombatExperimental` | `3786817782` | Lacccka B42 NPC Combat Experimental | Diagnostics, corpse tracing and admin stress-test tooling only. |
| `ActivityFixes` | `LaccckaB4220ActivityFixes` | `3786175725` | Lacccka B42 Activity Fixes | Lifestyle hygiene, Yoga/progression and perk compatibility fixes. |
| `CompatibilityBridges` | `LaccckaB4220CompatBridges` | `3786175808` | Lacccka B42 Compatibility Bridges | Build 42 legacy module/API redirects used by weapon, vehicle and framework mods. |
| `SafetyFixes` | `LaccckaB4220SafetyFixes` | `3786176221` | Lacccka B42 Safety Fixes | Defensive inventory/UI compatibility guards. |
| `RussianTextFixes` | `LaccckaB4220RussianText` | `3786176120` | Lacccka B42 Russian Text Fixes | Russian localization and skill/UI text corrections. |

`NPCFixes` remains a private release-candidate item until its source-clean transformers pass a regression run against the normal installed `Bandits2` rather than `Bandits-LCC-Dev`.

## Dependency model

`RuntimeFixes`, `NPCFixes`, `NPCCombatExperimental`, `ActivityFixes`, `CompatibilityBridges`, and `SafetyFixes` use `LaccckaB4220PatchCore` as a **recommended soft dependency**. `RussianTextFixes` remains standalone.

Functional patches do not use `mod.info require=` for Patch Core. They declare Core in `loadafter=` and ship the same `LCC/Guard.lua` bootstrap:

1. try the Core-only `LCC/CoreGuard` entrypoint;
2. when compatible Core is available, run in `GUARDED` mode;
3. otherwise use the local fallback and run in `DEGRADED` mode.

`DEGRADED` mode is best-effort. The patch remains loadable, but correct behavior and failure isolation are not guaranteed without Patch Core.

Upstream mods are not hard-required by the generic patch items because fixes are guarded/late-loaded and users should install only the patches relevant to their setup.

## RuntimeFixes source-clean contract

`RuntimeFixes` contains LCC-authored hooks, wrappers and narrow path shims only. It must not contain complete Bandits source files. Its current responsibility includes:

- zero-player dedicated wanderer protection;
- squareless/despawned Bandits cache protection;
- farming callback guards;
- dedicated `BanditZombie.GetInstanceById()` compatibility without complete-zombie-list scans;
- the B42 character-screen path shim.

NPC combat/death behavior does not belong in `RuntimeFixes` now that `NPCFixes` exists.

## NPCFixes source-clean contract

`NPCFixes` is the stable destination for validated NPC behavior fixes. It includes combat relationship sanitation, exact fake-hit cleanup, terminal `Die` progress, live clothing restoration and server corpse-clothing repair.

Two Bandits functions are local to upstream files and cannot be safely monkeypatched from another Lua module. For those seams, `NPCFixes` uses small same-path **runtime transformers**, not copies of the original source:

- `client/BanditUpdate.lua` reads the installed `Bandits2` source through `getModFileReader()`, validates exact B42.20 fingerprints, changes only the confirmed pursuit relationships in memory and executes the transformed text;
- `shared/ZombieActions/ZAShoot.lua` does the same for the unsafe gunshot target bridge.

The Workshop item therefore contains no complete third-party implementation. A fingerprint mismatch must result in a visible bypass warning rather than an unverified guessed patch.

## NPCCombatExperimental isolation contract

`NPCCombatExperimental` is optional tooling for controlled tests. Production target-disconnect behavior has been promoted out of it. It retains observe-only target/AttackState diagnostics, death/corpse tracing and the staff-only stress spawner/server bridge.

It loads after `RuntimeFixes` and `NPCFixes` when those stable items are present, so diagnostics observe the production stack instead of masking it.

## Source ownership

The split Workshop items are the supported distribution model. `Bandits-LCC-Dev` may contain full modified upstream files for research and regression comparison inside this private repository, but those files are not publication payloads and are excluded from the grouped Workshop audit.

Every stable/experimental Workshop item is reviewed independently for scope and source ownership.

## Publishing

Each published/staged child directory has its own `workshop.txt` and `Contents/mods/.../42/mod.info`.

Published items keep their assigned Workshop IDs and non-empty previews. A new item may remain `id=0`, `visibility=private`, and without a final preview while it is still a release candidate; it must receive a real Workshop ID and preview before public publication.

## Test rule

Normal regression tests use the original upstream mods plus the relevant split patches. Do not enable or copy `Bandits-LCC-Dev` during a stable-package acceptance test, because that would bypass the source-clean `NPCFixes` integration being validated.
