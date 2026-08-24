#!/usr/bin/env python3
"""Static contract audit for WorkshopPatches/GridInventorySort.

This intentionally does not emulate Project Zomboid/Kahlua. It catches package
regressions that are cheap to detect in CI/local development: missing files,
metadata drift, invalid translation JSON and loss of the critical GridInventory
integration seams used by the addon.
"""

from __future__ import annotations

import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
PKG = ROOT / "WorkshopPatches" / "GridInventorySort"
MOD = PKG / "Contents" / "mods" / "LaccckaB4220GridInventorySort" / "42"

FILES = {
    "modinfo": MOD / "mod.info",
    "algorithm": MOD / "media" / "lua" / "client" / "LCC" / "GridAutoSort.lua",
    "hook": MOD / "media" / "lua" / "client" / "zzz_LCC_GridInventorySort.lua",
    "en": MOD / "media" / "lua" / "shared" / "Translate" / "EN" / "UI.json",
    "ru": MOD / "media" / "lua" / "shared" / "Translate" / "RU" / "UI.json",
    "workshop": PKG / "workshop.txt",
}

errors: list[str] = []


def require_file(name: str) -> str:
    path = FILES[name]
    if not path.is_file():
        errors.append(f"missing file: {path.relative_to(ROOT)}")
        return ""
    text = path.read_text(encoding="utf-8")
    if not text.strip():
        errors.append(f"empty file: {path.relative_to(ROOT)}")
    return text


def require_markers(label: str, text: str, markers: tuple[str, ...]) -> None:
    for marker in markers:
        if marker not in text:
            errors.append(f"{label}: missing contract marker: {marker}")


modinfo = require_file("modinfo")
algorithm = require_file("algorithm")
hook = require_file("hook")
workshop = require_file("workshop")

require_markers(
    "mod.info",
    modinfo,
    (
        "id=LaccckaB4220GridInventorySort",
        "versionMin=42.20.0",
        "require=\\GridInventory",
        "loadafter=\\GridInventory",
    ),
)

require_markers(
    "GridAutoSort.lua",
    algorithm,
    (
        'require("DataModel/GridCore")',
        'require("DataModel/GridContainer")',
        'require("Algorithm/ItemFootprint")',
        'require("Network/GridClientNetwork")',
        "GridContainer.getStackInfo",
        "grid:findCompatibleStack",
        "grid:findFreeSpace",
        "GridContainer.containerSignature",
        "GridClientNetwork.sendReorder",
        "GridClientNetwork.markGridChanged",
        'return false, "floor"',
        'return false, "corpse"',
        'return false, "busy"',
        'return false, "search"',
    ),
)

require_markers(
    "zzz_LCC_GridInventorySort.lua",
    hook,
    (
        'pcall(require, "LCC/GridAutoSort")',
        "ISInventoryWindowContainerControls",
        "ISLootWindowContainerControls",
        "_lccGridSortArrangeWrapped",
        "GridAutoSort.canSort",
        "GridAutoSort.sort",
        "LCC_GRID_AUTO_SORT",
    ),
)

require_markers(
    "workshop.txt",
    workshop,
    (
        "id=0",
        "visibility=private",
        "original GridInventory mod is not included",
    ),
)

expected_keys = {
    "UI_LCC_GridSort_Button",
    "UI_LCC_GridSort_Tooltip",
    "UI_LCC_GridSort_Floor",
    "UI_LCC_GridSort_Corpse",
    "UI_LCC_GridSort_SearchFirst",
    "UI_LCC_GridSort_Busy",
    "UI_LCC_GridSort_Locked",
    "UI_LCC_GridSort_Unavailable",
}

for lang in ("en", "ru"):
    text = require_file(lang)
    if not text:
        continue
    try:
        data = json.loads(text)
    except json.JSONDecodeError as exc:
        errors.append(f"{lang}: invalid JSON: {exc}")
        continue
    missing = expected_keys - set(data)
    extra = set(data) - expected_keys
    if missing:
        errors.append(f"{lang}: missing translation keys: {sorted(missing)}")
    if extra:
        errors.append(f"{lang}: unexpected translation keys: {sorted(extra)}")

# Source-clean addon rule: never publish copies of upstream GridInventory files.
for forbidden in (
    "GridRender.lua",
    "GridContainer.lua",
    "GridCore.lua",
    "GridClientNetwork.lua",
    "GridServerNetwork.lua",
    "GridReorder.lua",
):
    matches = list((MOD / "media").rglob(forbidden)) if (MOD / "media").exists() else []
    if matches:
        errors.append(
            f"source-clean violation: addon contains upstream filename {forbidden}: "
            + ", ".join(str(p.relative_to(ROOT)) for p in matches)
        )

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)

print("GridInventorySort static contract audit: OK")
