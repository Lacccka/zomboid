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
    "multipage": MOD / "media" / "lua" / "client" / "LCC" / "GridMultiPage.lua",
    "network": MOD / "media" / "lua" / "client" / "LCC" / "GridSortNetwork.lua",
    "paneux": MOD / "media" / "lua" / "client" / "LCC" / "GridPaneUX.lua",
    "state": MOD / "media" / "lua" / "shared" / "LCC" / "GridSortState.lua",
    "server": MOD / "media" / "lua" / "server" / "zzz_LCC_GridSortServer.lua",
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
multipage = require_file("multipage")
network = require_file("network")
paneux = require_file("paneux")
state = require_file("state")
server = require_file("server")
hook = require_file("hook")
workshop = require_file("workshop")

require_markers(
    "mod.info",
    modinfo,
    (
        "id=LaccckaB4220GridInventorySort",
        "modversion=0.2.1",
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
        'require("LCC/GridSortState")',
        'require("LCC/GridSortNetwork")',
        "GridContainer.getStackInfo",
        "grid:canPlaceItem",
        "EXTRA_PASS_BUDGET_MS",
        "lowerBoundArea",
        "packGreedySingle",
        "packGreedyMulti",
        "GridSortState.isPlayerRootContainer",
        "GridSortState.authorityHash",
        "GridSortNetwork.sendSort",
        "GridClientNetwork.markGridChanged",
        "isNestedBagContainer",
        'return false, "nested"',
        'return false, "floor"',
        'return false, "corpse"',
        'return false, "busy"',
        'return false, "search"',
    ),
)

# The blocking v0.2 beam implementation cloned/rescanned full grids per
# candidate and caused visible UI freezes. It must not silently return.
for forbidden_marker in (
    "packBeam",
    "cloneGrid",
    "beamWidth",
    "placementsPerState",
):
    if forbidden_marker in algorithm:
        errors.append(
            f"GridAutoSort.lua: blocking beam-search marker present: {forbidden_marker}"
        )

require_markers(
    "GridMultiPage.lua",
    multipage,
    (
        "GridContainer._lccMultiPageInstalled",
        "GridSortState.isPlayerRootContainer(container)",
        "GridSortState.MAX_PAGES",
        "GridCore.new(width, height)",
        "md.gridPage = page",
        "md.gridManual = nil",
        "autoAssignments",
        "GridSortNetwork.sendPageAssignments",
        "self.unpositioned = stillUnpositioned",
        "multi-page overflow rescue installed",
    ),
)

require_markers(
    "GridSortState.lua",
    state,
    (
        'MODULE = "LCCGridInventorySort"',
        'SORT_REQUEST = "SortRequest"',
        'PAGE_ASSIGN = "PageAssign"',
        'REJECT_LAYOUT = "RejectLayout"',
        "function GridSortState.authorityHash(container)",
        "gridManual",
        "function GridSortState.layoutHash(container)",
        "function GridSortState.snapshot(container)",
        "function GridSortState.isPlayerRootContainer(container)",
    ),
)

require_markers(
    "GridSortNetwork.lua",
    network,
    (
        "containerHasExtraPages",
        'return "item:" .. tostring(containing:getID())',
        "function GridSortNetwork.sendPageAssignments",
        "GridSortState.COMMANDS.PAGE_ASSIGN",
        "GridSortState.COMMANDS.SORT_REQUEST",
        "GridSortState.COMMANDS.PAGE_MOVE",
        "GridSortState.COMMANDS.PAGE_REORDER",
        "GridSortState.COMMANDS.REJECT_LAYOUT",
        "authoritative snapshot restored",
        "containerHasExtraPages(container) or oldPage > 1 or targetPage > 1",
        "page-aware MP network installed",
    ),
)

require_markers(
    "zzz_LCC_GridSortServer.lua",
    server,
    (
        "GridSortState.authorityHash(target)",
        "local function processPageAssign",
        "GridSortState.COMMANDS.PAGE_ASSIGN",
        "applyPosition(entry.item, entry.move, args.gridContainer, false, target)",
        'REJECT_LAYOUT, "stale"',
        'REJECT_LAYOUT, "stale-after-validate"',
        "sameItemSet",
        "GridCore.new(w, h)",
        "server CAS/page authority installed",
    ),
)

require_markers(
    "GridPaneUX.lua",
    paneux,
    (
        "_lccGridSortPaneUXInstalled",
        "_lccForcePaneScroll",
        "scrollHeight > viewHeight + 2",
        "unified inventory-pane scrolling installed",
    ),
)

require_markers(
    "zzz_LCC_GridInventorySort.lua",
    hook,
    (
        'pcall(require, "LCC/GridMultiPage")',
        'pcall(require, "LCC/GridPaneUX")',
        'pcall(require, "LCC/GridAutoSort")',
        'require "ISUI/InventoryWindow/ISInventoryWindowContainerControls"',
        'require "ISUI/InventoryWindow/ISInventoryWindowControlHandler"',
        'require "ISUI/LootWindow/ISLootWindowContainerControls"',
        'require "ISUI/LootWindow/ISLootWindowObjectControlHandler"',
        'ISInventoryWindowContainerControls.AddHandler(LCC_InventorySortHandler)',
        'ISLootWindowContainerControls.AddHandler(LCC_LootSortHandler)',
        'ISInventoryWindowControlHandler:derive("LCC_InventorySortHandler")',
        'ISLootWindowObjectControlHandler:derive("LCC_LootSortHandler")',
        "GridAutoSort.canSort",
        "GridAutoSort.sort",
        "LCC_GRID_AUTO_SORT",
        "native footer handlers registered",
    ),
)

# The old implementation wrapped arrange()/ISInventoryPage.update and fought
# vanilla footer rebuilds. It must never come back.
obsolete_visibility = (
    MOD / "media" / "lua" / "client" / "zzzz_LCC_GridInventorySortVisibility.lua"
)
if obsolete_visibility.exists():
    errors.append(
        "obsolete footer-repair hook must stay deleted: "
        + str(obsolete_visibility.relative_to(ROOT))
    )
for forbidden_marker in (
    "_lccGridSortArrangeWrapper",
    "_lccGridSortArrangeWrapped",
    "_lccGridSortVisibilityUpdateWrapper",
    "footer membership repaired",
):
    if forbidden_marker in hook:
        errors.append(
            f"zzz_LCC_GridInventorySort.lua: obsolete injection marker present: "
            f"{forbidden_marker}"
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
    "UI_LCC_GridSort_Nested",
    "UI_LCC_GridSort_SearchFirst",
    "UI_LCC_GridSort_Busy",
    "UI_LCC_GridSort_Locked",
    "UI_LCC_GridSort_Nothing",
    "UI_LCC_GridSort_NoSpace",
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
