#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPLIT="$ROOT/WorkshopPatches"
fail=0

error() { printf 'ERROR: %s\n' "$*" >&2; fail=1; }
require_file() {
    local path="$1"
    [[ -f "$path" && -s "$path" ]] || { error "missing/empty required file: ${path#$ROOT/}"; return 1; }
}
require_marker() {
    local path="$1" marker="$2" message="$3"
    [[ -f "$path" ]] || { error "cannot validate marker; file missing: ${path#$ROOT/}"; return; }
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

expected_patch_dirs=(ActivityFixes CompatibilityBridges GridInventorySort NPCCombatExperimental NPCFixes PatchCore RuntimeFixes RussianTextFixes SafetyFixes)
actual="$(mktemp)"; expected="$(mktemp)"
trap 'rm -f "$actual" "$expected"' EXIT
find "$SPLIT" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | grep -Fvx Bandits-LCC-Dev | grep -Fvx QuestFramework | sort > "$actual"
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

# PatchCore / Guard bootstrap.
core_guard="$core/lua/shared/LCC/Guard.lua"; core_entry="$core/lua/shared/LCC/CoreGuard.lua"
require_file "$core_guard" || true; require_file "$core_entry" || true
if [[ -f "$core_guard" && -f "$core_entry" ]] && ! cmp -s "$core_guard" "$core_entry"; then error "PatchCore Guard.lua/CoreGuard.lua drift"; fi
for marker in 'function Guard.safeRequire' 'function Guard.protect' 'function Guard.install' 'function Guard.wrapBefore' 'Guard.__initialized = true'; do
    require_marker "$core_guard" "$marker" "PatchCore Guard lost marker: $marker"
done
for guard in \
    "$runtime/lua/shared/LCC/Guard.lua" "$npc/lua/shared/LCC/Guard.lua" "$experimental/lua/shared/LCC/Guard.lua" \
    "$activity/lua/shared/LCC/Guard.lua" "$bridges/lua/shared/LCC/Guard.lua" "$safety/lua/shared/LCC/Guard.lua"; do
    require_file "$guard" || true
    require_marker "$guard" 'pcall(require, "LCC/CoreGuard")' "Guard no longer prefers PatchCore: ${guard#$ROOT/}"
    require_marker "$guard" 'CoreGuard.MODE = "GUARDED"' "Guard lost GUARDED mode: ${guard#$ROOT/}"
    require_marker "$guard" 'Guard.MODE = "DEGRADED"' "Guard lost DEGRADED mode: ${guard#$ROOT/}"
done

# RuntimeFixes source-clean Bandits contracts.
runtime_character="$runtime/lua/client/ISUI/ISCharacterScreen.lua"
runtime_mfs="$runtime/lua/client/MFSAttachmentAccessFix.lua"
runtime_cache="$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
runtime_dedicated="$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
runtime_farming="$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"
for path in "$runtime_character" "$runtime_mfs" "$runtime_cache" "$runtime_dedicated" "$runtime_farming"; do require_file "$path" || true; done
for forbidden in "$runtime/lua/client/BanditZombie.lua" "$runtime/lua/client/BanditUpdate.lua" "$runtime/lua/server/BanditServerWanderers.lua" "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua" "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua"; do
    [[ ! -e "$forbidden" ]] || error "RuntimeFixes bundled upstream Bandits source: ${forbidden#$ROOT/}"
done
require_marker "$runtime_character" 'XpSystem/ISUI/ISCharacterScreen' "RuntimeFixes character-screen redirect drift"
require_marker "$runtime_cache" 'not getSquareSafe(zombie)' "RuntimeFixes squareless predicate missing"
require_marker "$runtime_cache" 'Events.OnZombieUpdate.Add' "RuntimeFixes cache cleanup hook missing"
require_marker "$runtime_farming" 'shouldSkipWaterComplete' "RuntimeFixes farming invalid-state guard missing"
require_marker "$runtime_dedicated" 'BanditZombie.GetInstanceById = lookupZombie' "RuntimeFixes dedicated lookup missing"
require_marker "$runtime_dedicated" 'BanditServerZombie.Cache' "RuntimeFixes native cache fallback missing"
require_marker "$runtime_dedicated" 'Events.EveryOneMinute.Add(pruneRegistry)' "RuntimeFixes registry prune missing"
forbid_marker "$runtime_dedicated" 'getZombieList()' "RuntimeFixes dedicated lookup must not scan global zombies"

# MFS 2026-09-02 ownership boundary.
for marker in 'Fix.VERSION = "1.1.0"' 'type(getReachableContainers) == "function"' 'part:canAttach(player, weapon)' 'expectedInstalledId' 'Fix.queueInstallOrReplace' 'ISInventoryTransferAction:new' 'ISRemoveWeaponUpgrade:new' 'ISUpgradeWeapon:new' 'upstream selector retained'; do
    require_marker "$runtime_mfs" "$marker" "RuntimeFixes MFS bridge lost marker: $marker"
done
forbid_marker "$runtime_mfs" 'function selectAttachmentPane:renderInventory' "RuntimeFixes must not override updated MFS selector rendering"
forbid_marker "$runtime_mfs" 'function selectAttachmentPane:update' "RuntimeFixes must not override updated MFS selector update"
mfs_root="$ROOT/3633421539/mods/Escape from Kentucky4215/42/media/lua/client"
mfs_core="$mfs_root/UI/risky_inspect_core.lua"; mfs_pane="$mfs_root/UI/risky_inspect_selectAttachmentPane.lua"; mfs_button="$mfs_root/UI/risky_inspect_button.lua"; mfs_upgrade="$mfs_root/FixCode/ISUpgradeWeapon_FIX.lua"
for path in "$mfs_core" "$mfs_pane" "$mfs_button" "$mfs_upgrade"; do require_file "$path" || true; done
require_marker "$mfs_core" 'function getReachableContainers(player)' "MFS reachable-container API moved/removed"
require_marker "$mfs_pane" 'getReachableContainers(getPlayer())' "MFS selector no longer uses reachable containers"
require_marker "$mfs_pane" 'scanParts(container, getPlayer(), weapon' "MFS recursive attachment scan moved/removed"
require_marker "$mfs_button" 'ISInventoryTransferAction:new(player, part, srcContainer' "MFS non-root attachment transfer moved/removed"
require_marker "$mfs_upgrade" 'MFS_RefreshWeaponAttachmentState' "MFS attachment/model refresh moved/removed"

# NPCFixes source-clean 1.0.5 contract.
npc_bridge="$npc/lua/client/zz_LCC_BanditCallbackBridge.lua"
for path in "$npc_bridge" "$npc/lua/client/zzz_LCC_BanditCrawlerPlayerLunge.lua" "$npc/lua/client/zz_LCC_BanditClothingRestore.lua" "$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua" "$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua" "$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua" "$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua" "$npc/lua/server/zz_LCC_BanditServerClothingRestore.lua" "$npc/lua/server/zzz_LCC_BanditServerClothingSnapshotFallback.lua" "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua"; do require_file "$path" || true; done
require_marker "$npc_bridge" 'loadstring-free-predicate-bridge-v2' "NPCFixes predicate bridge marker missing"
require_marker "$npc_bridge" 'getModFileReader' "NPCFixes fingerprint reader missing"
require_marker "$npc_bridge" 'runtimeTransform=false' "NPCFixes source-clean marker missing"
forbid_marker "$npc_bridge" 'loadstring(' "NPCFixes must not runtime-compile upstream source"
require_marker "$npc/lua/client/zzz_LCC_BanditCrawlerPlayerLunge.lua" 'ordinary-crawler-player-lunge-v1' "NPCFixes crawler preservation missing"
require_marker "$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua" 'character-relation-suppression-v6' "NPCFixes relationship sanitation missing"
require_marker "$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua" 'fake-hit-relation-cleanup-v3' "NPCFixes late fake-hit cleanup missing"
require_marker "$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua" 'fake-hit-immediate-cleanup-v1' "NPCFixes immediate fake-hit cleanup missing"
require_marker "$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua" 'terminal-die-onground-pump-v1' "NPCFixes terminal Die pump missing"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'wanderer-devirtualization-bandit-preservation-v1' "NPCFixes wanderer preservation missing"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'PURGE_RADIUS = 30' "NPCFixes wanderer purge radius drift"
require_marker "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" 'DEFER_ON_BANDIT_OVERLAP' "NPCFixes wanderer deferral mode missing"
for forbidden in "$npc/lua/client/BanditUpdate.lua" "$npc/lua/shared/ZombieActions/ZAShoot.lua" "$npc/lua/client/BanditZombie.lua" "$npc/lua/server/BanditServerWanderers.lua"; do
    [[ ! -e "$forbidden" ]] || error "NPCFixes bundled upstream same-path Bandits source: ${forbidden#$ROOT/}"
done

# Experimental remains observe/admin-only.
require_file "$experimental/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua" || true
require_file "$experimental/lua/client/zzz_LCC_BanditsTargetDiagnostics.lua" || true
require_file "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua" || true
require_marker "$experimental/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua" 'hasStaffAccess' "NPCCombatExperimental staff guard missing"
require_marker "$experimental/lua/client/zzz_LCC_BanditsTargetDiagnostics.lua" '[LCC][BanditsDiag][SUMMARY]' "NPCCombatExperimental summary missing"
require_marker "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua" 'Events.OnZombieDead.Add' "NPCCombatExperimental death observation missing"
forbid_marker "$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua" 'inventory:AddItem(' "NPCCombatExperimental must remain observe-only"

# Activity / bridge / safety contracts.
require_marker "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua" 'BathTubFunctions.walkToFront' "ActivityFixes bathtub seam missing"
require_marker "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua" 'HiddenSkills.getSkill' "ActivityFixes Yoga authority missing"
require_marker "$activity/lua/client/zzz_LCC_SkillDescriptions.lua" 'ISSkillProgressBar.updateTooltip = function' "ActivityFixes tooltip wrapper missing"
require_marker "$activity/perks.txt" 'perk Yoga' "ActivityFixes Yoga perk proxy missing"
require_marker "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua" 'Vehicles/ISUI/ISVehiclePartMenu' "CompatibilityBridges vehicle redirect missing"
require_marker "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua" 'ISPlace3DItemCursor.__LCCWeaponPartRenderFix' "CompatibilityBridges 3D cursor fix missing"
require_marker "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua" 'OtherModsSupport/SVU3_PZKVLCCars_Stuffs' "CompatibilityBridges SVU3 redirect missing"
require_marker "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua" 'ItemCodeOnCreate.onCreateRecipeMagazine' "CompatibilityBridges callback shim missing"
require_marker "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua" 'ISInventoryTransferAction.isValid' "SafetyFixes Aegis transfer guard missing"
require_marker "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua" 'Guard.install' "SafetyFixes Chimera guard missing"

# PackFlow 0.7.12 current sorter/network contract.
pack_sort="$packflow/lua/client/LCC/GridAutoSort.lua"; pack_hook="$packflow/lua/client/zzz_LCC_GridInventorySort.lua"
for path in "$pack_sort" "$pack_hook" "$packflow/lua/shared/Translate/EN/UI.json" "$packflow/lua/shared/Translate/RU/UI.json"; do require_file "$path" || true; done
require_marker "$pack_sort" 'GridContainer.getStackInfo(item)' "PackFlow stack metadata path missing"
require_marker "$pack_sort" 'local function findBestPlacement' "PackFlow placement solver missing"
require_marker "$pack_sort" 'grid:canPlaceItem' "PackFlow compatibility-aware placement check missing"
require_marker "$pack_sort" 'GridSortNetwork.sendSort' "PackFlow authoritative MP sort transaction missing"
require_marker "$pack_sort" 'GridClientNetwork.markGridChanged' "PackFlow local grid refresh missing"
require_marker "$pack_hook" 'ISInventoryWindowContainerControls' "PackFlow inventory footer integration missing"
require_marker "$pack_hook" 'ISLootWindowContainerControls' "PackFlow loot footer integration missing"
for forbidden in GridRender.lua GridContainer.lua GridCore.lua GridClientNetwork.lua GridServerNetwork.lua GridReorder.lua; do
    if find "$packflow" -type f -name "$forbidden" -print -quit | grep -q .; then error "PackFlow bundled upstream GridInventory file: $forbidden"; fi
done
require_json "$packflow/lua/shared/Translate/EN/UI.json"; require_json "$packflow/lua/shared/Translate/RU/UI.json"

# RussianTextFixes canonical loader contract.
translate42="$text42/lua/shared/Translate"; ru42="$translate42/RU"; en42="$translate42/EN"; ru_common="$text_common/lua/shared/Translate/RU"
require_file "$en42/LCC_Runtime_UI.json" || true
[[ -d "$ru42" ]] || error "RussianTextFixes RU tree missing"
[[ -d "$ru_common" ]] || error "RussianTextFixes common RU tree missing"
if [[ -d "$translate42" ]] && find "$translate42" -mindepth 1 -maxdepth 1 -type d ! -name RU ! -name EN -print -quit | grep -q .; then error "RussianTextFixes 42 may contain only RU and EN"; fi
for table in ContextMenu Fluids IG_UI ItemName Recipes Sandbox Tooltip UI; do require_json "$ru42/$table.json"; done
while IFS= read -r -d '' json; do python3 -m json.tool "$json" >/dev/null 2>&1 || error "invalid translation JSON: ${json#$ROOT/}"; done < <(find "$translate42" "$ru_common" -type f -name '*.json' -print0)
mfs_ru="$ru42/LCC_MFS_IG_UI.json"
require_marker "$mfs_ru" '"IGUI_WeaponUI_CritDmg": "Крит. урон"' "RussianTextFixes MFS crit translation missing"
require_marker "$mfs_ru" '"IGUI_WeaponUI_CyclicRate": "Множитель темпа стрельбы"' "RussianTextFixes MFS cyclic-rate translation missing"
require_marker "$mfs_ru" '"IGUI_MFSTrade_Title": "Торговля по радио"' "RussianTextFixes MFS trade translation missing"
require_marker "$ru42/IG_UI.json" '"IGUI_MFSTrade_Title": "Торговля по радио"' "canonical MFS trade translation missing"

# Metadata / dependency contracts.
declare -A mod_ids=([PatchCore]=LaccckaB4220PatchCore [RuntimeFixes]=LaccckaB4220RuntimeFixes [NPCFixes]=LaccckaB4220NPCFixes [NPCCombatExperimental]=LaccckaB4220NPCCombatExperimental [ActivityFixes]=LaccckaB4220ActivityFixes [CompatibilityBridges]=LaccckaB4220CompatBridges [SafetyFixes]=LaccckaB4220SafetyFixes [GridInventorySort]=LaccckaPackFlow [RussianTextFixes]=LaccckaB4220RussianText)
declare -A workshop_ids=([PatchCore]=3786175901 [RuntimeFixes]=3786175979 [NPCFixes]=3787592350 [NPCCombatExperimental]=3786817782 [ActivityFixes]=3786175725 [CompatibilityBridges]=3786175808 [SafetyFixes]=3786176221 [GridInventorySort]=3789630746 [RussianTextFixes]=3786176120)
for folder in "${expected_patch_dirs[@]}"; do
    id="${mod_ids[$folder]}"; wid="${workshop_ids[$folder]}"; modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"; workshop="$SPLIT/$folder/workshop.txt"
    require_file "$modinfo" || true; require_file "$workshop" || true
    require_marker "$modinfo" "id=$id" "$folder Mod ID drift"
    require_marker "$modinfo" 'versionMin=42.20.0' "$folder Build minimum drift"
    require_marker "$workshop" "id=$wid" "$folder Workshop ID drift"
    require_marker "$workshop" 'Do not use' "$folder Workshop warning missing"
done
runtime_info="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/mod.info"; npc_info="$SPLIT/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/mod.info"; exp_info="$SPLIT/NPCCombatExperimental/Contents/mods/LaccckaB4220NPCCombatExperimental/42/mod.info"; act_info="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/mod.info"; bridge_info="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/mod.info"; safe_info="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/mod.info"; pack_info="$SPLIT/GridInventorySort/Contents/mods/LaccckaPackFlow/42/mod.info"; text_info="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/mod.info"
require_marker "$runtime_info" 'modversion=1.2.3' "RuntimeFixes must be 1.2.3"
require_marker "$runtime_info" '\ModernFirearmsSystem' "RuntimeFixes MFS load order missing"
require_marker "$runtime_info" '\MFS_community_fix' "RuntimeFixes MFS community-fix load order missing"
require_marker "$npc_info" 'modversion=1.0.5' "NPCFixes must be 1.0.5"
require_marker "$npc_info" '\LaccckaB4220RuntimeFixes' "NPCFixes RuntimeFixes load order missing"
require_marker "$exp_info" '\LaccckaB4220NPCFixes' "NPCCombatExperimental NPCFixes load order missing"
require_marker "$act_info" '\LifestyleHobbies' "ActivityFixes Lifestyle load order missing"
require_marker "$bridge_info" '\ModernFirearmsSystem' "CompatibilityBridges MFS load order missing"
require_marker "$safe_info" '\Federal_Rangers_Chimera' "SafetyFixes Chimera load order missing"
require_marker "$pack_info" 'name=PackFlow' "PackFlow name drift"
require_marker "$pack_info" 'modversion=0.7.12' "PackFlow must be 0.7.12"
require_marker "$pack_info" 'require=\GridInventory' "PackFlow must require GridInventory"
require_marker "$pack_info" 'loadModAfter=\GridInventory' "PackFlow GridInventory load order missing"
require_marker "$text_info" 'modversion=1.1.6' "RussianTextFixes must remain 1.1.6"
forbid_marker "$text_info" '\LaccckaB4220PatchCore' "RussianTextFixes must remain standalone"
for info in "$runtime_info" "$npc_info" "$exp_info" "$act_info" "$bridge_info" "$safe_info"; do
    require_marker "$info" '\LaccckaB4220PatchCore' "PatchCore soft load order missing: ${info#$ROOT/}"
    grep -Eq '^require=.*LaccckaB4220PatchCore' "$info" && error "PatchCore became a hard dependency: ${info#$ROOT/}"
done

# Syntax-check the newly rebased high-risk Lua seams with stock Lua.
for lua_file in "$runtime_mfs" "$npc/lua/server/zz_LCC_BanditWandererDevirtualizationGuard.lua" "$pack_sort"; do
    lua -e "assert(loadfile([[$lua_file]]))" >/dev/null 2>&1 || error "Lua syntax failure: ${lua_file#$ROOT/}"
done

(( fail == 0 )) || exit 1
printf 'Grouped Workshop patches audit: OK (RuntimeFixes=1.2.3 MFS-rebased; NPCFixes=1.0.5; PackFlow=0.7.12; RussianTextFixes=1.1.6)\n'
