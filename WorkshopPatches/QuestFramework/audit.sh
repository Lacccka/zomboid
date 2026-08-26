#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$project_root/../.." && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"
npc_fixes_bridge="$repo_root/WorkshopPatches/NPCFixes/Contents/mods/LaccckaB4220NPCFixes/42/media/lua/client/zz_LCC_BanditCallbackBridge.lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
npc_registry="$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua"
npc_runtime="$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
npc_definitions="$lua_root/shared/LCCQF/Content/LCCQFNPCDefinitions.lua"
quest_giver_program="$lua_root/shared/LCCQF/Runtime/LCCQFBanditsQuestGiverProgram.lua"

client_interaction="$lua_root/client/LCCQF/LCCQFInteractionClient.lua"
client_quest_state="$lua_root/client/LCCQF/Quest/LCCQFQuestClientState.lua"
client_quest_lifecycle="$lua_root/client/LCCQF/Quest/LCCQFCharacterProjectionLifecycle.lua"
client_quest_marker="$lua_root/client/LCCQF/Quest/LCCQFQuestMarkerService.lua"
client_people_state="$lua_root/client/LCCQF/Knowledge/LCCQFKnownPeopleClientState.lua"
client_knowledge_lifecycle="$lua_root/client/LCCQF/Knowledge/LCCQFCharacterKnowledgeLifecycle.lua"
client_hub="$lua_root/client/LCCQF/UI/LCCQFHub.lua"
client_journal="$lua_root/client/LCCQF/UI/LCCQFRPGJournalPages.lua"
client_hub_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
client_bandits_presentation="$lua_root/client/LCCQF/Runtime/LCCQFBanditsClientPresentation.lua"
legacy_client_bandits="$lua_root/client/LCCQF/Runtime/LCCQFBanditsRuntime.lua"

server_interaction="$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
server_bandits_wrapper="$lua_root/server/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_bandits="$lua_root/server/LCCQF/Runtime/LCCQFBanditsServerRuntime.lua"
server_bandits_ownership="$lua_root/server/LCCQF/Runtime/LCCQFBanditsEntityOwnership.lua"
server_quest_giver_protection="$lua_root/server/LCCQF/Runtime/zz_LCCQFBanditsQuestGiverProtection.lua"
dialogue_session="$lua_root/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
dialogue_content="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
quest_registry="$lua_root/server/LCCQF/Quest/LCCQFQuestRegistry.lua"
quest_instance="$lua_root/server/LCCQF/Quest/LCCQFQuestInstance.lua"
quest_service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
quest_event_bridge="$lua_root/server/LCCQF/Quest/zz_LCCQFQuestEventBridge.lua"
character_identity="$lua_root/server/LCCQF/Persistence/LCCQFCharacterIdentity.lua"
quest_persistence="$lua_root/server/LCCQF/Persistence/LCCQFQuestPersistence.lua"
character_knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterKnowledge.lua"
knowledge_bridge="$lua_root/server/LCCQF/Knowledge/zz_LCCQFCharacterKnowledgeBridge.lua"
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

required_files=(
    "$constants"
    "$npc_registry"
    "$npc_runtime"
    "$npc_definitions"
    "$quest_giver_program"
    "$client_interaction"
    "$client_quest_state"
    "$client_quest_lifecycle"
    "$client_quest_marker"
    "$client_people_state"
    "$client_knowledge_lifecycle"
    "$client_hub"
    "$client_journal"
    "$client_hub_bootstrap"
    "$client_bandits_presentation"
    "$server_interaction"
    "$server_bandits_wrapper"
    "$server_bandits"
    "$server_bandits_ownership"
    "$server_quest_giver_protection"
    "$dialogue_session"
    "$dialogue_content"
    "$quest_definitions"
    "$quest_registry"
    "$quest_instance"
    "$quest_service"
    "$quest_event_bridge"
    "$character_identity"
    "$quest_persistence"
    "$character_knowledge"
    "$knowledge_bridge"
    "$objective_reach"
    "$objective_talk"
    "$objective_item_utils"
    "$objective_fetch"
    "$objective_deliver"
    "$objective_kill"
    "$objective_clear"
    "$translation_en"
    "$translation_ru"
    "$npc_fixes_bridge"
)
for required in "${required_files[@]}"; do
    [[ -f "$required" ]] || fail "missing $required"
done

[[ ! -e "$legacy_client_bandits" ]] \
    || fail "legacy client/server Bandits runtime module-name collision reintroduced"

# Provider boundary. Bandits is allowed only in Runtime/presentation adapters.
non_runtime_files=(
    "$client_interaction"
    "$client_quest_state"
    "$client_quest_lifecycle"
    "$client_quest_marker"
    "$client_people_state"
    "$client_knowledge_lifecycle"
    "$client_hub"
    "$client_journal"
    "$client_hub_bootstrap"
    "$server_interaction"
    "$dialogue_session"
    "$quest_registry"
    "$quest_instance"
    "$quest_service"
    "$quest_event_bridge"
    "$character_identity"
    "$quest_persistence"
    "$character_knowledge"
    "$knowledge_bridge"
    "$objective_reach"
    "$objective_talk"
    "$objective_item_utils"
    "$objective_fetch"
    "$objective_deliver"
    "$objective_kill"
    "$objective_clear"
    "$npc_registry"
    "$npc_runtime"
)
if rg -n 'Bandit(Brain|Custom|Server|Zombie)|require "Bandit"' "${non_runtime_files[@]}"; then
    fail "Bandits API escaped the runtime/presentation adapter boundary"
fi
if rg -n 'getZombieList\(' "$lua_root"; then
    fail "broad zombie scan reintroduced into Quest Framework"
fi

# Provider-neutral runtime and presentation boundary.
require_pattern 'function Runtime\.FindNearestInteractive' "$npc_runtime" "provider-neutral interaction discovery missing"
require_pattern 'runtimeAnchors' "$npc_runtime" "runtime anchor storage missing"
require_pattern 'activeRuntimeByNPCId' "$npc_runtime" "one-active-runtime-per-npc invariant missing"
require_pattern 'function Runtime\.UnbindRuntime' "$npc_runtime" "runtime unbind API missing"
require_pattern 'definition\.interactive ~= false' "$npc_runtime" "generic interaction eligibility missing"
require_pattern 'function Runtime\.IsFrameworkEntity' "$npc_runtime" "framework entity classifier missing"
require_pattern 'function Runtime\.RegisterClientResolver' "$npc_runtime" "provider-neutral client presentation resolver missing"
require_pattern 'function Runtime\.ResolveClientEntity' "$npc_runtime" "client presentation resolution API missing"
require_pattern 'GetActiveRuntimeId\(npcId\)' "$npc_runtime" "portrait resolver is not bound to synchronized framework runtime identity"
require_pattern 'RegisterClientResolver\("Bandits"' "$client_bandits_presentation" "Bandits presentation resolver is not registered"
require_pattern 'BanditZombie\.GetInstanceById' "$client_bandits_presentation" "Bandits portrait resolver does not use bounded runtime-id lookup"
if rg -n 'FindNearestInteractive|getZombieList\(' "$client_bandits_presentation"; then
    fail "presentation adapter is participating in interaction discovery"
fi

# Current NPCFixes scheduling compatibility contract (1.0.5 loadstring-free bridge).
require_pattern '^require=\\Bandits2,\\LaccckaB4220NPCFixes$' "$mod_info" "Quest Framework does not require NPCFixes"
require_pattern 'loadstring-free-predicate-bridge-v2' "$npc_fixes_bridge" "NPCFixes predicate bridge marker missing"
require_pattern 'getModFileReader' "$npc_fixes_bridge" "NPCFixes upstream fingerprint validation missing"
require_pattern 'BanditCompatibility\.IsReanimatedForGrappleOnly = function' "$npc_fixes_bridge" "ordinary-zombie predicate gate missing"
require_pattern 'BanditCompatibility\.IsRagdoll = function' "$npc_fixes_bridge" "OnBanditUpdate discriminator missing"
require_pattern 'not isNonCombatBandit\(real\)' "$npc_fixes_bridge" "ordinary zombie pursuit does not exclude non-combat NPCs"
require_pattern 'Bandit\.IsSleeping = function' "$npc_fixes_bridge" "non-combat generic combat gate missing"
require_pattern 'Bandit\.GetTask = function' "$npc_fixes_bridge" "non-combat collision gate missing"
require_pattern 'brain\.lccqNonCombat = true' "$quest_giver_program" "quest giver does not publish non-combat policy"
require_pattern 'setInvulnerable\(true\)' "$quest_giver_program" "client quest giver invulnerability missing"
require_pattern 'setShootable\(false\)' "$quest_giver_program" "client quest giver shootable suppression missing"
require_pattern 'ForceStationary\(bandit, true\)' "$quest_giver_program" "quest giver stationary intent missing"
require_pattern 'setInvulnerable\(true\)' "$server_quest_giver_protection" "server quest giver invulnerability missing"
require_pattern 'lccqNonCombat' "$server_quest_giver_protection" "server non-combat policy missing"
if rg -n 'getCheats\(|CheatType|PlayerCheats' "$server_quest_giver_protection" "$quest_giver_program"; then
    fail "non-Lua cheat internals reintroduced"
fi
if rg -n 'BanditZombie\.CacheLightB' "$quest_giver_program"; then
    fail "late CacheLightB suppression reintroduced into quest-giver program"
fi

# Server Bandits runtime lifecycle.
require_pattern 'LCCQFBanditsServerRuntime' "$server_bandits_wrapper" "server Bandits wrapper lost unique runtime target"
require_pattern 'RUNTIME:BANDITS:SERVER' "$server_bandits" "Bandits server runtime marker missing"
require_pattern 'function Adapter\.RefreshRuntimeBindings' "$server_bandits" "runtime refresh hook missing"
require_pattern 'function Adapter\.ReconcileRuntimeBindings' "$server_bandits" "runtime reconciliation missing"
require_pattern 'function Adapter\.SetBindingEventSink' "$server_bandits" "runtime binding event sink missing"
require_pattern 'Events\.OnZombieDead\.Add' "$server_bandits" "runtime death invalidation hook missing"
require_pattern 'MOVEMENT_PUBLISH_DISTANCE' "$server_bandits" "moving anchor replication threshold missing"
require_pattern 'function adapter\.OwnsEntity' "$server_bandits_ownership" "Bandits entity ownership classifier missing"
if rg -n 'brain\.key\s*==\s*definition\.npcId|Registry\.IsRegistered\(brain\.key\)' "$server_bandits"; then
    fail "legacy brain.key logical identity reintroduced"
fi

# Interaction/dialogue lifecycle remains server-authoritative.
require_pattern 'RUNTIME_BINDING_REMOVE' "$constants" "runtime binding removal protocol missing"
require_pattern 'RUNTIME_BINDING_REMOVE' "$server_interaction" "server runtime removal broadcast missing"
require_pattern 'RUNTIME_BINDING_REMOVE' "$client_interaction" "client runtime removal handling missing"
require_pattern 'DialogueSession\.InvalidateRuntime' "$server_interaction" "runtime removal does not invalidate dialogue"
require_pattern 'function DialogueSession\.InvalidateRuntime' "$dialogue_session" "dialogue runtime invalidation API missing"
require_pattern 'GetActiveRuntimeId' "$server_interaction" "server stale runtime-id rejection missing"
require_pattern 'isChoiceAvailable' "$dialogue_session" "dialogue choices are not revalidated server-side"
require_pattern 'QuestService\.EvaluateCondition' "$server_interaction" "dialogue conditions are not server-authoritative"
require_pattern 'QuestService\.ExecuteAction' "$server_interaction" "dialogue actions are not server-authoritative"

# Character Knowledge: world existence != per-life character knowledge.
require_pattern 'KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "knowledge schema constant missing"
require_pattern 'REQUEST_KNOWLEDGE = "RequestKnowledge"' "$constants" "knowledge snapshot request command missing"
require_pattern 'KNOWN_PERSON_UPSERT = "KnownPersonUpsert"' "$constants" "known-person incremental protocol missing"
require_pattern 'record\.knowledge' "$character_knowledge" "knowledge is not stored under durable character record"
require_pattern 'knownNPCs' "$character_knowledge" "known NPC store missing"
require_pattern 'function CharacterKnowledge\.DiscoverNPC' "$character_knowledge" "DiscoverNPC API missing"
require_pattern 'function CharacterKnowledge\.UnlockFact' "$character_knowledge" "knowledge-fragment unlock API missing"
require_pattern 'function CharacterKnowledge\.ExportViews' "$character_knowledge" "sanitized Known People projection missing"
require_pattern 'NPCRegistry\.Get\(npcId\)' "$character_knowledge" "DiscoverNPC does not validate framework NPC identity"
require_pattern 'initialKnowledgeFacts' "$character_knowledge" "initial discovery facts missing"
require_pattern 'DialogueSession\.Open = function' "$knowledge_bridge" "validated-dialogue discovery seam missing"
require_pattern 'if view and definition' "$knowledge_bridge" "discovery is not gated on successful dialogue open"
require_pattern 'CharacterKnowledge\.DiscoverNPC' "$knowledge_bridge" "validated dialogue does not discover NPC"
require_pattern '"validated-dialogue"' "$knowledge_bridge" "discovery source marker missing"
require_pattern 'C\.COMMAND\.REQUEST_KNOWLEDGE' "$knowledge_bridge" "server knowledge snapshot route missing"
require_pattern 'C\.COMMAND\.KNOWN_PERSON_UPSERT' "$knowledge_bridge" "server knowledge upsert route missing"
if rg -n 'GetDefinitions\(' "$character_knowledge" "$client_people_state" "$client_journal"; then
    fail "full NPC registry projection leaked into character knowledge/UI"
fi

# Per-life client knowledge lifecycle.
require_pattern 'function KnownPeopleClientState\.BeginCharacterTransition' "$client_people_state" "Known People per-life reset missing"
require_pattern 'function KnownPeopleClientState\.Replace' "$client_people_state" "Known People full snapshot API missing"
require_pattern 'function KnownPeopleClientState\.Apply' "$client_people_state" "Known People incremental upsert API missing"
require_pattern 'Events\.OnPlayerDeath\.Add' "$client_knowledge_lifecycle" "Known People death reset hook missing"
require_pattern 'Events\.OnCreatePlayer\.Add' "$client_knowledge_lifecycle" "Known People new-character reset hook missing"
require_pattern 'REQUEST_KNOWLEDGE' "$client_knowledge_lifecycle" "Known People resync request missing"
require_pattern 'C\.COMMAND\.KNOWLEDGE' "$client_knowledge_lifecycle" "Known People snapshot receive path missing"
require_pattern 'C\.COMMAND\.KNOWN_PERSON_UPSERT' "$client_knowledge_lifecycle" "Known People upsert receive path missing"

# RPG Hub: Known People dossier + Skyrim-style quest journal + cross navigation.
require_pattern 'function Hub\.RegisterPage' "$client_hub" "RPG Hub page registry missing"
require_pattern 'LCCQFKnownPeoplePage' "$client_journal" "Known People page missing"
require_pattern 'LCCQFQuestJournalPage' "$client_journal" "quest journal page missing"
require_pattern 'id = "known_people"' "$client_journal" "Known People top-level tab missing"
require_pattern 'id = "quests"' "$client_journal" "Quests top-level tab missing"
require_pattern 'ISUI3DModel:new' "$client_journal" "live 3D NPC portrait viewer missing"
require_pattern 'Runtime\.ResolveClientEntity' "$client_journal" "portrait does not use provider-neutral presentation boundary"
require_pattern 'view\.facts' "$client_journal" "NPC dossier does not render discovered history fragments"
require_pattern 'questsForPerson' "$client_journal" "NPC dossier quest linkage missing"
require_pattern 'function Hub\.OpenPerson' "$client_journal" "quest-to-person navigation missing"
require_pattern 'function Hub\.OpenQuest' "$client_journal" "person-to-quest navigation missing"
require_pattern 'showCompleted' "$client_journal" "completed quest archive toggle missing"
require_pattern 'IGUI_LCCQF_Hub_ActiveQuests' "$client_journal" "active quest journal section missing"
require_pattern 'IGUI_LCCQF_Hub_CompletedQuests' "$client_journal" "completed quest archive section missing"
require_pattern 'LCCQF/Knowledge/LCCQFKnownPeopleClientState' "$client_hub_bootstrap" "Known People state not bootstrapped"
require_pattern 'LCCQF/Knowledge/LCCQFCharacterKnowledgeLifecycle' "$client_hub_bootstrap" "knowledge lifecycle not bootstrapped"
require_pattern 'LCCQF/Runtime/LCCQFBanditsClientPresentation' "$client_hub_bootstrap" "portrait adapter not bootstrapped"
require_pattern 'LCCQF/UI/LCCQFRPGJournalPages' "$client_hub_bootstrap" "new RPG journal pages not bootstrapped"

# Quest definition/instance/runtime contracts retained from 0.3.3.
require_pattern 'function QuestRegistry\.Register' "$quest_registry" "quest registry missing"
require_pattern 'function QuestInstance\.Create' "$quest_instance" "QuestInstance creation missing"
require_pattern 'function QuestInstance\.Restore' "$quest_instance" "QuestInstance restore missing"
require_pattern 'ownerCharacterId' "$quest_instance" "QuestInstance durable owner missing"
require_pattern 'function QuestInstance\.CompleteCurrentObjective' "$quest_instance" "objective transition missing"
for objective_type in ReachArea TalkToNPC Fetch Deliver Kill ClearArea; do
    require_pattern "$objective_type = LCCQF\.QuestObjectives\.$objective_type" "$quest_instance" "objective handler not registered: $objective_type"
    require_pattern "type = \"$objective_type\"" "$quest_definitions" "test quest coverage missing objective type: $objective_type"
done
require_pattern 'function QuestService\.Accept' "$quest_service" "server quest accept missing"
require_pattern 'function QuestService\.NotifyTalkToNPC' "$quest_service" "TalkToNPC transition missing"
require_pattern 'function QuestService\.NotifyZombieDead' "$quest_service" "Kill transition missing"
require_pattern 'handler\.EvaluateTick' "$quest_service" "generic tick dispatch missing"
require_pattern 'handler\.EvaluateTalk' "$quest_service" "generic talk dispatch missing"
require_pattern 'handler\.EvaluateZombieDeath' "$quest_service" "generic death dispatch missing"
require_pattern 'QuestPersistence\.GetQuestStore' "$quest_service" "QuestService does not use persistence"
require_pattern 'Events\.OnZombieDead\.Add' "$quest_event_bridge" "authoritative death bridge missing"
require_pattern 'IsFrameworkEntity' "$quest_event_bridge" "framework NPCs are not excluded from kill objectives"
require_pattern 'function Fetch\.EvaluateTick' "$objective_fetch" "Fetch evaluator missing"
require_pattern 'function Deliver\.EvaluateTalk' "$objective_deliver" "Deliver evaluator missing"
require_pattern 'function Kill\.EvaluateZombieDeath' "$objective_kill" "Kill evaluator missing"
require_pattern 'function ClearArea\.EvaluateTick' "$objective_clear" "ClearArea evaluator missing"
require_pattern 'function ReachArea\.MakeMarkerView' "$objective_reach" "ReachArea marker missing"
require_pattern 'function ClearArea\.MakeMarkerView' "$objective_clear" "ClearArea marker missing"

# Durable identity and quest projection lifecycle.
require_pattern 'ModData\.getOrCreate\(C\.PERSISTENCE_TAG\)' "$character_identity" "GlobalModData persistence missing"
require_pattern 'player:getModData\(\)' "$character_identity" "saved character identity anchor missing"
require_pattern 'getRandomUUID\(\)' "$character_identity" "durable character UUID generation missing"
require_pattern 'retiredCharacterIds' "$character_identity" "retired identity guard missing"
require_pattern 'function CharacterIdentity\.Retire' "$character_identity" "character retirement API missing"
require_pattern 'function QuestPersistence\.GetQuestStore' "$quest_persistence" "persistent quest store missing"
require_pattern 'function QuestClientState\.BeginCharacterTransition' "$client_quest_state" "quest projection per-life reset missing"
require_pattern 'Events\.OnPlayerDeath\.Add' "$client_quest_lifecycle" "quest death reset hook missing"
require_pattern 'Events\.OnCreatePlayer\.Add' "$client_quest_lifecycle" "quest create-player resync hook missing"
if rg -n 'getOnlineID\(' "$quest_service" "$character_identity" "$quest_persistence" "$character_knowledge"; then
    fail "transient onlineID reintroduced as durable RPG identity"
fi

# Quest markers remain projections, never authority.
require_pattern 'getSymbolsAPIv2' "$client_quest_marker" "world-map symbols v2 adapter missing"
require_pattern 'QuestClientState\.AddListener' "$client_quest_marker" "marker lifecycle not quest-state driven"
require_pattern 'clearOwnedSymbols' "$client_quest_marker" "stale quest marker cleanup missing"
if rg -n 'QuestService\.(Accept|ExecuteAction|NotifyTalkToNPC|NotifyZombieDead|Tick)|CompleteCurrentObjective|questAccept' "$lua_root/client"; then
    fail "client-owned quest transition reintroduced"
fi
if rg -n 'LCCQF/Persistence/' "$lua_root/client"; then
    fail "client depends directly on persistence internals"
fi

# Version/schema/build hygiene.
require_pattern '^modversion=0\.3\.4$' "$mod_info" "mod.info version mismatch"
require_pattern 'Constants\.VERSION = "0\.3\.4"' "$constants" "Lua version mismatch"
require_pattern 'Constants\.PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "world persistence schema missing"
require_pattern 'Constants\.QUEST_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "quest persistence schema missing"
require_pattern 'Constants\.KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "knowledge persistence schema missing"
require_pattern 'Constants\.CHARACTER_ID_MODDATA_KEY' "$constants" "character identity key missing"
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
    lua -e "assert(loadfile([[$npc_fixes_bridge]]))"
fi

echo "QuestFramework audit: PASS (0.3.4 Character Knowledge + Known People + quest journal)"
