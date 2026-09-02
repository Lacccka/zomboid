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
| `NPCFixes` | `LaccckaB4220NPCFixes` | `3787592350` | Lacccka B42 NPC Fixes | Stable NPC combat, scheduling, terminal-death and corpse/clothing lifecycle fixes. |
| `NPCCombatExperimental` | `LaccckaB4220NPCCombatExperimental` | `3786817782` | Lacccka B42 NPC Combat Experimental | Diagnostics, stable-fix heartbeat summaries, corpse tracing and admin stress-test tooling only. |
| `ActivityFixes` | `LaccckaB4220ActivityFixes` | `3786175725` | Lacccka B42 Activity Fixes | Lifestyle hygiene, Yoga/progression and perk compatibility fixes. |
| `CompatibilityBridges` | `LaccckaB4220CompatBridges` | `3786175808` | Lacccka B42 Compatibility Bridges | Build 42 legacy module/API redirects used by weapon, vehicle and framework mods. |
| `SafetyFixes` | `LaccckaB4220SafetyFixes` | `3786176221` | Lacccka B42 Safety Fixes | Defensive inventory/UI compatibility guards. |
| `GridInventorySort` | `LaccckaPackFlow` | `3789630746` | Lacccka B42 Pack Flow | Spatial-inventory packing, navigation and multiplayer-safe sorting fixes for GridInventory. |
| `RussianTextFixes` | `LaccckaB4220RussianText` | `3786176120` | Lacccka B42 Russian Text Fixes | Russian localization and skill/UI text corrections. |

`RuntimeFixes` is currently `1.2.3`. The 2026-09-02 rebase against `ModernFirearmsSystem` commit `02462abb9a87b9d1ed58661e626a82559d5afae8` removes the obsolete duplicate attachment-selector/container scan and keeps only the occupied-slot replacement and stale-source safety bridge. Upstream MFS remains authoritative for selector rendering, recursive container discovery, magazine selection and attachment-state/model refresh.

`NPCFixes` is currently `1.0.5`. It no longer compiles transformed copies of `BanditUpdate.lua` or `ZombieActions/ZAShoot.lua`. The installed Bandits2 files remain authoritative; NPCFixes reads them only for compatibility fingerprints and installs source-clean public API/predicate wrappers. Version `1.0.5` is the current static compatibility candidate and still requires its documented live MP smoke pass before being called runtime accepted.

`Lacccka B42 Pack Flow` is currently `0.7.12` and is published as Workshop item `3789630746`.

`RussianTextFixes` is currently `1.1.6`; the 2026-09-02 update adds Russian text for the new MFS critical-damage/cyclic-rate fields and Radio Trade UI while retaining the earlier PZK Vanilla Plus Car Pack recipe/category fixes. Split translation fragments are aggregated into canonical Build 42 table filenames so the game loader can consume them.

## Dependency model

`RuntimeFixes`, `NPCFixes`, `NPCCombatExperimental`, `ActivityFixes`, `CompatibilityBridges`, and `SafetyFixes` use `LaccckaB4220PatchCore` as a **recommended soft dependency**. `RussianTextFixes` remains standalone.

`Lacccka B42 Pack Flow` is a direct fix layer for `GridInventory`: it hard-requires the upstream `GridInventory` mod and loads after it. It does not depend on Patch Core or any other Lacccka patch. Pack Flow reuses GridInventory's `GridCore`, `GridContainer`, item-footprint and network/protocol APIs and adds its own token/CAS layout transaction for multiplayer sorting rather than bundling or replacing upstream implementation files.

Functional patches do not use `mod.info require=` for Patch Core. They declare Core in `loadModAfter=` and ship the same `LCC/Guard.lua` bootstrap:

1. try the Core-only `LCC/CoreGuard` entrypoint;
2. when compatible Core is available, run in `GUARDED` mode;
3. otherwise use the local fallback and run in `DEGRADED` mode.

`DEGRADED` mode is best-effort. The patch remains loadable, but correct behavior and failure isolation are not guaranteed without Patch Core.

Upstream mods are not hard-required by the generic patch items because fixes are guarded/late-loaded and users should install only the patches relevant to their setup.

## RuntimeFixes source-clean contract

`RuntimeFixes` contains LCC-authored hooks, wrappers and narrow path shims only. It must not contain complete Bandits or MFS source files. Its current responsibility includes:

- squareless/despawned Bandits cache protection;
- farming callback guards;
- dedicated `BanditZombie.GetInstanceById()` compatibility without complete-zombie-list scans;
- the B42 character-screen path shim;
- a narrow MFS occupied-slot replacement/stale-source guard that delegates selector/container discovery and final weapon mutation to upstream MFS actions.

The old dedicated zero-player wanderer wrapper is no longer part of the current RuntimeFixes payload. Current Bandits2 already leaves its multiplayer wanderer scheduler inactive when there is no online player because `day` remains `nil` and the spawn branch is guarded by `if day and ...`.

NPC combat/death behavior does not belong in `RuntimeFixes` now that `NPCFixes` exists.

## MFS 2026-09-02 ownership boundary

The imported MFS now provides recursive `scanParts()` and `getReachableContainers()` discovery for the player inventory, nested/equipped containers and nearby world containers. Its attachment button transfers a non-root part through `ISInventoryTransferAction`, and `ISUpgradeWeapon:perform()` owns attachment/model/MP refresh.

`RuntimeFixes/MFSAttachmentAccessFix.lua` must therefore **not** replace `selectAttachmentPane:renderInventory()` or `selectAttachmentPane:update()`. It only preserves the remaining normal-WeaponPart UX/safety behavior:

- occupied normal slots open the upstream selector on LMB;
- double-LMB removal is disabled and RMB remains explicit removal;
- replacement validates the selected source container is still reachable;
- a CAS-style snapshot of the installed part ID prevents a stale selector from removing a different attachment;
- transfer, remove, upgrade and equip remain queued through the normal upstream/vanilla timed actions.

The grouped audit fingerprints both sides of this boundary. If a future MFS update removes or moves the expected discovery/transfer/refresh seams, CI must fail and force a deliberate re-review rather than silently continuing an obsolete override.

## NPCFixes source-clean contract

`NPCFixes` is the stable destination for validated NPC combat, scheduling, death and corpse behavior fixes.

Version `1.0.5` uses `zz_LCC_BanditCallbackBridge.lua` with compatibility marker `loadstring-free-predicate-bridge-v2`. It reads the installed Bandits2 source with `getModFileReader()` only to validate exact expected seams. It does not call `loadstring()` and does not ship same-path replacements for `BanditUpdate.lua` or `ZombieActions/ZAShoot.lua`.

The bridge keeps ordinary zombie -> Bandit pursuit coordinate-only, suppresses unsafe character-target relationships, preserves the ordinary crawler-to-player lunge seam, gates generic task generation for non-combat provider NPCs, and wraps the public Bandits gunshot completion path without replacing the upstream firing implementation.

Stable relationship/fake-hit cleanup, terminal `Die` progression, live clothing restoration, server corpse-clothing repair and wanderer-devirtualization protection remain separate narrow LCC-authored guards.

A fingerprint mismatch must result in an explicit compatibility error/bypass rather than guessed rewriting of unknown upstream code.

The detailed current contract and smoke-test requirements live in `WorkshopPatches/NPCFixes/README.md`. Historical accepted evidence remains in `docs/final-reports/npcfixes-source-clean-bandits-acceptance-2026-08-21.md`, but it does not by itself runtime-accept the newer 1.0.5 architecture.

## NPCCombatExperimental isolation contract

`NPCCombatExperimental` is optional tooling for controlled tests. Production target-disconnect behavior has been promoted out of it. It retains observe-only target/AttackState diagnostics, death/corpse tracing, periodic stable-fix summaries, and the staff-only stress spawner/server bridge.

It loads after `RuntimeFixes` and `NPCFixes` when those stable items are present, so diagnostics observe the production stack instead of masking it.

## RussianTextFixes loader contract

Build 42 consumes canonical translation table names such as `IG_UI.json`, `ItemName.json`, `Recipes.json`, `Sandbox.json`, `Tooltip.json` and `UI.json`. Target-specific `LCC_*` fragments remain the maintainable source units, while `scripts/aggregate_russian_text.py` deterministically merges them into the canonical RU tables.

The 42 translation tree intentionally contains RU plus a small EN runtime fallback. CI validates all JSON, the canonical RU tables, the MFS 1.1.6 keys and the release-version pin so the aggregation workflow cannot silently downgrade `mod.info` again.

## Source ownership

The split Workshop items are the supported distribution model. `Bandits-LCC-Dev` may contain full modified upstream files for research and regression comparison inside this repository, but those files are not publication payloads and are excluded from the grouped Workshop audit.

Every stable/experimental Workshop item is reviewed independently for scope and source ownership.

## Publishing

Each published/staged child directory has its own `workshop.txt` and `Contents/mods/.../42/mod.info`.

Published items keep their assigned Workshop IDs and non-empty previews. A genuinely new stable item may remain `id=0`, `visibility=private`, and without a final preview while its Workshop page is not yet created; it must receive a real Workshop ID and preview before public publication.

## Test rule

Normal regression tests use the original upstream mods plus the relevant split patches. Do not enable or copy `Bandits-LCC-Dev` during a stable-package acceptance test, because that would bypass the source-clean `NPCFixes` integration being validated.
