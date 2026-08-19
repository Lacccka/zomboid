#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "WorkshopProjects/LCCB4220SurvivorAIStability/Contents/mods/LCCB4220SurvivorAIStability/42/media/lua"

EMPTY = BASE / "server/zzz_LCC_BanditsEmptyServerWandererGuard.lua"
FARMING = BASE / "shared/zzz_LCC_BanditsFarmingGuard.lua"
CACHE = BASE / "client/zzz_LCC_BanditsZombieCacheGuard.lua"
DEDICATED = BASE / "server/zzz_LCC_BanditsDedicatedServerGuard.lua"

errors = []

for path in (EMPTY, FARMING, CACHE, DEDICATED):
    if not path.is_file():
        errors.append(f"missing survivor AI guard: {path.relative_to(ROOT)}")

if EMPTY.is_file():
    text = EMPTY.read_text(encoding="utf-8")
    if "isClient()" in text:
        errors.append("empty-server guard must not require client state on dedicated server")
    for marker in (
        'world:getGameMode() ~= "Multiplayer"',
        "getOnlinePlayers()",
        "players:size() == 0",
        "BanditCustom.ClanGetAll = function",
        "return originalClanGetAll(...)",
    ):
        if marker not in text:
            errors.append(f"empty-server guard missing marker: {marker}")

if FARMING.is_file():
    text = FARMING.read_text(encoding="utf-8")
    if "if not ok or skip then" in text:
        errors.append("farming wrapper must not swallow original callback when LCC precheck fails")
    for marker in (
        "if ok and skip then",
        "return original(...)",
        'Guard.protect(feature, methodName .. " precheck", shouldSkip, ...)',
        'installSkipWrapper(WATER, ZombieActions.WaterFarm, "onStart", shouldSkipWaterStart)',
        'installSkipWrapper(WATER, ZombieActions.WaterFarm, "onComplete", farmingUnavailable)',
        'installSkipWrapper(STOMP, ZombieActions.StompPlant, "onComplete", farmingUnavailable)',
    ):
        if marker not in text:
            errors.append(f"farming guard missing fail-open contract marker: {marker}")

if CACHE.is_file():
    text = CACHE.read_text(encoding="utf-8")
    for marker in (
        "if isServer() then return end",
        'Guard.safeRequire(FEATURE, "BanditCompatibility")',
        "BanditCompatibility.IsReanimatedForGrappleOnly = function",
        "BanditCompatibility.__LCCSquarelessUpdateGate",
        "if Guard.isEnabled(FEATURE) and zombie and not getSquareSafe(zombie) then",
        "removeTransientZombie(zombie)",
        "return true",
        "return originalIsReanimated(zombie, ...)",
        "Events.OnZombieUpdate.Add",
        "Events.EveryOneMinute.Add",
        "removeFromCaches",
        "recountLightCaches",
    ):
        if marker not in text:
            errors.append(f"cache/update guard missing marker: {marker}")

if DEDICATED.is_file():
    text = DEDICATED.read_text(encoding="utf-8")
    for marker in (
        "if not isServer() then return end",
        "BanditZombie = BanditZombie or {}",
        "if not BanditZombie.GetInstanceById then",
        "return nil",
    ):
        if marker not in text:
            errors.append(f"dedicated lookup guard missing marker: {marker}")

for forbidden in (
    BASE / "client/BanditZombie.lua",
    BASE / "server/BanditServerWanderers.lua",
    BASE / "shared/ZombieActions/ZAStompPlant.lua",
    BASE / "shared/ZombieActions/ZAWaterFarm.lua",
    BASE / "client/BanditUpdate.lua",
):
    if forbidden.exists():
        errors.append(f"full upstream override must not return: {forbidden.relative_to(ROOT)}")

if errors:
    print("Survivor AI guard audit FAILED:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("Survivor AI guard audit OK: source-clean, squareless-consumer gated, dedicated-MP safe, farming wrappers fail open")
