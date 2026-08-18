#!/usr/bin/env python3
"""Static contract audit for the Lifestyle Yoga UI compatibility proxy.

The compatibility patch intentionally exposes Lifestyle's hidden Yoga skill in
Project Zomboid's normal Skills panel without moving progression out of
Lifestyle's LSHiddenSkills storage. This script catches upstream changes that
would make the proxy stale or unsafe.

Run from the repository root:
    python3 tools/audit_lifestyle_yoga_proxy.py
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE_HS = ROOT / "3403870858/mods/Lifestyle/common/media/lua/client/Helper/HSMng.lua"
SOURCE_PERKS = ROOT / "3403870858/mods/Lifestyle/common/media/perks.txt"
PATCH_PERKS = ROOT / "LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch/42/media/perks.txt"
PATCH_UI = ROOT / "LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch/42/media/lua/client/zzz_LCC_LifestyleYogaProgress.lua"
PATCH_RU = ROOT / "LaccckaCompatibilityPatch/Contents/mods/LaccckaCompatibilityPatch/42/media/lua/shared/Translate/RU"

LEGACY_ASSIGNMENT_RE = re.compile(r'^\s*([A-Za-z0-9_:.\-]+)\s*=\s*"', re.MULTILINE)


def fail(message: str) -> None:
    raise RuntimeError(message)


def read(path: Path) -> str:
    if not path.is_file():
        fail(f"required file not found: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8-sig")


def extract_block(text: str, block_header: str) -> str:
    start = text.find(block_header)
    if start < 0:
        fail(f"block not found: {block_header}")
    brace = text.find("{", start)
    if brace < 0:
        fail(f"opening brace not found for: {block_header}")

    depth = 0
    for index in range(brace, len(text)):
        if text[index] == "{":
            depth += 1
        elif text[index] == "}":
            depth -= 1
            if depth == 0:
                return text[brace + 1 : index]
    fail(f"unterminated block: {block_header}")


def parse_source_yoga_thresholds(text: str) -> list[int]:
    # HSMng maps current hidden level 0..9 to XP required for the next level.
    match = re.search(r"local\s+function\s+getNewValues\s*\([^)]*\)(.*?)return\s*\{level,0,t\[level\]\}", text, re.S)
    if not match:
        fail("Lifestyle getNewValues() table was not found")

    body = match.group(1)
    values: dict[int, int] = {}
    for level, xp in re.findall(r"\[(\d+)\]\s*=\s*(\d+)", body):
        values[int(level)] = int(xp)

    missing = [level for level in range(10) if level not in values]
    if missing:
        fail(f"Lifestyle Yoga XP thresholds missing levels: {missing}")
    return [values[level] for level in range(10)]


def parse_proxy_thresholds(text: str) -> list[int]:
    block = extract_block(text, "perk Yoga")
    values: list[int] = []
    for level in range(1, 11):
        match = re.search(rf"\bxp{level}\s*=\s*(\d+)\s*,", block)
        if not match:
            fail(f"proxy perk is missing xp{level}")
        values.append(int(match.group(1)))
    return values


def load_json_keys(path: Path) -> set[str]:
    data = json.loads(read(path))
    if not isinstance(data, dict):
        fail(f"expected JSON object: {path.relative_to(ROOT)}")
    return set(data)


def has_translation_key(category: str, key: str) -> bool:
    json_path = PATCH_RU / f"{category}.json"
    if json_path.is_file() and key in load_json_keys(json_path):
        return True

    legacy_path = PATCH_RU / f"{category}_RU.txt"
    if legacy_path.is_file():
        keys = set(LEGACY_ASSIGNMENT_RE.findall(read(legacy_path)))
        if key in keys:
            return True
    return False


def main() -> int:
    try:
        source_hs = read(SOURCE_HS)
        source_perks = read(SOURCE_PERKS)
        patch_perks = read(PATCH_PERKS)
        patch_ui = read(PATCH_UI)

        if not re.search(r"\['Yoga'\]\s*=\s*true", source_hs):
            fail("Lifestyle no longer registers Yoga as an enabled HiddenSkill")
        if "modData.LSHiddenSkills[skill] = {0,0,100}" not in source_hs:
            fail("Lifestyle hidden-skill storage shape/default changed")
        if not re.search(r"HiddenSkills\.getSkill\s*=\s*function", source_hs):
            fail("Lifestyle HiddenSkills.getSkill API changed")

        source_xp = parse_source_yoga_thresholds(source_hs)
        proxy_xp = parse_proxy_thresholds(patch_perks)
        if proxy_xp != source_xp:
            fail(f"Yoga proxy XP thresholds drifted: source={source_xp}, proxy={proxy_xp}")

        yoga_block = extract_block(patch_perks, "perk Yoga")
        if not re.search(r"\bparent\s*=\s*Lifestyle\s*,", yoga_block):
            fail("Yoga proxy must remain under the Lifestyle skill parent")
        if not re.search(r"\bpassive\s*=\s*false\s*,", yoga_block):
            fail("Yoga proxy must be non-passive to stay with Lifestyle skills")

        # Upstream Lifestyle must still not have a normal Yoga perk; if it gains
        # one, our proxy would duplicate it and should be removed/reworked.
        if re.search(r"(?m)^\s*perk\s+Yoga\s*$", source_perks):
            fail("Lifestyle now defines a normal Yoga perk; compatibility proxy would duplicate it")

        required_ui_contract = (
            'require "Helper/HSMng"',
            'HiddenSkills.getSkill, character, "Yoga"',
            'Perks.Yoga',
            'function LCCYogaSkillProgressBar:onMouseUp',
            'ISSkillProgressBar.new = function',
            'ISCharacterInfo.loadPerk = function',
            'DividerMeditationNew',
            'Lifestyle Yoga progress UI installed',
        )
        missing_ui = [needle for needle in required_ui_contract if needle not in patch_ui]
        if missing_ui:
            fail(f"Yoga UI proxy contract is incomplete: {missing_ui}")

        required_ru = {
            "IG_UI": ("IGUI_perks_Yoga", "IGUI_T_Yoga_Title", "IGUI_T_Yoga_Body9"),
            "UI": ("UI_LSHS_Yoga",),
            "Tooltip": ("Tooltip_Yoga_Option",),
            "ContextMenu": ("ContextMenu_LSBody", "ContextMenu_LSBody_Yoga"),
        }
        missing_ru: list[str] = []
        for category, keys in required_ru.items():
            for key in keys:
                if not has_translation_key(category, key):
                    missing_ru.append(f"{category}:{key}")
        if missing_ru:
            fail(f"required Russian Yoga strings are missing: {missing_ru}")

    except (OSError, UnicodeError, json.JSONDecodeError, RuntimeError) as exc:
        print(f"Yoga proxy audit FAILED: {exc}", file=sys.stderr)
        return 1

    print("Lifestyle Yoga proxy audit OK")
    print(f"Hidden Yoga thresholds: {source_xp}")
    print("Storage: LSHiddenSkills.Yoga remains authoritative")
    print("UI: non-passive proxy under Lifestyle, click leveling blocked")
    print("RU: skill, tutorial, tooltip and wellness menu keys present")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
