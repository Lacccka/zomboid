#!/usr/bin/env python3
"""Static contract audit for WorkshopPatches/GridInventorySort."""
from __future__ import annotations
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
PKG = ROOT / "WorkshopPatches" / "GridInventorySort"
MOD = PKG / "Contents" / "mods" / "LaccckaB4220GridInventorySort" / "42"
FILES = {
    "modinfo": MOD / "mod.info",
    "algorithm": MOD / "media/lua/client/LCC/GridAutoSort.lua",
    "pages": MOD / "media/lua/client/LCC/GridMultiPage.lua",
    "network": MOD / "media/lua/client/LCC/GridSortNetwork.lua",
    "state": MOD / "media/lua/shared/LCC/GridSortState.lua",
    "server": MOD / "media/lua/server/zzz_LCC_GridSortServer.lua",
    "bootstrap": MOD / "media/lua/client/zz_LCC_GridInventorySortV02.lua",
    "hook": MOD / "media/lua/client/zzz_LCC_GridInventorySort.lua",
    "en": MOD / "media/lua/shared/Translate/EN/UI.json",
    "ru": MOD / "media/lua/shared/Translate/RU/UI.json",
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
pages = require_file("pages")
network = require_file("network")
state = require_file("state")
server = require_file("server")
bootstrap = require_file("bootstrap")
hook = require_file("hook")
workshop = require_file("workshop")

require_markers("mod.info", modinfo, (
    "id=LaccckaB4220GridInventorySort", "modversion=0.2.0", "versionMin=42.20.0",
    "require=\\GridInventory", "loadafter=\\GridInventory",
))
require_markers("GridAutoSort.lua", algorithm, (
    'require("DataModel/GridCore")', 'require("DataModel/GridContainer")',
    'require("Algorithm/ItemFootprint")', 'require("LCC/GridSortState")',
    'require("LCC/GridSortNetwork")', "packBeam", "packMultiGreedy",
    "enumeratePlacements", "GridSortState.layoutHash", "GridSortNetwork.sendSort",
    "md.gridPage", "isNestedBagContainer", 'return false, "nested"',
    'return false, "floor"', 'return false, "corpse"', 'return false, "busy"',
    'return false, "search"',
))
require_markers("GridMultiPage.lua", pages, (
    "GridContainer._lccMultiPageInstalled", "originalRefresh", "GridSortState.MAX_PAGES",
    "self.unpositioned = stillUnpositioned", "multi-page overflow rescue installed",
))
require_markers("GridSortNetwork.lua", network, (
    "SORT_REQUEST", "PAGE_MOVE", "PAGE_REORDER", "REJECT_LAYOUT",
    "GridClientNetwork.sendItemMove = function", "GridClientNetwork.sendReorder = function",
    "page-aware MP network installed",
))
require_markers("GridSortState.lua", state, (
    'MODULE = "LCCGridInventorySort"', "layoutHash", "snapshot", "MAX_PAGES = 32",
))
require_markers("zzz_LCC_GridSortServer.lua", server, (
    "processSort", "processPageMove", "processPageReorder", "expectedHash",
    "stale-after-validate", "SYNC_LAYOUT", "REJECT_LAYOUT",
    "server CAS/page authority installed",
))
require_markers("zz_LCC_GridInventorySortV02.lua", bootstrap, (
    'require, "LCC/GridMultiPage"', 'require, "LCC/GridSortNetwork"',
))
require_markers("zzz_LCC_GridInventorySort.lua", hook, (
    'pcall(require, "LCC/GridAutoSort")',
    'ISInventoryWindowContainerControls.AddHandler(LCC_InventorySortHandler)',
    'ISLootWindowContainerControls.AddHandler(LCC_LootSortHandler)',
    "native footer handlers registered",
))

obsolete_visibility = MOD / "media/lua/client/zzzz_LCC_GridInventorySortVisibility.lua"
if obsolete_visibility.exists():
    errors.append("obsolete footer-repair hook must stay deleted: " + str(obsolete_visibility.relative_to(ROOT)))
for forbidden_marker in (
    "_lccGridSortArrangeWrapper", "_lccGridSortArrangeWrapped",
    "_lccGridSortVisibilityUpdateWrapper", "footer membership repaired",
):
    if forbidden_marker in hook:
        errors.append(f"zzz_LCC_GridInventorySort.lua: obsolete injection marker present: {forbidden_marker}")

require_markers("workshop.txt", workshop, ("id=0", "visibility=private", "original GridInventory mod is not included"))
expected_keys = {
    "UI_LCC_GridSort_Button", "UI_LCC_GridSort_Tooltip", "UI_LCC_GridSort_Floor",
    "UI_LCC_GridSort_Corpse", "UI_LCC_GridSort_Nested", "UI_LCC_GridSort_SearchFirst",
    "UI_LCC_GridSort_Busy", "UI_LCC_GridSort_Locked", "UI_LCC_GridSort_Nothing",
    "UI_LCC_GridSort_NoSpace", "UI_LCC_GridSort_Unavailable",
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

for forbidden in (
    "GridRender.lua", "GridContainer.lua", "GridCore.lua", "GridClientNetwork.lua",
    "GridServerNetwork.lua", "GridReorder.lua",
):
    matches = list((MOD / "media").rglob(forbidden)) if (MOD / "media").exists() else []
    if matches:
        errors.append(f"source-clean violation: addon contains upstream filename {forbidden}: " + ", ".join(str(p.relative_to(ROOT)) for p in matches))

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)
print("GridInventorySort static contract audit: OK")
