#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
character_knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterKnowledge.lua"
relationships="$lua_root/server/LCCQF/Persistence/LCCQFCharacterRelationships.lua"
relationship_bridge="$lua_root/server/LCCQF/Relationship/zz_LCCQFCharacterRelationshipBridge.lua"
server_bootstrap="$lua_root/server/zz_LCCQFRelationshipBootstrap.lua"
quest_service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
quest_definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
dialogue_content="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
interaction_server="$lua_root/server/LCCQF/LCCQFInteractionServer.lua"
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
    "$quest_service" \
    "$quest_definitions" \
    "$dialogue_content" \
    "$interaction_server" \
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
require_pattern 'function CharacterRelationships\.SetFlag' "$relationships" "server relationship flag API missing"
require_pattern 'function CharacterRelationships\.GetStat' "$relationships" "relationship stat query API missing"
require_pattern 'function CharacterRelationships\.GetTier' "$relationships" "relationship tier query API missing"
require_pattern 'function CharacterRelationships\.GetFlag' "$relationships" "relationship flag query API missing"
require_pattern 'entry\.flags' "$relationships" "relationship story flag storage missing"
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

# Generic condition/action DSL. No authored NPC may be encoded in core evaluation.
require_pattern 'function QuestService\.EvaluateCondition\(player, condition, context\)' "$quest_service" "context-aware condition API missing"
require_pattern 'condition\.kind == "relationship"' "$quest_service" "numeric relationship condition missing"
require_pattern 'condition\.kind == "relationshipTier"' "$quest_service" "relationship tier condition missing"
require_pattern 'condition\.kind == "relationshipFlag"' "$quest_service" "relationship flag condition missing"
require_pattern 'condition\.kind == "any"' "$quest_service" "generic any condition composition missing"
require_pattern 'condition\.kind == "not"' "$quest_service" "generic not condition composition missing"
require_pattern 'action\.kind == "relationshipDelta"' "$quest_service" "relationship delta dialogue action missing"
require_pattern 'action\.kind == "relationshipFlag"' "$quest_service" "relationship flag dialogue action missing"
require_pattern 'action\.kind == "all"' "$quest_service" "composite dialogue action missing"
require_pattern 'definition\.acceptCondition' "$quest_service" "quest-level prerequisite enforcement missing"
require_pattern 'resolveRelationshipNpcId' "$quest_service" "generic relationship target resolver missing"
require_pattern 'target == "dialogueNpc"' "$quest_service" "dialogue NPC target missing"
require_pattern 'target == "giverNpc"' "$quest_service" "quest giver target missing"
require_pattern 'dialogueNpcId = session\.npcId' "$interaction_server" "dialogue context does not publish logical NPC identity"
require_pattern 'QuestService\.EvaluateCondition' "$interaction_server" "dialogue availability does not use server condition engine"

# Authored test content must exercise the generic contracts end-to-end.
require_pattern 'relationshipReward' "$quest_definitions" "test quest relationship rewards missing"
require_pattern 'trust = 8' "$quest_definitions" "checkpoint trust reward missing"
require_pattern 'reputation = 20' "$quest_definitions" "supply reputation reward missing"
require_pattern 'acceptCondition' "$quest_definitions" "test quest relationship prerequisite missing"
require_pattern 'target = "giverNpc"' "$quest_definitions" "test quest prerequisite is not giver-neutral"
require_pattern 'kind = "relationship"' "$dialogue_content" "relationship-aware dialogue choice missing"
require_pattern 'target = "dialogueNpc"' "$dialogue_content" "dialogue relationship condition is not NPC-neutral"
require_pattern 'kind = "relationshipFlag"' "$dialogue_content" "relationship story flag dialogue path missing"
require_pattern 'kind = "relationshipDelta"' "$dialogue_content" "relationship-changing dialogue action missing"
require_pattern 'dialogue:trust-topic:v1' "$dialogue_content" "idempotent dialogue relationship token missing"
require_pattern 'trust_topic_acknowledged' "$dialogue_content" "relationship flag consumer missing"

require_pattern 'LCCQF/Relationship/zz_LCCQFCharacterRelationshipBridge' "$server_bootstrap" "relationship server bootstrap missing"
require_pattern 'view\.relationship' "$client_presentation" "Known People relationship presentation missing"
require_pattern 'IGUI_LCCQF_Hub_Person_Relationship' "$client_presentation" "relationship dossier heading missing"
require_pattern 'LCCQF/Relationship/LCCQFKnownPeopleRelationshipPresentation' "$client_bootstrap" "relationship client presentation not bootstrapped"
require_pattern 'IGUI_LCCQF_Hub_Relation_Tier_Friendly' "$translation_en" "EN relationship tier translations missing"
require_pattern 'IGUI_LCCQF_Hub_Relation_Tier_Friendly' "$translation_ru" "RU relationship tier translations missing"
require_pattern 'IGUI_LCCQF_Dialog_Trust' "$translation_en" "EN trust dialogue translation missing"
require_pattern 'IGUI_LCCQF_Dialog_Trust' "$translation_ru" "RU trust dialogue translation missing"

if rg -n 'getOnlineID\(' "$relationships" "$relationship_bridge"; then
    fail "transient onlineID used by relationship persistence"
fi
if rg -n 'Bandit(Brain|Custom|Server|Zombie)|require "Bandit"' "$relationships" "$relationship_bridge" "$quest_service"; then
    fail "Bandits provider API leaked into relationship domain"
fi
if rg -n 'C\.TEST_NPC_ID|lccq_test_npc_01|Alexey|Алексей' "$relationships" "$relationship_bridge" "$quest_service" "$interaction_server"; then
    fail "authored NPC special case leaked into reusable relationship core"
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
    lua -e "assert(loadfile([[$quest_service]]))"
    lua -e "assert(loadfile([[$dialogue_content]]))"
    lua -e "assert(loadfile([[$interaction_server]]))"
    lua -e "assert(loadfile([[$client_presentation]]))"
fi

echo "QuestFramework relationship audit: PASS (generic NPC relationships + server-authoritative dialogue/quest conditions + story flags)"
