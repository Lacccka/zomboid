#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
faction_registry="$lua_root/shared/LCCQF/Core/LCCQFFactionRegistry.lua"
faction_definitions="$lua_root/shared/LCCQF/Content/LCCQFFactionDefinitions.lua"
npc_definitions="$lua_root/shared/LCCQF/Content/LCCQFNPCDefinitions.lua"
faction_knowledge="$lua_root/server/LCCQF/Persistence/LCCQFCharacterFactionKnowledge.lua"
faction_bridge="$lua_root/server/LCCQF/Faction/zz_LCCQFFactionDiscoveryBridge.lua"
server_bootstrap="$lua_root/server/zz_LCCQFFactionBootstrap.lua"
client_state="$lua_root/client/LCCQF/Faction/LCCQFKnownFactionsClientState.lua"
client_lifecycle="$lua_root/client/LCCQF/Faction/LCCQFCharacterFactionKnowledgeLifecycle.lua"
faction_page="$lua_root/client/LCCQF/UI/LCCQFFactionPage.lua"
client_bootstrap="$lua_root/client/LCCQF/zz_LCCQFRPGHubBootstrap.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework faction audit: FAIL: $1" >&2
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
    "$npc_definitions" \
    "$faction_knowledge" \
    "$faction_bridge" \
    "$server_bootstrap" \
    "$client_state" \
    "$client_lifecycle" \
    "$faction_page" \
    "$client_bootstrap" \
    "$translation_en" \
    "$translation_ru"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'FACTION_KNOWLEDGE_PERSISTENCE_SCHEMA_VERSION = 1' "$constants" "faction knowledge schema missing"
require_pattern 'REQUEST_FACTIONS = "RequestFactions"' "$constants" "faction snapshot request command missing"
require_pattern 'KNOWN_FACTION_UPSERT = "KnownFactionUpsert"' "$constants" "faction incremental command missing"

require_pattern 'function Registry\.Register' "$faction_registry" "faction registry registration API missing"
require_pattern 'definition\.factionId' "$faction_registry" "faction registry identity validation missing"
require_pattern 'function Registry\.Get' "$faction_registry" "faction registry lookup missing"
require_pattern 'knowledgeFacts' "$faction_registry" "faction knowledge fact validation missing"
require_pattern 'FactionRegistry\.Register' "$faction_definitions" "authored faction definition missing"
require_pattern 'factionId = C\.TEST_FACTION_ID' "$npc_definitions" "NPC faction affiliation missing"
require_pattern 'revealFactionOnDiscovery = true' "$npc_definitions" "authored faction reveal policy missing"

require_pattern 'record\.factionKnowledge' "$faction_knowledge" "faction knowledge is not stored on durable character record"
require_pattern 'knownFactions' "$faction_knowledge" "per-life known faction index missing"
require_pattern 'function CharacterFactionKnowledge\.DiscoverFaction' "$faction_knowledge" "faction discovery API missing"
require_pattern 'function CharacterFactionKnowledge\.UnlockFact' "$faction_knowledge" "faction fact unlock API missing"
require_pattern 'function CharacterFactionKnowledge\.IsKnown' "$faction_knowledge" "known faction query missing"
require_pattern 'function CharacterFactionKnowledge\.ExportViews' "$faction_knowledge" "sanitized faction projection missing"
require_pattern 'FactionRegistry\.Get\(factionId\)' "$faction_knowledge" "faction discovery does not validate registry identity"

require_pattern 'CharacterKnowledge\.DiscoverNPC = function' "$faction_bridge" "NPC-to-faction discovery bridge missing"
require_pattern 'definition\.revealFactionOnDiscovery ~= true' "$faction_bridge" "faction reveal is not controlled by authored policy"
require_pattern 'definition\.factionId' "$faction_bridge" "faction reveal does not use NPC affiliation"
require_pattern 'CharacterFactionKnowledge\.DiscoverFaction' "$faction_bridge" "NPC discovery does not reveal faction through faction service"
require_pattern 'C\.COMMAND\.REQUEST_FACTIONS' "$faction_bridge" "server faction snapshot route missing"
require_pattern 'C\.COMMAND\.KNOWN_FACTION_UPSERT' "$faction_bridge" "server faction incremental route missing"
require_pattern 'LCCQF/Faction/zz_LCCQFFactionDiscoveryBridge' "$server_bootstrap" "faction server bootstrap missing"

require_pattern 'function KnownFactionsClientState\.BeginCharacterTransition' "$client_state" "known factions character reset missing"
require_pattern 'function KnownFactionsClientState\.Replace' "$client_state" "known factions snapshot API missing"
require_pattern 'function KnownFactionsClientState\.Apply' "$client_state" "known factions upsert API missing"
require_pattern 'Events\.OnPlayerDeath\.Add' "$client_lifecycle" "known factions death reset missing"
require_pattern 'Events\.OnCreatePlayer\.Add' "$client_lifecycle" "known factions new-life reset missing"
require_pattern 'REQUEST_FACTIONS' "$client_lifecycle" "known factions resync request missing"
require_pattern 'KNOWN_FACTION_UPSERT' "$client_lifecycle" "known factions incremental receive path missing"

require_pattern 'LCCQFKnownFactionsPage' "$faction_page" "known factions Hub page missing"
require_pattern 'id = "factions"' "$faction_page" "factions Hub tab missing"
require_pattern 'KnownFactions\.ListAll' "$faction_page" "faction Hub page is not driven by sanitized projection"
require_pattern 'function Hub\.OpenFaction' "$faction_page" "future faction cross-navigation API missing"
require_pattern 'LCCQF/Faction/LCCQFKnownFactionsClientState' "$client_bootstrap" "known faction state not bootstrapped"
require_pattern 'LCCQF/Faction/LCCQFCharacterFactionKnowledgeLifecycle' "$client_bootstrap" "faction lifecycle not bootstrapped"
require_pattern 'LCCQF/UI/LCCQFFactionPage' "$client_bootstrap" "faction Hub page not bootstrapped"

require_pattern 'IGUI_LCCQF_Hub_Tab_Factions' "$translation_en" "EN faction Hub translations missing"
require_pattern 'IGUI_LCCQF_Hub_Tab_Factions' "$translation_ru" "RU faction Hub translations missing"
require_pattern 'IGUI_LCCQF_Faction_CheckpointSurvivors' "$translation_en" "EN authored faction content missing"
require_pattern 'IGUI_LCCQF_Faction_CheckpointSurvivors' "$translation_ru" "RU authored faction content missing"

if rg -n 'getOnlineID\(' "$faction_knowledge" "$faction_bridge"; then
    fail "transient onlineID used by faction knowledge persistence"
fi
if rg -n 'Bandit(Brain|Custom|Server|Zombie)|require "Bandit"' "$faction_registry" "$faction_knowledge" "$faction_bridge"; then
    fail "Bandits provider API leaked into faction domain"
fi
if rg -n 'C\.TEST_FACTION_ID|CheckpointSurvivors|Alexey|Алексей' "$faction_registry" "$faction_knowledge" "$faction_bridge"; then
    fail "authored faction special case leaked into reusable faction core"
fi
if rg -n 'LCCQFFactionRegistry|LCCQFFactionDefinitions' "$client_state" "$client_lifecycle" "$faction_page"; then
    fail "client faction projection can access hidden world faction registry"
fi
if rg -n 'GetDefinitions\(' "$client_state" "$client_lifecycle" "$faction_page"; then
    fail "full faction registry leaked into client projection"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi
if command -v lua >/dev/null 2>&1; then
    for lua_file in \
        "$faction_registry" \
        "$faction_definitions" \
        "$npc_definitions" \
        "$faction_knowledge" \
        "$faction_bridge" \
        "$server_bootstrap" \
        "$client_state" \
        "$client_lifecycle" \
        "$faction_page"; do
        lua -e "assert(loadfile([[$lua_file]]))"
    done
fi

echo "QuestFramework faction audit: PASS (registry + per-life discovery + sanitized Known Factions Hub projection)"
