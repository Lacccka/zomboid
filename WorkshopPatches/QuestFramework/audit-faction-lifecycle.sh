#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
registry_def="$lua_root/shared/LCCQF/Core/LCCQFFactionRegistry.lua"
faction_defs="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
site_registry="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteRegistry.lua"
population="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSitePopulation.lua"
safety="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteSafetyValidator.lua"
materializer_registry="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteMaterializerRegistry.lua"
bandits_materializer="$lua_root/server/LCCQF/Runtime/LCCQFBanditsFactionSiteMaterializer.lua"
bandits_lifecycle="$lua_root/server/LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle.lua"
guard_program="$lua_root/shared/LCCQF/Runtime/LCCQFBanditsFactionGuardProgram.lua"
lifecycle="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteLifecycleService.lua"
maintenance="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSitePopulationMaintenance.lua"
relocation="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteRelocationService.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"
client_presentation="$lua_root/client/LCCQF/Runtime/LCCQFBanditsClientPresentation.lua"
debug_server="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteDebugServer.lua"

fail() {
    echo "QuestFramework faction lifecycle audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

required_files=(
    "$constants" "$registry_def" "$faction_defs" "$site_registry" "$population" "$safety"
    "$materializer_registry" "$bandits_materializer" "$bandits_lifecycle" "$guard_program"
    "$lifecycle" "$maintenance" "$relocation" "$bootstrap" "$client_presentation" "$debug_server"
)
for required in "${required_files[@]}"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_SITE_RUNTIME_SCAN_MAX_TILES' "$constants" "bounded runtime reconciliation scan cap missing"

require_pattern 'minMaterializationDistanceFromPlayers' "$registry_def" "materialization proximity policy is not validated"
require_pattern 'homeRadius' "$registry_def" "home radius policy is not validated"
require_pattern 'returnRadius' "$registry_def" "return radius policy is not validated"
require_pattern 'guardRadius' "$registry_def" "guard radius policy is not validated"
require_pattern 'replaceDead' "$registry_def" "death replacement policy is not validated"
require_pattern 'replacementDelayHours' "$registry_def" "replacement delay is not validated"
require_pattern 'returnRadius must be >= homeRadius' "$registry_def" "home leash ordering validation missing"

require_pattern 'program = "LCCQFFactionGuard"' "$faction_defs" "checkpoint faction does not use framework guard program"
require_pattern 'minMaterializationDistanceFromPlayers = 24' "$faction_defs" "separate pop-in proximity radius missing"
require_pattern 'replaceDead = true' "$faction_defs" "checkpoint population maintenance intent missing"
require_pattern 'replacementDelayHours = 24' "$faction_defs" "checkpoint replacement delay missing"

require_pattern 'VIRTUALIZED = true' "$population" "logical virtualization state missing"
require_pattern 'function Population\.ListAllMembers' "$population" "population history API missing"
require_pattern 'function Population\.MarkVirtualized' "$population" "virtualization transition API missing"
require_pattern 'function Population\.AppendReplacement' "$population" "dead-member replacement planning missing"
require_pattern 'replacesNpcId' "$population" "replacement lineage missing"
require_pattern 'function Population\.TransferPlan' "$population" "identity-preserving relocation transfer missing"
require_pattern 'CountVirtualized' "$population" "virtualized population count missing"
require_pattern 'member\.state ~= "DEAD"' "$population" "dead identities are not excluded from current population"

require_pattern 'CAPACITY_STATES' "$site_registry" "relocation-aware capacity states missing"
require_pattern 'RELOCATING = \{ ACTIVE = true, ABANDONED = true \}' "$site_registry" "relocation rollback/completion transitions missing"
require_pattern 'function Sites\.FindRelocatingSite' "$site_registry" "relocating site lookup missing"
require_pattern 'function Sites\.BeginRelocation' "$site_registry" "server relocation request API missing"
require_pattern 'relocatesSiteId' "$site_registry" "replacement site linkage missing"
require_pattern 'replacementSiteId' "$site_registry" "old-site replacement linkage missing"

require_pattern 'materializationMinimumDistance' "$safety" "allocation/pop-in proximity policies are not separated"
require_pattern 'minMaterializationDistanceFromPlayers' "$safety" "pop-in proximity policy is not consumed"
require_pattern 'profile\.minDistanceFromPlayers' "$safety" "allocation proximity policy disappeared"

require_pattern 'ZombiePrograms\.LCCQFFactionGuard' "$guard_program" "custom faction guard program missing"
require_pattern 'lccqHomeX' "$guard_program" "guard home anchor missing"
require_pattern 'lccqReturnRadius' "$guard_program" "guard return leash missing"
require_pattern 'BanditUtils\.GetTarget' "$guard_program" "guard threat acquisition missing"
require_pattern 'BanditUtils\.GetMoveTaskTarget' "$guard_program" "guard combat movement missing"
require_pattern 'BanditPrograms\.Idle' "$guard_program" "guard idle behavior missing"
if rg -n 'Looter|GetMasterPlayer|BanditPost\.At|BanditPlayerBase' "$guard_program"; then
    fail "framework faction guard leaks roaming/player-owned Bandits behavior"
fi

require_pattern 'function Adapter\.Reconcile' "$bandits_lifecycle" "Bandits reconciliation adapter missing"
require_pattern 'function Adapter\.Rematerialize' "$bandits_lifecycle" "Bandits rematerialization adapter missing"
require_pattern 'lccqRetired' "$bandits_lifecycle" "duplicate runtime retirement tag missing"
require_pattern 'Population\.MarkVirtualized' "$bandits_lifecycle" "provider unload is not virtualized"
require_pattern 'Population\.MarkDead' "$bandits_lifecycle" "provider death is not persisted logically"
require_pattern 'Events\.OnZombieDead' "$bandits_lifecycle" "physical faction death hook missing"
require_pattern 'BanditClusters unavailable' "$bandits_lifecycle" "provider startup must fail closed before marking members missing"
require_pattern 'FACTION_SITE_RUNTIME_SCAN_MAX_TILES' "$bandits_lifecycle" "runtime fallback scan is not bounded"
require_pattern 'Spawner\.Individual' "$bandits_lifecycle" "provider rematerialization path missing"
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies' "$bandits_lifecycle" "$guard_program"; then
    fail "lifecycle adapter destructively removes world entities"
fi

require_pattern 'Materializers\.Get\(profile\.materializer\)' "$lifecycle" "lifecycle core bypasses provider registry"
require_pattern 'adapter\.Reconcile' "$lifecycle" "lifecycle core does not reconcile provider state"
require_pattern 'adapter\.Rematerialize' "$lifecycle" "lifecycle core does not use provider rematerialization boundary"
require_pattern 'Safety\.ValidateMaterializationSite' "$lifecycle" "rematerialization skips safety gate"
require_pattern '"DORMANT"' "$lifecycle" "virtualized sites never become dormant"
require_pattern '"ACTIVE"' "$lifecycle" "rematerialized sites never reactivate"

require_pattern 'replaceDead ~= true' "$maintenance" "maintenance does not honor replacement policy"
require_pattern 'replacementDelayHours' "$maintenance" "maintenance ignores replacement delay"
require_pattern 'Population\.AppendReplacement' "$maintenance" "maintenance resurrects instead of appending identity"

require_pattern 'function Service\.Request' "$relocation" "server relocation request service missing"
require_pattern 'Population\.TransferPlan' "$relocation" "relocation does not preserve logical population"
require_pattern 'previousRuntimeId' "$relocation" "relocation does not remember previous physical runtime"
require_pattern 'replacement\.state == "ACTIVE"' "$relocation" "old site can be abandoned before replacement activation"

require_pattern 'LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle' "$bootstrap" "Bandits lifecycle extension not bootstrapped"
require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSiteRelocationService' "$bootstrap" "relocation service not bootstrapped"
require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSiteLifecycleService' "$bootstrap" "lifecycle coordinator not bootstrapped"
require_pattern 'LCCQF/FactionWorld/zz_LCCQFFactionSitePopulationMaintenance' "$bootstrap" "population maintenance not bootstrapped"
require_pattern 'LCCQF/Runtime/LCCQFBanditsFactionGuardProgram' "$client_presentation" "client does not load synchronized guard program"

require_pattern 'virtualized = counts\.VIRTUALIZED' "$debug_server" "admin debug omits virtualized population count"
require_pattern 'previousRuntimeId' "$debug_server" "admin debug omits relocation/runtime lineage"
require_pattern 'replacementSiteId' "$debug_server" "admin debug omits relocation destination"

core_files=("$lifecycle" "$maintenance" "$relocation" "$population" "$site_registry" "$safety")
if rg -n 'BanditServer|BanditCustom|BanditBrain|BanditClusters|Bandits2' "${core_files[@]}"; then
    fail "provider-neutral lifecycle core leaks Bandits runtime dependency"
fi
if rg -n 'sendClientCommand|sendServerCommand|OnClientCommand' "$lifecycle" "$maintenance" "$relocation" "$population"; then
    fail "server lifecycle core exposes gameplay client authority"
fi
if rg -n 'loadstring|loadstream' "${required_files[@]}"; then
    fail "removed dynamic code execution API is present"
fi

# Ensure relocation service registers its minute handler before initial materialization.
relocation_line="$(rg -n 'zz_LCCQFFactionSiteRelocationService' "$bootstrap" | head -n1 | cut -d: -f1)"
materialization_line="$(rg -n 'zz_LCCQFFactionSiteMaterializationService' "$bootstrap" | head -n1 | cut -d: -f1)"
[[ "$relocation_line" -lt "$materialization_line" ]] || fail "relocation must bootstrap before materialization"

if command -v lua >/dev/null 2>&1; then
    for lua_file in "${required_files[@]}"; do
        lua -e "assert(loadfile([[$lua_file]]))"
    done
fi

echo "QuestFramework faction lifecycle audit: PASS (reconciliation + virtualization + maintenance + home guard + relocation)"
