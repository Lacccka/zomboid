#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
IDENTITY="$MOD/server/LCCQF/FactionWorld/LCCQFSettlementTransferCharacterIdentity.lua"
RUNTIME="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestRuntimeBridge.lua"
CHARACTER="$MOD/server/LCCQF/Persistence/LCCQFCharacterIdentity.lua"

fail() {
    echo "[settlement-transfer-character-identity-audit] ERROR: $*" >&2
    exit 1
}

for file in "$IDENTITY" "$RUNTIME" "$CHARACTER"; do
    [[ -f "$file" ]] || fail "missing file: $file"
done

rg -q 'REPORT_SETTLEMENT_TRANSFER_INTENT' "$IDENTITY" \
    || fail "identity snapshot is not captured from the existing pre-transfer intent"
rg -q 'CharacterIdentity.GetExistingCharacterId\(player\)' "$IDENTITY" \
    || fail "identity snapshot does not use canonical per-life characterId"
rg -q 'characterId = characterId' "$IDENTITY" \
    || fail "captured transfer identity does not persist characterId in the ephemeral snapshot"
rg -q 'function Bridge.ConsumeConfirmedTransfer\(event, player\)' "$IDENTITY" \
    || fail "confirmed transfer identity has no consume-once validation API"
rg -q 'snapshots\[key\] = nil' "$IDENTITY" \
    || fail "identity snapshot is reusable after confirmed transfer"
rg -q 'currentCharacterId.*row.characterId|tostring\(currentCharacterId\) ~= tostring\(row.characterId\)' "$IDENTITY" \
    || fail "confirmed transfer does not reject a different character life"
rg -q 'maximumSnapshots' "$IDENTITY" \
    || fail "identity snapshot cache is not bounded"
rg -q 'pruneExpired' "$IDENTITY" \
    || fail "identity snapshot cache has no TTL cleanup"

rg -q 'LCCQFSettlementTransferCharacterIdentity' "$RUNTIME" \
    || fail "supply runtime does not load per-life transfer identity"
rg -q 'TransferIdentity.ConsumeConfirmedTransfer\(event, player\)' "$RUNTIME" \
    || fail "supply runtime does not validate per-life identity before credit"
rg -q 'if not characterId then' "$RUNTIME" \
    || fail "supply runtime does not fail closed when per-life identity validation fails"

identity_line=$(rg -n 'TransferIdentity.ConsumeConfirmedTransfer\(event, player\)' "$RUNTIME" | head -n1 | cut -d: -f1)
queue_line=$(rg -n 'Objective.QueueConfirmedTransfer\(player, event\)' "$RUNTIME" | head -n1 | cut -d: -f1)
[[ -n "$identity_line" && -n "$queue_line" && "$identity_line" -lt "$queue_line" ]] \
    || fail "quest credit is queued before per-life identity validation"

if rg -q 'ModData\.(get|getOrCreate|transmit)' "$IDENTITY"; then
    fail "transfer identity snapshot must remain ephemeral and reuse CharacterIdentity"
fi

if rg -q 'sendClientCommand' "$IDENTITY"; then
    fail "server transfer identity bridge must not create a new client authority surface"
fi

echo "[settlement-transfer-character-identity-audit] OK"
