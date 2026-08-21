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
    return 0
}

require_marker() {
    local path="$1"
    local marker="$2"
    local message="$3"
    if [[ ! -f "$path" ]]; then
        error "cannot validate marker; file missing: ${path#$ROOT/}"
        return
    fi
    if ! grep -Fq -- "$marker" "$path"; then
        error "$message"
    fi
}

forbid_marker() {
    local path="$1"
    local marker="$2"
    local message="$3"
    if [[ -f "$path" ]] && grep -Fq -- "$marker" "$path"; then
        error "$message"
    fi
}

forbid_regex() {
    local path="$1"
    local regex="$2"
    local message="$3"
    if [[ -f "$path" ]] && grep -Eq -- "$regex" "$path"; then
        error "$message"
    fi
}

max_size() {
    local path="$1"
    local limit="$2"
    local message="$3"
    [[ -f "$path" ]] || return
    local size
    size="$(wc -c < "$path")"
    if (( size > limit )); then
        error "$message (size=$size, limit=$limit)"
    fi
}

# Bandits-LCC-Dev is an internal research tree, not a Workshop package.
expected_patch_dirs=(
    ActivityFixes
    CompatibilityBridges
    NPCCombatExperimental
    NPCFixes
    PatchCore
    RuntimeFixes
    RussianTextFixes
    SafetyFixes
)

tmp_actual_dirs="$(mktemp)"
tmp_expected_dirs="$(mktemp)"
trap 'rm -f "$tmp_actual_dirs" "$tmp_expected_dirs"' EXIT

find "$SPLIT" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' \
    | grep -Fvx 'Bandits-LCC-Dev' \
    | sort > "$tmp_actual_dirs"
printf '%s\n' "${expected_patch_dirs[@]}" | sort > "$tmp_expected_dirs"
if ! diff -u "$tmp_expected_dirs" "$tmp_actual_dirs"; then
    error "WorkshopPatches published/staged set must contain exactly the eight supported package directories; Bandits-LCC-Dev is ignored as internal research"
fi

core="$SPLIT/PatchCore/Contents/mods/LaccckaB4220PatchCore/42/media"
runtime="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/media"
npc="$SPLIT/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/media"
experimental="$SPLIT/NPCCombatExperimental/Contents/mods/LaccckaB4220NPCCombatExperimental/42/media"
activity="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media"
bridges="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/media"
safety="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/media"
text42="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media"
text_common="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/common/media"

required_files=(
    "$core/lua/shared/LCC/Guard.lua"
    "$core/lua/shared/LCC/CoreGuard.lua"

    "$runtime/lua/client/ISUI/ISCharacterScreen.lua"
    "$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
    "$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
    "$runtime/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
    "$runtime/lua/shared/LCC/Guard.lua"
    "$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

    "$npc/lua/client/BanditUpdate.lua"
    "$npc/lua/client/zz_LCC_BanditClothingRestore.lua"
    "$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua"
    "$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua"
    "$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua"
    "$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua"
    "$npc/lua/server/zz_LCC_BanditServerClothingRestore.lua"
    "$npc/lua/server/zzz_LCC_BanditServerClothingSnapshotFallback.lua"
    "$npc/lua/shared/LCC/Guard.lua"
    "$npc/lua/shared/ZombieActions/ZAShoot.lua"

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
)

for path in "${required_files[@]}"; do
    require_file "$path" || true
done

# PatchCore and optional Guard bootstrap contract.
core_guard="$core/lua/shared/LCC/Guard.lua"
core_entry="$core/lua/shared/LCC/CoreGuard.lua"
if [[ -f "$core_guard" && -f "$core_entry" ]] && ! cmp -s "$core_guard" "$core_entry"; then
    error "PatchCore CoreGuard.lua and Guard.lua must remain byte-for-byte equivalent"
fi
for marker in \
    'function Guard.safeRequire' \
    'function Guard.protect' \
    'function Guard.install' \
    'function Guard.wrapBefore' \
    'Guard.__initialized = true'; do
    require_marker "$core_guard" "$marker" "PatchCore Guard lost contract marker: $marker"
done

runtime_guard="$runtime/lua/shared/LCC/Guard.lua"
npc_guard="$npc/lua/shared/LCC/Guard.lua"
experimental_guard="$experimental/lua/shared/LCC/Guard.lua"
activity_guard="$activity/lua/shared/LCC/Guard.lua"
bridges_guard="$bridges/lua/shared/LCC/Guard.lua"
safety_guard="$safety/lua/shared/LCC/Guard.lua"
functional_guards=(
    "$runtime_guard"
    "$npc_guard"
    "$experimental_guard"
    "$activity_guard"
    "$bridges_guard"
    "$safety_guard"
)
for guard in "${functional_guards[@]}"; do
    require_marker "$guard" 'pcall(require, "LCC/CoreGuard")' "Guard bootstrap does not prefer PatchCore: ${guard#$ROOT/}"
    require_marker "$guard" 'CoreGuard.MODE = "GUARDED"' "Guard bootstrap lost GUARDED mode: ${guard#$ROOT/}"
    require_marker "$guard" 'Guard.MODE = "DEGRADED"' "Guard bootstrap lost DEGRADED fallback: ${guard#$ROOT/}"
    require_marker "$guard" 'Correct operation is not guaranteed' "Guard bootstrap lost degraded warning: ${guard#$ROOT/}"
done
for guard in "$npc_guard" "$experimental_guard" "$activity_guard" "$bridges_guard" "$safety_guard"; do
    if [[ -f "$runtime_guard" && -f "$guard" ]] && ! cmp -s "$runtime_guard" "$guard"; then
        error "functional Guard bootstraps must remain identical: ${guard#$ROOT/}"
    fi
done

# RuntimeFixes: low-level source-clean Bandits contracts.
runtime_character="$runtime/lua/client/ISUI/ISCharacterScreen.lua"
runtime_cache="$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
runtime_dedicated="$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
runtime_empty="$runtime/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
runtime_farming="$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"
for forbidden in \
    "$runtime/lua/client/BanditZombie.lua" \
    "$runtime/lua/client/BanditUpdate.lua" \
    "$runtime/lua/server/BanditServerWanderers.lua" \
    "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua" \
    "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua"; do
    [[ ! -e "$forbidden" ]] || error "RuntimeFixes must not bundle upstream Bandits source: ${forbidden#$ROOT/}"
done
require_marker "$runtime_character" 'Guard.safeRequire(FEATURE, "XpSystem/ISUI/ISCharacterScreen")' "RuntimeFixes character-screen shim lost B42 target path"
require_marker "$runtime_empty" 'BanditCustom.ClanGetAll = function' "RuntimeFixes empty-server guard lost ClanGetAll wrapper"
require_marker "$runtime_empty" 'players:size() == 0' "RuntimeFixes empty-server guard lost zero-player condition"
require_marker "$runtime_empty" 'return originalClanGetAll(...)' "RuntimeFixes empty-server guard must preserve normal ClanGetAll behavior"
require_marker "$runtime_cache" 'BanditCompatibility.IsReanimatedForGrappleOnly = function' "RuntimeFixes cache guard lost BanditUpdate early-return seam"
require_marker "$runtime_cache" 'not getSquareSafe(zombie)' "RuntimeFixes cache guard lost squareless predicate"
require_marker "$runtime_cache" 'Events.OnZombieUpdate.Add' "RuntimeFixes cache guard lost post-update cleanup"
require_marker "$runtime_cache" 'Events.EveryOneMinute.Add' "RuntimeFixes cache guard lost post-flush sweep"
require_marker "$runtime_farming" 'return original(...)' "RuntimeFixes farming wrappers must preserve original callbacks"
require_marker "$runtime_farming" 'shouldSkipWaterComplete' "RuntimeFixes farming guard must finish invalid water tasks cleanly"
require_marker "$runtime_farming" 'CFarmingSystem.instance' "RuntimeFixes farming guard lost B42 farming availability check"
require_marker "$runtime_dedicated" 'BanditZombie.GetInstanceById = lookupZombie' "RuntimeFixes dedicated guard must install lookup contract"
require_marker "$runtime_dedicated" 'BanditServerZombie.Cache' "RuntimeFixes dedicated lookup lost optional native server-cache path"
require_marker "$runtime_dedicated" 'Guard.wrapBefore(FEATURE, Bandit, "ApplyVisuals", registerZombie)' "RuntimeFixes dedicated lookup lost Bandit registration hook"
require_marker "$runtime_dedicated" 'Guard.wrapBefore(FEATURE, Bandit, "UpdateItemsToSpawnAtDeath", registerZombie)' "RuntimeFixes dedicated lookup lost death-item registration hook"
require_marker "$runtime_dedicated" 'Events.EveryOneMinute.Add(pruneRegistry)' "RuntimeFixes dedicated registry lost stale-entry pruning"
forbid_marker "$runtime_dedicated" 'getZombieList()' "RuntimeFixes dedicated lookup must not scan the complete server zombie list"

# NPCFixes: stable source-clean combat/death/corpse behavior.
npc_update="$npc/lua/client/BanditUpdate.lua"
npc_shoot="$npc/lua/shared/ZombieActions/ZAShoot.lua"
npc_live_clothes="$npc/lua/client/zz_LCC_BanditClothingRestore.lua"
npc_relation="$npc/lua/client/zzzz_LCC_BanditRelationshipSuppression.lua"
npc_fake_late="$npc/lua/client/zzzzzzz_LCC_BanditFakeHitPfbCleanup.lua"
npc_fake_now="$npc/lua/client/zzzzzzzz_LCC_BanditFakeHitImmediateCleanup.lua"
npc_die="$npc/lua/client/zzzzzzzz_LCC_BanditTerminalDiePump.lua"
npc_server_clothes="$npc/lua/server/zz_LCC_BanditServerClothingRestore.lua"
npc_server_fallback="$npc/lua/server/zzz_LCC_BanditServerClothingSnapshotFallback.lua"

for shim in "$npc_update" "$npc_shoot"; do
    require_marker "$shim" 'getModFileReader' "NPCFixes source shim must read installed upstream source: ${shim#$ROOT/}"
    require_marker "$shim" 'loadstring' "NPCFixes source shim lost runtime compilation: ${shim#$ROOT/}"
    require_marker "$shim" 'replacePlainOnce' "NPCFixes source shim lost exact fingerprint replacement: ${shim#$ROOT/}"
    require_marker "$shim" 'MOD_ID = "Bandits2"' "NPCFixes source shim lost explicit Bandits2 source selection: ${shim#$ROOT/}"
    require_marker "$shim" 'BYPASS_FINGERPRINT' "NPCFixes source shim must fail open on upstream fingerprint drift: ${shim#$ROOT/}"
    require_marker "$shim" 'bundledUpstream=false' "NPCFixes source shim must report source-clean execution: ${shim#$ROOT/}"
done
max_size "$npc_update" 20000 "NPCFixes BanditUpdate.lua must remain a small transformer, not an upstream source copy"
max_size "$npc_shoot" 12000 "NPCFixes ZAShoot.lua must remain a small transformer, not an upstream source copy"
require_marker "$npc_update" 'source-clean-coordinate-pursuit-v1' "NPCFixes pursuit shim lost validated marker"
require_marker "$npc_update" 'LCC_PURSUIT_ALIGN_DIST2 = 0.5625' "NPCFixes pursuit shim lost 0.75-tile throttle"
require_marker "$npc_update" 'LCC_PURSUIT_IDLE_RETRY_MS = 750' "NPCFixes pursuit shim lost bounded idle retry"
require_marker "$npc_shoot" 'source-clean-gunshot-coordinate-alert-v1' "NPCFixes gunshot shim lost validated marker"
require_marker "$npc_shoot" 'zombie:pathToLocationF(sx, sy, sz)' "NPCFixes gunshot shim lost coordinate-only alert"
require_marker "$npc_relation" 'character-relation-suppression-v6' "NPCFixes relationship suppression lost validated marker"
require_marker "$npc_fake_late" 'fake-hit-relation-cleanup-v3' "NPCFixes late fake-hit cleanup lost validated marker"
require_marker "$npc_fake_now" 'fake-hit-immediate-cleanup-v1' "NPCFixes immediate fake-hit cleanup lost validated marker"
require_marker "$npc_die" 'terminal-die-onground-pump-v1' "NPCFixes terminal Die pump lost validated marker"
require_marker "$npc_live_clothes" 'real-worn-reconnect-v2' "NPCFixes live clothing repair lost validated marker"
require_marker "$npc_server_clothes" 'server-authoritative-death-worn-v2' "NPCFixes server clothing repair lost validated marker"
require_marker "$npc_server_fallback" 'server-death-worn-remove-snapshot-v2' "NPCFixes clothing race fallback lost validated marker"

for forbidden in \
    "$npc/lua/client/BanditZombie.lua" \
    "$npc/lua/server/BanditServerWanderers.lua" \
    "$npc/lua/client/zzzzzz_LCC_BanditPursuitStallTrace.lua" \
    "$npc/lua/client/zzzzz_LCC_BanditPfbLateSweep.lua" \
    "$npc/lua/client/zz_LCC_BanditCloseRangeBiteTrace.lua" \
    "$npc/lua/client/zzz_LCC_BanditBiteOutcomeTrace.lua"; do
    [[ ! -e "$forbidden" ]] || error "NPCFixes must not contain upstream source or experimental diagnostics: ${forbidden#$ROOT/}"
done

# NPCCombatExperimental: diagnostics/admin only; no production target disconnect.
experimental_admin="$experimental/lua/client/zzz_LCC_BanditsAdminSpawnMenu.lua"
experimental_diag="$experimental/lua/client/zzz_LCC_BanditsTargetDiagnostics.lua"
experimental_spawn="$experimental/lua/server/zzz_LCC_BanditsTestSpawnBridge.lua"
experimental_death="$experimental/lua/shared/zzz_LCC_BanditsDeathLootDiagnostics.lua"
experimental_old_guard="$experimental/lua/client/zzz_LCC_BanditsAttackStateGuard.lua"
[[ ! -e "$experimental_old_guard" ]] || error "NPCCombatExperimental must not retain the mutating target-disconnect guard after promotion to NPCFixes"
for forbidden in \
    "$experimental/lua/client/BanditZombie.lua" \
    "$experimental/lua/client/BanditUpdate.lua" \
    "$experimental/lua/server/BanditServerWanderers.lua"; do
    [[ ! -e "$forbidden" ]] || error "NPCCombatExperimental must not bundle upstream Bandits source: ${forbidden#$ROOT/}"
done
require_marker "$experimental_admin" 'local MODULE = "LCCBanditsTest"' "NPCCombatExperimental admin spawner lost isolated command channel"
require_marker "$experimental_admin" 'hasStaffAccess' "NPCCombatExperimental admin spawner lost access guard"
require_marker "$experimental_spawn" 'module ~= MODULE or command ~= COMMAND' "NPCCombatExperimental server bridge lost isolated routing"
require_marker "$experimental_spawn" 'BanditServer.Spawner.Clan' "NPCCombatExperimental server bridge must preserve upstream spawn authority"
require_marker "$experimental_diag" '[LCC][BanditsDiag][SUMMARY]' "NPCCombatExperimental target diagnostics lost summary logging"
require_marker "$experimental_diag" 'DANGER_ATTACK_STATE' "NPCCombatExperimental target diagnostics lost AttackState observation"
require_marker "$experimental_death" 'Events.OnZombieDead.Add' "NPCCombatExperimental death diagnostics lost death observation"
require_marker "$experimental_death" 'Events.OnDeadBodySpawn.Add' "NPCCombatExperimental death diagnostics lost corpse observation"
forbid_marker "$experimental_death" 'addItemToSpawnAtDeath(' "NPCCombatExperimental death diagnostics must remain observe-only"
forbid_marker "$experimental_death" 'inventory:AddItem(' "NPCCombatExperimental death diagnostics must not add inventory items"

# ActivityFixes.
activity_bath="$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
activity_yoga="$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
activity_skills="$activity/lua/client/zzz_LCC_SkillDescriptions.lua"
activity_bathtub_shim="$activity/lua/shared/Hygiene/BathTubFunctions.lua"
activity_shower_shim="$activity/lua/shared/Hygiene/ShowerFunctions.lua"
activity_perks="$activity/perks.txt"
require_marker "$activity_bath" 'BathTubFunctions.walkToFront' "ActivityFixes bathtub hook lost Lifestyle seam"
require_marker "$activity_bath" 'fixtures_bathroom_01_25' "ActivityFixes bathtub hook lost west-entry fixture handling"
require_marker "$activity_yoga" 'HiddenSkills.getSkill' "ActivityFixes Yoga UI lost HiddenSkills authority"
require_marker "$activity_yoga" 'ISSkillProgressBar.new = function' "ActivityFixes Yoga UI lost progress-bar proxy"
require_marker "$activity_skills" 'ISSkillProgressBar.updateTooltip = function' "ActivityFixes skill-description repair lost tooltip wrapper"
require_marker "$activity_bathtub_shim" 'BathTubFunctions.DoAction = BathTubFunctions.DoAction or function() end' "ActivityFixes bathtub shim lost DoAction fallback"
require_marker "$activity_shower_shim" 'ShowerFunctions.DoAction = ShowerFunctions.DoAction or function() end' "ActivityFixes shower shim lost DoAction fallback"
require_marker "$activity_perks" 'perk Yoga' "ActivityFixes perks.txt must declare Yoga"
require_marker "$activity_perks" 'parent = Lifestyle' "ActivityFixes Yoga proxy must remain under Lifestyle"
for skill in Art Cleaning Dancing Meditation Music; do
    if [[ -f "$activity_perks" ]] && grep -Eq "^[[:space:]]*perk[[:space:]]+$skill([[:space:]]|$)" "$activity_perks"; then
        error "ActivityFixes perks.txt must not redeclare upstream Lifestyle perk: $skill"
    fi
done

# CompatibilityBridges.
bridge_vehicle="$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
bridge_place3d="$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
bridge_tuning="$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
bridge_pzk="$bridges/lua/server/utils/pzkZonesFunction.lua"
bridge_body="$bridges/lua/shared/BodyLocations.lua"
bridge_timed="$bridges/lua/shared/ISBaseTimedAction.lua"
bridge_svu="$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
bridge_callbacks="$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"
require_marker "$bridge_vehicle" 'Guard.safeRequire(FEATURE, "Vehicles/ISUI/ISVehiclePartMenu")' "CompatibilityBridges vehicle shim lost B42 target"
require_marker "$bridge_place3d" 'ISPlace3DItemCursor.__LCCWeaponPartRenderFix' "CompatibilityBridges 3D-item cursor fix lost install marker"
for path in "$bridge_tuning" "$bridge_pzk" "$bridge_body" "$bridge_timed"; do
    require_marker "$path" 'LCC/Guard' "CompatibilityBridges guarded redirect lost Guard dependency: ${path#$ROOT/}"
done
require_marker "$bridge_svu" 'return require "OtherModsSupport/SVU3_PZKVLCCars_Stuffs"' "CompatibilityBridges SVU3/PZK redirect lost target"
require_marker "$bridge_callbacks" 'ItemCodeOnCreate.onCreateRecipeMagazine' "CompatibilityBridges legacy item callback shim lost B42 target"

# SafetyFixes.
safety_aegis="$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
safety_chimera="$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"
require_marker "$safety_aegis" 'ISInventoryTransferAction.isValid' "SafetyFixes Aegis guard lost transfer validity seam"
require_marker "$safety_aegis" 'not self.item or not self.srcContainer or not self.destContainer' "SafetyFixes Aegis guard lost nil-container precheck"
require_marker "$safety_chimera" 'Guard.install' "SafetyFixes Chimera guard lost guarded install contract"

# RussianTextFixes: standalone RU-only package.
ru42="$text42/lua/shared/Translate/RU"
ru_common="$text_common/lua/shared/Translate/RU"
[[ -d "$ru42" ]] || error "RussianTextFixes 42 RU tree is missing"
[[ -d "$ru_common" ]] || error "RussianTextFixes common RU tree is missing"
if [[ -d "$text42/lua/shared/Translate" ]] && find "$text42/lua/shared/Translate" -mindepth 1 -maxdepth 1 -type d ! -name RU -print -quit | grep -q .; then
    error "RussianTextFixes 42 translation layer must contain RU only"
fi
if [[ -d "$text_common/lua/shared/Translate" ]] && find "$text_common/lua/shared/Translate" -mindepth 1 -maxdepth 1 -type d ! -name RU -print -quit | grep -q .; then
    error "RussianTextFixes common translation layer must contain RU only"
fi
if [[ -d "$ru42" ]]; then
    count42="$(find "$ru42" -type f | wc -l)"
    [[ "$count42" -eq 16 ]] || error "RussianTextFixes 42 RU tree expected 16 files, found $count42"
fi
if [[ -d "$ru_common" ]]; then
    count_common="$(find "$ru_common" -type f | wc -l)"
    [[ "$count_common" -eq 4 ]] || error "RussianTextFixes common RU tree expected 4 files, found $count_common"
fi
for json_root in "$ru42" "$ru_common"; do
    [[ -d "$json_root" ]] || continue
    while IFS= read -r -d '' json; do
        python3 -m json.tool "$json" >/dev/null 2>&1 || error "invalid JSON translation file: ${json#$ROOT/}"
    done < <(find "$json_root" -type f -name '*.json' -print0)
done

# Workshop/mod.info metadata.
declare -A expected_ids=(
    [PatchCore]="LaccckaB4220PatchCore"
    [RuntimeFixes]="LaccckaB4220RuntimeFixes"
    [NPCFixes]="LaccckaB4220NPCFixes"
    [NPCCombatExperimental]="LaccckaB4220NPCCombatExperimental"
    [ActivityFixes]="LaccckaB4220ActivityFixes"
    [CompatibilityBridges]="LaccckaB4220CompatBridges"
    [SafetyFixes]="LaccckaB4220SafetyFixes"
    [RussianTextFixes]="LaccckaB4220RussianText"
)
declare -A expected_workshop_ids=(
    [PatchCore]="3786175901"
    [RuntimeFixes]="3786175979"
    [NPCFixes]="0"
    [NPCCombatExperimental]="3786817782"
    [ActivityFixes]="3786175725"
    [CompatibilityBridges]="3786175808"
    [SafetyFixes]="3786176221"
    [RussianTextFixes]="3786176120"
)

seen_mod_ids=""
seen_workshop_ids=""
for folder in "${expected_patch_dirs[@]}"; do
    id="${expected_ids[$folder]}"
    workshop_id="${expected_workshop_ids[$folder]}"
    workshop="$SPLIT/$folder/workshop.txt"
    preview="$SPLIT/$folder/preview.png"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"

    require_file "$workshop" || true
    require_file "$modinfo" || true
    if [[ "$workshop_id" != "0" ]]; then
        require_file "$preview" || true
    fi

    if [[ -f "$modinfo" ]]; then
        grep -Fxq "id=$id" "$modinfo" || error "$folder: wrong mod ID"
        grep -Fxq 'versionMin=42.20.0' "$modinfo" || error "$folder: versionMin must stay on 42.20.0"
    fi
    if [[ -f "$workshop" ]]; then
        grep -Fxq "id=$workshop_id" "$workshop" || error "$folder: wrong Workshop ID"
        grep -Fqi 'Do not use' "$workshop" || error "$folder: Workshop warning is missing"
        grep -Eqi 'original mod(s| Lua files| files)?.*not included|original mods are not included' "$workshop" || error "$folder: no-bundled-mods disclaimer is missing"
    fi

    if [[ "$folder" == "NPCFixes" ]]; then
        grep -Fxq 'name=Lacccka B42 NPC Fixes' "$modinfo" || error "$folder: wrong RC public mod name"
        grep -Fxq 'modversion=0.9.0' "$modinfo" || error "$folder: RC version must remain 0.9.0 until source-clean regression passes"
        grep -Fxq 'title=Lacccka B42 NPC Fixes' "$workshop" || error "$folder: wrong RC Workshop title"
        grep -Fxq 'visibility=private' "$workshop" || error "$folder: RC Workshop item must remain private while id=0"
    fi
    if [[ "$folder" == "NPCCombatExperimental" ]]; then
        grep -Fxq 'name=Lacccka B42 NPC Combat Experimental' "$modinfo" || error "$folder: public mod name must remain neutral"
        grep -Fxq 'modversion=0.2.0' "$modinfo" || error "$folder: diagnostics-only package version must be 0.2.0"
        grep -Fxq 'title=Lacccka B42 NPC Combat Experimental' "$workshop" || error "$folder: Workshop title must remain neutral"
        grep -Fxq 'visibility=public' "$workshop" || error "$folder: published Workshop item must remain public"
    fi

    grep -Fqx "$id" <<<"$seen_mod_ids" && error "$folder: duplicate mod ID $id"
    seen_mod_ids+="$id"$'\n'
    if [[ "$workshop_id" != "0" ]]; then
        grep -Fqx "$workshop_id" <<<"$seen_workshop_ids" && error "$folder: duplicate Workshop ID $workshop_id"
        seen_workshop_ids+="$workshop_id"$'\n'
    fi
done

for folder in RuntimeFixes NPCFixes NPCCombatExperimental ActivityFixes CompatibilityBridges SafetyFixes; do
    id="${expected_ids[$folder]}"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"
    [[ -f "$modinfo" ]] || continue
    grep -Eq '^require=.*LaccckaB4220PatchCore' "$modinfo" && error "$folder: PatchCore must remain a soft dependency, not require="
    grep -Fq '\LaccckaB4220PatchCore' "$modinfo" || error "$folder: PatchCore soft load-order dependency missing"
    grep -Fqi 'strongly recommended' "$modinfo" || error "$folder: optional PatchCore warning missing from mod.info"
done

runtime_modinfo="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/mod.info"
npc_modinfo="$SPLIT/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/mod.info"
experimental_modinfo="$SPLIT/NPCCombatExperimental/Contents/mods/LaccckaB4220NPCCombatExperimental/42/mod.info"
activity_modinfo="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/mod.info"
bridges_modinfo="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/mod.info"
safety_modinfo="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/mod.info"
text_modinfo="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/mod.info"
require_marker "$runtime_modinfo" '\Bandits2' "RuntimeFixes loadafter lost Bandits2"
require_marker "$npc_modinfo" '\Bandits2' "NPCFixes loadafter lost Bandits2"
require_marker "$npc_modinfo" '\LaccckaB4220RuntimeFixes' "NPCFixes must load after RuntimeFixes when both are enabled"
require_marker "$experimental_modinfo" '\Bandits2' "NPCCombatExperimental loadafter lost NPC integration"
require_marker "$experimental_modinfo" '\LaccckaB4220RuntimeFixes' "NPCCombatExperimental must load after RuntimeFixes"
require_marker "$experimental_modinfo" '\LaccckaB4220NPCFixes' "NPCCombatExperimental must load after stable NPCFixes"
require_marker "$activity_modinfo" '\LifestyleHobbies' "ActivityFixes loadafter lost LifestyleHobbies"
for dep in ModernFirearmsSystem MFS_community_fix PZKCarzoneWorkshop PzkVanillaPlusCarPack StandardizedVehicleUpgrades3Core tsarslib zReFRAMEWORK; do
    require_marker "$bridges_modinfo" "\\$dep" "CompatibilityBridges loadafter lost dependency: $dep"
done
for dep in AP GridInventory Federal_Rangers_Chimera; do
    require_marker "$safety_modinfo" "\\$dep" "SafetyFixes loadafter lost dependency: $dep"
done
require_marker "$text_modinfo" '\Bandits2' "RussianTextFixes loadafter lost Bandits2"
require_marker "$text_modinfo" '\LifestyleHobbies' "RussianTextFixes loadafter lost LifestyleHobbies"
if [[ -f "$text_modinfo" ]] && grep -Fq '\LaccckaB4220PatchCore' "$text_modinfo"; then
    error "RussianTextFixes must remain standalone and must not depend on PatchCore"
fi

if (( fail != 0 )); then
    exit 1
fi

printf 'Grouped Workshop patches audit: OK (8 package directories; NPCFixes=private RC id=0; NPCCombatExperimental=diagnostics-only 0.2.0; Bandits-LCC-Dev excluded as internal)\n'
