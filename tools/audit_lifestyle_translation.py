#!/usr/bin/env python3
"""Audit LaccckaCompatibilityPatch Russian Lifestyle translation coverage.

Run from the repository root:
    python3 tools/audit_lifestyle_translation.py

The script compares the current Lifestyle English translation keys with all
matching Russian translation files shipped by LaccckaCompatibilityPatch.
Both Build 42 JSON translation files and legacy *_RU.txt tables are supported.

Exit codes:
    0 - no missing translatable keys and no placeholder mismatches
    1 - one or more translatable English keys are missing/broken in the patch
    2 - audit could not be completed (invalid/missing source files)
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Iterable

ROOT = Path(__file__).resolve().parents[1]
LIFESTYLE_EN = (
    ROOT
    / "3403870858"
    / "mods"
    / "Lifestyle"
    / "common"
    / "media"
    / "lua"
    / "shared"
    / "Translate"
    / "EN"
)
PATCH_RU = (
    ROOT
    / "LaccckaCompatibilityPatch"
    / "Contents"
    / "mods"
    / "LaccckaCompatibilityPatch"
    / "42"
    / "media"
    / "lua"
    / "shared"
    / "Translate"
    / "RU"
)

# language.txt is metadata rather than a normal key/value translation table.
SKIP_FILES = {"language.txt"}

# Some ContextMenu entries are canonical song titles. They are intentionally
# left in their original spelling instead of being translated/transliterated.
# Lifestyle currently exposes them through both numbered media IDs and the
# dedicated duet-song menu. Missing RU entries for these keys are therefore
# valid fallbacks, not gaps in the Russian UI localization.
IGNORED_KEY_PATTERNS: dict[str, tuple[re.Pattern[str], ...]] = {
    "ContextMenu": (
        re.compile(r"^ContextMenu_\d{2}_\d{2}_[A-Z0-9]+$"),
        re.compile(r"^ContextMenu_Duet_.+$"),
    ),
}

# Legacy PZ translations look like:
#   IGUI_RU = {
#       IGUI_Foo = "...",
#   }
# Only keys are required for coverage auditing, so parsing full Lua syntax is
# deliberately avoided. Restrict the match to assignments whose value starts
# with a double-quoted string to avoid matching table names.
LEGACY_KEY_RE = re.compile(r'^\s*([A-Za-z0-9_:.\-]+)\s*=\s*"', re.MULTILINE)

# Project Zomboid translation strings use positional placeholders such as %1,
# %2 and escaped percent markers %% . A translated key can technically exist
# while still being broken at runtime if one of those placeholders is lost.
PLACEHOLDER_RE = re.compile(r"%(?:%|\d+)")


def load_json(path: Path) -> dict[str, object]:
    with path.open("r", encoding="utf-8-sig") as fh:
        data = json.load(fh)
    if not isinstance(data, dict):
        raise ValueError(f"{path}: expected a JSON object")
    return data


def load_json_keys(path: Path) -> set[str]:
    return set(load_json(path))


def load_legacy_keys(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8-sig")
    return set(LEGACY_KEY_RE.findall(text))


def collect_ru_keys(category: str) -> tuple[set[str], list[Path]]:
    keys: set[str] = set()
    sources: list[Path] = []

    json_path = PATCH_RU / f"{category}.json"
    if json_path.is_file():
        keys.update(load_json_keys(json_path))
        sources.append(json_path)

    legacy_path = PATCH_RU / f"{category}_RU.txt"
    if legacy_path.is_file():
        keys.update(load_legacy_keys(legacy_path))
        sources.append(legacy_path)

    return keys, sources


def short_paths(paths: Iterable[Path]) -> str:
    result = []
    for path in paths:
        try:
            result.append(str(path.relative_to(ROOT)))
        except ValueError:
            result.append(str(path))
    return ", ".join(result) if result else "<none>"


def is_ignored_key(category: str, key: str) -> bool:
    return any(pattern.fullmatch(key) for pattern in IGNORED_KEY_PATTERNS.get(category, ()))


def placeholders(value: object) -> list[str]:
    if not isinstance(value, str):
        return []
    return sorted(PLACEHOLDER_RE.findall(value))


def audit_json_placeholders(
    category: str,
    en_data: dict[str, object],
    audited_keys: set[str],
) -> list[str]:
    """Return placeholder mismatches for keys present in the RU JSON file.

    Legacy *_RU.txt files are intentionally excluded here because extracting
    complete Lua string values safely would require a real Lua parser. Key
    coverage still includes those files.
    """
    ru_json_path = PATCH_RU / f"{category}.json"
    if not ru_json_path.is_file():
        return []

    ru_data = load_json(ru_json_path)
    problems: list[str] = []
    for key in sorted(audited_keys & set(ru_data)):
        en_tokens = placeholders(en_data[key])
        ru_tokens = placeholders(ru_data[key])
        if en_tokens != ru_tokens:
            problems.append(
                f"{key}: EN placeholders {en_tokens or '<none>'} != "
                f"RU placeholders {ru_tokens or '<none>'}"
            )
    return problems


def main() -> int:
    if not LIFESTYLE_EN.is_dir():
        print(f"ERROR: Lifestyle EN directory not found: {LIFESTYLE_EN}", file=sys.stderr)
        return 2
    if not PATCH_RU.is_dir():
        print(f"ERROR: patch RU directory not found: {PATCH_RU}", file=sys.stderr)
        return 2

    total_en = 0
    total_ru = 0
    total_missing = 0
    total_extra = 0
    total_ignored = 0
    total_placeholder_errors = 0
    failures: list[str] = []

    print("Lifestyle Russian translation coverage")
    print("=" * 38)

    for en_path in sorted(LIFESTYLE_EN.glob("*.json")):
        if en_path.name in SKIP_FILES:
            continue

        category = en_path.stem
        try:
            en_data = load_json(en_path)
            all_en_keys = set(en_data)
            ignored = {key for key in all_en_keys if is_ignored_key(category, key)}
            en_keys = all_en_keys - ignored
            ru_keys, ru_sources = collect_ru_keys(category)
            placeholder_errors = audit_json_placeholders(category, en_data, en_keys)
        except (OSError, UnicodeError, json.JSONDecodeError, ValueError) as exc:
            failures.append(f"{category}: {exc}")
            continue

        missing = sorted(en_keys - ru_keys)
        extra = sorted(ru_keys - all_en_keys)

        total_en += len(en_keys)
        total_ru += len(en_keys & ru_keys)
        total_missing += len(missing)
        total_extra += len(extra)
        total_ignored += len(ignored)
        total_placeholder_errors += len(placeholder_errors)

        coverage = 100.0 if not en_keys else (len(en_keys & ru_keys) / len(en_keys)) * 100.0
        marker = "OK" if not missing and not placeholder_errors else "MISS"
        print(
            f"[{marker:4}] {category:16} "
            f"{len(en_keys & ru_keys):4}/{len(en_keys):4} "
            f"({coverage:6.2f}%)  RU: {short_paths(ru_sources)}"
        )

        if ignored:
            print(f"       Intentional original titles: {len(ignored)}")

        if missing:
            print("       Missing:")
            for key in missing:
                print(f"         {key}")

        if placeholder_errors:
            print("       Placeholder mismatches:")
            for problem in placeholder_errors:
                print(f"         {problem}")

        # Extra keys are not necessarily errors because the compatibility patch
        # also translates other mods and custom patch-only strings. Keep them
        # visible without failing the audit.
        if extra:
            print(f"       Extra/custom keys: {len(extra)}")

    print("-" * 38)
    coverage = 100.0 if not total_en else (total_ru / total_en) * 100.0
    print(f"Covered translatable keys: {total_ru}/{total_en} ({coverage:.2f}%)")
    print(f"Missing translatable keys: {total_missing}")
    print(f"Intentional original-title keys: {total_ignored}")
    print(f"Placeholder mismatches: {total_placeholder_errors}")
    print(f"Extra/custom: {total_extra}")

    if failures:
        print("\nAudit errors:", file=sys.stderr)
        for failure in failures:
            print(f"  {failure}", file=sys.stderr)
        return 2

    return 1 if total_missing or total_placeholder_errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
