#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MONO="$ROOT/LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch"
SPLIT="$ROOT/WorkshopPatches"

fail=0
mapped_sources=()

error() {
    printf 'ERROR: %s\n' "$*" >&2
    fail=1
}

same() {
    local src_rel="$1"
    local dst="$2"
    local src="$MONO/42/media/$src_rel"
    mapped_sources+=("$src_rel")
    if [[ ! -f "$src" ]]; then
        error "missing monolith source: $src_rel"
        return
    fi
    if [[ ! -f "$dst" ]]; then
        error "missing split target for $src_rel: ${dst#$ROOT/}"
        return
    fi
    if ! cmp -s "$src" "$dst"; then
        error "split target differs from monolith: $src_rel -> ${dst#$ROOT/}"
    fi
}

# Mark a historical monolith override as intentionally represented by a
# source-clean wrapper instead of a byte-for-byte copy in the public split.
account_only() {
    local src_rel="$1"
    local src="$MONO/42/media/$src_rel"
    mapped_sources+=("$src_rel")
    if [[ ! -f "$src" ]]; then
        error "missing historical monolith source: $src_rel"
    fi
}

core="$SPLIT/PatchCore/Contents/mods/LaccckaB4220PatchCore/42/media"
runtime="$SPLIT/RuntimeFixes/Contents/mods/LaccckaB4220RuntimeFixes/42/media"
activity="$SPLIT/ActivityFixes/Contents/mods/LaccckaB4220ActivityFixes/42/media"
bridges="$SPLIT/CompatibilityBridges/Contents/mods/LaccckaB4220CompatBridges/42/media"
safety="$SPLIT/SafetyFixes/Contents/mods/LaccckaB4220SafetyFixes/42/media"
text42="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42/media"
text_common="$SPLIT/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/common"

# Shared patch helper.
same "lua/shared/LCC/Guard.lua" "$core/lua/shared/LCC/Guard.lua"

# Runtime fixes: historical full-file overrides are accounted for but MUST NOT
# be copied into the public split. ISCharacterScreen is an LCC path shim.
account_only "lua/client/BanditZombie.lua"
same "lua/client/ISUI/ISCharacterScreen.lua" "$runtime/lua/client/ISUI/ISCharacterScreen.lua"
account_only "lua/server/BanditServerWanderers.lua"
account_only "lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
account_only "lua/shared/ZombieActions/ZAStompPlant.lua"
account_only "lua/shared/ZombieActions/ZAWaterFarm.lua"

runtime_cache="$runtime/lua/client/zzz_LCC_BanditsZombieCacheGuard.lua"
runtime_empty="$runtime/lua/server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
runtime_dedicated="$runtime/lua/server/zzz_LCC_BanditsDedicatedServerGuard.lua"
runtime_farming="$runtime/lua/shared/zzz_LCC_BanditsFarmingGuard.lua"

for path in "$runtime_cache" "$runtime_empty" "$runtime_dedicated" "$runtime_farming"; do
    [[ -f "$path" ]] || error "RuntimeFixes source-clean guard missing: ${path#$ROOT/}"
done

for forbidden in \
    "$runtime/lua/client/BanditZombie.lua" \
    "$runtime/lua/server/BanditServerWanderers.lua" \
    "$runtime/lua/shared/ZombieActions/ZAStompPlant.lua" \
    "$runtime/lua/shared/ZombieActions/ZAWaterFarm.lua" \
    "$runtime/lua/client/BanditUpdate.lua"; do
    [[ ! -e "$forbidden" ]] || error "RuntimeFixes must not bundle upstream Bandits source: ${forbidden#$ROOT/}"
done

if [[ -f "$runtime_empty" ]]; then
    grep -Fq 'BanditCustom.ClanGetAll = function' "$runtime_empty" || error "RuntimeFixes empty-server guard lost ClanGetAll wrapper"
    grep -Fq 'players:size() == 0' "$runtime_empty" || error "RuntimeFixes empty-server guard lost zero-player condition"
    grep -Fq 'return originalClanGetAll(...)' "$runtime_empty" || error "RuntimeFixes empty-server guard must preserve normal ClanGetAll behavior"
fi

if [[ -f "$runtime_cache" ]]; then
    grep -Fq 'BanditCompatibility.IsReanimatedForGrappleOnly = function' "$runtime_cache" || error "RuntimeFixes cache guard lost BanditUpdate early-return seam"
    grep -Fq 'not getSquareSafe(zombie)' "$runtime_cache" || error "RuntimeFixes cache guard lost squareless predicate"
    grep -Fq 'Events.OnZombieUpdate.Add' "$runtime_cache" || error "RuntimeFixes cache guard lost post-update cleanup"
    grep -Fq 'Events.EveryOneMinute.Add' "$runtime_cache" || error "RuntimeFixes cache guard lost post-flush sweep"
fi

if [[ -f "$runtime_farming" ]]; then
    grep -Fq 'return original(...)' "$runtime_farming" || error "RuntimeFixes farming wrappers must preserve original callbacks"
    grep -Fq 'shouldSkipWaterComplete' "$runtime_farming" || error "RuntimeFixes farming guard must finish invalid water tasks cleanly"
    grep -Fq 'CFarmingSystem.instance' "$runtime_farming" || error "RuntimeFixes farming guard lost B42 farming availability check"
fi

if [[ -f "$runtime_dedicated" ]]; then
    grep -Fq 'BanditZombie.GetInstanceById = lookupZombie' "$runtime_dedicated" || error "RuntimeFixes dedicated guard must install real lookup contract"
    grep -Fq 'BanditServerZombie.Cache' "$runtime_dedicated" || error "RuntimeFixes dedicated lookup lost optional native server-cache path"
    grep -Fq 'getZombieList()' "$runtime_dedicated" || error "RuntimeFixes dedicated lookup lost on-demand server fallback"
    grep -Fq 'pcall(getId, zombie)' "$runtime_dedicated" || error "RuntimeFixes dedicated lookup lost stale-IsoZombie protection"
fi

# Activity fixes.
same "lua/client/zzz_LCC_LifestyleBathFix.lua" "$activity/lua/client/zzz_LCC_LifestyleBathFix.lua"
same "lua/client/zzz_LCC_LifestyleYogaProgress.lua" "$activity/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
same "lua/shared/Hygiene/BathTubFunctions.lua" "$activity/lua/shared/Hygiene/BathTubFunctions.lua"
same "lua/shared/Hygiene/ShowerFunctions.lua" "$activity/lua/shared/Hygiene/ShowerFunctions.lua"
same "perks.txt" "$activity/perks.txt"

# Compatibility bridges.
same "lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua" "$bridges/lua/client/Vehicle/ISUI/ISVehiclePartMenu.lua"
same "lua/client/Vehicle/ISVehiclePartMenu.lua" "$bridges/lua/client/Vehicle/ISVehiclePartMenu.lua"
same "lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua" "$bridges/lua/server/BuildingObjects/ISPlace3DItemCursor_Fix.lua"
same "lua/server/Tuning2/ATA2Tuning2.lua" "$bridges/lua/server/Tuning2/ATA2Tuning2.lua"
same "lua/server/utils/pzkZonesFunction.lua" "$bridges/lua/server/utils/pzkZonesFunction.lua"
same "lua/shared/BodyLocations.lua" "$bridges/lua/shared/BodyLocations.lua"
same "lua/shared/ISBaseTimedAction.lua" "$bridges/lua/shared/ISBaseTimedAction.lua"
same "lua/shared/SVU3_PZKVLCCars_Stuffs.lua" "$bridges/lua/shared/SVU3_PZKVLCCars_Stuffs.lua"
same "lua/shared/zzz_LCC_LegacyItemCallbacks.lua" "$bridges/lua/shared/zzz_LCC_LegacyItemCallbacks.lua"

# Defensive/UI fixes.
same "lua/client/zzz_LCC_AegisTransferGuard.lua" "$safety/lua/client/zzz_LCC_AegisTransferGuard.lua"
same "lua/client/zzz_LCC_ChimeraGhillieFix.lua" "$safety/lua/client/zzz_LCC_ChimeraGhillieFix.lua"

# The translation package must remain byte-for-byte equivalent to both monolith layers.
if ! diff -qr "$MONO/42/media/lua/shared/Translate" "$text42/lua/shared/Translate" >/dev/null; then
    error "42 translation tree differs from monolith"
fi
if ! diff -qr "$MONO/common" "$text_common" >/dev/null; then
    error "common translation tree differs from monolith"
fi

# Catch any new non-translation monolith file that has not been assigned to a split item.
tmp_actual="$(mktemp)"
tmp_mapped="$(mktemp)"
trap 'rm -f "$tmp_actual" "$tmp_mapped"' EXIT
find "$MONO/42/media" -type f ! -path '*/lua/shared/Translate/*' \
    -printf '%P\n' | sort > "$tmp_actual"
printf '%s\n' "${mapped_sources[@]}" | sort -u > "$tmp_mapped"
if ! diff -u "$tmp_actual" "$tmp_mapped"; then
    error "non-translation source coverage is incomplete or stale"
fi

# Workshop metadata contracts.
declare -A expected_ids=(
    [PatchCore]="LaccckaB4220PatchCore"
    [RuntimeFixes]="LaccckaB4220RuntimeFixes"
    [ActivityFixes]="LaccckaB4220ActivityFixes"
    [CompatibilityBridges]="LaccckaB4220CompatBridges"
    [SafetyFixes]="LaccckaB4220SafetyFixes"
    [RussianTextFixes]="LaccckaB4220RussianText"
)

seen_ids=""
for folder in "${!expected_ids[@]}"; do
    id="${expected_ids[$folder]}"
    workshop="$SPLIT/$folder/workshop.txt"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"

    [[ -f "$workshop" ]] || { error "$folder: workshop.txt missing"; continue; }
    [[ -f "$modinfo" ]] || { error "$folder: mod.info missing"; continue; }

    grep -Fxq "id=$id" "$modinfo" || error "$folder: wrong mod ID"
    grep -Fxq 'versionMin=42.20.0' "$modinfo" || error "$folder: versionMin must stay on 42.20.0"
    grep -Fqi 'Do not use' "$workshop" || error "$folder: Workshop warning is missing"
    grep -Eqi 'original mod(s| Lua files| files)?.*not included|original mods are not included' "$workshop" || error "$folder: no-bundled-mods disclaimer is missing"
    grep -Fxq 'id=0' "$workshop" || error "$folder: staging Workshop ID must remain 0 until publication"

    if grep -Fqx "$id" <<<"$seen_ids"; then
        error "$folder: duplicate mod ID $id"
    fi
    seen_ids+="$id"$'\n'
done

for folder in RuntimeFixes ActivityFixes CompatibilityBridges SafetyFixes; do
    id="${expected_ids[$folder]}"
    modinfo="$SPLIT/$folder/Contents/mods/$id/42/mod.info"
    grep -Fxq 'require=\LaccckaB4220PatchCore' "$modinfo" || error "$folder: PatchCore dependency missing"
done

if (( fail != 0 )); then
    exit 1
fi

printf 'Workshop split audit: OK\n'
