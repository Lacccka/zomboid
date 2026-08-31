#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
RESTORE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestHistoricalRestore.lua"
BRIDGE="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge.lua"
PERSISTENCE="$MOD/server/LCCQF/Persistence/LCCQFQuestPersistence.lua"
BOOTSTRAP="$MOD/server/zz_LCCQFFactionBootstrap.lua"

fail() {
    echo "[faction-supply-restart-audit] ERROR: $*" >&2
    exit 1
}

for file in "$RESTORE" "$BRIDGE" "$PERSISTENCE" "$BOOTSTRAP"; do
    [[ -f "$file" ]] || fail "missing file: $file"
done

rg -q 'for _, site in ipairs\(Sites.ListSites\(\)\) do' "$RESTORE" \
    || fail "historical restore does not inspect every persisted site"
rg -q 'site.operations and site.operations.questOffers' "$RESTORE" \
    || fail "historical restore does not read persisted offer history"
rg -q 'SupplyBridge.RegisterDefinition\(offer\)' "$RESTORE" \
    || fail "historical restore bypasses the common generated-definition registrar"
rg -q 'Events.OnServerStarted.Add\(onServerStarted\)' "$RESTORE" \
    || fail "historical definitions are not restored during server startup"

if rg -q 'site.state ==|site.state ~=|ACTIVE|DORMANT|VALIDATING|RELOCATING|ABANDONED' "$RESTORE"; then
    fail "historical definition reconstruction must not be filtered by current site state"
fi

rg -q 'QuestRegistry.Get\(rawInstance.questId\)' "$PERSISTENCE" \
    || fail "persistence no longer demonstrates why definitions must exist before normalization"
rg -q 'zz_LCCQFFactionSupplyQuestBridge' "$BOOTSTRAP" \
    || fail "supply quest bridge is not explicitly bootstrapped"
rg -q 'zz_LCCQFFactionSupplyQuestHistoricalRestore' "$BOOTSTRAP" \
    || fail "historical restore is not explicitly bootstrapped"
rg -q 'zz_LCCQFFactionSupplyQuestRuntimeBridge' "$BOOTSTRAP" \
    || fail "supply runtime is not explicitly bootstrapped"

bridge_line=$(rg -n 'zz_LCCQFFactionSupplyQuestBridge' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
restore_line=$(rg -n 'zz_LCCQFFactionSupplyQuestHistoricalRestore' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
runtime_line=$(rg -n 'zz_LCCQFFactionSupplyQuestRuntimeBridge' "$BOOTSTRAP" | head -n1 | cut -d: -f1)
[[ -n "$bridge_line" && -n "$restore_line" && -n "$runtime_line" \
   && "$bridge_line" -lt "$restore_line" && "$restore_line" -lt "$runtime_line" ]] \
    || fail "supply bootstrap order must be bridge -> historical restore -> runtime"

if rg -q 'ModData\.(get|getOrCreate|transmit)' "$RESTORE"; then
    fail "historical restore must reuse persisted site offers instead of creating parallel ModData"
fi

echo "[faction-supply-restart-audit] OK"
