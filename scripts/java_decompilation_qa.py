#!/usr/bin/env python3
r"""Bytecode QA for the Project Zomboid Build 42.20.3 Java source snapshot.

This script does not decompile classes. It extracts the JVM bytecode for the
known CFR-problematic Project Zomboid methods with javap, records reproducible
metrics and class hashes, and writes per-method disassemblies plus JSON/Markdown
summaries.

It fails closed if the input projectzomboid.jar is not the exact JAR audited for
Build 42.20.3 unless --allow-jar-mismatch is explicitly supplied.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import zipfile
from dataclasses import dataclass
from pathlib import Path

EXPECTED_JAR_SHA256 = "bda809fb49004a07dbfc560d059c0ee58d0643ab0f33b53351b13bd62f1d8227"


@dataclass(frozen=True)
class Target:
    class_name: str
    method_name: str
    header: str
    source_status: str


TARGETS = (
    Target("zombie.CollisionManager", "resolveContactsInternal", "private void resolveContactsInternal();", "CFR partial"),
    Target("zombie.CombatManager", "pressedAttack", "public void pressedAttack(zombie.characters.IsoPlayer);", "CFR partial"),
    Target("zombie.characters.IsoGameCharacter", "updateUserName", "protected void updateUserName();", "CFR partial"),
    Target("zombie.characters.IsoPlayer", "getUsername", "public java.lang.String getUsername(java.lang.Boolean, java.lang.Boolean);", "CFR partial"),
    Target("zombie.core.Core", "loadOptions_OLD", "public boolean loadOptions_OLD() throws java.io.IOException;", "CFR partial"),
    Target("zombie.gameStates.ChooseGameInfo", "readModInfoAux", "private static zombie.gameStates.ChooseGameInfo$Mod readModInfoAux(java.lang.String);", "Vineflower fallback"),
    Target("zombie.inventory.CompressIdenticalItems", "areItemsIdentical", "private static boolean areItemsIdentical(zombie.inventory.CompressIdenticalItems$PerThreadData, zombie.inventory.InventoryItem, zombie.inventory.InventoryItem) throws java.io.IOException;", "Vineflower fallback"),
    Target("zombie.inventory.ItemPickerJava", "doRollItemInternal", "private static void doRollItemInternal(zombie.inventory.ItemPickInfo, zombie.inventory.ItemPickerJava$ItemPickerContainer, zombie.inventory.ItemContainer, float, zombie.characters.IsoGameCharacter, boolean, zombie.inventory.ItemPickerJava$ItemPickerRoom, boolean);", "CFR partial"),
    Target("zombie.inventory.ItemPickerJava", "rollContainerItemInternal", "private static void rollContainerItemInternal(zombie.inventory.ItemPickInfo, zombie.inventory.types.InventoryContainer, zombie.characters.IsoGameCharacter, zombie.inventory.ItemPickerJava$ItemPickerContainer, boolean);", "CFR partial"),
    Target("zombie.iso.Helicopter", "update", "public void update();", "CFR partial"),
    Target("zombie.iso.IsoGridSquare", "renderMinusFloor", "public boolean renderMinusFloor(int, boolean, boolean, int, int, int, int, int, zombie.core.opengl.Shader);", "CFR partial"),
    Target("zombie.iso.fboRenderChunk.FBORenderCell", "calculateObjectTargetAlpha", "private float calculateObjectTargetAlpha(zombie.iso.IsoObject);", "CFR partial"),
    Target("zombie.pathfind.PolygonalMap2", "findPath", "private boolean findPath(zombie.pathfind.PathFindRequest, boolean);", "CFR partial"),
    Target("zombie.randomizedWorld.randomizedBuilding.RBTrashed", "trashHouse", "public void trashHouse(zombie.iso.BuildingDef);", "CFR partial"),
)

METHOD_HEADER_RE = re.compile(r"^  .+\([^;]*\)(?: throws .+)?;$")
INSTRUCTION_RE = re.compile(r"^\s*(\d+):\s+([a-z][a-z0-9_]*)\b")
SOURCE_LINE_RE = re.compile(r"^\s*line\s+(\d+):")
BRANCH_OP_RE = re.compile(r"^(?:if|goto|jsr)")
SWITCH_OPS = {"tableswitch", "lookupswitch"}


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def class_sha256(jar: zipfile.ZipFile, class_name: str) -> str:
    member = class_name.replace(".", "/") + ".class"
    try:
        data = jar.read(member)
    except KeyError as exc:
        raise RuntimeError(f"Class not found in JAR: {class_name}") from exc
    return hashlib.sha256(data).hexdigest()


def run_javap(javap: str, jar_path: Path, class_name: str) -> str:
    proc = subprocess.run(
        [javap, "-classpath", str(jar_path), "-p", "-c", "-l", "-s", class_name],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        encoding="utf-8",
        errors="replace",
    )
    if proc.returncode != 0:
        raise RuntimeError(
            f"javap failed for {class_name} (exit {proc.returncode}):\n{proc.stderr.strip()}"
        )
    return proc.stdout


def extract_method(javap_text: str, target: Target) -> str:
    lines = javap_text.splitlines()
    start = None
    for i, line in enumerate(lines):
        if line.strip() == target.header:
            start = i
            break
    if start is None:
        raise RuntimeError(
            f"Method header not found for {target.class_name}.{target.method_name}:\n"
            f"  expected: {target.header}"
        )

    end = len(lines)
    for i in range(start + 1, len(lines)):
        line = lines[i]
        if METHOD_HEADER_RE.match(line) or line.strip() == "static {};":
            end = i
            break
    return "\n".join(lines[start:end]).rstrip() + "\n"


def count_exception_handlers(method_text: str) -> int:
    lines = method_text.splitlines()
    in_table = False
    count = 0
    for line in lines:
        stripped = line.strip()
        if stripped == "Exception table:":
            in_table = True
            continue
        if not in_table:
            continue
        if stripped.startswith("from") or not stripped:
            continue
        if stripped.startswith("LineNumberTable:") or stripped.startswith("LocalVariableTable:"):
            break
        if re.match(r"^\d+\s+\d+\s+\d+\s+", stripped):
            count += 1
    return count


def method_metrics(method_text: str) -> dict[str, int | None]:
    instructions = branches = switches = invokes = 0
    source_lines: list[int] = []
    for line in method_text.splitlines():
        m = INSTRUCTION_RE.match(line)
        if m:
            op = m.group(2)
            instructions += 1
            if BRANCH_OP_RE.match(op):
                branches += 1
            if op in SWITCH_OPS:
                switches += 1
            if op.startswith("invoke"):
                invokes += 1
        sm = SOURCE_LINE_RE.match(line)
        if sm:
            source_lines.append(int(sm.group(1)))
    return {
        "instructions": instructions,
        "branches": branches,
        "switches": switches,
        "invokes": invokes,
        "exception_handlers": count_exception_handlers(method_text),
        "source_line_min": min(source_lines) if source_lines else None,
        "source_line_max": max(source_lines) if source_lines else None,
    }


def safe_filename(target: Target) -> str:
    return f"{target.class_name}.{target.method_name}.javap.txt"


def write_markdown(path: Path, jar_sha: str, records: list[dict]) -> None:
    rows = []
    for r in records:
        line_range = (
            f"{r['source_line_min']}-{r['source_line_max']}"
            if r["source_line_min"] is not None
            else "n/a"
        )
        rows.append(
            f"| `{r['class']}.{r['method']}` | {r['source_status']} | "
            f"{r['instructions']} | {r['branches']} | {r['switches']} | "
            f"{r['exception_handlers']} | {line_range} |"
        )
    text = "# Project Zomboid Build 42.20.3 Java bytecode QA\n\n"
    text += f"Source JAR SHA-256: `{jar_sha}`\n\n"
    text += (
        "Generated from the original `.class` files with `javap -p -c -l -s`. "
        "This is bytecode evidence, not another Java decompilation.\n\n"
    )
    text += "| Method | Snapshot source | Instructions | Branches | Switches | Exception handlers | Original line range |\n"
    text += "|---|---:|---:|---:|---:|---:|---:|\n"
    text += "\n".join(rows) + "\n"
    path.write_text(text, encoding="utf-8")


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(description="Audit known CFR-problematic PZ 42.20.3 methods against JVM bytecode.")
    p.add_argument("jar", type=Path, help="Path to the original projectzomboid.jar")
    p.add_argument("--output", type=Path, default=Path("build") / "java-decompilation-qa")
    p.add_argument("--javap", default=None, help="Path to javap. Defaults to javap from PATH.")
    p.add_argument("--expected-sha256", default=EXPECTED_JAR_SHA256)
    p.add_argument("--allow-jar-mismatch", action="store_true")
    return p


def main() -> int:
    args = build_parser().parse_args()
    jar_path: Path = args.jar
    if not jar_path.is_file():
        raise RuntimeError(f"JAR not found: {jar_path}")

    jar_sha = sha256_file(jar_path)
    expected = args.expected_sha256.lower()
    if jar_sha.lower() != expected and not args.allow_jar_mismatch:
        raise RuntimeError(
            "JAR SHA-256 mismatch. Refusing to audit a different build.\n"
            f"Expected: {expected}\nActual:   {jar_sha}"
        )

    javap = args.javap or shutil.which("javap")
    if not javap:
        raise RuntimeError("javap was not found. Pass --javap with the JDK javap executable.")

    output: Path = args.output
    bytecode_dir = output / "bytecode"
    bytecode_dir.mkdir(parents=True, exist_ok=True)

    class_outputs: dict[str, str] = {}
    records: list[dict] = []
    with zipfile.ZipFile(jar_path) as jar:
        for target in TARGETS:
            if target.class_name not in class_outputs:
                class_outputs[target.class_name] = run_javap(javap, jar_path, target.class_name)
            method_text = extract_method(class_outputs[target.class_name], target)
            filename = safe_filename(target)
            (bytecode_dir / filename).write_text(method_text, encoding="utf-8")
            record = {
                "class": target.class_name,
                "method": target.method_name,
                "source_status": target.source_status,
                "header": target.header,
                **method_metrics(method_text),
                "class_sha256": class_sha256(jar, target.class_name),
                "bytecode_file": f"bytecode/{filename}",
            }
            records.append(record)

    (output / "report.json").write_text(json.dumps(records, indent=2) + "\n", encoding="utf-8")
    write_markdown(output / "REPORT.md", jar_sha, records)
    print(f"JAR SHA-256: {jar_sha}")
    print(f"Audited methods: {len(records)}")
    print(f"Output: {output.resolve()}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        raise SystemExit(1)
