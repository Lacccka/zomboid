#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
lua_root="$project_root/Contents/mods/LaccckaQuestFramework/42/media/lua"

constants="$lua_root/shared/LCCQF/LCCQFConstants.lua"
registry="$lua_root/server/LCCQF/Quest/LCCQFQuestRegistry.lua"
persistence="$lua_root/server/LCCQF/Persistence/LCCQFQuestPersistence.lua"
service="$lua_root/server/LCCQF/Quest/LCCQFQuestService.lua"
definitions="$lua_root/server/LCCQF/Content/LCCQFQuestDefinitions.lua"
dialogue="$lua_root/server/LCCQF/Content/LCCQFDialogueContent.lua"
translation_en="$lua_root/shared/Translate/EN/IG_UI.json"
translation_ru="$lua_root/shared/Translate/RU/IG_UI.json"

fail() {
    echo "QuestFramework branching audit: FAIL: $1" >&2
    exit 1
}

require_pattern() {
    local pattern="$1"
    local file="$2"
    local message="$3"
    rg -q "$pattern" "$file" || fail "$message"
}

for required in "$constants" "$registry" "$persistence" "$service" "$definitions" "$dialogue" "$translation_en" "$translation_ru"; do
    [[ -f "$required" ]] || fail "missing $required"
done

require_pattern 'validateBranch' "$registry" "quest registry branch validation missing"
require_pattern 'branch\.groupId' "$registry" "branch groupId validation missing"
require_pattern 'branch\.optionId' "$registry" "branch optionId validation missing"
require_pattern 'store\.branchChoices' "$persistence" "persistent branch choice store missing"
require_pattern 'store\.branchChoices\[branch\.groupId\] = branch\.optionId' "$persistence" "restored quest branch backfill missing"
require_pattern 'function QuestService\.GetBranchChoice' "$service" "branch choice query API missing"
require_pattern 'condition\.kind == "questBranch"' "$service" "chosen branch condition missing"
require_pattern 'condition\.kind == "questBranchAvailable"' "$service" "branch availability condition missing"
require_pattern 'quest branch already chosen' "$service" "mutually exclusive branch rejection missing"
require_pattern 'store\.branchChoices\[branch\.groupId\] = branch\.optionId' "$service" "branch claim persistence missing"

require_pattern 'TEST_BRANCH_GROUP_ID' "$constants" "test branch group missing"
require_pattern 'TEST_BRANCH_SUPPORT_OPTION' "$constants" "support branch option missing"
require_pattern 'TEST_BRANCH_INDEPENDENT_OPTION' "$constants" "independent branch option missing"
require_pattern 'branch = \{' "$definitions" "authored branch metadata missing"
require_pattern 'C\.TEST_BRANCH_SUPPORT_OPTION' "$definitions" "support branch quest missing"
require_pattern 'C\.TEST_BRANCH_INDEPENDENT_OPTION' "$definitions" "independent branch quest missing"
require_pattern 'kind = "questBranchAvailable"' "$definitions" "branch availability prerequisite missing from authored quests"
require_pattern 'kind = "questBranchAvailable"' "$dialogue" "branch availability not exercised by dialogue"
require_pattern 'C\.TEST_QUEST_BRANCH_SUPPORT_ID' "$dialogue" "support branch dialogue acceptance missing"
require_pattern 'C\.TEST_QUEST_BRANCH_INDEPENDENT_ID' "$dialogue" "independent branch dialogue acceptance missing"
require_pattern 'kind = "questAccept"' "$dialogue" "branch selection is not tied to quest acceptance"

require_pattern 'IGUI_LCCQF_Quest_BranchSupport_Title' "$translation_en" "EN support branch translation missing"
require_pattern 'IGUI_LCCQF_Quest_BranchSupport_Title' "$translation_ru" "RU support branch translation missing"
require_pattern 'IGUI_LCCQF_Quest_BranchIndependent_Title' "$translation_en" "EN independent branch translation missing"
require_pattern 'IGUI_LCCQF_Quest_BranchIndependent_Title' "$translation_ru" "RU independent branch translation missing"
require_pattern 'IGUI_LCCQF_Dialog_AlignmentOffer' "$translation_en" "EN branching dialogue translation missing"
require_pattern 'IGUI_LCCQF_Dialog_AlignmentOffer' "$translation_ru" "RU branching dialogue translation missing"

if rg -n 'C\.TEST_BRANCH_|checkpoint_alignment|support_group|stay_independent' "$service" "$persistence" "$registry"; then
    fail "authored branch identity leaked into reusable branching core"
fi

if command -v python3 >/dev/null 2>&1; then
    python3 - "$service" <<'PY'
import sys
text = open(sys.argv[1], encoding='utf-8').read()
create = text.index('local instance, err = QuestInstance.Create')
claim = text.index('store.branchChoices[branch.groupId] = branch.optionId')
if create >= claim:
    raise SystemExit('branch claim is not after successful quest instance creation')
PY
    python3 -m json.tool "$translation_en" >/dev/null
    python3 -m json.tool "$translation_ru" >/dev/null
fi

if command -v lua >/dev/null 2>&1; then
    lua -e "assert(loadfile([[$registry]]))"
    lua -e "assert(loadfile([[$persistence]]))"
    lua -e "assert(loadfile([[$service]]))"
    lua -e "assert(loadfile([[$definitions]]))"
    lua -e "assert(loadfile([[$dialogue]]))"
fi

echo "QuestFramework branching audit: PASS (persistent mutually exclusive branch groups + atomic quest acceptance)"
