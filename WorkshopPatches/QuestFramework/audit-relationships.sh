#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
character_knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterKnowledge.lua"
relationships="$lua_root/server/LCCQF/Persistence/LCCQFCharacterRelationships.lua"
relationship_bridge="$lua_root/server/LCCQF/Relationship/zz_LCCQFCharacterRelationshipBridge.lua"
server_bootstrap="$lua_root/server/zz_LCCQFRelationshipBootstrap.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
client_presentation="$lua_root/client/LCCQF/Relationship/LCCQFKnownPeopleRelationshipPresentation.lua"
client_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework relationship audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in \
    "$constants" \
    "$character_knowledge" \
    "$relationships" \
    "$relationship_bridge" \
    "$server_bootstrap" \
    "$quest_definitions" \
    "$client_presentation" \
    "$client_bootstrap" \
    "$translation_en" \
    "$translation_ru"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "relationship schema constant missing"
require_pattern 'record\.relationships' "$relationships" "relationship store is not attached to durable character record"
require_pattern 'byNPCId' "$relationships" "per-NPC relationship index missing"
require_pattern 'function CharacterRelationships\.EnsureNPC' "$relationships" "neutral relationship establishment API missing"
require_pattern 'function CharacterRelationships\.Adjust' "$relationships" "server relationship adjustment API missing"
require_pattern 'appliedTokens' "$relationships" "idempotent relationship tokens missing"
require_pattern 'quest-completed:' "$relationship_bridge" "quest reward idempotency token missing"
require_pattern 'quest-history-reconcile' "$relationship_bridge" "completed quest history reconciliation missing"
require_pattern 'QuestService\.SetEventSink = function' "$relationship_bridge" "live quest completion observer missing"
require_pattern 'IGUI_LCCQF_QuestEvent_Completed' "$relationship_bridge" "relationship bridge is not gated on completed quest events"
require_pattern 'CharacterRelationships\.SetEventSink' "$relationship_bridge" "relationship projection event sink missing"
require_pattern 'KNOWN_PERSON_UPSERT' "$relationship_bridge" "relationship changes do not update Known People projection"
require_pattern 'CharacterRelationships\.EnsureNPC' "$character_knowledge" "NPC discovery does not establish relationship state"
require_pattern 'relationship = CharacterRelationships\.ExportView' "$character_knowledge" "sanitized relationship view missing from Known People"
require_pattern 'function CharacterKnowledge\.GetView' "$character_knowledge" "single-person sanitized projection API missing"
require_pattern 'relationshipReward' "$quest_definitions" "test quest relationship rewards missing"
require_pattern 'trust = 8' "$quest_definitions" "checkpoint trust reward missing"
require_pattern 'reputation = 20' "$quest_definitions" "supply reputation reward missing"
require_pattern 'LCCQF/Relationship/zz_LCCQFCharacterRelationshipBridge' "$server_bootstrap" "relationship server bootstrap missing"
require_pattern 'view\.relationship' "$client_presentation" "Known People relationship presentation missing"
require_pattern 'IGUI_LCCQF_Hub_Person_Relationship' "$client_presentation" "relationship dossier heading missing"
require_pattern 'LCCQF/Relationship/LCCQFKnownPeopleRelationshipPresentation' "$client_bootstrap" "relationship client presentation not bootstrapped"
require_pattern 'IGUI_LCCQF_Hub_Relation_Tier_Friendly' "$translation_en" "EN relationship tier translations missing"
require_pattern 'IGUI_LCCQF_Hub_Relation_Tier_Friendly' "$translation_ru" "RU relationship tier translations missing"

if rg -n 'getOnlineID\(' "$relationships" "$relationship_bridge"; then
    fail "transient onlineID used by relationship persistence"
fi
if rg -n 'Bandit(Brain|Custom|Server|Zombie)|require "Bandit"' "$relationships" "$relationship_bridge"; then
    fail "Bandits provider API leaked into relationship domain"
fi
if rg -n 'LCCQF/Persistence/' "$client_presentation"; then
    fail "client relationship UI depends on persistence internals"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi
if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$relationships]]))"
    lua -e "assert(loadfile([[$relationship_bridge]]))"
    lua -e "assert(loadfile([[$server_bootstrap]]))"
    lua -e "assert(loadfile([[$client_presentation]]))"
fi

echo "QuestFramework relationship audit: PASS (per-life NPC relationships + quest outcome reconciliation)"
