#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPLIT="$ROOT/WorkshopPatches"
fail=0

error() {
    printf 'ERROR: %s\n' "$*" >&2
    fail=1
}

require_file() {
    local path="$1"
    if [[ ! -f "$path" ]]; then
        error "missing required file: ${path#$ROOT/}"
        return 1
    fi
    if [[ ! -s "$path" ]]; then
        error "required file is empty: ${path#$ROOT/}"
        return 1
    fi
}

require_marker() {
    local path="$1" marker="$2" message="$3"
    if [[ ! -f "$path" ]]; then
        error "cannot validate marker; file missing: ${path#$ROOT/}"
        return
    fi
    grep -Fq -- "$marker" "$path" || error "$message"
}

forbid_marker() {
    local path="$1" marker="$2" message="$3"
    [[ ! -f "$path" ]] || ! grep -Fq -- "$marker" "$path" || error "$message"
}

require_json() {
    local path="$1"
    require_file "$path" || return
    python3 -m json.tool "$path" >/dev/null 2>&1 || error "invalid JSON: ${path#$ROOT/}"
}

expected_patch_dirs=(
    ActivityFixes
    CompatibilityBridges
    GridInventorySort
    NPCCombatExperimental
    NPCFixes
    PatchCore
    RuntimeFixes
    RussianTextFixes
    SafetyFixes
)

actual="$(mktemp)"
expected="$(mktemp)"
trap 'rm -f "$actual" "$expected"' EXIT
find "$SPLIT" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' \
    | grep -Fvx 'Bandits-LCC-Dev' \
    | grep -Fvx 'QuestFramework' \
    | sort > "$actual"
printf '%s\n' "${expected_patch_dirs[@]}" | sort > "$expected"
diff -u "$expected" "$actual" >/dev/null || error "published/staged patch directory set changed unexpectedly"

core="$SPLIT/PatchCore/Contents/mods/LaccckaB4220PatchCore/42/media"
runtime="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/media"
npc="$SPLIT/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/media"
experimental="$SPLIT/NPCCombatExperimental/Contents/mods/LaccckaB4220NPCCombatExperimental/42/media"
activity="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media"
bridges="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/media"
safety="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/media"
packflow="$SPLIT/GridInventorySort/Contents/mods/LaccckaPackFlow/42/media"
text42="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media"
text_common="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/common/media"

# ---------------------------------------------------------------------------
# Required source-clean patch files
# ---------------------------------------------------------------------------
required_files=(
    "$core/lua/shared/LCC/Guard.lua"
    "$core/lua/shared/LCC/CoreGuard.lua"

    "$runtime/lua/client/ISUI/ISCharacterScreen.lua"
    "$runtime/lua/client/MFSAttachmentAccessFix.lua"
    "$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
    "$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
    "$runtime/lua/shared/LCC/Guard.lua"
    "$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

    "$npc/lua/client/zz_LCC_BanditCallbackBridge.lua"
    "$npc/lua/client/zzz_LCC_BanditCrawlerPlayerLunge.lua"
    "$npc/lua/client/zz_LCC_BanditClothingRestore.lua"
    "$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua"
    "$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua"
    "$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua"
    "$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua"
    "$npc/lua/server/zz_LCC_BanditServerClothingRestore.lua"
    "$npc/lua/server/zzz_LCC_BanditServerClothingSnapshotFallback.lua"
    "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua"
    "$npc/lua/shared/LCC/Guard.lua"

    "$experimental/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua"
    "$experimental/lua/client/zzz_LCC_BanditsTargetDiagnostics.lua"
    "$experimental/lua/server/zzz_LCC_BanditsTestSpawnBridge.lua"
    "$experimental/lua/shared/LCC/Guard.lua"
    "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua"

    "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
    "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
    "$activity/lua/client/zzz_LCC_SkillDescriptions.lua"
    "$activity/lua/shared/Hygiene/BathTubFunctions.lua"
    "$activity/lua/shared/Hygiene/ShowerFunctions.lua"
    "$activity/lua/shared/LCC/Guard.lua"
    "$activity/perks.txt"

    "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
    "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
    "$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
    "$bridges/lua/server/utils/pzkZonesFunction.lua"
    "$bridges/lua/shared/BodyLocations.lua"
    "$bridges/lua/shared/ISBaseTimedAction.lua"
    "$bridges/lua/shared/LCC/Guard.lua"
    "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
    "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"

    "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
    "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"
    "$safety/lua/shared/LCC/Guard.lua"

    "$packflow/lua/client/LCC/GridAutoSort.lua"
    "$packflow/lua/client/zzz_LCC_GridInventorySort.lua"
    "$packflow/lua/shared/Translate/EN/UI.json"
    "$packflow/lua/shared/Translate/RU/UI.json"
)
for path in "${required_files[@]}"; do require_file "$path" || true; done

# ---------------------------------------------------------------------------
# PatchCore / Guard bootstrap
# ---------------------------------------------------------------------------
core_guard="$core/lua/shared/LCC/Guard.lua"
core_entry="$core/lua/shared/LCC/CoreGuard.lua"
if [[ -f "$core_guard" && -f "$core_entry" ]] && ! cmp -s "$core_guard" "$core_entry"; then
    error "PatchCore Guard.lua and CoreGuard.lua must remain identical"
fi
for marker in 'function Guard.safeRequire' 'function Guard.protect' 'function Guard.install' 'function Guard.wrapBefore' 'Guard.__initialized = true'; do
    require_marker "$core_guard" "$marker" "PatchCore Guard lost contract marker: $marker"
done

functional_guards=(
    "$runtime/lua/shared/LCC/Guard.lua"
    "$npc/lua/shared/LCC/Guard.lua"
    "$experimental/lua/shared/LCC/Guard.lua"
    "$activity/lua/shared/LCC/Guard.lua"
    "$bridges/lua/shared/LCC/Guard.lua"
    "$safety/lua/shared/LCC/Guard.lua"
)
for guard in "${functional_guards[@]}"; do
    require_marker "$guard" 'pcall(require, "LCC/CoreGuard")' "Guard bootstrap does not prefer PatchCore: ${guard#$ROOT/}"
    require_marker "$guard" 'CoreGuard.MODE = "GUARDED"' "Guard bootstrap lost GUARDED mode: ${guard#$ROOT/}"
    require_marker "$guard" 'Guard.MODE = "DEGRADED"' "Guard bootstrap lost DEGRADED mode: ${guard#$ROOT/}"
done

# ---------------------------------------------------------------------------
# RuntimeFixes, including the 2026-09-02 MFS rebase
# ---------------------------------------------------------------------------
runtime_character="$runtime/lua/client/ISUI/ISCharacterScreen.lua"
runtime_mfs="$runtime/lua/client/MFSAttachmentAccessFix.lua"
runtime_cache="$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
runtime_dedicated="$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
runtime_farming="$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

for forbidden in \
    "$runtime/lua/client/BanditZombie.lua" \
    "$runtime/lua/client/BanditUpdate.lua" \
    "$runtime/lua/server/BanditServerWanderers.lua" \
    "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua" \
    "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua"; do
    [[ ! -e "$forbidden" ]] || error "RuntimeFixes must not bundle upstream Bandits source: ${forbidden#$ROOT/}"
done

require_marker "$runtime_character" 'XpSystem/ISUI/ISCharacterScreen' "RuntimeFixes character-screen shim lost B42 target"
require_marker "$runtime_cache" 'not getSquareSafe(zombie)' "RuntimeFixes cache guard lost squareless predicate"
require_marker "$runtime_cache" 'Events.OnZombieUpdate.Add' "RuntimeFixes cache guard lost cleanup hook"
require_marker "$runtime_farming" 'shouldSkipWaterComplete' "RuntimeFixes farming guard lost invalid-water completion seam"
require_marker "$runtime_farming" 'CFarmingSystem.instance' "RuntimeFixes farming guard lost B42 farming availability check"
require_marker "$runtime_dedicated" 'BanditZombie.GetInstanceById = lookupZombie' "RuntimeFixes dedicated lookup contract missing"
require_marker "$runtime_dedicated" 'BanditServerZombie.Cache' "RuntimeFixes dedicated lookup lost optional native-cache path"
require_marker "$runtime_dedicated" 'Events.EveryOneMinute.Add(pruneRegistry)' "RuntimeFixes dedicated lookup lost stale pruning"
forbid_marker "$runtime_dedicated" 'getZombieList()' "RuntimeFixes dedicated lookup must not scan the global zombie list"

# Upstream MFS now owns discovery/rendering; LCC keeps only occupied-slot UX and
# stale-source/CAS safety. A future MFS update that moves these seams must fail
# this audit instead of silently running an obsolete monkeypatch.
for marker in \
    'Fix.VERSION = "1.1.0"' \
    'type(getReachableContainers) == "function"' \
    'part:canAttach(player, weapon)' \
    'expectedInstalledId' \
    'Fix.queueInstallOrReplace' \
    'ISInventoryTransferAction:new' \
    'ISRemoveWeaponUpgrade:new' \
    'ISUpgradeWeapon:new' \
    'upstream selector retained'; do
    require_marker "$runtime_mfs" "$marker" "RuntimeFixes MFS bridge lost marker: $marker"
done
forbid_marker "$runtime_mfs" 'function selectAttachmentPane:renderInventory' "RuntimeFixes must not replace the updated upstream MFS selector"
forbid_marker "$runtime_mfs" 'function selectAttachmentPane:update' "RuntimeFixes must not replace the updated upstream MFS selector update loop"

mfs_root="$ROOT/3633421539/mods/Escape from Kentucky4215/42/media/lua/client"
mfs_core="$mfs_root/UI/risky_inspect_core.lua"
mfs_pane="$mfs_root/UI/risky_inspect_selectAttachmentPane.lua"
mfs_button="$mfs_root/UI/risky_inspect_button.lua"
mfs_upgrade="$mfs_root/FixCode/ISUpgradeWeapon_FIX.lua"
for path in "$mfs_core" "$mfs_pane" "$mfs_button" "$mfs_upgrade"; do require_file "$path" || true; done
require_marker "$mfs_core" 'function getReachableContainers(player)' "updated MFS lost reachable-container discovery seam"
require_marker "$mfs_pane" 'getReachableContainers(getPlayer())' "updated MFS selector no longer delegates to reachable containers"
require_marker "$mfs_pane" 'scanParts(container, getPlayer(), weapon' "updated MFS selector lost recursive part discovery"
require_marker "$mfs_button" 'ISInventoryTransferAction:new(player, part, srcContainer' "updated MFS attachment action lost non-root transfer"
require_marker "$mfs_upgrade" 'MFS_RefreshWeaponAttachmentState' "updated MFS upgrade action lost attachment/model refresh"

# ---------------------------------------------------------------------------
# NPCFixes / diagnostics isolation
# ---------------------------------------------------------------------------
npc_bridge="$npc/lua/client/zz_LCC_BanditCallbackBridge.lua"
require_marker "$npc_bridge" 'loadstring-free-predicate-bridge-v2' "NPCFixes lost validated loadstring-free bridge"
require_marker "$npc_bridge" 'getModFileReader' "NPCFixes bridge lost installed-source fingerprinting"
require_marker "$npc_bridge" 'runtimeTransform=false' "NPCFixes must remain source-clean at runtime"
forbid_marker "$npc_bridge" 'loadstring(' "NPCFixes must not compile upstream Lua source at runtime"
require_marker "$npc/lua/client/zzz_LCC_BanditCrawlerPlayerLunge.lua" 'ordinary-crawler-player-lunge-v1' "NPCFixes crawler seam marker missing"
require_marker "$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua" 'character-relation-suppression-v6' "NPCFixes relation suppression marker missing"
require_marker "$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua" 'fake-hit-relation-cleanup-v3' "NPCFixes late fake-hit marker missing"
require_marker "$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua" 'fake-hit-immediate-cleanup-v1' "NPCFixes immediate fake-hit marker missing"
require_marker "$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua" 'terminal-die-onground-pump-v1' "NPCFixes terminal-die marker missing"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'wanderer-devirtualization-bandit-preservation-v1' "NPCFixes wanderer devirtualization marker missing"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'PURGE_RADIUS = 30' "NPCFixes wanderer purge-radius contract changed"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'DEFER_ON_BANDIT_OVERLAP' "NPCFixes wanderer overlap deferral missing"

for forbidden in \
    "$npc/lua/client/BanditUpdate.lua" \
    "$npc/lua/shared/ZombieActions/ZAShoot.lua" \
    "$npc/lua/client/BanditZombie.lua" \
    "$npc/lua/server/BanditServerWanderers.lua"; do
    [[ ! -e "$forbidden" ]] || error "NPCFixes must not bundle upstream same-path Bandits source: ${forbidden#$ROOT/}"
done

require_marker "$experimental/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua" 'hasStaffAccess' "NPCCombatExperimental admin guard missing"
require_marker "$experimental/lua/client/zzz_LCC_BanditsTargetDiagnostics.lua" '[LCC][BanditsDiag][SUMMARY]' "NPCCombatExperimental summary diagnostics missing"
require_marker "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua" 'Events.OnZombieDead.Add' "NPCCombatExperimental death diagnostics missing"
forbid_marker "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua" 'inventory:AddItem(' "NPCCombatExperimental diagnostics must remain observe-only"

# ---------------------------------------------------------------------------
# Other patch contracts
# ---------------------------------------------------------------------------
require_marker "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua" 'BathTubFunctions.walkToFront' "ActivityFixes bathtub seam missing"
require_marker "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua" 'HiddenSkills.getSkill' "ActivityFixes Yoga authority missing"
require_marker "$activity/lua/client/zzz_LCC_SkillDescriptions.lua" 'ISSkillProgressBar.updateTooltip = function' "ActivityFixes tooltip wrapper missing"
require_marker "$activity/perks.txt" 'perk Yoga' "ActivityFixes Yoga perk proxy missing"

require_marker "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua" 'Vehicles/ISUI/ISVehiclePartMenu' "CompatibilityBridges vehicle path redirect missing"
require_marker "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua" 'ISPlace3DItemCursor.__LCCWeaponPartRenderFix' "CompatibilityBridges 3D cursor fix missing"
require_marker "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua" 'OtherModsSupport/SVU3_PZKVLCCars_Stuffs' "CompatibilityBridges SVU3 redirect missing"
require_marker "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua" 'ItemCodeOnCreate.onCreateRecipeMagazine' "CompatibilityBridges item callback shim missing"

require_marker "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua" 'ISInventoryTransferAction.isValid' "SafetyFixes Aegis transfer seam missing"
require_marker "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua" 'Guard.install' "SafetyFixes Chimera guard missing"

# PackFlow is the published successor of the original GridInventorySort staging
# package. It hard-requires GridInventory and must remain source-clean.
require_marker "$packflow/lua/client/LCC/GridAutoSort.lua" 'GridClientNetwork.sendReorder' "PackFlow authoritative reorder path missing"
require_marker "$packflow/lua/client/LCC/GridAutoSort.lua" 'grid:findCompatibleStack' "PackFlow stack consolidation missing"
require_marker "$packflow/lua/client/LCC/GridAutoSort.lua" 'grid:findFreeSpace' "PackFlow placement algorithm missing"
require_marker "$packflow/lua/client/zzz_LCC_GridInventorySort.lua" 'ISInventoryWindowContainerControls' "PackFlow inventory footer integration missing"
require_marker "$packflow/lua/client/zzz_LCC_GridInventorySort.lua" 'ISLootWindowContainerControls' "PackFlow loot footer integration missing"
for forbidden in GridRender.lua GridContainer.lua GridCore.lua GridClientNetwork.lua GridServerNetwork.lua GridReorder.lua; do
    if find "$packflow" -type f -name "$forbidden" -print -quit | grep -q .; then
        error "PackFlow must not bundle upstream GridInventory implementation: $forbidden"
    fi
done
require_json "$packflow/lua/shared/Translate/EN/UI.json"
require_json "$packflow/lua/shared/Translate/RU/UI.json"

# ---------------------------------------------------------------------------
# RussianTextFixes canonical Build 42 loader contract
# ---------------------------------------------------------------------------
translate42="$text42/lua/shared/Translate"
ru42="$translate42/RU"
en42="$translate42/EN"
ru_common="$text_common/lua/shared/Translate/RU"
require_file "$en42/LCC_Runtime_UI.json" || true
[[ -d "$ru42" ]] || error "RussianTextFixes 42 RU tree is missing"
[[ -d "$ru_common" ]] || error "RussianTextFixes common RU tree is missing"

# 42 intentionally contains RU plus the tiny EN runtime fallback. No unrelated
# language directory may enter this patch package.
if [[ -d "$translate42" ]] && find "$translate42" -mindepth 1 -maxdepth 1 -type d ! -name RU ! -name EN -print -quit | grep -q .; then
    error "RussianTextFixes 42 translation layer may contain only RU and EN"
fi

canonical_tables=(ContextMenu Fluids IG_UI ItemName Recipes Sandbox Tooltip UI)
for table in "${canonical_tables[@]}"; do
    require_json "$ru42/$table.json"
done
while IFS= read -r -d '' json; do
    python3 -m json.tool "$json" >/dev/null 2>&1 || error "invalid translation JSON: ${json#$ROOT/}"
done < <(find "$translate42" "$ru_common" -type f -name '*.json' -print0)

mfs_ru="$ru42/LCC_MFS_IG_UI.json"
require_marker "$mfs_ru" '"IGUI_WeaponUI_CritDmg": "Крит. урон"' "RussianTextFixes lost new MFS critical-damage translation"
require_marker "$mfs_ru" '"IGUI_WeaponUI_CyclicRate": "Множитель темпа стрельбы"' "RussianTextFixes lost new MFS cyclic-rate translation"
require_marker "$mfs_ru" '"IGUI_MFSTrade_Title": "Торговля по радио"' "RussianTextFixes lost MFS Radio Trade translations"
require_marker "$ru42/IG_UI.json" '"IGUI_MFSTrade_Title": "Торговля по радио"' "canonical IG_UI.json did not absorb MFS Radio Trade fragment"

# ---------------------------------------------------------------------------
# Metadata / dependency contracts
# ---------------------------------------------------------------------------
declare -A mod_ids=(
    [PatchCore]='LaccckaB4220PatchCore'
    [RuntimeFixes]='LaccckaB4220RuntimeFixes'
    [NPCFixes]='LaccckaB4220NPCFixes'
    [NPCCombatExperimental]='LaccckaB4220NPCCombatExperimental'
    [ActivityFixes]='LaccckaB4220ActivityFixes'
    [CompatibilityBridges]='LaccckaB4220CompatBridges'
    [SafetyFixes]='LaccckaB4220SafetyFixes'
    [GridInventorySort]='LaccckaPackFlow'
    [RussianTextFixes]='LaccckaB4220RussianText'
)
declare -A workshop_ids=(
    [PatchCore]='3786175901'
    [RuntimeFixes]='3786175979'
    [NPCFixes]='3787592350'
    [NPCCombatExperimental]='3786817782'
    [ActivityFixes]='3786175725'
    [CompatibilityBridges]='3786175808'
    [SafetyFixes]='3786176221'
    [GridInventorySort]='3789630746'
    [RussianTextFixes]='3786176120'
)

for folder in "${expected_patch_dirs[@]}"; do
    id="${mod_ids[$folder]}"
    wid="${workshop_ids[$folder]}"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"
    workshop="$SPLIT/$folder/workshop.txt"
    require_file "$modinfo" || true
    require_file "$workshop" || true
    require_marker "$modinfo" "id=$id" "$folder has wrong Mod ID"
    require_marker "$modinfo" 'versionMin=42.20.0' "$folder must target Build 42.20.0+"
    require_marker "$workshop" "id=$wid" "$folder has wrong Workshop ID"
    require_marker "$workshop" 'Do not use' "$folder lost the Workshop safety warning"
done

runtime_modinfo="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/mod.info"
npc_modinfo="$SPLIT/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/mod.info"
experimental_modinfo="$SPLIT/NPCCombatExperimental/Contents/mods/LaccckaB4220NPCCombatExperimental/42/mod.info"
activity_modinfo="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/mod.info"
bridges_modinfo="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/mod.info"
safety_modinfo="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/mod.info"
packflow_modinfo="$SPLIT/GridInventorySort/Contents/mods/LaccckaPackFlow/42/mod.info"
text_modinfo="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/mod.info"

require_marker "$runtime_modinfo" 'modversion=1.2.3' "RuntimeFixes version must be 1.2.3 after MFS rebase"
require_marker "$runtime_modinfo" '\ModernFirearmsSystem' "RuntimeFixes load order lost MFS"
require_marker "$runtime_modinfo" '\MFS_community_fix' "RuntimeFixes load order lost MFS community fix"
require_marker "$npc_modinfo" 'modversion=1.0.5' "NPCFixes version drifted from current stable 1.0.5"
require_marker "$npc_modinfo" '\LaccckaB4220RuntimeFixes' "NPCFixes must soft-load after RuntimeFixes"
require_marker "$experimental_modinfo" '\LaccckaB4220NPCFixes' "NPCCombatExperimental must load after stable NPCFixes"
require_marker "$activity_modinfo" '\LifestyleHobbies' "ActivityFixes load order lost LifestyleHobbies"
require_marker "$bridges_modinfo" '\ModernFirearmsSystem' "CompatibilityBridges load order lost MFS"
require_marker "$safety_modinfo" '\Federal_Rangers_Chimera' "SafetyFixes load order lost Chimera"
require_marker "$packflow_modinfo" 'name=PackFlow' "PackFlow public mod name changed"
require_marker "$packflow_modinfo" 'modversion=0.7.12' "PackFlow version must be 0.7.12"
require_marker "$packflow_modinfo" 'require=\GridInventory' "PackFlow must hard-require GridInventory"
require_marker "$packflow_modinfo" 'loadModAfter=\GridInventory' "PackFlow must load after GridInventory"
require_marker "$text_modinfo" 'modversion=1.1.6' "RussianTextFixes must remain 1.1.6 after MFS translation rebase"
forbid_marker "$text_modinfo" '\LaccckaB4220PatchCore' "RussianTextFixes must remain standalone"

for modinfo in "$runtime_modinfo" "$npc_modinfo" "$experimental_modinfo" "$activity_modinfo" "$bridges_modinfo" "$safety_modinfo"; do
    require_marker "$modinfo" '\LaccckaB4220PatchCore' "functional patch lost PatchCore soft load order: ${modinfo#$ROOT/}"
    if grep -Eq '^require=.*LaccckaB4220PatchCore' "$modinfo"; then
        error "PatchCore must remain a soft dependency: ${modinfo#$ROOT/}"
    fi
done

# Syntax-check LCC-authored Lua payloads. Upstream Workshop Lua is intentionally
# not parsed here because PZ's Kahlua dialect may contain constructs rejected by
# stock Lua 5.4; the contract concerns our patch files.
while IFS= read -r -d '' lua; do
    lua -e "assert(loadfile([[$lua]]))" >/dev/null 2>&1 || error "Lua syntax failure: ${lua#$ROOT/}"
done < <(find "$SPLIT" -path '*/42/media/lua/*' -type f -name '*.lua' -print0)

if (( fail != 0 )); then
    exit 1
fi

printf 'Grouped Workshop patches audit: OK (RuntimeFixes 1.2.3 MFS-rebased; NPCFixes 1.0.5; PackFlow 0.7.12; RussianTextFixes 1.1.6)\n'
