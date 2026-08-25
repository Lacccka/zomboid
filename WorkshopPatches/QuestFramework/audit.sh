#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"
mod_info="$project_root/Contents/mods/LaccckaQuestFramework/42/mod.info"

fail() {
    echo "QuestFramework audit: FAIL: $1" >&2
    exit 1
}

for required in \
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua" \
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua" \
    "$lua_root/client/LCCQF/Runtime/LCCQFBanditsRuntime.lua" \
    "$lua_root/server/LCCQF/Runtime/LCCQFBanditsRuntime.lua" \
    "$lua_root/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
do
    [[ -f "$required" ]] || fail "missing $required"
done

non_runtime_files=(
    "$lua_root/client/LCCQF/LCCQFInteractionClient.lua"
    "$lua_root/client/LCCQF/UI/LCCQFDialoguePanel.lua"
    "$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRegistry.lua"
    "$lua_root/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
)

if rg -n 'Bandit(Brain|Custom|Server)|require "Bandit"' "${non_runtime_files[@]}"; then
    fail "Bandits API escaped the runtime adapter boundary"
fi

if rg -n 'getZombieList\(' "$lua_root"; then
    fail "broad zombie scan reintroduced"
fi

if rg -n 'if\s+not\s+object:getVariableBoolean\("Bandit"\)' "$lua_root/client/LCCQF/Runtime"; then
    fail "transient Bandit animation variable reintroduced as quest identity gate"
fi

if rg -n 'choice\.next|showNode\(' "$lua_root/client"; then
    fail "client-owned dialogue transition reintroduced"
fi

rg -q '^modversion=0\.2\.5$' "$mod_info" || fail "mod.info version mismatch"
rg -q 'Constants\.VERSION = "0\.2\.5"' "$lua_root/shared/LCCQF/LCCQFConstants.lua" \
    || fail "Lua version mismatch"

if rg -n 'brain\.key\s*=\s*definition\.npcId|key\s*=\s*definition\.npcId' \
    "$lua_root/client/LCCQF/Runtime" "$lua_root/server/LCCQF/Runtime"; then
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
