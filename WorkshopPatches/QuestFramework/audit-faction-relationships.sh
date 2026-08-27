#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
faction_registry="$lua_root/shared/LCCQF/Core/LCCQFFactionRegistry.lua"
faction_definitions="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
faction_knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterFactionKnowledge.lua"
faction_relationships="$lua_root/server/LCCQF/Persistence/LCCQFCharacterFactionRelationships.lua"
faction_bridge="$lua_root/server/LCCQF/Faction/zz_LCCQFFactionRelationshipBridge.lua"
quest_service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
dialogue_content="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
faction_page="$lua_root/client/LCCQF/UI/LCCQFFactionPage.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework faction relationship audit: FAIL: $1" >&2
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
    "$faction_registry" \
    "$faction_definitions" \
    "$faction_knowledge" \
    "$faction_relationships" \
    "$faction_bridge" \
    "$quest_service" \
    "$quest_definitions" \
    "$dialogue_content" \
    "$faction_page" \
    "$translation_en" \
    "$translation_ru"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_RELATIONSHIP_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "faction relationship schema constant missing"
require_pattern 'record\.factionRelationships' "$faction_relationships" "faction relationship store is not attached to durable character record"
require_pattern 'byFactionId' "$faction_relationships" "per-faction relationship index missing"
require_pattern 'function CharacterFactionRelationships\.EnsureFaction' "$faction_relationships" "neutral faction relationship establishment API missing"
require_pattern 'function CharacterFactionRelationships\.AdjustReputation' "$faction_relationships" "faction reputation adjustment API missing"
require_pattern 'function CharacterFactionRelationships\.SetMembership' "$faction_relationships" "faction membership API missing"
require_pattern 'function CharacterFactionRelationships\.GetReputation' "$faction_relationships" "faction reputation query API missing"
require_pattern 'function CharacterFactionRelationships\.GetTier' "$faction_relationships" "faction tier query API missing"
require_pattern 'function CharacterFactionRelationships\.IsMember' "$faction_relationships" "faction membership query API missing"
require_pattern 'function CharacterFactionRelationships\.GetRankId' "$faction_relationships" "faction rank query API missing"
require_pattern 'appliedTokens' "$faction_relationships" "idempotent faction relationship tokens missing"
require_pattern 'function Registry\.GetRank' "$faction_registry" "authored faction rank lookup missing"
require_pattern 'rankId = "associate"' "$faction_definitions" "authored test faction rank missing"
require_pattern 'relationship = CharacterFactionRelationships\.ExportView' "$faction_knowledge" "sanitized faction relationship projection missing"
require_pattern 'CharacterFactionRelationships\.EnsureFaction' "$faction_knowledge" "faction discovery does not establish neutral faction relationship state"

require_pattern 'factionReward' "$quest_definitions" "quest faction rewards missing"
require_pattern 'quest-faction-completed:' "$faction_bridge" "faction quest reward idempotency token missing"
require_pattern 'quest-faction-history-reconcile' "$faction_bridge" "faction quest history reconciliation missing"
require_pattern 'QuestService\.AddEventListener' "$faction_bridge" "faction bridge does not use multi-listener quest event bus"
require_pattern 'KNOWN_FACTION_UPSERT' "$faction_bridge" "faction relationship changes do not update known faction projection"

require_pattern 'condition\.kind == "factionReputation"' "$quest_service" "faction reputation condition missing"
require_pattern 'condition\.kind == "factionTier"' "$quest_service" "faction tier condition missing"
require_pattern 'condition\.kind == "factionMembership"' "$quest_service" "faction membership condition missing"
require_pattern 'condition\.kind == "factionRank"' "$quest_service" "faction rank condition missing"
require_pattern 'action\.kind == "factionReputationDelta"' "$quest_service" "faction reputation action missing"
require_pattern 'action\.kind == "factionMembership"' "$quest_service" "faction membership action missing"
require_pattern 'resolveFactionId' "$quest_service" "generic faction target resolver missing"
require_pattern 'target == "dialogueFaction"' "$quest_service" "dialogue faction target missing"
require_pattern 'target == "giverFaction"' "$quest_service" "giver faction target missing"

require_pattern 'kind = "factionReputation"' "$dialogue_content" "authored faction reputation condition missing"
require_pattern 'kind = "factionMembership"' "$dialogue_content" "authored faction membership condition missing"
require_pattern 'kind = "factionRank"' "$dialogue_content" "authored faction rank condition missing"
require_pattern 'kind = "factionReputationDelta"' "$dialogue_content" "authored faction reputation action missing"
require_pattern 'dialogue:faction-member-topic:v1' "$dialogue_content" "idempotent authored faction dialogue token missing"

require_pattern 'view\.relationship' "$faction_page" "faction standing is not rendered in faction dossier"
require_pattern 'IGUI_LCCQF_Hub_FactionRelation_Rank' "$faction_page" "faction rank presentation missing"
require_pattern 'IGUI_LCCQF_Faction_CheckpointSurvivors_Rank_Associate' "$translation_en" "EN faction rank translation missing"
require_pattern 'IGUI_LCCQF_Faction_CheckpointSurvivors_Rank_Associate' "$translation_ru" "RU faction rank translation missing"
require_pattern 'IGUI_LCCQF_Hub_FactionRelation_Tier_Allied' "$translation_en" "EN faction standing translations missing"
require_pattern 'IGUI_LCCQF_Hub_FactionRelation_Tier_Allied' "$translation_ru" "RU faction standing translations missing"
require_pattern 'IGUI_LCCQF_Dialog_FactionMember' "$translation_en" "EN faction dialogue translation missing"
require_pattern 'IGUI_LCCQF_Dialog_FactionMember' "$translation_ru" "RU faction dialogue translation missing"

if rg -n 'SetEventSink = function' "$faction_bridge"; then
    fail "faction relationship bridge monkey-patches single quest event sink"
fi
if rg -n 'getOnlineID\(' "$faction_relationships" "$faction_bridge"; then
    fail "transient onlineID used by faction relationship persistence"
fi
if rg -n 'Bandit(Brain|Custom|Server|Zombie)|require "Bandit' "$faction_relationships" "$faction_bridge" "$faction_knowledge"; then
    fail "Bandits provider API leaked into faction domain"
fi
if rg -n 'C\.TEST_FACTION_ID|lccq_checkpoint_survivors|CheckpointSurvivors|Alexey|Алексей' "$faction_relationships" "$faction_bridge"; then
    fail "authored faction or NPC special case leaked into reusable faction relationship core"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi
if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$faction_registry]]))"
    lua -e "assert(loadfile([[$faction_knowledge]]))"
    lua -e "assert(loadfile([[$faction_relationships]]))"
    lua -e "assert(loadfile([[$faction_bridge]]))"
    lua -e "assert(loadfile([[$quest_service]]))"
    lua -e "assert(loadfile([[$dialogue_content]]))"
    lua -e "assert(loadfile([[$faction_page]]))"
fi

echo "QuestFramework faction relationship audit: PASS (per-life standing + membership + rank + generic dialogue/quest DSL)"
