# Workshop publication audit

This is an engineering/publication-safety checklist, not legal advice. Re-check current Project Zomboid/Steam rules and target Workshop pages before changing an item from unlisted to public.

## Boundary used by this split

1. Runtime compatibility items do not ship target-mod Lua source, models, textures, sounds, vehicles, clothing assets, or other repacked runtime content.
2. Independent LCC items require original mods separately and interact with installed APIs/types through LCC-authored hooks, wrappers, guards, and path shims.
3. Public Workshop titles are neutral functional LCC names. Direct compatibility targets/authors remain explicit in the item description/credits rather than the title.
4. Translation projects are separate Workshop items rather than being bundled into runtime fixes.
5. Modpacks/reuploads are intentionally not used here.
6. All split projects begin as `visibility=unlisted` and require smoke testing before wider visibility.

## Current project status

All 12 projects are **READY_FOR_UNLISTED_TEST**:

| Project | Public title | Payload boundary |
|---|---|---|
| `LCCB4220FirearmsBridge` | LCC B42.20 Firearms Placement Bridge | LCC runtime wrapper only. |
| `LCCB4220SVUTsarBridge` | LCC B42.20 Vehicle API Bridge | LCC legacy path/API shim only. |
| `LCCB4220zReBridge` | LCC B42.20 Vaccine API Bridge | LCC API path shim only. |
| `LCCB4220AegisGuard` | LCC B42.20 Inventory Safety Guard | LCC inventory validation wrapper only. |
| `LCCB4220LegacyCallbacks` | LCC B42.20 Legacy Callback Bridge | Generic LCC Build 42 callback bridge. |
| `LCCB4220SkillDescriptionsRU` | LCC Russian Skill Descriptions for B42.20 | LCC-authored vanilla/B42 skill descriptions. |
| `LCCB4220SurvivorAIStability` | LCC B42.20 Survivor AI Stability | LCC guards/shims; no full target Lua overrides. |
| `LCCB4220WellnessCompat` | LCC B42.20 Wellness Compatibility | LCC bath/shower/Yoga runtime code and Yoga-only proxy declaration. |
| `LCCB4220PZKBridge` | LCC B42.20 Vehicle Integration Bridge | LCC path/API shims only. |
| `LCCB4220OutfitMenuSafety` | LCC B42.20 Outfit Menu Safety | LCC runtime wrapper only. |
| `LCCB4220BanditsRU` | LCC B42.20 Survivor Dialogue RU | Standalone RU survivor/NPC dialogue localization. |
| `LCCB4220LifestyleRU` | LCC B42.20 Wellness & Hobbies RU | Standalone RU wellness/hobby localization plus LCC strings. |

## Runtime architecture checks

### Survivor AI stability

Compatibility target/credit is kept in the Workshop description. The split carries none of the four former full-file overrides:

- `BanditZombie.lua`
- `BanditServerWanderers.lua`
- `ZombieActions/ZAStompPlant.lua`
- `ZombieActions/ZAWaterFarm.lua`

LCC-owned cache, farming, dedicated-server and empty-server guards work against the separately installed dependency. The empty-server guard must not depend on `isClient()` because current target server code treats client state as the path to skip and uses `getOnlinePlayers()` for multiplayer player enumeration.

### Wellness compatibility

The runtime split contains LCC-authored bath/shower wrappers/placeholders plus Yoga UI integration. Its `media/perks.txt` declares only `Yoga`, with `parent = Lifestyle` and zero proxy XP thresholds. Authoritative Yoga level/XP remains in the installed target's `HiddenSkills` storage.

`zzy_LCC_LifestyleYogaContract.lua` validates the resolved CustomPerk parent on `OnGameStart`; an upstream/load-order contract change disables only the Yoga UI feature and emits an LCC diagnostic.

### Vehicle/API and outfit modules

The vehicle integration/API projects are path/require bridges only. The outfit-menu project is a wrapper over the installed UI method and does not contain clothing/source assets.

## Translation separation

### Survivor Dialogue RU

`LCCB4220BanditsRU/.../IG_UI.json` is byte-for-byte identical to the isolated target-side RU `IG_UI` snapshot used by the compatibility package. It is now its own unlisted Workshop item and is not a dependency of the runtime stability project.

### Wellness & Hobbies RU

`LCCB4220LifestyleRU` contains the remaining wellness/hobby-oriented RU categories plus LCC custom compatibility strings. The survivor-dialogue `42/.../IG_UI.json` blob is explicitly excluded, and CI forbids the deprecated mixed `LCCB4220ThirdPartyRU` package from returning.

## Naming rules

- `title=` must remain target-neutral for direct compatibility modules.
- Do not use target names such as Bandits/Lifestyle/PZK/Chimera/Aegis/SVU/Tsar/zRe in the public Workshop title.
- Do name/credit the actual compatibility target and author in `description=` when there is a direct relationship.
- Do not describe an LCC module as official.

## Publication/test rules

- Never bundle original Workshop directories or target runtime source/assets into source-clean compatibility items.
- Keep every item `visibility=unlisted` until its feature/localization smoke test passes.
- Do not enable split and equivalent monolithic runtime fixes together during A/B tests.
- Keep translation items separate from runtime patches.
- Assign a Workshop ID only after the item passes its initial test.
- If an upstream contract changes, prefer a narrow LCC shim/wrapper over copying the target file.

`tools/audit_workshop_split.py` enforces these packaging boundaries. `tools/audit_wellness_proxy.py` checks the Yoga-only CustomPerk contract.
