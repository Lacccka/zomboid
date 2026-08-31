#!/usr/bin/env bash
set -euo pipefail

ROOT="WorkshopPatches/QuestFramework"
LUA_ROOT="$ROOT/Contents/mods/LaccckaQuestFramework/42/media/lua"

python3 - "$LUA_ROOT" <<'PY'
from pathlib import Path
import re
import sys

root = Path(sys.argv[1])
pattern = re.compile(r'''require\s*(?:\(\s*)?["'](LCCQF/[^"']+)["']\s*\)?''')
requires = {}

for path in sorted(root.rglob("*.lua")):
    text = path.read_text(encoding="utf-8")
    for match in pattern.finditer(text):
        module = match.group(1)
        requires.setdefault(module, set()).add(path)

missing = []
for module in sorted(requires):
    relative = Path(module + ".lua")
    candidates = [root / realm / relative for realm in ("shared", "client", "server")]
    if not any(path.is_file() for path in candidates):
        missing.append((module, sorted(requires[module])))

if missing:
    for module, sources in missing:
        print(f"[lccqf-require-audit] ERROR: missing internal module {module}", file=sys.stderr)
        for source in sources:
            print(f"  required by: {source}", file=sys.stderr)
    raise SystemExit(1)

print(f"[lccqf-require-audit] OK modules={len(requires)}")
PY
