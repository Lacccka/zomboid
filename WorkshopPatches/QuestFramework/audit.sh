#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"
client_interaction="$lua_root/client/LCCQF/LCCQFInteractionClient.lua"
client_quest_state="$lua_root/client/LCCQF/Quest/LCCQFQuestClientState.lua"
client_quest_marker="$lua_root/client/LCCQF/Quest/LCCQFQuestMarkerService.lua"
client_hub="$lua_root/client/LCCQF/UI/LCCQFHub.lua"
client_hub_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
legacy_client_bandits="$lua_root/client/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_interaction="$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
server_bandits_wrapper="$lua_root/server/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_bandits="$lua_root/server/LCCQF/Runtime/LCCQFBanditsServerRuntime.lua"
dialogue_session="$lua_root/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
dialogue_content="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
quest_registry="$lua_root/server/LCCQF/Quest/LCCQFQuestRegistry.lua"
quest_instance="$lua_root/server/LCCQF/Quest/LCCQFQuestInstance.lua"
quest_service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
objective_reach="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveReachArea.lua"
objective_talk="$lua_root/server/LCCQF/Quest/Objectives/LCCQFObjectiveTalkToNPC.lua"
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
    "$client_interaction" \
    "$client_quest_state" \
    "$client_quest_marker" \
    "$client_hub" \
    "$client_hub_bootstrap" \
    "$server_interaction" \
    "$server_bandits_wrapper" \
    "$server_bandits" \
    "$dialogue_session" \
    "$dialogue_content" \
    "$quest_definitions" \
    "$quest_registry" \
    "$quest_instance" \
    "$quest_service" \
    "$objective_reach" \
    "$objective_talk" \
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
    "$client_quest_marker"
    "$client_hub"
    "$client_hub_bootstrap"
    "$lua_root/client/LCCQF/UI/LCCQFDialoguePanel.lua"
    "$server_interaction"
    "$dialogue_session"
    "$quest_registry"
    "$quest_instance"
    "$quest_service"
    "$objective_reach"
    "$objective_talk"
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
rg -q 'function QuestInstance\.CompleteCurrentObjective' "$quest_instance" \
    || fail "QuestInstance objective transition missing"
rg -q 'function QuestService\.Accept' "$quest_service" \
    || fail "server quest accept path missing"
rg -q 'function QuestService\.NotifyTalkToNPC' "$quest_service" \
    || fail "TalkToNPC server transition missing"
rg -q 'function QuestService\.Tick' "$quest_service" \
    || fail "server objective update loop missing"
rg -q 'QuestService\.NotifyTalkToNPC' "$server_interaction" \
    || fail "validated dialogue interaction is not connected to TalkToNPC objectives"
rg -q 'QuestService\.EvaluateCondition' "$server_interaction" \
    || fail "dialogue choice conditions are not server-authoritative"
rg -q 'QuestService\.ExecuteAction' "$server_interaction" \
    || fail "dialogue quest actions are not server-authoritative"
rg -q 'isChoiceAvailable' "$dialogue_session" \
    || fail "dialogue session does not revalidate conditional choices"
rg -q 'questAccept' "$dialogue_content" \
    || fail "first dialogue quest offer missing"
rg -q 'type = "ReachArea"' "$quest_definitions" \
    || fail "first ReachArea objective missing"
rg -q 'type = "TalkToNPC"' "$quest_definitions" \
    || fail "first return-to-NPC objective missing"
rg -q 'mode = "EXACT"' "$quest_definitions" \
    || fail "first authored QuestMarker presentation missing"
rg -q 'function ReachArea\.MakeMarkerView' "$objective_reach" \
    || fail "ReachArea marker projection missing"
rg -q 'markerId = tostring\(instance\.id\)' "$quest_instance" \
    || fail "QuestInstance does not expose stable per-objective marker projection"
rg -q 'REQUEST_QUESTS' "$constants" \
    || fail "quest state request protocol missing"
rg -q 'QUEST_UPSERT' "$server_interaction" \
    || fail "server quest view synchronization missing"
rg -q 'QuestClientState\.Apply' "$client_interaction" \
    || fail "client sanitized quest view store not connected"

rg -q 'function QuestClientState\.AddListener' "$client_quest_state" \
    || fail "quest client state change notification missing"
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

if rg -n 'QuestService\.(Accept|ExecuteAction|NotifyTalkToNPC|Tick)|CompleteCurrentObjective|questAccept' "$lua_root/client"; then
    fail "client-owned quest state transition reintroduced"
fi

rg -q 'Events\.OnKeyPressed\.Add' "$client_interaction" \
    || fail "interaction input is not using OnKeyPressed"

if rg -n 'Events\.OnKeyStartPressed\.Add|Events\.OnTick\.Add\(onTick\)' "$client_interaction"; then
    fail "legacy client interaction polling path reintroduced"
fi

if rg -n 'choice\.next|showNode\(' "$lua_root/client"; then
    fail "client-owned dialogue transition reintroduced"
fi

rg -q '^modversion=0\.3\.1$' "$mod_info" || fail "mod.info version mismatch"
rg -q 'Constants\.VERSION = "0\.3\.1"' "$constants" \
    || fail "Lua version mismatch"

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
