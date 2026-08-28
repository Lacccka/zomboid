#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
registry_def="$lua_root/shared/LCCQF/Core/LCCQFFactionRegistry.lua"
faction_defs="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
site_registry="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteRegistry.lua"
candidates="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteCandidateIndex.lua"
validator="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteSafetyValidator.lua"
allocator="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "QuestFramework faction site audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in "$constants" "$registry_def" "$faction_defs" "$site_registry" "$candidates" "$validator" "$allocator" "$bootstrap"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_SITE_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "faction site schema constant missing"
require_pattern 'FACTION_SITE_MODDATA_KEY = "LCCQF_FactionSites"' "$constants" "faction site ModData key missing"
require_pattern 'FACTION_SITE_MAX_CANDIDATES' "$constants" "bounded candidate cap missing"
require_pattern 'FACTION_SITE_MAX_ROOMS_PER_PASS' "$constants" "bounded room discovery cap missing"

require_pattern 'validateSiteProfile' "$registry_def" "faction site profile validation missing"
require_pattern 'siteProfile\.' "$registry_def" "site profile field validation missing"
require_pattern 'is not implemented yet' "$registry_def" "unsupported site profile fields are silently accepted"
require_pattern 'siteProfile = \{' "$faction_defs" "authored faction site profile missing"
require_pattern 'preferredZones' "$faction_defs" "authored zone preferences missing"
require_pattern 'minDistanceFromPlayers' "$faction_defs" "authored player distance policy missing"
require_pattern 'minDistanceFromOtherFactionSites' "$faction_defs" "authored faction separation policy missing"
if rg -n 'baseX|baseY|baseZ|fixedX|fixedY|fixedZ' "$faction_defs"; then
    fail "faction content contains fixed base coordinate fields"
fi

require_pattern 'ModData\.getOrCreate\(C\.FACTION_SITE_MODDATA_KEY\)' "$site_registry" "persistent faction site store missing"
require_pattern 'siteIdsByFaction' "$site_registry" "faction-to-multiple-sites persistence missing"
require_pattern 'reservationsByCandidateKey' "$site_registry" "candidate reservation index missing"
require_pattern 'state = "RESERVED"' "$site_registry" "reservation state missing"
require_pattern 'function Sites\.Transition' "$site_registry" "site state transition API missing"
require_pattern 'DistanceToNearestOtherFactionSite' "$site_registry" "site separation query missing"

require_pattern 'getCell and getCell\(\)' "$candidates" "loaded-world-only discovery missing"
require_pattern 'getRoomList' "$candidates" "loaded room discovery missing"
require_pattern 'FACTION_SITE_MAX_ROOMS_PER_PASS' "$candidates" "room discovery is not bounded"
require_pattern 'FACTION_SITE_MAX_CANDIDATES' "$candidates" "candidate index is not bounded"
require_pattern 'buildingFingerprint' "$candidates" "reconstructible building fingerprint missing"
require_pattern 'getMetaGrid' "$candidates" "meta-zone scoring context missing"
require_pattern 'preferredZones' "$candidates" "preferred zone scoring missing"
require_pattern 'avoidedZones' "$candidates" "avoided zone scoring missing"
require_pattern 'rejectionHistory' "$candidates" "repeated bad candidate penalty missing"

require_pattern 'SafeHouse\.isSafeHouse' "$validator" "SafeHouse exclusion missing"
require_pattern 'getOnlinePlayers' "$validator" "active player proximity validation missing"
require_pattern 'getFreeSquare' "$validator" "free room square validation missing"
require_pattern 'building fingerprint changed' "$validator" "live building fingerprint reconciliation missing"
require_pattern 'candidate overlaps a player SafeHouse' "$validator" "whole-building SafeHouse rejection missing"

require_pattern 'dryRun=true materialization=false' "$allocator" "allocator is not explicitly dry-run"
require_pattern 'Candidates\.DiscoverLoadedBuildings' "$allocator" "allocator does not use bounded loaded candidate discovery"
require_pattern 'Validator\.Validate' "$allocator" "allocator skips non-destructive safety validation"
require_pattern 'Sites\.ReserveCandidate' "$allocator" "allocator does not persist reservation"
require_pattern 'Events\.EveryOneMinute' "$allocator" "allocator is not globally throttled"
require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSiteAllocator' "$bootstrap" "site allocator not bootstrapped"

faction_world_files=("$site_registry" "$candidates" "$validator" "$allocator")
if rg -n 'BanditServer|BanditCustom|BanditBrain|Bandits2|Bandit\.Spawner' "${faction_world_files[@]}"; then
    fail "faction world allocation layer leaks Bandits runtime dependency"
fi
if rg -n 'sendClientCommand|sendServerCommand|OnClientCommand' "${faction_world_files[@]}"; then
    fail "dry-run faction allocator exposes unnecessary client/network authority"
fi
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies|setSquare|AddZombie' "${faction_world_files[@]}"; then
    fail "faction site allocator contains destructive world mutation"
fi
if rg -n 'loadstring|loadstream' "${faction_world_files[@]}" "$registry_def" "$faction_defs"; then
    fail "Build 42.20.4 removed dynamic code execution API is present"
fi
if rg -n 'TEST_FACTION_ID|checkpoint_survivors|CheckpointSurvivors' "${faction_world_files[@]}"; then
    fail "reusable faction site core contains authored test faction special-case"
fi

if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$constants]]))"
    lua -e "assert(loadfile([[$registry_def]]))"
    lua -e "assert(loadfile([[$faction_defs]]))"
    lua -e "assert(loadfile([[$site_registry]]))"
    lua -e "assert(loadfile([[$candidates]]))"
    lua -e "assert(loadfile([[$validator]]))"
    lua -e "assert(loadfile([[$allocator]]))"
    lua -e "assert(loadfile([[$bootstrap]]))"
fi

echo "QuestFramework faction site audit: PASS (persistent dry-run sites + bounded autonomous candidate scoring + non-destructive reservation)"
