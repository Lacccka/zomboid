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
materialization="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteMaterializationService.lua"
bandits_materializer="$lua_root/server/LCCQF/Runtime/LCCQFBanditsFactionSiteMaterializer.lua"
bandits_lifecycle="$lua_root/server/LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle.lua"
guard_program="$lua_root/shared/LCCQF/Runtime/LCCQFBanditsFactionGuardProgram.lua"
lifecycle="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteLifecycleService.lua"
maintenance="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSitePopulationMaintenance.lua"
relocation="$lua_root/server/LCCQF/FactionWorld/zz_LCCQFFactionSiteRelocationService.lua"
bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"
client_presentation="$lua_root/client/LCCQF/Runtime/LCCQFBanditsClientPresentation.lua"
debug_server="$lua_root/server/LCCQF/FactionWorld/LCCQFFactionSiteDebugServer.lua"

fail(){ echo "QuestFramework faction lifecycle audit: FAIL: $1" >&2; exit 1; }
req(){ rg -q "$1" "$2" || fail "$3"; }

files=("$constants" "$registry_def" "$faction_defs" "$site_registry" "$population" "$safety" "$materialization" "$bandits_materializer" "$bandits_lifecycle" "$guard_program" "$lifecycle" "$maintenance" "$relocation" "$bootstrap" "$client_presentation" "$debug_server")
for f in "${files[@]}"; do [[ -f "$f" ]] || fail "missing $f"; done

req 'FACTION_SITE_RUNTIME_SCAN_MAX_TILES' "$constants" 'bounded runtime reconciliation scan cap missing'
req 'minMaterializationDistanceFromPlayers' "$registry_def" 'materialization proximity policy is not validated'
req 'homeRadius' "$registry_def" 'home radius policy is not validated'
req 'returnRadius' "$registry_def" 'return radius policy is not validated'
req 'guardRadius' "$registry_def" 'guard radius policy is not validated'
req 'replaceDead' "$registry_def" 'death replacement policy is not validated'
req 'replacementDelayHours' "$registry_def" 'replacement delay is not validated'
req 'returnRadius must be >= homeRadius' "$registry_def" 'home leash ordering validation missing'

req 'program = "LCCQFFactionGuard"' "$faction_defs" 'checkpoint faction does not use framework guard program'
req 'minMaterializationDistanceFromPlayers = 24' "$faction_defs" 'separate pop-in proximity radius missing'
req 'replaceDead = true' "$faction_defs" 'checkpoint population maintenance intent missing'

req 'VIRTUALIZED = true' "$population" 'logical virtualization state missing'
req 'function Population\.MarkVirtualized' "$population" 'virtualization transition API missing'
req 'function Population\.AppendReplacement' "$population" 'dead-member replacement planning missing'
req 'replacesNpcId' "$population" 'replacement lineage missing'
req 'function Population\.TransferPlan' "$population" 'identity-preserving relocation transfer missing'
req 'CountVirtualized' "$population" 'virtualized population count missing'

req 'CAPACITY_STATES' "$site_registry" 'relocation-aware capacity states missing'
req 'RELOCATING = \{ ACTIVE = true, ABANDONED = true \}' "$site_registry" 'relocation rollback/completion transitions missing'
req 'function Sites\.BeginRelocation' "$site_registry" 'server relocation request API missing'
req 'relocatesSiteId' "$site_registry" 'replacement site linkage missing'

req 'materializationMinimumDistance' "$safety" 'allocation/pop-in proximity policies are not separated'
req 'needsIdentityPreservingMaterialization' "$materialization" 'materialization does not detect prior provider identity'
req 'member\.providerId ~= nil' "$materialization" 'provider identity is ignored during materialization'
req 'member\.previousRuntimeId ~= nil' "$materialization" 'relocation runtime lineage is ignored'
req 'adapter\.Rematerialize' "$materialization" 'identity-preserving provider path is missing'
req 'identity-preserving rematerializer unavailable' "$materialization" 'identity-preserving materialization does not fail closed'

req 'ZombiePrograms\.LCCQFFactionGuard' "$guard_program" 'custom faction resident program missing'
req 'pointFromBrain' "$guard_program" 'coordinate resolver missing'
req '"lccqHome"' "$guard_program" 'home coordinate prefix missing'
req 'lccqReturnRadius' "$guard_program" 'home return leash missing'
req 'BanditUtils\.GetTarget' "$guard_program" 'threat acquisition missing'
req 'BanditUtils\.GetMoveTaskTarget' "$guard_program" 'combat movement missing'
req 'BanditPrograms\.Idle' "$guard_program" 'idle behavior missing'
if rg -n 'GetMasterPlayer|BanditPost\.At|BanditPlayerBase' "$guard_program"; then fail 'faction resident program leaks player-owned Bandits behavior'; fi

req 'function Adapter\.Reconcile' "$bandits_lifecycle" 'Bandits reconciliation adapter missing'
req 'function Adapter\.Rematerialize' "$bandits_lifecycle" 'Bandits rematerialization adapter missing'
req 'lccqRetired' "$bandits_lifecycle" 'duplicate runtime retirement tag missing'
req 'Population\.MarkVirtualized' "$bandits_lifecycle" 'provider unload is not virtualized'
req 'Population\.MarkDead' "$bandits_lifecycle" 'provider death is not persisted logically'
req 'Events\.OnZombieDead' "$bandits_lifecycle" 'physical faction death hook missing'
req 'FACTION_SITE_RUNTIME_SCAN_MAX_TILES' "$bandits_lifecycle" 'runtime scan is not bounded'
req 'Spawner\.Individual' "$bandits_lifecycle" 'provider rematerialization path missing'
if rg -n 'removeFromWorld|removeFromSquare|RemoveZombie|clearZombies|ClearZombies' "$bandits_lifecycle" "$guard_program"; then fail 'lifecycle destructively removes world entities'; fi

req 'Materializers\.Get\(profile\.materializer\)' "$lifecycle" 'lifecycle core bypasses provider registry'
req 'adapter\.Reconcile' "$lifecycle" 'lifecycle core does not reconcile provider state'
req 'adapter\.Rematerialize' "$lifecycle" 'lifecycle core does not use provider rematerialization boundary'
req '"DORMANT"' "$lifecycle" 'virtualized sites never become dormant'
req 'Population\.AppendReplacement' "$maintenance" 'maintenance resurrects instead of appending identity'
req 'Population\.TransferPlan' "$relocation" 'relocation does not preserve logical population'
req 'previousRuntimeId' "$relocation" 'relocation does not remember previous physical runtime'
req 'replacement\.state == "ACTIVE"' "$relocation" 'old site can be abandoned before replacement activation'

req 'LCCQF/Runtime/LCCQFBanditsFactionSiteLifecycle' "$bootstrap" 'Bandits lifecycle extension not bootstrapped'
req 'zz_LCCQFFactionSiteRelocationService' "$bootstrap" 'relocation service not bootstrapped'
req 'zz_LCCQFFactionSiteLifecycleService' "$bootstrap" 'lifecycle coordinator not bootstrapped'
req 'zz_LCCQFFactionSitePopulationMaintenance' "$bootstrap" 'population maintenance not bootstrapped'
req 'LCCQF/Runtime/LCCQFBanditsFactionGuardProgram' "$client_presentation" 'client does not load synchronized faction resident program'
req 'virtualized = counts\.VIRTUALIZED' "$debug_server" 'admin debug omits virtualized population count'
req 'previousRuntimeId' "$debug_server" 'admin debug omits runtime lineage'

core=("$materialization" "$lifecycle" "$maintenance" "$relocation" "$population" "$site_registry" "$safety")
if rg -n 'BanditServer|BanditCustom|BanditBrain|BanditClusters|Bandits2' "${core[@]}"; then fail 'provider-neutral lifecycle core leaks Bandits runtime dependency'; fi
if rg -n 'sendClientCommand|sendServerCommand|OnClientCommand' "$materialization" "$lifecycle" "$maintenance" "$relocation" "$population"; then fail 'server lifecycle core exposes gameplay client authority'; fi

relocation_line="$(rg -n 'zz_LCCQFFactionSiteRelocationService' "$bootstrap" | head -n1 | cut -d: -f1)"
materialization_line="$(rg -n 'zz_LCCQFFactionSiteMaterializationService' "$bootstrap" | head -n1 | cut -d: -f1)"
[[ "$relocation_line" -lt "$materialization_line" ]] || fail 'relocation must bootstrap before materialization'

if command -v lua >/dev/null 2>&1; then for f in "${files[@]}"; do lua -e "assert(loadfile([[$f]]))"; done; fi

echo 'QuestFramework faction lifecycle audit: PASS (reconciliation + virtualization + identity-preserving rematerialization + maintenance + duty-aware home behavior + relocation)'
