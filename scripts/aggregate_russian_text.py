#!/usr/bin/env python3
"""Aggregate split Russian translation fragments into Project Zomboid canonical tables.

Project Zomboid Build 42 loads translation JSON by canonical table filename
(ItemName.json, Recipes.json, Sandbox.json, etc.).  The RussianTextFixes patch
keeps per-target fragments as LCC_<target>_<table>.json for maintainability.
This script merges those fragments into the canonical files that the game
actually consumes.

The operation is deterministic and idempotent.  Existing canonical entries are
kept unless a split LCC fragment explicitly defines the same key, in which case
the fragment wins.  Conflicts between split fragments are reported.
"""

from __future__ import annotations

import json
from collections import OrderedDict
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
RU_DIR = (
    ROOT
    / "WorkshopPatches"
    / "RussianTextFixes"
    / "Contents"
    / "mods"
    / "LaccckaB4220RussianText"
    / "42"
    / "media"
    / "lua"
    / "shared"
    / "Translate"
    / "RU"
)
MOD_INFO = (
    ROOT
    / "WorkshopPatches"
    / "RussianTextFixes"
    / "Contents"
    / "mods"
    / "LaccckaB4220RussianText"
    / "42"
    / "mod.info"
)

# These are the canonical Build 42 translation table filenames currently used
# by split LCC fragments in RussianTextFixes.
TABLES = (
    "ContextMenu",
    "Fluids",
    "IG_UI",
    "ItemName",
    "Recipes",
    "Sandbox",
    "Tooltip",
    "UI",
)

TARGET_MOD_VERSION = "1.1.5"


def load_object(path: Path) -> OrderedDict[str, object]:
    try:
        with path.open("r", encoding="utf-8-sig") as fh:
            value = json.load(fh, object_pairs_hook=OrderedDict)
    except json.JSONDecodeError as exc:
        raise SystemExit(f"Invalid JSON in {path.relative_to(ROOT)}: {exc}") from exc

    if not isinstance(value, dict):
        raise SystemExit(
            f"Expected a JSON object in {path.relative_to(ROOT)}, got {type(value).__name__}"
        )
    return value


def write_object(path: Path, value: dict[str, object]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(value, ensure_ascii=False, indent=4) + "\n"
    path.write_text(text, encoding="utf-8", newline="\n")


def aggregate_table(table: str) -> tuple[int, int, list[str]]:
    canonical = RU_DIR / f"{table}.json"
    merged: OrderedDict[str, object] = OrderedDict()

    if canonical.exists():
        merged.update(load_object(canonical))

    fragment_paths = sorted(RU_DIR.glob(f"LCC_*_{table}.json"), key=lambda p: p.name.casefold())
    conflicts: list[str] = []
    fragment_keys: dict[str, str] = {}

    for fragment in fragment_paths:
        data = load_object(fragment)
        for key, value in data.items():
            previous_fragment = fragment_keys.get(key)
            if previous_fragment is not None and merged.get(key) != value:
                conflicts.append(f"{key}: {previous_fragment} -> {fragment.name}")
            merged[key] = value
            fragment_keys[key] = fragment.name

    if fragment_paths:
        write_object(canonical, merged)

    return len(fragment_paths), len(merged), conflicts


def bump_mod_version() -> bool:
    if not MOD_INFO.exists():
        raise SystemExit(f"Missing {MOD_INFO.relative_to(ROOT)}")

    text = MOD_INFO.read_text(encoding="utf-8-sig")
    replacement = f"modversion={TARGET_MOD_VERSION}"
    updated, count = re.subn(r"(?m)^modversion=.*$", replacement, text, count=1)
    if count == 0:
        updated = text.rstrip("\r\n") + f"\n{replacement}\n"

    if updated != text:
        MOD_INFO.write_text(updated, encoding="utf-8", newline="\n")
        return True
    return False


def main() -> int:
    if not RU_DIR.is_dir():
        raise SystemExit(f"Missing RU translation directory: {RU_DIR.relative_to(ROOT)}")

    total_fragments = 0
    total_conflicts = 0
    for table in TABLES:
        fragments, keys, conflicts = aggregate_table(table)
        total_fragments += fragments
        total_conflicts += len(conflicts)
        if fragments:
            print(f"{table}.json: {fragments} fragments -> {keys} keys")
        for conflict in conflicts:
            print(f"WARNING [{table}] conflicting fragment key: {conflict}", file=sys.stderr)

    version_changed = bump_mod_version()
    print(
        f"Aggregated {total_fragments} split translation fragments; "
        f"fragment conflicts={total_conflicts}; modversion={'updated' if version_changed else 'current'}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
