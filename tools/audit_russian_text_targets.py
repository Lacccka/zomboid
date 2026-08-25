#!/usr/bin/env python3
"""Audit Russian coverage for the active mod localization overlay.

The audit compares the English dictionaries shipped by each target mod with
that mod's own Russian dictionaries plus the Lacccka overlay. This avoids
accidentally hiding a missing translation behind an unrelated mod that happens
to reuse the same global Project Zomboid text key.
"""

from __future__ import annotations

import json
import re
import sys
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PATCH_ROOT = ROOT / "WorkshopPatches/RussianTextFixes/Contents/mods/LaccckaB4220RussianText/42"
PATCH_EN = PATCH_ROOT / "media/lua/shared/Translate/EN"
PATCH_RU = PATCH_ROOT / "media/lua/shared/Translate/RU"
RUNTIME_LUA = PATCH_ROOT / "media/lua/client/LCC_RussianTextRuntime.lua"
PLACEHOLDER_RE = re.compile(r"%(?:%|\d+)")

ERRORS: list[str] = []
CHECKED_KEYS: set[tuple[str, str]] = set()


def load_json(path: Path) -> dict[str, str]:
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, json.JSONDecodeError) as exc:
        ERRORS.append(f"cannot load {path.relative_to(ROOT)}: {exc}")
        return {}
    if not isinstance(value, dict) or any(not isinstance(k, str) or not isinstance(v, str) for k, v in value.items()):
        ERRORS.append(f"expected a flat string dictionary: {path.relative_to(ROOT)}")
        return {}
    return value


def load_dir(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    if not path.is_dir():
        return result
    for file in sorted(path.glob("*.json")):
        for key, value in load_json(file).items():
            if key in result and result[key] != value:
                ERRORS.append(f"conflicting key {key!r} under {path.relative_to(ROOT)}")
            result[key] = value
    return result


def overlay_ru(*directories: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for directory in directories:
        result.update(load_dir(directory))
    result.update(load_dir(PATCH_RU))
    return result


def audit_keys(label: str, english: dict[str, str], russian: dict[str, str]) -> None:
    missing = sorted(set(english) - set(russian))
    placeholder_errors = []
    for key in set(english) & set(russian):
        if Counter(PLACEHOLDER_RE.findall(english[key])) != Counter(PLACEHOLDER_RE.findall(russian[key])):
            placeholder_errors.append(key)
        CHECKED_KEYS.add((label, key))
    if missing:
        ERRORS.append(f"{label}: {len(missing)} missing keys: {', '.join(missing[:8])}")
    if placeholder_errors:
        ERRORS.append(f"{label}: placeholder mismatch: {', '.join(sorted(placeholder_errors)[:8])}")
    status = "OK" if not missing and not placeholder_errors else "FAIL"
    print(f"[{status}] {label}: {len(english) - len(missing)}/{len(english)} keys")


def audit_directory(label: str, english_dir: Path, *russian_dirs: Path) -> None:
    audit_keys(label, load_dir(english_dir), overlay_ru(*russian_dirs))


def chimera_active_items() -> dict[str, str]:
    scripts = ROOT / "3766693411/mods/Chimera/42/media/scripts/clothing"
    item_re = re.compile(r"^\s*item\s+([A-Za-z_][A-Za-z0-9_]*)")
    result: dict[str, str] = {}
    for path in sorted(scripts.glob("*.txt")):
        for line in path.read_text(encoding="utf-8-sig", errors="replace").splitlines():
            match = item_re.match(line)
            if match:
                key = f"CHIMERA.{match.group(1)}"
                result[key] = key
    return result


def check_patch_collisions() -> None:
    seen: dict[str, tuple[str, Path]] = {}
    for file in sorted(PATCH_RU.glob("*.json")):
        for key, value in load_json(file).items():
            if key in seen and seen[key][0] != value:
                ERRORS.append(
                    f"patch collision {key!r}: {seen[key][1].name} != {file.name}"
                )
            seen[key] = (value, file)
    print(f"[{'OK' if not any('patch collision' in e for e in ERRORS) else 'FAIL'}] patch collisions: {len(seen)} unique keys")


def check_runtime_contract() -> None:
    en = load_dir(PATCH_EN)
    ru = load_dir(PATCH_RU)
    runtime_keys = {key: value for key, value in en.items() if "_LCC_" in key}
    audit_keys("runtime EN/RU", runtime_keys, ru)

    lua = RUNTIME_LUA.read_text(encoding="utf-8") if RUNTIME_LUA.is_file() else ""
    requirements = {
        "Explosives menu source": (
            ROOT / "3745718141/mods/Explosives/42/media/lua/client/FlareHandler.lua",
            "Ignite Flare",
        ),
        "PZK menu source": (
            ROOT / "3217685049/mods/PZKCarzoneWorkshop/42.0/media/lua/client/ISUI/pzkRustRemover_ISVehicleMenu_FillPartMenu.lua",
            "Remove Rust",
        ),
        "PZK tooltip source": (
            ROOT / "3217685049/mods/PZKCarzoneWorkshop/42.0/media/lua/client/ISUI/pzkRustRemover_ISVehicleMenu_FillPartMenu.lua",
            "Use Rust Solvent and Ripped Sheets to remove rust from this vehicle.",
        ),
        "Hauler force source": (
            ROOT / "3774448621/mods/Survivals.Hauler/42/media/lua/client/SurvivalsHauler/SurvivalsHauler_Client.lua",
            "Force Attach Hauler",
        ),
    }
    for label, (path, literal) in requirements.items():
        source = path.read_text(encoding="utf-8-sig", errors="replace") if path.is_file() else ""
        if literal not in source:
            ERRORS.append(f"{label}: upstream literal changed or source missing")
        if literal not in lua:
            ERRORS.append(f"{label}: runtime bridge does not contain expected literal")

    required_lua_tokens = (
        "ISContextMenu.addOption",
        "ISRadialMenu.addSlice",
        "ISVehicleMenu.FillPartMenu",
        "^Load (.+) %[(%d+)%]$",
        "^Unload (%d+): (.+)$",
    )
    for token in required_lua_tokens:
        if token not in lua:
            ERRORS.append(f"runtime bridge token missing: {token}")
    print(f"[{'OK' if not any('runtime bridge' in e or 'source' in e for e in ERRORS) else 'FAIL'}] runtime source contract")


def main() -> int:
    check_patch_collisions()

    backpack_en = ROOT / "3633421539/mods/BackpackSystem/42/media/lua/shared/Translate/EN"
    audit_directory("BackpackSystemB42", backpack_en)

    chimera_en = ROOT / "3766693411/mods/Chimera/42/media/lua/shared/Translate/EN"
    chimera_ru = overlay_ru()
    audit_keys("Chimera active items", chimera_active_items(), chimera_ru)
    for name in ("ContextMenu.json", "IG_UI.json"):
        audit_keys(f"Chimera {name[:-5]}", load_json(chimera_en / name), chimera_ru)

    audit_directory(
        "SVU3 Core",
        ROOT / "3403490889/mods/StandardizedVehicleUpgrades3Core/common/media/lua/shared/Translate/EN",
    )
    audit_directory(
        "SVU3 Vanilla",
        ROOT / "3304582091/mods/StandardizedVehicleUpgrades3Vanilla/common/media/lua/shared/Translate/EN",
    )
    audit_directory(
        "ImmersiveVehiclePaint",
        ROOT / "3464606086/mods/HDCP_ImmersiveVehiclePaint/common/media/lua/shared/Translate/EN",
    )

    mfs_base = ROOT / "3633421539/mods/Escape from Kentucky4215/42/media/lua/shared/Translate"
    mfs_fix = ROOT / "3780151182/mods/MFS_community_fix/42/media/lua/shared/Translate"
    mfs_english = load_dir(mfs_base / "EN")
    mfs_english.update(load_dir(mfs_fix / "EN"))
    audit_keys("ModernFirearmsSystem + community fix", mfs_english, overlay_ru(mfs_base / "RU", mfs_fix / "RU"))

    explosives = ROOT / "3745718141/mods/Explosives/42/media/lua/shared/translate"
    audit_directory("Explosives", explosives / "EN", explosives / "RU")

    pzk = ROOT / "3217685049/mods/PZKVanillaPlusCarPack/42.0/media/lua/shared/Translate"
    audit_directory("PZKVanillaPlusCarPack", pzk / "EN", pzk / "RU")

    pzk_workshop = ROOT / "3217685049/mods/PZKCarzoneWorkshop/42.0/media/lua/shared/Translate"
    audit_directory("PZKCarzoneWorkshop", pzk_workshop / "EN", pzk_workshop / "RU")

    ap = ROOT / "3766508989/mods/AP/42/media/lua/shared/Translate"
    audit_keys("AP pre-existing RU", load_dir(ap / "EN"), load_dir(ap / "RU"))

    hauler_expected = {
        "Base.SurvivalsHaulerCargoProxy": "",
        "Base.SurvivalsHaulerBodyVisual": "",
        "IGUI_VehicleNameTrailerSurvivalsHauler": "",
    }
    audit_keys("SurvivalsHauler static", hauler_expected, overlay_ru())
    check_runtime_contract()

    if ERRORS:
        print("\nErrors:")
        for error in ERRORS:
            print(f"- {error}")
        return 1
    print(f"\nAll checks passed ({len(CHECKED_KEYS)} target/key checks).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
