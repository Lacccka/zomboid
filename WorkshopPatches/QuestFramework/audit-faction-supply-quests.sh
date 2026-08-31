#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
INSTANCE="$MOD/server/LCCQF/Quest/LCCQFQuestInstance.lua"
OBJECTIVE="$MOD/server/LCCQF/Quest/Objectives/LCCQFObjectiveSettlementSupply.lua"
BRIDGE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge.lua"

fail() {
    echo "[faction-supply-quest-audit] ERROR: $*" >&2
    exit 1
}

[[ -f "$OBJECTIVE" ]] || fail "SettlementSupply objective missing"
[[ -f "$BRIDGE" ]] || fail "supply quest bridge missing"

rg -q 'LCCQFObjectiveSettlementSupply' "$INSTANCE" \
    || fail "QuestInstance does not load SettlementSupply objective"
rg -q 'SettlementSupply = LCCQF\.QuestObjectives\.SettlementSupply' "$INSTANCE" \
    || fail "QuestInstance handler map missing SettlementSupply"

rg -q 'signal\.openEpoch' "$BRIDGE" \
    || fail "offer identity must derive from openEpoch"
rg -q 'operations\.questOffers' "$BRIDGE" \
    || fail "persistent site offer history missing"
rg -q 'registerHistoricalDefinitions' "$BRIDGE" \
    || fail "historical dynamic definitions are not reconstructed"
rg -q 'QuestRegistry\.Register\(definition\)' "$BRIDGE" \
    || fail "generated quests do not use common QuestRegistry"
rg -q 'giverNpcId = giver\.npcId' "$BRIDGE" \
    || fail "offer does not bind to logical faction NPC identity"
rg -q 'status = signal\.status == "OPEN" and "OPEN" or "RESOLVED"' "$BRIDGE" \
    || fail "offer lifecycle is not driven by settlement need state"

rg -q 'minimumContribution = 1' "$BRIDGE" \
    || fail "supply quest has no contributor requirement"
rg -q 'originalEpisodeClosed' "$OBJECTIVE" \
    || fail "objective completion does not require need episode closure"
rg -q 'EvaluateSettlementTransfer' "$OBJECTIVE" \
    || fail "objective cannot consume confirmed transfer events"
rg -q 'categories\[objective\.category\].*true' "$OBJECTIVE" \
    || fail "objective does not verify server-observed supply category"
rg -q 'normalizedEpoch\(signal\.openEpoch\) == normalizedEpoch\(objective\.openEpoch\)' "$OBJECTIVE" \
    || fail "objective transfer credit is not scoped to one openEpoch"

if rg -q 'ModData\.(get|getOrCreate|transmit)' "$BRIDGE" "$OBJECTIVE"; then
    fail "dynamic supply quests must reuse existing site/quest persistence, not create parallel ModData"
fi

echo "[faction-supply-quest-audit] OK"
