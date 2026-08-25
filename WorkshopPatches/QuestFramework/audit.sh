#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"
client_interaction="$lua_root/client/LCCQF/LCCQFInteractionClient.lua"
legacy_client_bandits="$lua_root/client/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_interaction="$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
server_bandits_wrapper="$lua_root/server/LCCQF/Runtime/LCCQFBanditsRuntime.lua"
server_bandits="$lua_root/server/LCCQF/Runtime/LCCQFBanditsServerRuntime.lua"
dialogue_session="$lua_root/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
shared_runtime="$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"

fail() {
    echo "QuestFramework audit: FAIL: $1" >&2
    exit 1
}

for required in \
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua" \
    "$shared_runtime" \
    "$client_interaction" \
    "$server_interaction" \
    "$server_bandits_wrapper" \
    "$server_bandits" \
    "$dialogue_session"
do
    [[ -f "$required" ]] || fail "missing $required"
done

[[ ! -e "$legacy_client_bandits" ]] \
    || fail "legacy client/server Bandits runtime module-name collision reintroduced"

non_runtime_files=(
    "$client_interaction"
    "$lua_root/client/LCCQF/UI/LCCQFDialoguePanel.lua"
    "$server_interaction"
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

rg -q 'Events\.OnKeyPressed\.Add' "$client_interaction" \
    || fail "interaction input is not using OnKeyPressed"

if rg -n 'Events\.OnKeyStartPressed\.Add|Events\.OnTick\.Add\(onTick\)' "$client_interaction"; then
    fail "legacy client interaction polling path reintroduced"
fi

if rg -n 'choice\.next|showNode\(' "$lua_root/client"; then
    fail "client-owned dialogue transition reintroduced"
fi

rg -q '^modversion=0\.2\.9$' "$mod_info" || fail "mod.info version mismatch"
rg -q 'Constants\.VERSION = "0\.2\.9"' "$constants" \
    || fail "Lua version mismatch"

if rg -n 'brain\.key\s*=\s*definition\.npcId|key\s*=\s*definition\.npcId' \
    "$lua_root/server/LCCQF/Runtime"; then
    fail "framework npcId escaped into Bandits2 numeric door-key field"
fi

if LC_ALL=C rg -n '[^\x00-\x7F]' "$lua_root" -g '*.lua'; then
    fail "non-ASCII user-facing text reintroduced into Lua/network payloads"
fi

if command -v lua >/dev/null 2>&1; then
    find "$lua_root" -type f -name '*.lua' -print0 | while IFS= read -r -d '' lua_file; do
        lua -e "assert(loadfile([[$lua_file]]))"
    done
fi

echo "QuestFramework audit: PASS"
