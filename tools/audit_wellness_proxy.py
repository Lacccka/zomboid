#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "WorkshopProjects/LCCB4220WellnessCompat/Contents/mods/LCCB4220WellnessCompat/42"
PERKS = BASE / "media/perks.txt"
CONTRACT = BASE / "media/lua/client/zzy_LCC_LifestyleYogaContract.lua"

errors = []

if not PERKS.is_file():
    errors.append("missing Wellness media/perks.txt")
else:
    text = PERKS.read_text(encoding="utf-8")
    perk_names = re.findall(r"(?m)^\s*perk\s+([A-Za-z0-9_]+)\s*$", text)
    if perk_names != ["Yoga"]:
        errors.append(f"Wellness perks.txt must declare only Yoga; found {perk_names}")

    if not re.search(r"(?m)^\s*parent\s*=\s*Lifestyle\s*,?\s*$", text):
        errors.append("Yoga proxy parent must be Lifestyle")

    if not re.search(r"(?m)^\s*translation\s*=\s*Yoga\s*,?\s*$", text):
        errors.append("Yoga proxy translation key must be Yoga")

    for key in ("xp1", "xp2", "xp3", "xp4", "xp5", "xp6", "xp7", "xp8", "xp9", "xp10"):
        if not re.search(rf"(?m)^\s*{key}\s*=\s*0\s*,?\s*$", text):
            errors.append(f"Yoga UI proxy must keep {key}=0")

    forbidden = ("perk Lifestyle", "perk Art", "perk Cleaning", "perk Dancing", "perk Meditation", "perk Music")
    for marker in forbidden:
        if marker in text:
            errors.append(f"Wellness split must not reproduce upstream declaration: {marker}")

if not CONTRACT.is_file():
    errors.append("missing Yoga runtime contract guard")
else:
    text = CONTRACT.read_text(encoding="utf-8")
    required_markers = (
        'FEATURE = "lifestyle.yoga-progress-ui"',
        "Perks.Yoga",
        "getParent",
        'parentId == "Lifestyle"',
        "Guard.disable",
        "Events.OnGameStart",
    )
    for marker in required_markers:
        if marker not in text:
            errors.append(f"Yoga contract guard missing marker: {marker}")

if errors:
    print("Wellness proxy audit FAILED:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("Wellness proxy audit OK: Yoga-only declaration, Lifestyle parent, zero proxy XP, runtime contract guard present")
