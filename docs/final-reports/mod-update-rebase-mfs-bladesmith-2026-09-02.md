# 2026-09-02 upstream mod update rebase — static acceptance report

Status: **STATIC ACCEPTED / RUNTIME REGRESSION REQUIRED**

Branch: `agent/b42-20-compatibility-patch`

Upstream import commits reviewed:

- `6242ea1b4f9387bb7258b984cede9f67d047e93e` — diagnostic Workshop imports (`errorMagnifier`, `Mod Update and Alert System`);
- `02462abb9a87b9d1ed58661e626a82559d5afae8` — `3633421539` update containing `BladesmithSystem` and `Escape from Kentucky4215` / `ModernFirearmsSystem` changes.

This report records static compatibility work only. It does **not** replace the live Project Zomboid Build 42.20.x dedicated-server/client regression pass.

## Executive result

No evidence was found that the imported diagnostic mods or the Bladesmith update invalidate an LCC runtime patch.

The MFS update did overlap a real LCC responsibility: upstream now implements recursive attachment discovery in equipped/nested containers and nearby world containers, including transfer of selected non-root parts. The previous `MFSAttachmentAccessFix.lua` architecture therefore became too invasive because it duplicated and replaced the new upstream selector implementation.

RuntimeFixes was rebased so upstream MFS is authoritative for attachment discovery/rendering and weapon-state refresh. LCC now keeps only the remaining occupied-slot UX and stale-selection safety behavior.

RussianTextFixes was also rebased for the new MFS UI keys. During that work an independent release-automation bug was found: the canonical translation aggregator still pinned `1.1.5` and could silently downgrade the already-bumped `1.1.6` `mod.info`. The generator pin was corrected and the canonical aggregate workflow restored `1.1.6`.

The grouped Workshop audit itself was stale and initially hid useful validation behind obsolete expectations. It was rebased to the current RuntimeFixes, NPCFixes, PackFlow and RussianTextFixes package contracts and now triggers when imported top-level Workshop mod sources (`*/mods/**`) change.

## 1. Diagnostic Workshop imports (`6242ea1b...`)

### `2896041179` — errorMagnifier

The imported mod provides client-side error collection/report UI and print/error diagnostics. No LCC patch owns the same gameplay/runtime seam.

Static result: **no patch change required**.

### `3077900375` — Mod Update and Alert System

The imported alert system handles menu/load notifications and changelog presentation. No LCC gameplay compatibility patch overlaps this responsibility.

Static result: **no patch change required**.

## 2. BladesmithSystem update

The update normalizes several scabbard model/GUID identifiers and adds `Avalon_cat` registrations through the clothing GUID table, hotbar attachment definitions, attached/body locations and registries.

Observed examples include:

- `catKatana_` -> `MurasamaBladeScabbard` model/GUID normalization;
- Chinese scabbard path/GUID names replaced with normalized identifiers;
- new `Avalon_cat` body/attachment registration.

No LCC patch references the replaced Bladesmith identifiers or overrides those registries.

Static result: **no LCC patch change required**.

Runtime smoke recommendation: equip/remove the changed/new scabbards once to catch asset/GUID problems that static Lua/XML comparison cannot detect.

## 3. ModernFirearmsSystem update — ownership change

The updated upstream MFS now contains the functionality for which the old LCC attachment overlay originally existed:

- `scanParts(container, player, weapon, slot, result, visited)` recursively scans nested inventory containers;
- `getReachableContainers(player)` collects player inventory plus nearby built/world containers;
- `selectAttachmentPane:renderInventory()` uses the upstream reachable-container/recursive scan;
- `addAttachmentButton:onMouseDown()` transfers a selected non-root part with `ISInventoryTransferAction` before `ISUpgradeWeapon`;
- `ISUpgradeWeapon:perform()` runs the new MFS attachment/model/sync refresh path and refreshes the inspect preview.

The old LCC implementation could no longer safely remain a full selector override because it would hide future upstream fixes and used a weaker compatibility check than upstream `part:canAttach(player, weapon)`.

## 4. RuntimeFixes rebase

Package: `LaccckaB4220RuntimeFixes`

Package version: **`1.2.3`**

MFS bridge version: **`MFSAttachmentAccessFix 1.1.0`**

The rebased bridge deliberately does **not** replace:

- `selectAttachmentPane:renderInventory()`;
- `selectAttachmentPane:update()`;
- upstream magazine selection;
- upstream recursive container discovery;
- upstream final `ISUpgradeWeapon` attachment/model refresh.

It keeps only the remaining normal-WeaponPart gap:

1. LMB opens the upstream selector even when the slot is occupied.
2. Double-LMB removal is disabled; RMB is the explicit removal action.
3. A selected non-root source is revalidated against current reachability before mutation is queued.
4. Replacement captures the installed part ID and performs a CAS-style check before removing anything; a stale selector cannot remove a different attachment installed after the selector was opened.
5. The selected part is transferred into the root inventory first, then normal `ISRemoveWeaponUpgrade`, `ISUpgradeWeapon` and `ISEquipWeaponAction` are queued.
6. Compatibility prefers upstream `part:canAttach(player, weapon)` and uses the older `mountOn` check only as fallback.

Static result: **architecture accepted**.

## 5. MFS grenade / ballistic changes

The imported MFS also changes grenade/explosion performance behavior:

- removes/replaces stale duplicate `OnTick` registrations;
- removes expired FX once instead of repeatedly in a loop;
- stores bounded zombie kill-confirmation entries and removes them after a confirmation window;
- removes old duplicate explosion-render/event code from `Ballistic_lite.lua` / `ExplosionTextures.lua`.

No LCC patch currently overrides these grenade/ballistic seams, so adding another compatibility layer would be counterproductive.

Static result: **leave upstream authoritative**.

Runtime recommendation: stress repeated explosive use while watching client FPS, `OnTick`-related errors and post-explosion recovery.

## 6. MFS magazine / inspection changes

The updated selector derives compatible magazines from ammo type/tags and supports multiple magazines/drums of the same ammunition family.

The rebased LCC attachment bridge explicitly delegates `ClipType`, `WeaponAttackType` and `Skin` controls to upstream behavior.

Static result: **no LCC override required**.

## 7. RussianTextFixes rebase

Package version: **`1.1.6`**

New MFS Russian coverage includes:

- `IGUI_WeaponUI_CritDmg`;
- `IGUI_WeaponUI_CyclicRate`;
- MFS Radio Trade title/money/buy/sell/hints/radio/refresh keys.

The split MFS fragment is aggregated into canonical Build 42 translation tables so these keys are visible to the normal loader.

### Generator bug fixed

`scripts/aggregate_russian_text.py` still had `TARGET_MOD_VERSION = "1.1.5"` after the package was bumped to 1.1.6. Running the aggregate workflow could therefore downgrade `mod.info`.

The pin is now `1.1.6`, and the canonical aggregate workflow restored the release metadata correctly.

Static result: **accepted**.

## 8. Audit/CI rebase

The previous grouped audit contained several obsolete assumptions unrelated to the 2026-09-02 MFS code itself:

- expected a removed RuntimeFixes zero-player wanderer file even though current Bandits2 already leaves its MP wanderer spawn branch inactive with no online player;
- expected the old `LaccckaB4220GridInventorySort` staging package instead of published `LaccckaPackFlow` 0.7.12 / Workshop `3789630746`;
- expected old PackFlow sorter APIs (`sendReorder`, `findCompatibleStack`, `findFreeSpace`) instead of the current `GridSortNetwork.sendSort` + compatibility-aware placement solver;
- assumed RussianTextFixes contained only RU and exact legacy file counts, which conflicts with canonical aggregation and the intentional small EN runtime fallback.

The audit now validates current package ownership and, specifically for MFS, fingerprints both sides of the dependency boundary:

- LCC bridge still has the occupied-slot/CAS/reachability contract;
- LCC bridge does not reintroduce selector overrides;
- upstream MFS still exposes reachable-container discovery, recursive attachment scanning, non-root transfer and attachment-state refresh.

The workflow now also triggers on imported Workshop source changes under top-level `*/mods/**`, so a future Workshop refresh can no longer bypass the grouped patch-impact audit simply because no `WorkshopPatches/**` file changed in the same import commit.

Static CI evidence:

- grouped patch contract: **PASS**;
- MFS rebase contract: **PASS**;
- NPC wanderer devirtualization guard: **PASS**.

## 9. Required live regression pass

Before treating this update as runtime accepted, test on the real Build 42.20.x MP stack with original upstream mods plus split LCC patches:

1. Open a normal empty attachment slot and install from root inventory.
2. Install from an equipped bag.
3. Install from a nested bag.
4. Install from a nearby crate/container.
5. Install from a nearby dropped bag/container.
6. Open an occupied normal attachment slot with LMB and replace the existing part.
7. Verify RMB removes; double-LMB does not remove.
8. Open a selector from a nearby world container, move out of reach, then select the old entry; the installed part must remain unchanged.
9. In multiplayer, verify transfer -> remove -> install -> equip ordering with no duplication/loss/desync.
10. Verify inspect 3D preview and equipped hand model refresh immediately after attachment changes.
11. Verify magazine/drum alternatives of the same ammo family still work and are unaffected by LCC.
12. Verify attack-mode and skin selectors still use upstream behavior.
13. Stress repeated grenade/explosive use and watch for FPS collapse, duplicate FX, persistent OnTick work or zombie-list retention.
14. Confirm the newly added MFS Russian UI strings appear in game and no English fallback remains for the covered keys.
15. Equip/remove a changed Bladesmith scabbard and `Avalon_cat` item once to validate model/GUID registration at runtime.

## Final static disposition

- `errorMagnifier` / Alert System: **compatible; no LCC changes required**.
- BladesmithSystem: **compatible by static inspection; asset smoke test pending**.
- MFS: **LCC RuntimeFixes rebased; duplicate selector override removed; narrow safety/UX bridge retained**.
- MFS grenade/ballistic update: **upstream authoritative; no duplicate LCC patch added**.
- RussianTextFixes: **rebased to 1.1.6; canonical loader coverage and version automation corrected**.
- grouped CI: **rebased to current package topology and upstream-mod-change triggering**.

Overall status: **STATIC ACCEPTED / RUNTIME REGRESSION REQUIRED**.
