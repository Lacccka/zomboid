#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
OBJECTIVE="$MOD/server/LCCQF/Quest/Objectives/LCCQFObjectiveSettlementSupply.lua"
SERVICE="$MOD/server/LCCQF/Quest/zz_LCCQFFactionSupplyQuestServiceExtension.lua"
DIALOGUE="$MOD/server/LCCQF/Dialogue/zz_LCCQFFactionResidentSupplyDialogue.lua"
RUNTIME="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestRuntimeBridge.lua"
BRIDGE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge.lua"

fail() {
    echo "[faction-supply-runtime-audit] ERROR: $*" >&2
    exit 1
}

for file in "$OBJECTIVE" "$SERVICE" "$DIALOGUE" "$RUNTIME" "$BRIDGE"; do
    [[ -f "$file" ]] || fail "missing runtime file: $file"
done

rg -q 'kind == "factionSupplyQuestAvailable"' "$SERVICE" \
    || fail "dynamic availability condition missing"
rg -q 'kind == "factionSupplyQuestActive"' "$SERVICE" \
    || fail "dynamic active condition missing"
rg -q 'action.kind == "factionSupplyQuestAccept"' "$SERVICE" \
    || fail "dynamic accept action missing"
rg -q 'SupplyBridge.IsOfferOpen\(offer.questId\)' "$SERVICE" \
    || fail "accept action does not revalidate current OPEN offer"
rg -q 'SupplyBridge.CanNpcHandleOffer\(offer, npcId\)' "$SERVICE" \
    || fail "accept action does not revalidate presenter delegation"
rg -q 'out.giverNpcId = offer.giverNpcId' "$SERVICE" \
    || fail "delegated acceptance does not restore canonical historical giver"
rg -q 'out.dialogueNpcId = dialogueNpcId' "$SERVICE" \
    || fail "delegated acceptance loses the actual dialogue NPC identity"
rg -q 'QuestService.Accept\(player, offer.questId, acceptContext\)' "$SERVICE" \
    || fail "dynamic quest bypasses common QuestService.Accept canonical-giver validation"

rg -q 'function Bridge.CanNpcHandleOffer\(offer, npcId\)' "$BRIDGE" \
    || fail "supply bridge has no presenter delegation policy"
rg -q 'local presenter = materializedMember\(site, npcId\)' "$BRIDGE" \
    || fail "delegated presenter is not constrained to a materialized logical site member"
rg -q 'local historicalGiver = materializedMember\(site, tostring\(offer.giverNpcId or ""\)\)' "$BRIDGE" \
    || fail "delegation does not check whether the canonical giver remains materialized"
rg -q 'return historicalGiver == nil' "$BRIDGE" \
    || fail "delegation must fail closed while the canonical giver is materialized"
rg -q 'Bridge.CanNpcHandleOffer\(offer, npcId\)' "$BRIDGE" \
    || fail "open-offer lookup does not use the delegation policy"

rg -q 'condition = \{ kind = "factionSupplyQuestAvailable" \}' "$DIALOGUE" \
    || fail "resident dialogue does not expose server-filtered offer"
rg -q 'action = \{ kind = "factionSupplyQuestAccept" \}' "$DIALOGUE" \
    || fail "resident dialogue does not use dynamic server accept action"
rg -q 'condition = \{ kind = "factionSupplyQuestActive" \}' "$DIALOGUE" \
    || fail "resident dialogue has no active supply quest topic"

rg -q 'QueueConfirmedTransfer' "$OBJECTIVE" \
    || fail "confirmed transfer queue missing"
rg -q 'event.stockRefreshOk ~= true' "$OBJECTIVE" \
    || fail "objective queue does not fail closed on stock refresh"
rg -q 'consumeMatchingTransfer' "$OBJECTIVE" \
    || fail "objective does not consume server-confirmed transfer events"
rg -q 'DiscardQueuedTransfers' "$OBJECTIVE" \
    || fail "ephemeral transfer queue cleanup missing"

rg -q 'Observer.AddListener\(onConfirmedTransfer\)' "$RUNTIME" \
    || fail "runtime bridge is not attached to confirmed server transfers"
rg -q 'SupplyBridge.RunOnce\(\)' "$RUNTIME" \
    || fail "runtime bridge does not synchronize offer lifecycle"
rg -q 'Objective.QueueConfirmedTransfer\(player, event\)' "$RUNTIME" \
    || fail "runtime bridge does not queue confirmed contribution"
rg -q 'QuestService.UpdatePlayer\(player\)' "$RUNTIME" \
    || fail "runtime bridge bypasses normal QuestService tick/application path"
rg -q 'Objective.DiscardQueuedTransfers\(player\)' "$RUNTIME" \
    || fail "runtime bridge can leave stale transfer credits"

if rg -q 'sendClientCommand|OnClientCommand' "$RUNTIME" "$SERVICE" "$BRIDGE"; then
    fail "quest runtime integration must not trust a new client command surface"
fi

echo "[faction-supply-runtime-audit] OK"
