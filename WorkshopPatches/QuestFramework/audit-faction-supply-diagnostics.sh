#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
DIAG="$MOD/server/LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestDiagnostics.lua"

fail() {
    echo "[faction-supply-diagnostics-audit] ERROR: $*" >&2
    exit 1
}

[[ -f "$DIAG" ]] || fail "settlement supply diagnostics missing"

rg -q '\[FACTION:SUPPLY:DIAG\]' "$DIAG" \
    || fail "diagnostic log prefix missing"
rg -q 'function Diagnostics\.Dump\(force, source\)' "$DIAG" \
    || fail "explicit diagnostic dump API missing"
rg -q 'local lastSignature = Diagnostics\.lastSignature or \{\}' "$DIAG" \
    || fail "signature-gated state missing"
rg -q 'lastSignature\[key\] ~= nextSignature' "$DIAG" \
    || fail "periodic diagnostics are not change-gated"

for token in stockRev openEpoch available target deficit questId offer definition giverNpcId giverState giverRuntimeId; do
    rg -q "$token" "$DIAG" || fail "diagnostic projection missing field: $token"
done

rg -q 'QuestRegistry\.Get\(questId\)' "$DIAG" \
    || fail "diagnostics do not verify dynamic definition registration"
rg -q 'Population\.GetMemberByNpcId\(site, npcId\)' "$DIAG" \
    || fail "diagnostics do not project logical giver state"
rg -q 'Events\.OnServerStarted\.Add\(onServerStarted\)' "$DIAG" \
    || fail "server-start diagnostic hook missing"
rg -q 'Events\.EveryOneMinute\.Add\(onEveryOneMinute\)' "$DIAG" \
    || fail "periodic diagnostic hook missing"

if rg -q 'ModData\.|Stock\.Refresh|Operations\.UpdateSite|SupplyBridge\.RunOnce|QuestRegistry\.Register|sendClientCommand|sendServerCommand' "$DIAG"; then
    fail "diagnostics must remain a read-only projection"
fi

if rg -q 'IsoObject|IsoRoom|IsoBuilding|IsoGridSquare|ItemContainer' "$DIAG"; then
    fail "diagnostics must not retain or depend on live world object references"
fi

echo "[faction-supply-diagnostics-audit] OK"
