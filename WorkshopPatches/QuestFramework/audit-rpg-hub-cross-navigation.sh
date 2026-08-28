#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterKnowledge.lua"
faction_bridge="$lua_root/server/LCCQF/Faction/zz_LCCQFFactionDiscoveryBridge.lua"
person_faction="$lua_root/client/LCCQF/Faction/LCCQFKnownPeopleFactionPresentation.lua"
faction_members="$lua_root/client/LCCQF/Faction/LCCQFFactionKnownMembersPresentation.lua"
bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework RPG Hub cross-navigation audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in "$knowledge" "$faction_bridge" "$person_faction" "$faction_members" "$bootstrap" "$translation_en" "$translation_ru"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'function CharacterKnowledge\.AddViewDecorator' "$knowledge" "known person sanitized view decorator API missing"
require_pattern 'function CharacterKnowledge\.RemoveViewDecorator' "$knowledge" "known person view decorator removal API missing"
require_pattern 'pcall\(decorator' "$knowledge" "view decorators are not isolated"
require_pattern 'CharacterFactionKnowledge\.IsKnown\(player, factionId\)' "$faction_bridge" "NPC faction link is not gated by per-life faction discovery"
require_pattern 'view\.faction = \{' "$faction_bridge" "sanitized NPC faction projection missing"
require_pattern 'KNOWN_PERSON_UPSERT' "$faction_bridge" "new faction discovery does not refresh known person projections"

require_pattern 'KnownFactions\.Get\(factionId\)' "$person_faction" "person-to-faction link is not gated by known faction client state"
require_pattern 'Hub\.OpenFaction' "$person_faction" "person-to-faction navigation missing"
require_pattern 'KnownPeople\.ListAll\(\)' "$faction_members" "faction known-member list is not derived from known people"
require_pattern 'person\.faction\.factionId == factionId' "$faction_members" "faction known-member filter missing"
require_pattern 'Hub\.OpenPerson' "$faction_members" "faction-to-person navigation missing"

require_pattern 'LCCQF/Faction/LCCQFKnownPeopleFactionPresentation' "$bootstrap" "person-to-faction presentation not bootstrapped"
require_pattern 'LCCQF/Faction/LCCQFFactionKnownMembersPresentation' "$bootstrap" "faction known-member presentation not bootstrapped"

require_pattern 'IGUI_LCCQF_Hub_Person_Faction' "$translation_en" "EN person faction label missing"
require_pattern 'IGUI_LCCQF_Hub_Person_Faction' "$translation_ru" "RU person faction label missing"
require_pattern 'IGUI_LCCQF_Hub_Faction_KnownMembers' "$translation_en" "EN known faction members label missing"
require_pattern 'IGUI_LCCQF_Hub_Faction_KnownMembers' "$translation_ru" "RU known faction members label missing"

if rg -n 'LCCQF/Core/LCCQFFactionRegistry|LCCQF/Content/LCCQFFactionDefinitions' "$person_faction" "$faction_members"; then
    fail "client cross-navigation reads full faction registry/content instead of sanitized projections"
fi
if rg -n 'LCCQF/Persistence/' "$person_faction" "$faction_members"; then
    fail "client cross-navigation depends on server persistence internals"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi
if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$knowledge]]))"
    lua -e "assert(loadfile([[$faction_bridge]]))"
    lua -e "assert(loadfile([[$person_faction]]))"
    lua -e "assert(loadfile([[$faction_members]]))"
    lua -e "assert(loadfile([[$bootstrap]]))"
fi

echo "QuestFramework RPG Hub cross-navigation audit: PASS (discovery-gated NPC/faction links + known-member privacy)"
