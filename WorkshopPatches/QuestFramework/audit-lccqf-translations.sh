#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
LUA_ROOT="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"
EN="$LUA_ROOT/shared/Translate/EN/IG_UI.json"
RU="$LUA_ROOT/shared/Translate/RU/IG_UI.json"

python3 - "$LUA_ROOT" "$EN" "$RU" <<'PY'
from pathlib import Path
import json
import re
import sys

lua_root = Path(sys.argv[1])
translation_paths = {
    "EN": Path(sys.argv[2]),
    "RU": Path(sys.argv[3]),
}

# Only complete quoted keys are audited. Prefix literals used for concatenation, such as
# "IGUI_LCCQF_" or "IGUI_LCCQF_NPC_Faction_", deliberately end in '_' and are excluded.
key_pattern = re.compile(r'''["'](IGUI_LCCQF_[A-Za-z0-9_]*[A-Za-z0-9])["']''')
used_by = {}
for path in sorted(lua_root.rglob("*.lua")):
    text = path.read_text(encoding="utf-8")
    for match in key_pattern.finditer(text):
        used_by.setdefault(match.group(1), set()).add(path)

translations = {}
for language, path in translation_paths.items():
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"[lccqf-translation-audit] ERROR: invalid {language} JSON: {path}: {exc}", file=sys.stderr)
        raise SystemExit(1)
    if not isinstance(raw, dict):
        print(f"[lccqf-translation-audit] ERROR: {language} translation root is not an object", file=sys.stderr)
        raise SystemExit(1)
    translations[language] = raw

failed = False
for key in sorted(used_by):
    missing = [language for language, table in translations.items() if key not in table]
    if missing:
        failed = True
        print(
            f"[lccqf-translation-audit] ERROR: {key} missing from {','.join(missing)}",
            file=sys.stderr,
        )
        for source in sorted(used_by[key]):
            print(f"  used by: {source}", file=sys.stderr)

if failed:
    raise SystemExit(1)

print(
    f"[lccqf-translation-audit] OK literalKeys={len(used_by)} "
    f"enKeys={len(translations['EN'])} ruKeys={len(translations['RU'])}"
)
PY
