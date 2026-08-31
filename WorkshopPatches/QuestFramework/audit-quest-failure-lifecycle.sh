#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
MOD="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
INSTANCE="$MOD/server/LCCQF/Quest/LCCQFQuestInstance.lua"
SERVICE="$MOD/server/LCCQF/Quest/LCCQFQuestService.lua"
JOURNAL="$MOD/client/LCCQF/UI/zz_LCCQFQuestTerminalHistory.lua"
RELATIONSHIP="$MOD/server/LCCQF/Relationship/zz_LCCQFCharacterRelationshipBridge.lua"
FACTION_RELATIONSHIP="$MOD/server/LCCQF/Faction/zz_LCCQFFactionRelationshipBridge.lua"

fail() {
    echo "[quest-failure-lifecycle-audit] ERROR: $*" >&2
    exit 1
}

for file in "$INSTANCE" "$SERVICE" "$JOURNAL" "$RELATIONSHIP" "$FACTION_RELATIONSHIP"; do
    [[ -f "$file" ]] || fail "missing failure lifecycle file: $file"
done

rg -q 'state == "active" or state == "completed" or state == "failed"' "$INSTANCE" \
    || fail "QuestInstance restore does not accept failed terminal state"
rg -q 'function QuestInstance.Fail\(instance, reason\)' "$INSTANCE" \
    || fail "QuestInstance.Fail missing"
rg -q 'objective.state = "failed"' "$INSTANCE" \
    || fail "failed instance does not retain failed objective state"
rg -q 'instance.state = "failed"' "$INSTANCE" \
    || fail "QuestInstance.Fail does not make the instance terminal"
rg -q 'failureReason = instance.state == "failed"' "$INSTANCE" \
    || fail "failed reason is not projected to client history"

rg -q 'local function failInstance\(player, instance, reason\)' "$SERVICE" \
    || fail "QuestService lacks authoritative failure transition"
rg -q 'QuestInstance.Fail\(instance, reason\)' "$SERVICE" \
    || fail "QuestService bypasses QuestInstance failure transition"
rg -q 'IGUI_LCCQF_QuestEvent_Failed' "$SERVICE" \
    || fail "QuestService does not emit failure event"
rg -q 'function QuestService.Fail\(player, questId, reason\)' "$SERVICE" \
    || fail "generic server-side QuestService.Fail API missing"
rg -q 'local complete, changed, reason, failed = handler.EvaluateTick' "$SERVICE" \
    || fail "objective tick result cannot request a failure transition"
rg -q 'if failed == true then' "$SERVICE" \
    || fail "handler failure result is ignored"

rg -q 'quest.state ~= "active"' "$JOURNAL" \
    || fail "terminal quest history does not reveal failed quests"
rg -q 'terminal\[#terminal \+ 1\] = quest' "$JOURNAL" \
    || fail "journal terminal bucket excludes non-active states"
rg -q 'IGUI_LCCQF_Hub_QuestHistory' "$JOURNAL" \
    || fail "journal terminal history has no dedicated localized label"

# Failure must never enter the existing reward path. Both reward bridges are deliberately
# completion-only and history reconciliation must stay completion-only too.
rg -q 'payload.messageKey ~= "IGUI_LCCQF_QuestEvent_Completed" or payload.state ~= "completed"' "$RELATIONSHIP" \
    || fail "NPC relationship rewards are not completion-only"
rg -q 'quest.state == "completed"' "$RELATIONSHIP" \
    || fail "NPC relationship history reward reconciliation is not completion-only"
rg -q 'payload.messageKey ~= "IGUI_LCCQF_QuestEvent_Completed" or payload.state ~= "completed"' "$FACTION_RELATIONSHIP" \
    || fail "faction rewards are not completion-only"
rg -q 'quest.state == "completed"' "$FACTION_RELATIONSHIP" \
    || fail "faction history reward reconciliation is not completion-only"

echo "[quest-failure-lifecycle-audit] OK"
