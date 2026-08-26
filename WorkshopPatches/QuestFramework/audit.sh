#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"
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
shared_runtime="$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework audit: FAIL: $1" >&2
    exit 1
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
    "$translation_ru"
do
    [[ -f "$required" ]] || fail "missing $required"
done

[[ ! -e "$legacy_client_bandits" ]] \
    || fail "legacy client/server Bandits runtime module-name collision reintroduced"

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
    fail "Bandits API escaped the server runtime adapter boundary"
fi

if rg -n 'getZombieList\(' "$lua_root"; then
    fail "broad zombie scan reintroduced"
fi

if rg -n 'LCCQF/Runtime/LCCQFBanditsRuntime' "$client_interaction"; then
    fail "client interaction depends on a Bandits adapter module"
fi

rg -q 'function Runtime\.FindNearestInteractive' "$shared_runtime" \
    || fail "provider-neutral interaction discovery missing"
rg -q 'runtimeAnchors' "$shared_runtime" \
    || fail "runtime anchor storage missing"
rg -q 'activeRuntimeByNPCId' "$shared_runtime" \
    || fail "one-active-runtime-per-npc invariant missing"
rg -q 'function Runtime\.UnbindRuntime' "$shared_runtime" \
    || fail "runtime unbind lifecycle API missing"
rg -q 'definition\.interactive ~= false' "$shared_runtime" \
    || fail "generic framework interaction eligibility missing"
rg -q 'function Runtime\.IsFrameworkEntity' "$shared_runtime" \
    || fail "provider-neutral framework entity classifier missing"

rg -q 'LCCQFBanditsServerRuntime' "$server_bandits_wrapper" \
    || fail "legacy server module does not redirect to unique server runtime"
rg -q 'RUNTIME:BANDITS:SERVER' "$server_bandits" \
    || fail "unique Bandits server runtime marker missing"
rg -q 'function Adapter\.RefreshRuntimeBindings' "$server_bandits" \
    || fail "server runtime binding sync hook missing"
rg -q 'function Adapter\.ReconcileRuntimeBindings' "$server_bandits" \
    || fail "server runtime lifecycle reconciliation missing"
rg -q 'function Adapter\.SetBindingEventSink' "$server_bandits" \
    || fail "server runtime lifecycle event sink missing"
rg -q 'Events\.OnZombieDead\.Add' "$server_bandits" \
    || fail "Bandits runtime death invalidation hook missing"
rg -q 'reason=unload|"unload"' "$server_bandits" \
    || fail "Bandits runtime unload invalidation missing"
rg -q 'ExportRuntimeBindings' "$server_bandits" \
    || fail "server runtime refresh is not framework-state based"
rg -q 'anchorFor' "$server_bandits" \
    || fail "Bandits server adapter does not publish interaction anchors"
rg -q 'MOVEMENT_PUBLISH_DISTANCE' "$server_bandits" \
    || fail "moving NPC anchor replication threshold missing"
rg -q 'emitBindingEvent\("upsert", handle, "movement"\)' "$server_bandits" \
    || fail "moving NPC anchors are not replicated to clients"
rg -q 'if isServer and isServer\(\) then' "$server_bandits" \
    || fail "Bandits server event hooks are not guarded from client init"
rg -q 'function adapter\.OwnsEntity' "$server_bandits_ownership" \
    || fail "Bandits provider entity ownership classifier missing"
rg -q 'setInvulnerable\(true\)' "$server_quest_giver_protection" \
    || fail "essential quest giver server invulnerability enforcement missing"
rg -q 'setShootable\(false\)' "$server_quest_giver_protection" \
    || fail "essential quest giver server combat target suppression missing"
rg -q 'lccqIgnoreZombieAggro' "$server_quest_giver_protection" \
    || fail "essential quest giver server non-combat marker missing"
rg -q 'BanditZombie\.CacheLightB' "$shared_quest_giver_program" \
    || fail "Bandits zombie combat-cache opt-out missing"
rg -q 'setShootable\(false\)' "$shared_quest_giver_program" \
    || fail "client physical quest giver combat target suppression missing"
rg -q 'if not \(isServer and isServer\(\)\) then' "$shared_quest_giver_program" \
    || fail "client-only BanditZombie dependency is not guarded from dedicated server"
if rg -n 'getCheats\(|CheatType|PlayerCheats' "$server_quest_giver_protection" "$shared_quest_giver_program"; then
    fail "non-Lua PlayerCheats access reintroduced"
fi

if rg -n 'brain\.key\s*==\s*definition\.npcId|Registry\.IsRegistered\(brain\.key\)' "$server_bandits"; then
    fail "legacy Bandits brain.key quest identity restoration reintroduced"
fi

rg -q 'RUNTIME_BINDING_REMOVE' "$constants" \
    || fail "runtime binding removal command missing"
rg -q 'RUNTIME_BINDING_REMOVE' "$server_interaction" \
    || fail "server does not broadcast runtime binding removals"
rg -q 'RUNTIME_BINDING_REMOVE' "$client_interaction" \
    || fail "client does not consume runtime binding removals"
rg -q 'DialogueSession\.InvalidateRuntime' "$server_interaction" \
    || fail "runtime removal does not invalidate server dialogue sessions"
rg -q 'function DialogueSession\.InvalidateRuntime' "$dialogue_session" \
    || fail "dialogue runtime invalidation API missing"
rg -q 'GetActiveRuntimeId' "$server_interaction" \
    || fail "server does not reject stale runtime ids before dialogue operations"
rg -q 'if isServer and isServer\(\) then' "$server_interaction" \
    || fail "interaction server event hooks are not guarded from client init"
rg -q 'if isServer and isServer\(\) then' "$dialogue_session" \
    || fail "dialogue expiry hook is not guarded from client init"

rg -q 'function QuestRegistry\.Register' "$quest_registry" \
    || fail "quest definition registry missing"
rg -q 'function QuestInstance\.Create' "$quest_instance" \
    || fail "QuestInstance creation missing"
rg -q 'function QuestInstance\.Restore' "$quest_instance" \
    || fail "persisted QuestInstance restore path missing"
rg -q 'ownerCharacterId' "$quest_instance" \
    || fail "QuestInstance is not owned by durable character identity"
rg -q 'function QuestInstance\.CompleteCurrentObjective' "$quest_instance" \
    || fail "QuestInstance objective transition missing"
for objective_type in ReachArea TalkToNPC Fetch Deliver Kill ClearArea; do
    rg -q "$objective_type = LCCQF\.QuestObjectives\.$objective_type" "$quest_instance" \
        || fail "objective handler not registered: $objective_type"
done
rg -q 'handler\.ValidatePersisted' "$quest_instance" \
    || fail "objective-specific persistence validation missing"
rg -q 'handler\.MakeProgressView' "$quest_instance" \
    || fail "objective progress projection missing"

rg -q 'function QuestService\.Initialize' "$quest_service" \
    || fail "quest persistence initialization missing"
rg -q 'function QuestService\.Accept' "$quest_service" \
    || fail "server quest accept path missing"
rg -q 'function QuestService\.NotifyTalkToNPC' "$quest_service" \
    || fail "TalkToNPC server transition missing"
rg -q 'function QuestService\.NotifyZombieDead' "$quest_service" \
    || fail "Kill objective server death transition missing"
rg -q 'handler\.EvaluateTick' "$quest_service" \
    || fail "generic tick objective dispatch missing"
rg -q 'handler\.EvaluateTalk' "$quest_service" \
    || fail "generic talk objective dispatch missing"
rg -q 'handler\.EvaluateZombieDeath' "$quest_service" \
    || fail "generic zombie-death objective dispatch missing"
rg -q 'function QuestService\.OnPlayerDeath' "$quest_service" \
    || fail "character retirement path missing"
rg -q 'function QuestService\.Tick' "$quest_service" \
    || fail "server objective update loop missing"
rg -q 'QuestPersistence\.GetQuestStore' "$quest_service" \
    || fail "QuestService does not use world-backed persistence"
rg -q 'QuestService\.Initialize' "$server_interaction" \
    || fail "server startup does not initialize quest persistence"
rg -q 'Events\.OnPlayerDeath|QuestService\.OnPlayerDeath' "$server_interaction" \
    || fail "server interaction layer does not retire dead character identities"
rg -q 'QuestService\.NotifyTalkToNPC' "$server_interaction" \
    || fail "validated dialogue interaction is not connected to talk objectives"
rg -q 'QuestService\.EvaluateCondition' "$server_interaction" \
    || fail "dialogue choice conditions are not server-authoritative"
rg -q 'QuestService\.ExecuteAction' "$server_interaction" \
    || fail "dialogue quest actions are not server-authoritative"
rg -q 'Events\.OnZombieDead\.Add' "$quest_event_bridge" \
    || fail "authoritative quest zombie death bridge missing"
rg -q 'IsFrameworkEntity' "$quest_event_bridge" \
    || fail "quest zombie death bridge does not exclude framework NPCs"
rg -q 'isChoiceAvailable' "$dialogue_session" \
    || fail "dialogue session does not revalidate conditional choices"
rg -q 'questAccept' "$dialogue_content" \
    || fail "dialogue quest offer missing"
rg -q 'condition.kind == "all"' "$quest_service" \
    || fail "composite server-authoritative dialogue conditions missing"

for objective_type in ReachArea TalkToNPC Fetch Deliver Kill ClearArea; do
    rg -q "type = \"$objective_type\"" "$quest_definitions" \
        || fail "test quest coverage missing objective type: $objective_type"
done
rg -q 'mode = "EXACT"' "$quest_definitions" \
    || fail "exact QuestMarker presentation missing"
rg -q 'mode = "AREA"' "$quest_definitions" \
    || fail "area QuestMarker presentation missing"
rg -q 'function ReachArea\.MakeMarkerView' "$objective_reach" \
    || fail "ReachArea marker projection missing"
rg -q 'function ClearArea\.MakeMarkerView' "$objective_clear" \
    || fail "ClearArea marker projection missing"
rg -q 'function Fetch\.EvaluateTick' "$objective_fetch" \
    || fail "Fetch objective evaluator missing"
rg -q 'function Deliver\.EvaluateTalk' "$objective_deliver" \
    || fail "Deliver objective talk evaluator missing"
rg -q 'ItemUtils\.Remove' "$objective_deliver" \
    || fail "Deliver objective does not consume server-validated items"
rg -q 'sendRemoveItemFromContainer' "$objective_item_utils" \
    || fail "server item removal is not replicated to inventory clients"
rg -q 'function Kill\.EvaluateZombieDeath' "$objective_kill" \
    || fail "Kill objective death evaluator missing"
rg -q 'getAttackedBy' "$objective_kill" \
    || fail "Kill objective does not use authoritative B42 attacker credit"
rg -q 'function ClearArea\.EvaluateTick' "$objective_clear" \
    || fail "ClearArea objective evaluator missing"
rg -q 'IsFrameworkEntity' "$objective_clear" \
    || fail "ClearArea does not exclude framework-owned NPC entities"
rg -q 'markerId = tostring\(instance\.id\)' "$quest_instance" \
    || fail "QuestInstance does not expose stable per-objective marker projection"
rg -q 'REQUEST_QUESTS' "$constants" \
    || fail "quest state request protocol missing"
rg -q 'QUEST_UPSERT' "$server_interaction" \
    || fail "server quest view synchronization missing"
rg -q 'QuestClientState\.Apply' "$client_interaction" \
    || fail "client sanitized quest view store not connected"

rg -q 'ModData\.getOrCreate\(C\.PERSISTENCE_TAG\)' "$character_identity" \
    || fail "B42 GlobalModData-backed world persistence missing"
rg -q 'player:getModData\(\)' "$character_identity" \
    || fail "per-character saved modData identity anchor missing"
rg -q 'getRandomUUID\(\)' "$character_identity" \
    || fail "framework character UUID creation missing"
rg -q 'retiredCharacterIds' "$character_identity" \
    || fail "retired character identity guard missing"
rg -q 'function CharacterIdentity\.Retire' "$character_identity" \
    || fail "character identity retirement API missing"
rg -q 'function QuestPersistence\.GetQuestStore' "$quest_persistence" \
    || fail "persistent quest store API missing"
rg -q 'QuestInstance\.Restore' "$quest_persistence" \
    || fail "persistent quest store does not normalize restored instances"
rg -q 'QUEST_PERSISTENCE_SCHEMA_VERSION' "$quest_persistence" \
    || fail "versioned quest persistence schema missing"

if rg -n 'getOnlineID\(' "$quest_service" "$character_identity" "$quest_persistence"; then
    fail "transient onlineID reintroduced as durable quest/character owner"
fi

rg -q 'function QuestClientState\.AddListener' "$client_quest_state" \
    || fail "quest client state change notification missing"
rg -q 'function QuestClientState\.BeginCharacterTransition' "$client_quest_state" \
    || fail "per-life client quest projection reset missing"
rg -q 'Events\.OnPlayerDeath\.Add' "$client_character_lifecycle" \
    || fail "client death projection reset hook missing"
rg -q 'Events\.OnCreatePlayer\.Add' "$client_character_lifecycle" \
    || fail "new-character authoritative quest resync hook missing"
rg -q 'LCCQF/Quest/LCCQFCharacterProjectionLifecycle' "$client_hub_bootstrap" \
    || fail "character projection lifecycle is not loaded"
rg -q 'getSymbolsAPIv2' "$client_quest_marker" \
    || fail "world-map symbols v2 adapter missing"
rg -q 'addUntranslatedText|addTexture' "$client_quest_marker" \
    || fail "quest marker renderer missing"
rg -q 'setUserDefined\(true\)' "$client_quest_marker" \
    || fail "quest marker is not visible independently of the B42 PlaceNames renderer flag"
rg -q 'clearOwnedSymbols' "$client_quest_marker" \
    || fail "quest marker stale-symbol cleanup missing"
rg -q 'countOwnedSymbols' "$client_quest_marker" \
    || fail "quest marker integrity check missing"
rg -q 'QuestClientState\.AddListener' "$client_quest_marker" \
    || fail "quest marker lifecycle is not driven by quest-state changes"
rg -q 'function Hub\.RegisterPage' "$client_hub" \
    || fail "RPG hub page registry missing"
rg -q 'IGUI_LCCQF_Hub_Tab_Quests' "$client_hub" \
    || fail "Quest hub page missing"
rg -q 'LCCQF/Quest/LCCQFQuestMarkerService' "$client_hub_bootstrap" \
    || fail "RPG hub bootstrap does not load quest marker service"
rg -q 'LCCQF/UI/LCCQFHub' "$client_hub_bootstrap" \
    || fail "RPG hub bootstrap does not load tabbed hub"

if rg -n 'QuestService\.(Accept|ExecuteAction|NotifyTalkToNPC|NotifyZombieDead|Tick)|CompleteCurrentObjective|questAccept' "$lua_root/client"; then
    fail "client-owned quest state transition reintroduced"
fi

if rg -n 'LCCQF/Persistence/' "$lua_root/client"; then
    fail "client depends directly on server persistence internals"
fi

rg -q 'Events\.OnKeyPressed\.Add' "$client_interaction" \
    || fail "interaction input is not using OnKeyPressed"

if rg -n 'Events\.OnKeyStartPressed\.Add|Events\.OnTick\.Add\(onTick\)' "$client_interaction"; then
    fail "legacy client interaction polling path reintroduced"
fi

if rg -n 'choice\.next|showNode\(' "$lua_root/client"; then
    fail "client-owned dialogue transition reintroduced"
fi

rg -q '^modversion=0\.3\.3$' "$mod_info" || fail "mod.info version mismatch"
rg -q 'Constants\.VERSION = "0\.3\.3"' "$constants" \
    || fail "Lua version mismatch"
rg -q 'Constants\.PERSISTENCE_SCHEMA_VERSION = 1' "$constants" \
    || fail "world persistence schema constant missing"
rg -q 'Constants\.QUEST_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" \
    || fail "quest persistence schema constant missing"
rg -q 'Constants\.CHARACTER_ID_MODDATA_KEY' "$constants" \
    || fail "character identity modData key missing"
rg -q 'Constants\.TEST_QUEST_2_ID' "$constants" \
    || fail "second objective-runtime test quest id missing"

if rg -n 'brain\.key\s*=\s*definition\.npcId|key\s*=\s*definition\.npcId' \
    "$lua_root/server/LCCQF/Runtime"; then
    fail "framework npcId escaped into Bandits2 numeric door-key field"
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
fi

echo "QuestFramework audit: PASS"
