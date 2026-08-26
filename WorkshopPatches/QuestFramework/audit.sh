#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$project_root/../.." && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"
npc_fixes_bandit_update="$repo_root/WorkshopPatches/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/media/lua/client/BanditUpdate.lua"

client_interaction="$lua_root/client/LCCQF/LCCQFInteractionClient.lua"
client_quest_state="$lua_root/client/LCCQF/Quest/LCCQFQuestClientState.lua"
client_character_lifecycle="$lua_root/client/LCCQF/Quest/LCCQFCharacterProjectionLifecycle.lua"
client_quest_marker="$lua_root/client/LCCQF/Quest/LCCQFQuestMarkerService.lua"
client_hub="$lua_root/client/LCCQF/UI/LCCQFHub.lua"
client_hub_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
legacy_client_bandits="$lua_root/client/LCCQF/Runtime/LCCQFBanditsRuntime.lua"

server_interaction="$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
server_bandits_wrapper="$lua_root/server/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_bandits="$lua_root/server/LCCQF/Runtime/LCCQFBanditsServerRuntime.lua"
server_bandits_ownership="$lua_root/server/LCCQF/Runtime/LCCQFBanditsEntityOwnership.lua"
server_quest_giver_protection="$lua_root/server/LCCQF/Runtime/zz_LCCQFBanditsQuestGiverProtection.lua"
shared_quest_giver_program="$lua_root/shared/LCCQF/Runtime/LCCQFBanditsQuestGiverProgram.lua"
shared_runtime="$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"

dialogue_session="$lua_root/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
dialogue_content="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
quest_registry="$lua_root/server/LCCQF/Quest/LCCQFQuestRegistry.lua"
quest_instance="$lua_root/server/LCCQF/Quest/LCCQFQuestInstance.lua"
quest_service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
quest_event_bridge="$lua_root/server/LCCQF/Quest/zz_LCCQFQuestEventBridge.lua"
character_identity="$lua_root/server/LCCQF/Persistence/LCCQFCharacterIdentity.lua"
quest_persistence="$lua_root/server/LCCQF/Persistence/LCCQFQuestPersistence.lua"
objective_reach="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveReachArea.lua"
objective_talk="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveTalkToNPC.lua"
objective_item_utils="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveItemUtils.lua"
objective_fetch="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveFetch.lua"
objective_deliver="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveDeliver.lua"
objective_kill="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveKill.lua"
objective_clear="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveClearArea.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in \
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua" \
    "$shared_runtime" \
    "$shared_quest_giver_program" \
    "$client_interaction" \
    "$client_quest_state" \
    "$client_character_lifecycle" \
    "$client_quest_marker" \
    "$client_hub" \
    "$client_hub_bootstrap" \
    "$server_interaction" \
    "$server_bandits_wrapper" \
    "$server_bandits" \
    "$server_bandits_ownership" \
    "$server_quest_giver_protection" \
    "$dialogue_session" \
    "$dialogue_content" \
    "$quest_definitions" \
    "$quest_registry" \
    "$quest_instance" \
    "$quest_service" \
    "$quest_event_bridge" \
    "$character_identity" \
    "$quest_persistence" \
    "$objective_reach" \
    "$objective_talk" \
    "$objective_item_utils" \
    "$objective_fetch" \
    "$objective_deliver" \
    "$objective_kill" \
    "$objective_clear" \
    "$translation_en" \
    "$translation_ru" \
    "$npc_fixes_bandit_update"
do
    [[ -f "$required" ]] || fail "missing $required"
done

[[ ! -e "$legacy_client_bandits" ]] \
    || fail "legacy client/server Bandits runtime module-name collision reintroduced"

# Provider boundary: quest/dialogue/persistence/UI code must not import Bandits.
non_runtime_files=(
    "$client_interaction"
    "$client_quest_state"
    "$client_character_lifecycle"
    "$client_quest_marker"
    "$client_hub"
    "$client_hub_bootstrap"
    "$lua_root/client/LCCQF/UI/LCCQFDialoguePanel.lua"
    "$server_interaction"
    "$dialogue_session"
    "$quest_registry"
    "$quest_instance"
    "$quest_service"
    "$quest_event_bridge"
    "$character_identity"
    "$quest_persistence"
    "$objective_reach"
    "$objective_talk"
    "$objective_item_utils"
    "$objective_fetch"
    "$objective_deliver"
    "$objective_kill"
    "$objective_clear"
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua"
    "$shared_runtime"
)
if rg -n 'Bandit(Brain|Custom|Server)|require "Bandit"' "${non_runtime_files[@]}"; then
    fail "Bandits API escaped the runtime adapter boundary"
fi
if rg -n 'getZombieList\(' "$lua_root"; then
    fail "broad zombie scan reintroduced into Quest Framework"
fi
if rg -n 'LCCQF/Runtime/LCCQFBanditsRuntime' "$client_interaction"; then
    fail "client interaction depends directly on Bandits adapter"
fi

# Framework-neutral NPC runtime contracts.
require_pattern 'function Runtime\.FindNearestInteractive' "$shared_runtime" "provider-neutral interaction discovery missing"
require_pattern 'runtimeAnchors' "$shared_runtime" "runtime anchor storage missing"
require_pattern 'activeRuntimeByNPCId' "$shared_runtime" "one-active-runtime-per-npc invariant missing"
require_pattern 'function Runtime\.UnbindRuntime' "$shared_runtime" "runtime unbind API missing"
require_pattern 'definition\.interactive ~= false' "$shared_runtime" "generic interaction eligibility missing"
require_pattern 'function Runtime\.IsFrameworkEntity' "$shared_runtime" "framework entity classifier missing"

# Bandits server adapter boundary/lifecycle.
require_pattern 'LCCQFBanditsServerRuntime' "$server_bandits_wrapper" "legacy server wrapper does not redirect to unique runtime"
require_pattern 'RUNTIME:BANDITS:SERVER' "$server_bandits" "Bandits server runtime marker missing"
require_pattern 'function Adapter\.RefreshRuntimeBindings' "$server_bandits" "runtime refresh hook missing"
require_pattern 'function Adapter\.ReconcileRuntimeBindings' "$server_bandits" "runtime reconciliation missing"
require_pattern 'function Adapter\.SetBindingEventSink' "$server_bandits" "runtime binding event sink missing"
require_pattern 'Events\.OnZombieDead\.Add' "$server_bandits" "runtime death invalidation hook missing"
require_pattern 'reason=unload|"unload"' "$server_bandits" "runtime unload invalidation missing"
require_pattern 'ExportRuntimeBindings' "$server_bandits" "runtime refresh is not framework-state based"
require_pattern 'anchorFor' "$server_bandits" "Bandits adapter does not publish anchors"
require_pattern 'MOVEMENT_PUBLISH_DISTANCE' "$server_bandits" "moving anchor replication threshold missing"
require_pattern 'emitBindingEvent\("upsert", handle, "movement"\)' "$server_bandits" "moving anchors are not replicated"
require_pattern 'if isServer and isServer\(\) then' "$server_bandits" "Bandits server hooks are not server-guarded"
require_pattern 'function adapter\.OwnsEntity' "$server_bandits_ownership" "Bandits entity ownership classifier missing"
if rg -n 'brain\.key\s*==\s*definition\.npcId|Registry\.IsRegistered\(brain\.key\)' "$server_bandits"; then
    fail "legacy Bandits brain.key quest identity restoration reintroduced"
fi

# Essential quest giver policy. Generic Bandits combat is scheduled before custom
# ZombiePrograms, so the veto MUST live in the NPCFixes BanditUpdate seam rather
# than deleting the physical NPC from CacheLightB after the fact.
require_pattern '^require=\\Bandits2,\\LaccckaB4220NPCFixes$' "$mod_info" "Quest Framework does not require its Bandits scheduling compatibility layer"
require_pattern 'source-clean-coordinate-pursuit-v4' "$npc_fixes_bandit_update" "NPCFixes non-combat BanditUpdate v4 seam missing"
require_pattern 'local function LCCIsNonCombatBandit' "$npc_fixes_bandit_update" "non-combat Bandits classifier missing"
require_pattern 'program\.name == "LCCQFQuestGiver"' "$npc_fixes_bandit_update" "quest-giver early program classification missing"
for seam in \
    non-combat-victim-selection \
    non-combat-combat-dispatch \
    non-combat-collision-dispatch \
    non-combat-hit-dispatch
do
    require_pattern "$seam" "$npc_fixes_bandit_update" "NPCFixes scheduling seam missing: $seam"
done
require_pattern 'not LCCIsNonCombatBandit\(candidate\)' "$npc_fixes_bandit_update" "zombie victim selection does not exclude non-combat Bandits"
require_pattern '#tasks == 0 and not LCCIsNonCombatBandit\(bandit\)' "$npc_fixes_bandit_update" "generic Bandits task scheduling is not gated for non-combat NPCs"
require_pattern 'if LCCIsNonCombatBandit\(zombie\) then return end' "$npc_fixes_bandit_update" "Bandits hit reaction is not gated for non-combat NPCs"
require_pattern 'brain\.lccqNonCombat = true' "$shared_quest_giver_program" "quest giver does not publish non-combat brain policy"
require_pattern 'setInvulnerable\(true\)' "$shared_quest_giver_program" "client quest giver invulnerability missing"
require_pattern 'setShootable\(false\)' "$shared_quest_giver_program" "client quest giver target suppression missing"
require_pattern 'ForceStationary\(bandit, true\)' "$shared_quest_giver_program" "quest giver stationary intent missing"
require_pattern 'setInvulnerable\(true\)' "$server_quest_giver_protection" "server quest giver invulnerability missing"
require_pattern 'setShootable\(false\)' "$server_quest_giver_protection" "server quest giver target suppression missing"
require_pattern 'lccqNonCombat' "$server_quest_giver_protection" "server quest giver non-combat brain policy missing"
if rg -n 'require "BanditZombie"|BanditZombie\.CacheLightB' "$shared_quest_giver_program"; then
    fail "late Bandit combat-cache suppression reintroduced into custom quest-giver program"
fi
if rg -n 'getCheats\(|CheatType|PlayerCheats' "$server_quest_giver_protection" "$shared_quest_giver_program"; then
    fail "non-Lua cheat internals reintroduced"
fi

# Interaction/dialogue lifecycle.
require_pattern 'RUNTIME_BINDING_REMOVE' "$constants" "runtime binding removal command missing"
require_pattern 'RUNTIME_BINDING_REMOVE' "$server_interaction" "server does not broadcast runtime removals"
require_pattern 'RUNTIME_BINDING_REMOVE' "$client_interaction" "client does not consume runtime removals"
require_pattern 'DialogueSession\.InvalidateRuntime' "$server_interaction" "runtime removal does not invalidate dialogue"
require_pattern 'function DialogueSession\.InvalidateRuntime' "$dialogue_session" "dialogue runtime invalidation API missing"
require_pattern 'GetActiveRuntimeId' "$server_interaction" "server does not reject stale runtime ids"
require_pattern 'if isServer and isServer\(\) then' "$server_interaction" "interaction server hooks are not server-guarded"
require_pattern 'if isServer and isServer\(\) then' "$dialogue_session" "dialogue expiry hook is not server-guarded"
require_pattern 'isChoiceAvailable' "$dialogue_session" "dialogue choices are not revalidated server-side"

# Quest definition/instance/runtime contracts.
require_pattern 'function QuestRegistry\.Register' "$quest_registry" "quest definition registry missing"
require_pattern 'function QuestInstance\.Create' "$quest_instance" "QuestInstance creation missing"
require_pattern 'function QuestInstance\.Restore' "$quest_instance" "QuestInstance restore path missing"
require_pattern 'ownerCharacterId' "$quest_instance" "QuestInstance is not character-owned"
require_pattern 'function QuestInstance\.CompleteCurrentObjective' "$quest_instance" "objective transition missing"
for objective_type in ReachArea TalkToNPC Fetch Deliver Kill ClearArea; do
    require_pattern "$objective_type = LCCQF\.QuestObjectives\.$objective_type" "$quest_instance" "objective handler not registered: $objective_type"
    require_pattern "type = \"$objective_type\"" "$quest_definitions" "test quest coverage missing objective type: $objective_type"
done
require_pattern 'handler\.ValidatePersisted' "$quest_instance" "objective-specific persistence validation missing"
require_pattern 'handler\.MakeProgressView' "$quest_instance" "objective progress projection missing"

require_pattern 'function QuestService\.Initialize' "$quest_service" "quest persistence initialization missing"
require_pattern 'function QuestService\.Accept' "$quest_service" "server quest accept path missing"
require_pattern 'function QuestService\.NotifyTalkToNPC' "$quest_service" "TalkToNPC transition missing"
require_pattern 'function QuestService\.NotifyZombieDead' "$quest_service" "Kill objective death transition missing"
require_pattern 'handler\.EvaluateTick' "$quest_service" "generic tick dispatch missing"
require_pattern 'handler\.EvaluateTalk' "$quest_service" "generic talk dispatch missing"
require_pattern 'handler\.EvaluateZombieDeath' "$quest_service" "generic zombie-death dispatch missing"
require_pattern 'function QuestService\.OnPlayerDeath' "$quest_service" "character retirement path missing"
require_pattern 'function QuestService\.Tick' "$quest_service" "server objective update loop missing"
require_pattern 'QuestPersistence\.GetQuestStore' "$quest_service" "QuestService does not use world-backed persistence"
require_pattern 'QuestService\.Initialize' "$server_interaction" "server startup does not initialize quest persistence"
require_pattern 'Events\.OnPlayerDeath|QuestService\.OnPlayerDeath' "$server_interaction" "dead character identities are not retired"
require_pattern 'QuestService\.NotifyTalkToNPC' "$server_interaction" "dialogue is not connected to talk objectives"
require_pattern 'QuestService\.EvaluateCondition' "$server_interaction" "dialogue conditions are not server-authoritative"
require_pattern 'QuestService\.ExecuteAction' "$server_interaction" "dialogue actions are not server-authoritative"
require_pattern 'Events\.OnZombieDead\.Add' "$quest_event_bridge" "authoritative zombie-death bridge missing"
require_pattern 'IsFrameworkEntity' "$quest_event_bridge" "framework NPCs are not excluded from zombie kills"
require_pattern 'questAccept' "$dialogue_content" "dialogue quest offer missing"
require_pattern 'condition.kind == "all"' "$quest_service" "composite dialogue conditions missing"

# Objective implementations/markers.
require_pattern 'mode = "EXACT"' "$quest_definitions" "exact QuestMarker presentation missing"
require_pattern 'mode = "AREA"' "$quest_definitions" "area QuestMarker presentation missing"
require_pattern 'function ReachArea\.MakeMarkerView' "$objective_reach" "ReachArea marker projection missing"
require_pattern 'function ClearArea\.MakeMarkerView' "$objective_clear" "ClearArea marker projection missing"
require_pattern 'function Fetch\.EvaluateTick' "$objective_fetch" "Fetch evaluator missing"
require_pattern 'function Deliver\.EvaluateTalk' "$objective_deliver" "Deliver evaluator missing"
require_pattern 'ItemUtils\.Remove' "$objective_deliver" "Deliver does not consume validated items"
require_pattern 'sendRemoveItemFromContainer' "$objective_item_utils" "server item removal is not replicated"
require_pattern 'function Kill\.EvaluateZombieDeath' "$objective_kill" "Kill evaluator missing"
require_pattern 'getAttackedBy' "$objective_kill" "Kill credit does not use authoritative attacker"
require_pattern 'function ClearArea\.EvaluateTick' "$objective_clear" "ClearArea evaluator missing"
require_pattern 'IsFrameworkEntity' "$objective_clear" "ClearArea does not exclude framework NPCs"
require_pattern 'markerId = tostring\(instance\.id\)' "$quest_instance" "stable objective marker id missing"

# Persistence / per-life character identity.
require_pattern 'ModData\.getOrCreate\(C\.PERSISTENCE_TAG\)' "$character_identity" "GlobalModData-backed persistence missing"
require_pattern 'player:getModData\(\)' "$character_identity" "saved player identity anchor missing"
require_pattern 'getRandomUUID\(\)' "$character_identity" "character UUID creation missing"
require_pattern 'retiredCharacterIds' "$character_identity" "retired identity guard missing"
require_pattern 'function CharacterIdentity\.Retire' "$character_identity" "character retirement API missing"
require_pattern 'function QuestPersistence\.GetQuestStore' "$quest_persistence" "persistent quest store API missing"
require_pattern 'QuestInstance\.Restore' "$quest_persistence" "persisted quest normalization missing"
require_pattern 'QUEST_PERSISTENCE_SCHEMA_VERSION' "$quest_persistence" "quest persistence schema missing"
if rg -n 'getOnlineID\(' "$quest_service" "$character_identity" "$quest_persistence"; then
    fail "transient onlineID reintroduced as durable quest owner"
fi

# Client projection lifecycle / RPG Hub / map markers.
require_pattern 'REQUEST_QUESTS' "$constants" "quest state request protocol missing"
require_pattern 'QUEST_UPSERT' "$server_interaction" "server quest view sync missing"
require_pattern 'QuestClientState\.Apply' "$client_interaction" "client quest view store not connected"
require_pattern 'function QuestClientState\.AddListener' "$client_quest_state" "quest state notification missing"
require_pattern 'function QuestClientState\.BeginCharacterTransition' "$client_quest_state" "per-life client reset missing"
require_pattern 'Events\.OnPlayerDeath\.Add' "$client_character_lifecycle" "client death reset hook missing"
require_pattern 'Events\.OnCreatePlayer\.Add' "$client_character_lifecycle" "new-character resync hook missing"
require_pattern 'LCCQF/Quest/LCCQFCharacterProjectionLifecycle' "$client_hub_bootstrap" "character lifecycle module not loaded"
require_pattern 'getSymbolsAPIv2' "$client_quest_marker" "world-map symbols v2 adapter missing"
require_pattern 'addUntranslatedText|addTexture' "$client_quest_marker" "quest marker renderer missing"
require_pattern 'setUserDefined\(true\)' "$client_quest_marker" "quest marker visibility flag missing"
require_pattern 'clearOwnedSymbols' "$client_quest_marker" "stale quest-marker cleanup missing"
require_pattern 'countOwnedSymbols' "$client_quest_marker" "quest-marker integrity check missing"
require_pattern 'QuestClientState\.AddListener' "$client_quest_marker" "marker lifecycle is not quest-state driven"
require_pattern 'function Hub\.RegisterPage' "$client_hub" "RPG Hub page registry missing"
require_pattern 'IGUI_LCCQF_Hub_Tab_Quests' "$client_hub" "RPG Hub Quests page missing"
require_pattern 'LCCQF/Quest/LCCQFQuestMarkerService' "$client_hub_bootstrap" "marker service not loaded by Hub bootstrap"
require_pattern 'LCCQF/UI/LCCQFHub' "$client_hub_bootstrap" "RPG Hub not loaded by bootstrap"
require_pattern 'Events\.OnKeyPressed\.Add' "$client_interaction" "interaction input is not OnKeyPressed"
if rg -n 'Events\.OnKeyStartPressed\.Add|Events\.OnTick\.Add\(onTick\)' "$client_interaction"; then
    fail "legacy interaction polling path reintroduced"
fi
if rg -n 'QuestService\.(Accept|ExecuteAction|NotifyTalkToNPC|NotifyZombieDead|Tick)|CompleteCurrentObjective|questAccept' "$lua_root/client"; then
    fail "client-owned quest state transition reintroduced"
fi
if rg -n 'LCCQF/Persistence/' "$lua_root/client"; then
    fail "client depends directly on persistence internals"
fi
if rg -n 'choice\.next|showNode\(' "$lua_root/client"; then
    fail "client-owned dialogue transition reintroduced"
fi

# Version/schema/build hygiene.
require_pattern '^modversion=0\.3\.3$' "$mod_info" "mod.info version mismatch"
require_pattern 'Constants\.VERSION = "0\.3\.3"' "$constants" "Lua version mismatch"
require_pattern 'Constants\.PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "world persistence schema constant missing"
require_pattern 'Constants\.QUEST_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "quest persistence schema constant missing"
require_pattern 'Constants\.CHARACTER_ID_MODDATA_KEY' "$constants" "character identity modData key missing"
require_pattern 'Constants\.TEST_QUEST_2_ID' "$constants" "objective-runtime test quest id missing"
if rg -n 'brain\.key\s*=\s*definition\.npcId|key\s*=\s*definition\.npcId' "$lua_root/server/LCCQF/Runtime"; then
    fail "framework npcId escaped into Bandits numeric key field"
fi
if LC_ALL=C rg -n '[^\x00-\x7F]' "$lua_root" -g '*.lua'; then
    fail "non-ASCII user-facing text reintroduced into Lua/network payloads"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi
if command -v lua >/dev/null 2>&1; then
    find "$lua_root" -type f -name '*.lua' -print0 | while IFS= read -r -d '' lua_file; do
        lua -e "assert(loadfile([[$lua_file]]))"
    done
    lua -e "assert(loadfile([[$npc_fixes_bandit_update]]))"
fi

echo "QuestFramework audit: PASS"
