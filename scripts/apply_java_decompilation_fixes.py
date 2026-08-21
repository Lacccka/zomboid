#!/usr/bin/env python3
r"""Apply bytecode-verified cleanup fixes to the PZ Build 42.20.3 Java snapshot.

This intentionally touches only two small CFR-partial methods whose control flow
was reconstructed directly from the original JVM bytecode. It fails closed if
the Build/JAR provenance does not match the audited Build 42.20.3 snapshot or if
the target method no longer has the expected CFR warning/artifacts.

After applying, BUILD.txt is annotated and MANIFEST.sha256 is regenerated.
"""

from __future__ import annotations

import argparse
import hashlib
import re
import sys
from dataclasses import dataclass
from pathlib import Path

BUILD = "42.20.3"
EXPECTED_JAR_SHA256 = "bda809fb49004a07dbfc560d059c0ee58d0643ab0f33b53351b13bd62f1d8227"
WARNING_COMMENT = "    /*\n     * Unable to fully structure code\n     */\n"
QA_MARKER = "Bytecode-verified clean reconstructions:"
QA_REPORT = "../../docs/final-reports/java-decompilation-qa-42.20.3.md"


@dataclass(frozen=True)
class Fix:
    relative_path: str
    signature: str
    expected_artifacts: tuple[str, ...]
    replacement: str
    provenance_name: str


FIXES = (
    Fix(
        relative_path="java/zombie/characters/IsoPlayer.java",
        signature="public String getUsername(Boolean canShowFirstname, Boolean canShowDisguisedName) {",
        expected_artifacts=("block9:", "Unable to fully structure code"),
        provenance_name="zombie.characters.IsoPlayer.getUsername(Boolean, Boolean)",
        replacement='''    public String getUsername(Boolean canShowFirstname, Boolean canShowDisguisedName) {
        String nameStr = this.username;
        if (canShowDisguisedName) {
            this.updateDisguisedState();
            IsoGameCharacter isoGameCharacter = IsoCamera.getCameraCharacter();
            boolean bViewerIsAdmin = GameClient.client
                    && isoGameCharacter instanceof IsoPlayer player
                    && player.role.hasCapability(Capability.CanSeePlayersStats);
            if (this.isDisguised() && !bViewerIsAdmin) {
                nameStr = ServerOptions.getInstance().hideDisguisedUserName.getValue()
                        ? ""
                        : Translator.getText("IGUI_Disguised_Player_Name");
            } else if (canShowFirstname
                    && GameClient.client
                    && ServerOptions.instance.showFirstAndLastName.getValue()) {
                nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
                if (ServerOptions.instance.displayUserName.getValue()) {
                    nameStr = nameStr + " (" + this.username + ")";
                }
            }
        } else if (canShowFirstname
                && GameClient.client
                && ServerOptions.instance.showFirstAndLastName.getValue()) {
            nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
            if (ServerOptions.instance.displayUserName.getValue()) {
                nameStr = nameStr + " (" + this.username + ")";
            }
        }
        return nameStr;
    }''',
    ),
    Fix(
        relative_path="java/zombie/iso/fboRenderChunk/FBORenderCell.java",
        signature="private float calculateObjectTargetAlpha(IsoObject object) {",
        expected_artifacts=("block6:", "** GOTO", "Unable to fully structure code"),
        provenance_name="zombie.iso.fboRenderChunk.FBORenderCell.calculateObjectTargetAlpha(IsoObject)",
        replacement='''    private float calculateObjectTargetAlpha(IsoObject object) {
        int playerIndex = IsoCamera.frameState.playerIndex;
        ObjectRenderLayer renderLayer = object.getRenderInfo(playerIndex).layer;
        IsoObjectType t = IsoObjectType.MAX;
        if (object.sprite != null) {
            t = object.sprite.getTileType();
        }
        if (renderLayer == ObjectRenderLayer.MinusFloor
                || renderLayer == ObjectRenderLayer.MinusFloorSE
                || renderLayer == ObjectRenderLayer.Translucent
                || renderLayer == ObjectRenderLayer.TranslucentSE) {
            boolean isOpenDoor = object instanceof IsoDoor door && door.isOpen()
                    || object instanceof IsoThumpable isoThumpable && isoThumpable.open;
            if (isOpenDoor
                    && object.getProperties() != null
                    && !object.getProperties().has(IsoPropertyType.GARAGE_DOOR)) {
                return 0.6f;
            }
            boolean isWestDoorOrWall = t == IsoObjectType.doorFrW
                    || t == IsoObjectType.doorW
                    || object.sprite != null && object.sprite.cutW;
            boolean isNorthDoorOrWall = t == IsoObjectType.doorFrN
                    || t == IsoObjectType.doorN
                    || object.sprite != null && object.sprite.cutN;
            if (isWestDoorOrWall || isNorthDoorOrWall) {
                return this.calculateObjectTargetAlpha_DoorOrWall(object);
            }
            return this.calculateObjectTargetAlpha_NotDoorOrWall(object);
        }
        return 1.0f;
    }''',
    ),
)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify_build_info(build_info: Path) -> str:
    if not build_info.is_file():
        raise RuntimeError(f"BUILD.txt not found: {build_info}")
    text = build_info.read_text(encoding="utf-8")
    if f"Build: {BUILD}" not in text:
        raise RuntimeError(f"BUILD.txt is not Build {BUILD}; refusing to modify it.")
    m = re.search(r"Source JAR SHA-256:\s*\n([0-9a-fA-F]{64})", text)
    if not m:
        raise RuntimeError("BUILD.txt has no Java Source JAR SHA-256 provenance.")
    actual = m.group(1).lower()
    if actual != EXPECTED_JAR_SHA256:
        raise RuntimeError(
            "Java source provenance belongs to a different JAR.\n"
            f"Expected: {EXPECTED_JAR_SHA256}\nActual:   {actual}"
        )
    return text


def method_end(text: str, signature_index: int) -> int:
    brace = text.find("{", signature_index)
    if brace < 0:
        raise RuntimeError("Opening method brace not found.")

    depth = 0
    state = "code"
    i = brace
    while i < len(text):
        ch = text[i]
        nxt = text[i + 1] if i + 1 < len(text) else ""
        if state == "code":
            if ch == '"':
                state = "string"
            elif ch == "'":
                state = "char"
            elif ch == "/" and nxt == "/":
                state = "line_comment"
                i += 1
            elif ch == "/" and nxt == "*":
                state = "block_comment"
                i += 1
            elif ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    return i + 1
        elif state == "string":
            if ch == "\\":
                i += 1
            elif ch == '"':
                state = "code"
        elif state == "char":
            if ch == "\\":
                i += 1
            elif ch == "'":
                state = "code"
        elif state == "line_comment":
            if ch == "\n":
                state = "code"
        elif state == "block_comment":
            if ch == "*" and nxt == "/":
                state = "code"
                i += 1
        i += 1
    raise RuntimeError("Unbalanced Java method braces.")


def replace_fix(text: str, fix: Fix) -> tuple[str, str]:
    sig = text.find(fix.signature)
    if sig < 0:
        raise RuntimeError(f"Target method not found: {fix.provenance_name}")
    end = method_end(text, sig)

    warning_start = sig
    prefix = text[max(0, sig - len(WARNING_COMMENT) - 16):sig]
    if WARNING_COMMENT in prefix:
        candidate = text.rfind(WARNING_COMMENT, max(0, sig - len(WARNING_COMMENT) - 16), sig)
        if candidate >= 0:
            warning_start = candidate

    current_region = text[warning_start:end]
    replacement_normalized = fix.replacement.strip()
    if not any(marker in current_region for marker in fix.expected_artifacts):
        if replacement_normalized in current_region.strip():
            return text, "already-clean"
        raise RuntimeError(
            f"{fix.provenance_name}: expected CFR artifacts were not found. "
            "Refusing to replace an unknown source version."
        )

    new_text = text[:warning_start] + fix.replacement + text[end:]
    return new_text, "replaced"


def annotate_build(build_text: str) -> str:
    if QA_MARKER in build_text:
        return build_text
    block = (
        "\nBytecode QA:\n"
        f"Report: {QA_REPORT}\n\n"
        f"{QA_MARKER}\n"
        + "\n".join(f"- {fix.provenance_name}" for fix in FIXES)
        + "\n"
    )
    return build_text.rstrip() + "\n" + block


def write_manifest(root: Path) -> int:
    entries: list[str] = []
    for path in sorted(root.rglob("*")):
        if not path.is_file() or path.name == "MANIFEST.sha256":
            continue
        entries.append(f"{sha256_file(path)}  {path.relative_to(root).as_posix()}")
    (root / "MANIFEST.sha256").write_text("\n".join(entries) + "\n", encoding="utf-8")
    return len(entries)


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(description="Apply bytecode-verified Java cleanup fixes to PZ Build 42.20.3 snapshot.")
    p.add_argument("--repo-root", type=Path, default=Path(r"C:\zomboid"))
    p.add_argument("--check", action="store_true", help="Validate targets but do not write changes.")
    return p


def main() -> int:
    args = build_parser().parse_args()
    common = args.repo_root / "game_source" / f"common-{BUILD}"
    build_info = common / "BUILD.txt"
    build_text = verify_build_info(build_info)

    pending: list[tuple[Path, str, str]] = []
    for fix in FIXES:
        path = common / fix.relative_path
        if not path.is_file():
            raise RuntimeError(f"Target file not found: {path}")
        original = path.read_text(encoding="utf-8")
        updated, status = replace_fix(original, fix)
        print(f"{fix.provenance_name}: {status}")
        pending.append((path, original, updated))

    if args.check:
        print("Check passed; no files written.")
        return 0

    changed = 0
    for path, original, updated in pending:
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1

    new_build = annotate_build(build_text)
    if new_build != build_text:
        build_info.write_text(new_build, encoding="utf-8")

    manifest_count = write_manifest(common)
    print(f"Changed Java files: {changed}")
    print(f"Manifest entries: {manifest_count}")
    print("Bytecode-verified Java cleanup complete.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        raise SystemExit(1)
