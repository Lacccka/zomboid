#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua="$root/Contents/mods/LaccckaQuestFramework/42/media/lua"
registry="$lua/shared/LCCQF/Core/LCCQFNPCRegistry.lua"
runtime="$lua/shared/LCCQF/Core/LCCQFNPCRuntime.lua"
dialogue_registry="$lua/server/LCCQF/Dialogue/LCCQFDialogueRegistry.lua"
dialogue_content="$lua/server/LCCQF/Dialogue/LCCQFFactionResidentDialogue.lua"
dialogue_session="$lua/server/LCCQF/Dialogue/LCCQFDialogueSession.lua"
bridge="$lua/server/LCCQF/FactionWorld/LCCQFFactionNPCBridge.lua"
projection="$lua/server/LCCQF/Runtime/LCCQFBanditsFactionNPCProjection.lua"
bootstrap="$lua/server/zz_LCCQFFactionBootstrap.lua"
interaction_client="$lua/client/LCCQF/LCCQFInteractionClient.lua"
interaction_server="$lua/server/LCCQF/LCCQFInteractionServer.lua"

fail(){ echo "QuestFramework faction NPC audit: FAIL: $1" >&2; exit 1; }
req(){ rg -q "$1" "$2" || fail "$3"; }
for f in "$registry" "$runtime" "$dialogue_registry" "$dialogue_content" "$dialogue_session" "$bridge" "$projection" "$bootstrap" "$interaction_client" "$interaction_server"; do [[ -f "$f" ]] || fail "missing $f"; done

req 'function Registry\.RegisterGenerated' "$registry" 'generated logical NPC registration missing'
req 'npcId belongs to authored definition' "$registry" 'generated definitions can overwrite authored NPCs'
req 'function Registry\.ApplyPublicDefinition' "$registry" 'client public definition projection missing'
req 'function Registry\.MakePublicDefinition' "$registry" 'server public definition sanitizer missing'

req 'publicDefinition' "$runtime" 'runtime binding omits public definition'
req 'ApplyPublicDefinition' "$runtime" 'client binding cannot learn generated NPC'
req 'definition\.spawnable == false' "$runtime" 'generated faction residents can be manually spawned through common runtime'

req 'function Registry\.Register' "$dialogue_registry" 'generated dialogue registry missing'
req 'Static\.Get' "$dialogue_registry" 'authored dialogue fallback disappeared'
req 'lccq_faction_resident_generic' "$dialogue_content" 'generic faction resident dialogue missing'
req 'LCCQF/Dialogue/LCCQFDialogueRegistry' "$dialogue_session" 'dialogue session bypasses overlay registry'

req 'NPCRegistry\.RegisterGenerated' "$bridge" 'population is not promoted into common NPC registry'
req 'spawnable = false' "$bridge" 'generated faction NPC is spawnable'
req 'dialogueId = "lccq_faction_resident_generic"' "$bridge" 'generated faction NPC has no generic dialogue'
req 'Runtime\.BindRuntime' "$bridge" 'faction NPC bypasses common runtime bindings'
req 'RUNTIME_BINDING_UPSERT' "$bridge" 'live generated binding is not projected to clients'
req 'RUNTIME_BINDING_REMOVE' "$bridge" 'generated binding removal is not projected'
req 'publicDefinition = definition' "$bridge" 'generated live upsert omits public definition'

# Public definition must contain only physical interaction metadata. These fields may exist
# in the server logical definition, but never inside MakePublicDefinition's returned table.
public_block="$(sed -n '/function Registry.MakePublicDefinition/,/^end$/p' "$registry")"
if grep -Eq 'factionId|factionSiteId|factionRoleId|siteId|roleId|population' <<<"$public_block"; then
    fail 'public NPC definition leaks faction world state'
fi
req 'runtimeAdapter = definition\.runtime' "$registry" 'public definition does not expose runtime adapter'

req 'FACTION_SITE_RUNTIME_SCAN_MAX_TILES' "$projection" 'physical generated-NPC scan is not bounded'
req 'member\.state == "MATERIALIZED"' "$projection" 'non-materialized residents can be published as physical'
req 'brain\.lccqNpcId' "$projection" 'provider projection ignores logical npcId tag'
req 'tostring\(brain\.id\) == tostring\(member\.runtimeId\)' "$projection" 'provider projection ignores authoritative runtimeId'
req 'Bridge\.BindPhysical' "$projection" 'provider projection bypasses common bridge'
req 'Bridge\.UnbindPhysical' "$projection" 'provider lifecycle cannot remove interaction binding'
if rg -n 'getZombieList\(' "$projection"; then fail 'generated NPC projection reintroduced broad zombie list scan'; fi

req 'LCCQF/FactionWorld/LCCQFFactionNPCBridge' "$bootstrap" 'logical faction NPC bridge not bootstrapped'
req 'LCCQF/Runtime/LCCQFBanditsFactionNPCProjection' "$bootstrap" 'Bandits faction NPC projection not bootstrapped'

# The existing client/server interaction path must remain the actual interaction mechanism.
req 'NPCRuntime\.FindNearestInteractive' "$interaction_client" 'client common proximity path missing'
req 'NPCRuntime\.ResolveForPlayer' "$interaction_server" 'server common proximity validation missing'
req 'DialogueSession\.Open' "$interaction_server" 'common dialogue session path missing'

core_files=("$registry" "$runtime" "$dialogue_registry" "$dialogue_content" "$dialogue_session" "$bridge")
if rg -n 'BanditServer|BanditCustom|BanditBrain|BanditClusters|Bandits2' "${core_files[@]}"; then
    fail 'provider-neutral generated NPC core leaks Bandits dependency'
fi
if rg -n 'sendClientCommand|OnClientCommand' "$bridge" "$dialogue_registry"; then
    fail 'client can author generated NPC definitions'
fi

if command -v lua >/dev/null 2>&1; then
    for f in "$registry" "$runtime" "$dialogue_registry" "$dialogue_content" "$dialogue_session" "$bridge" "$projection"; do
        lua -e "assert(loadfile([[$f]]))"
    done
fi

echo 'QuestFramework faction NPC audit: PASS (generated logical NPCs + sanitized public projection + common E/dialogue runtime)'
