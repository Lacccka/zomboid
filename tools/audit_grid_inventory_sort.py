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
    "algorithm": MOD / "media" / "lua" / "client" / "LCC" / "GridAutoSort.lua",
    "network": MOD / "media" / "lua" / "client" / "LCC" / "GridSortNetwork.lua",
    "paneux": MOD / "media" / "lua" / "client" / "LCC" / "GridPaneUX.lua",
    "continuous": MOD / "media" / "lua" / "shared" / "LCC" / "GridContinuousGrid.lua",
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
network = require_file("network")
paneux = require_file("paneux")
continuous = require_file("continuous")
state = require_file("state")
server = require_file("server")
hook = require_file("hook")
workshop = require_file("workshop")

require_markers(
    "mod.info",
    modinfo,
    (
        "id=LaccckaB4220GridInventorySort",
        "modversion=0.4.0",
        "versionMin=42.20.0",
        "require=\\GridInventory",
        "loadafter=\\GridInventory",
    ),
)

require_markers(
    "GridContinuousGrid.lua",
    continuous,
    (
        "GridContainer._lccContinuousGridInstalled",
        "MAX_HEIGHT = 60",
        "ROW_STEP = 4",
        "GridSortState.collectItems(container)",
        "GridContainer.getStackInfo(item)",
        "function GridContinuousGrid.getGridSize(container)",
        "function GridContinuousGrid.normalizeLegacyPages(container)",
        "math.ceil(desiredH / ROW_STEP) * ROW_STEP",
        "md.gridPage = nil",
        "self.grids = { GridCore.new(w, h) }",
        "adaptive continuous grid installed",
    ),
)

require_markers(
    "GridAutoSort.lua",
    algorithm,
    (
        'require("DataModel/GridCore")',
        'require("DataModel/GridContainer")',
        'require("Algorithm/ItemFootprint")',
        'require("LCC/GridSortState")',
        'require("LCC/GridSortNetwork")',
        "GridSortState.collectItems(container)",
        "GridContainer.getStackInfo",
        "grid:canPlaceItem",
        "lowerBoundArea",
        "packGreedy",
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

# Never reintroduce synchronous search matrices/page packing into the click path.
for forbidden_marker in (
    "packBeam",
    "cloneGrid",
    "beamWidth",
    "placementsPerState",
    "packGreedyMulti",
    "bestMultiPage",
    "MAX_PAGES",
):
    if forbidden_marker in algorithm:
        errors.append(f"GridAutoSort.lua: obsolete/heavy marker present: {forbidden_marker}")

require_markers(
    "GridSortState.lua",
    state,
    (
        'MODULE = "LCCGridInventorySort"',
        'SORT_PREPARE = "SortPrepare"',
        'SORT_TOKEN = "SortToken"',
        'SORT_REQUEST = "SortRequest"',
        'REJECT_LAYOUT = "RejectLayout"',
        "function GridSortState.authorityHash(container)",
        "gridManual",
        "function GridSortState.layoutHash(container)",
        "function GridSortState.snapshot(container)",
        "function GridSortState.isPlayerRootContainer(container)",
    ),
)
for forbidden_marker in ("PAGE_ASSIGN", "PAGE_MOVE", "PAGE_REORDER", "PAGE_CLEAR", "MAX_PAGES"):
    if forbidden_marker in state:
        errors.append(f"GridSortState.lua: pagination protocol marker present: {forbidden_marker}")

require_markers(
    "GridSortNetwork.lua",
    network,
    (
        'return "item:" .. tostring(containing:getID())',
        "function GridSortNetwork.sendSort",
        "pendingByRequest",
        "itemIds = itemIds",
        "GridSortState.COMMANDS.SORT_PREPARE",
        "GridSortState.COMMANDS.SORT_TOKEN",
        "expectedToken",
        "GridSortState.COMMANDS.SORT_REQUEST",
        "GridSortState.COMMANDS.REJECT_LAYOUT",
        "membership changed before commit",
        "simple token/CAS network installed",
    ),
)
for forbidden_marker in (
    "sendPageAssignments",
    "sendPageMove",
    "sendPageReorder",
    "containerHasExtraPages",
    "originalSendItemMove",
    "PAGE_ASSIGN",
    "PAGE_MOVE",
    "PAGE_REORDER",
):
    if forbidden_marker in network:
        errors.append(f"GridSortNetwork.lua: pagination/network wrapper marker present: {forbidden_marker}")

require_markers(
    "zzz_LCC_GridSortServer.lua",
    server,
    (
        'require("LCC/GridContinuousGrid")',
        "GridContainer._lccSafeBuildOccupancyInstalled",
        "function GridContainer.buildOccupancy(container, grid)",
        "GridSortState.isGridItem(item)",
        "ItemFootprint.getSize(item)",
        "server safe occupancy installed",
        "GridSortState.authorityHash(target)",
        "local function processSortPrepare",
        "sameIdSet(target, args.itemIds or {})",
        '"membership-before-sort"',
        "GridSortState.COMMANDS.SORT_TOKEN",
        "expectedToken",
        "GridCore.new(w, h)",
        "md.gridPage = nil",
        "server simple token/CAS authority installed",
    ),
)
for forbidden_marker in ("processPageAssign", "processPageMove", "processPageReorder", "PAGE_ASSIGN", "PAGE_MOVE"):
    if forbidden_marker in server:
        errors.append(f"zzz_LCC_GridSortServer.lua: pagination server marker present: {forbidden_marker}")

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
        'pcall(require, "LCC/GridContinuousGrid")',
        'pcall(require, "LCC/GridPaneUX")',
        'pcall(require, "LCC/GridAutoSort")',
        'require "ISUI/InventoryWindow/ISInventoryWindowContainerControls"',
        'require "ISUI/InventoryWindow/ISInventoryWindowControlHandler"',
        'require "ISUI/LootWindow/ISLootWindowContainerControls"',
        'require "ISUI/LootWindow/ISLootWindowObjectControlHandler"',
        'ISInventoryWindowContainerControls.AddHandler(LCC_InventorySortHandler)',
        'ISLootWindowContainerControls.AddHandler(LCC_LootSortHandler)',
        "GridAutoSort.canSort",
        "GridAutoSort.sort",
        "LCC_GRID_AUTO_SORT",
        "native footer handlers registered",
    ),
)
for forbidden_marker in ("GridMultiPage", "GridPageView", "_lccAllGridUis", "_lccSelectedGridPages"):
    if forbidden_marker in hook:
        errors.append(f"zzz_LCC_GridInventorySort.lua: pager marker present: {forbidden_marker}")

# Pager/multi-page files caused duplicate GridRender lifetime and renderer-ring
# overruns in the 2026-08-25 dedicated test. They must stay physically deleted.
for obsolete in (
    MOD / "media" / "lua" / "client" / "LCC" / "GridMultiPage.lua",
    MOD / "media" / "lua" / "client" / "LCC" / "GridPageView.lua",
    MOD / "media" / "lua" / "client" / "zzzz_LCC_GridInventorySortVisibility.lua",
):
    if obsolete.exists():
        errors.append("obsolete UI layer must stay deleted: " + str(obsolete.relative_to(ROOT)))

require_markers(
    "workshop.txt",
    workshop,
    ("id=0", "visibility=private", "original GridInventory mod is not included"),
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

# Source-clean addon rule: never publish replacement copies of upstream files.
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
