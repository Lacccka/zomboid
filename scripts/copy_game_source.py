#!/usr/bin/env python3
r"""
Build a compact Project Zomboid vanilla source snapshot for compatibility work.

Designed for:
  - Project Zomboid client Build 42.20.3
  - Project Zomboid Dedicated Server Build 42.20.3
  - repository C:\zomboid

The script:
  * NEVER modifies the Steam installations;
  * verifies the client/server projectzomboid.jar SHA-256 hashes;
  * verifies selected vanilla source/config files are byte-identical;
  * copies only source-like files useful for mod compatibility/debugging;
  * excludes Workshop content, Steam caches, binaries, textures, audio,
    map cell binaries, JRE files, logs and other runtime junk;
  * writes BUILD.txt and MANIFEST.sha256;
  * preserves an existing verified Java decompilation during normal --clean.

The raw projectzomboid.jar is NOT copied into the repository.
Java decompilation reads the JAR directly from the Steam installation.
"""

from __future__ import annotations

import argparse
import hashlib
import shutil
import sys
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable


DEFAULT_REPO_ROOT = Path(r"C:\zomboid")
DEFAULT_CLIENT_ROOT = Path(
    r"C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid"
)
DEFAULT_SERVER_ROOT = Path(
    r"C:\Program Files (x86)\Steam\steamapps\common\Project Zomboid Dedicated Server"
)
DEFAULT_BUILD = "42.20.3"

CLIENT_JAR = Path("projectzomboid.jar")
SERVER_JAR = Path("java") / "projectzomboid.jar"

TRANSLATION_LANGUAGES = {"EN", "RU"}
TRANSLATION_EXTENSIONS = {".json", ".tbx", ".txt"}

# Entire directories are copied only for these source/config extensions.
MEDIA_RULES: tuple[tuple[str, set[str]], ...] = (
    ("scripts", {".txt"}),
    ("actiongroups", {".xml"}),
    ("AnimSets", {".xml"}),
    ("animscript", {".xml"}),
    ("animstates", {".xml", ".animstates"}),
    ("models", {".txt"}),
    ("effects", {".txt"}),
    ("items", {".xml"}),
    ("clothing", {".xml"}),
    ("hairStyles", {".xml"}),
    ("radio", {".xml"}),
    ("voiceStyles", {".xml"}),
)

# Map source/metadata only. Explicitly excludes .lotpack/.lotheader/.bin,
# images, zips and huge world-map XML files.
MAP_EXTENSIONS = {".lua", ".info"}

MEDIA_ROOT_FILES = {
    "fileGuidTable.xml",
    "SpritePaddingSettings.xml",
}

CLIENT_RUNTIME_FILES = (
    "ProjectZomboid64.json",
    "ProjectZomboid64.bat",
    "ProjectZomboid64ShowConsole.bat",
    "ProjectZomboidOpenGLDebug64.bat",
    "ProjectZomboidServer.bat",
)

SERVER_RUNTIME_FILES = (
    "ProjectZomboid64.json",
    "start.bat",
    "StartServer64.bat",
    "StartServer64_nosteam.bat",
)

JAVA_METADATA_MARKER = "Java decompilation:"
SOURCE_JAR_SHA_LABEL = "Source JAR SHA-256:"


@dataclass(frozen=True)
class SelectedFile:
    source: Path
    relative: Path


def sha256_file(path: Path, chunk_size: int = 1024 * 1024) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as fh:
        while True:
            chunk = fh.read(chunk_size)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def format_bytes(value: int) -> str:
    units = ("B", "KB", "MB", "GB")
    size = float(value)
    for unit in units:
        if size < 1024.0 or unit == units[-1]:
            return f"{size:.2f} {unit}"
        size /= 1024.0
    return f"{value} B"


def require_directory(path: Path, label: str) -> None:
    if not path.is_dir():
        raise RuntimeError(f"{label} not found or is not a directory:\n{path}")


def require_file(path: Path, label: str) -> None:
    if not path.is_file():
        raise RuntimeError(f"{label} not found:\n{path}")


def iter_files_with_extensions(
    root: Path,
    relative_root: Path,
    extensions: set[str],
) -> Iterable[SelectedFile]:
    if not root.is_dir():
        return

    for path in sorted(root.rglob("*")):
        if path.is_file() and path.suffix.lower() in extensions:
            yield SelectedFile(
                source=path,
                relative=relative_root / path.relative_to(root),
            )


def iter_selected_media(install_root: Path) -> Iterable[SelectedFile]:
    media = install_root / "media"
    require_directory(media, "media directory")

    # Vanilla Lua code: client/server/shared.
    lua_root = media / "lua"
    if lua_root.is_dir():
        for path in sorted(lua_root.rglob("*.lua")):
            if path.is_file():
                yield SelectedFile(
                    source=path,
                    relative=Path("media") / "lua" / path.relative_to(lua_root),
                )

        # Keep only EN/RU translation data. Other languages account for most
        # of media/lua size and are unnecessary for this patch workflow.
        translate_root = lua_root / "shared" / "Translate"
        for lang in sorted(TRANSLATION_LANGUAGES):
            lang_root = translate_root / lang
            if not lang_root.is_dir():
                continue
            for path in sorted(lang_root.rglob("*")):
                if path.is_file() and path.suffix.lower() in TRANSLATION_EXTENSIONS:
                    yield SelectedFile(
                        source=path,
                        relative=(
                            Path("media")
                            / "lua"
                            / "shared"
                            / "Translate"
                            / lang
                            / path.relative_to(lang_root)
                        ),
                    )

    # Text/config subsystems useful for compatibility debugging.
    for dirname, extensions in MEDIA_RULES:
        source_root = media / dirname
        yield from iter_files_with_extensions(
            source_root,
            Path("media") / dirname,
            extensions,
        )

    # Map Lua and map.info only. This keeps objects.lua/spawn definitions and
    # metadata without copying several gigabytes of map cell binaries.
    maps_root = media / "maps"
    yield from iter_files_with_extensions(
        maps_root,
        Path("media") / "maps",
        MAP_EXTENSIONS,
    )

    for filename in sorted(MEDIA_ROOT_FILES):
        path = media / filename
        if path.is_file():
            yield SelectedFile(path, Path("media") / filename)


def selected_file_map(install_root: Path) -> dict[str, SelectedFile]:
    result: dict[str, SelectedFile] = {}
    for item in iter_selected_media(install_root):
        key = item.relative.as_posix()
        if key in result:
            raise RuntimeError(f"Duplicate selected relative path: {key}")
        result[key] = item
    return result


def compare_selected_sources(
    client_root: Path,
    server_root: Path,
) -> tuple[dict[str, SelectedFile], list[str]]:
    client = selected_file_map(client_root)
    server = selected_file_map(server_root)

    problems: list[str] = []

    client_paths = set(client)
    server_paths = set(server)

    for rel in sorted(client_paths - server_paths):
        problems.append(f"CLIENT ONLY: {rel}")
    for rel in sorted(server_paths - client_paths):
        problems.append(f"SERVER ONLY: {rel}")

    for rel in sorted(client_paths & server_paths):
        c = client[rel].source
        s = server[rel].source

        if c.stat().st_size != s.stat().st_size:
            problems.append(
                f"SIZE DIFF: {rel} "
                f"(client={c.stat().st_size}, server={s.stat().st_size})"
            )
            continue

        # Hash contents, not only sizes. This is intentionally strict.
        if sha256_file(c) != sha256_file(s):
            problems.append(f"CONTENT DIFF: {rel}")

    return client, problems


def is_within(path: Path, root: Path) -> bool:
    try:
        path.resolve().relative_to(root.resolve())
        return True
    except ValueError:
        return False


def safe_remove_generated(path: Path, game_source_root: Path) -> None:
    if not path.exists():
        return

    if path.resolve() == game_source_root.resolve():
        raise RuntimeError("Refusing to remove the whole game_source root.")

    if not is_within(path, game_source_root):
        raise RuntimeError(f"Refusing to remove path outside game_source:\n{path}")

    if path.is_dir():
        shutil.rmtree(path)
    else:
        path.unlink()


def directory_has_files(path: Path) -> bool:
    return path.is_dir() and any(item.is_file() for item in path.rglob("*"))


def read_java_decompilation_metadata(build_info: Path) -> str | None:
    if not build_info.is_file():
        return None

    text = build_info.read_text(encoding="utf-8")
    marker_index = text.find(JAVA_METADATA_MARKER)
    if marker_index < 0:
        return None

    return text[marker_index:].strip()


def metadata_source_jar_sha(metadata: str | None) -> str | None:
    if not metadata:
        return None

    lines = metadata.splitlines()
    for index, line in enumerate(lines):
        if line.strip() != SOURCE_JAR_SHA_LABEL:
            continue
        if index + 1 >= len(lines):
            return None
        value = lines[index + 1].strip().lower()
        if len(value) == 64 and all(ch in "0123456789abcdef" for ch in value):
            return value
        return None
    return None


def clean_generated_snapshot(
    common_dir: Path,
    client_dir: Path,
    server_dir: Path,
    game_source_root: Path,
    clean_java: bool,
) -> None:
    if clean_java:
        safe_remove_generated(common_dir, game_source_root)
    else:
        # Java is expensive to reconstruct and can contain manual Vineflower
        # fallback replacements. Normal --clean refreshes only imported media
        # and generated metadata while preserving verified Java output.
        safe_remove_generated(common_dir / "media", game_source_root)
        safe_remove_generated(common_dir / "BUILD.txt", game_source_root)
        safe_remove_generated(common_dir / "MANIFEST.sha256", game_source_root)

    safe_remove_generated(client_dir, game_source_root)
    safe_remove_generated(server_dir, game_source_root)


def copy_selected(
    selected: dict[str, SelectedFile],
    destination_root: Path,
) -> tuple[int, int]:
    copied = 0
    copied_bytes = 0

    for rel in sorted(selected):
        item = selected[rel]
        destination = destination_root / item.relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(item.source, destination)
        copied += 1
        copied_bytes += destination.stat().st_size

    return copied, copied_bytes


def copy_runtime_files(
    install_root: Path,
    names: Iterable[str],
    destination: Path,
) -> tuple[int, int, list[str]]:
    copied = 0
    copied_bytes = 0
    missing: list[str] = []

    for name in names:
        source = install_root / name
        if not source.is_file():
            missing.append(name)
            continue

        target = destination / name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        copied += 1
        copied_bytes += target.stat().st_size

    return copied, copied_bytes, missing


def write_manifest(root: Path) -> int:
    entries: list[str] = []

    for path in sorted(root.rglob("*")):
        if not path.is_file():
            continue
        if path.name == "MANIFEST.sha256":
            continue

        digest = sha256_file(path)
        rel = path.relative_to(root).as_posix()
        entries.append(f"{digest}  {rel}")

    manifest = root / "MANIFEST.sha256"
    manifest.write_text(
        "\n".join(entries) + ("\n" if entries else ""),
        encoding="utf-8",
    )
    return len(entries)


def write_build_info(
    common_dir: Path,
    client_dir: Path,
    server_dir: Path,
    build: str,
    client_root: Path,
    server_root: Path,
    client_jar_sha: str,
    server_jar_sha: str,
    selected_count: int,
    java_decompilation_metadata: str | None,
) -> None:
    timestamp = datetime.now(timezone.utc).isoformat(timespec="seconds")

    common_text = f"""Project Zomboid vanilla source snapshot
Build: {build}
Generated UTC: {timestamp}

Client installation:
{client_root}

Dedicated server installation:
{server_root}

Client projectzomboid.jar SHA-256:
{client_jar_sha}

Dedicated server projectzomboid.jar SHA-256:
{server_jar_sha}

Verification:
- JAR hashes are identical.
- {selected_count} selected vanilla source/config files were compared.
- Selected client/server files are byte-identical by SHA-256.

Snapshot policy:
- media/lua/**/*.lua
- media/lua/shared/Translate/EN and RU text translation files
- media/scripts/**/*.txt
- actiongroups / AnimSets / animscript / animstates source configs
- selected model/item/clothing/effect/radio XML/TXT configs
- media/maps/**/*.lua and *.info only
- media/fileGuidTable.xml
- media/SpritePaddingSettings.xml

Explicitly excluded:
- Steam Workshop content
- appcache / depotcache / userdata
- JRE / native binaries / DLLs
- logs
- textures / texturepacks / UI images
- audio / video
- 3D binary/model assets
- .lotpack / .lotheader / .bin map cells
- large world-map XML data
- raw projectzomboid.jar

Java decompilation target:
{common_dir / "java"}

The decompiler should read the JAR directly from:
{client_root / CLIENT_JAR}
"""

    if java_decompilation_metadata:
        common_text += "\n" + java_decompilation_metadata.strip() + "\n"

    (common_dir / "BUILD.txt").write_text(common_text, encoding="utf-8")

    client_text = f"""Project Zomboid client runtime overlay
Build: {build}
Generated UTC: {timestamp}

Runtime source:
{client_root}

Shared vanilla source:
../common-{build}

projectzomboid.jar SHA-256:
{client_jar_sha}
"""
    (client_dir / "BUILD.txt").write_text(client_text, encoding="utf-8")

    server_text = f"""Project Zomboid dedicated-server runtime overlay
Build: {build}
Generated UTC: {timestamp}

Runtime source:
{server_root}

Shared vanilla source:
../common-{build}

projectzomboid.jar SHA-256:
{server_jar_sha}
"""
    (server_dir / "BUILD.txt").write_text(server_text, encoding="utf-8")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Create a compact Project Zomboid vanilla source snapshot."
    )
    parser.add_argument("--build", default=DEFAULT_BUILD)
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=DEFAULT_REPO_ROOT,
        help=f"Repository root (default: {DEFAULT_REPO_ROOT})",
    )
    parser.add_argument(
        "--client-root",
        type=Path,
        default=DEFAULT_CLIENT_ROOT,
        help=f"Project Zomboid client root (default: {DEFAULT_CLIENT_ROOT})",
    )
    parser.add_argument(
        "--server-root",
        type=Path,
        default=DEFAULT_SERVER_ROOT,
        help=f"Dedicated server root (default: {DEFAULT_SERVER_ROOT})",
    )
    parser.add_argument(
        "--clean",
        action="store_true",
        help=(
            "Refresh generated media/runtime directories before copying. "
            "Preserves existing verified Java decompilation output."
        ),
    )
    parser.add_argument(
        "--clean-java",
        action="store_true",
        help=(
            "With --clean, also delete common-<build>/java. Use only when "
            "you intentionally want to decompile the JAR again."
        ),
    )
    return parser


def main() -> int:
    args = build_parser().parse_args()

    if args.clean_java and not args.clean:
        raise RuntimeError("--clean-java requires --clean.")

    repo_root: Path = args.repo_root
    client_root: Path = args.client_root
    server_root: Path = args.server_root
    build: str = args.build

    require_directory(repo_root, "Repository root")
    require_directory(client_root, "Project Zomboid client")
    require_directory(server_root, "Project Zomboid dedicated server")

    client_jar = client_root / CLIENT_JAR
    server_jar = server_root / SERVER_JAR
    require_file(client_jar, "Client projectzomboid.jar")
    require_file(server_jar, "Server projectzomboid.jar")

    print("Hashing Project Zomboid JARs...")
    client_jar_sha = sha256_file(client_jar)
    server_jar_sha = sha256_file(server_jar)

    print(f"Client JAR SHA-256 : {client_jar_sha}")
    print(f"Server JAR SHA-256 : {server_jar_sha}")

    if client_jar_sha != server_jar_sha:
        raise RuntimeError(
            "Client and dedicated-server projectzomboid.jar differ.\n"
            "Refusing to build a shared Java source snapshot. "
            "They must be decompiled separately."
        )

    print("\nComparing selected client/server vanilla source files...")
    selected, problems = compare_selected_sources(client_root, server_root)

    if problems:
        report = "\n".join(problems[:200])
        if len(problems) > 200:
            report += f"\n... and {len(problems) - 200} more differences."
        raise RuntimeError(
            "Selected client/server source files differ.\n"
            "Refusing to merge them into common source.\n\n"
            + report
        )

    print(f"Verified identical selected files: {len(selected)}")

    game_source_root = repo_root / "game_source"
    common_dir = game_source_root / f"common-{build}"
    client_dir = game_source_root / f"client-{build}"
    server_dir = game_source_root / f"dedicated-server-{build}"
    java_dir = common_dir / "java"

    java_metadata = read_java_decompilation_metadata(common_dir / "BUILD.txt")
    java_exists = directory_has_files(java_dir)
    metadata_jar_sha = metadata_source_jar_sha(java_metadata)

    if java_exists and not args.clean_java:
        if not java_metadata:
            raise RuntimeError(
                "Existing Java decompilation was found, but BUILD.txt has no "
                "Java provenance section. Refusing to overwrite metadata. "
                "Restore BUILD.txt provenance or use --clean --clean-java "
                "and decompile again."
            )
        if not metadata_jar_sha:
            raise RuntimeError(
                "Existing Java provenance does not contain a valid Source JAR "
                "SHA-256. Refusing to preserve an unverifiable Java tree."
            )
        if metadata_jar_sha != client_jar_sha.lower():
            raise RuntimeError(
                "Existing Java decompilation belongs to a different JAR.\n"
                f"Java source JAR: {metadata_jar_sha}\n"
                f"Current JAR    : {client_jar_sha}\n"
                "Use --clean --clean-java, then decompile the current JAR."
            )
        print("Java decompilation provenance verified; Java tree will be preserved.")

    if args.clean:
        print("\nCleaning generated snapshot data...")
        clean_generated_snapshot(
            common_dir=common_dir,
            client_dir=client_dir,
            server_dir=server_dir,
            game_source_root=game_source_root,
            clean_java=args.clean_java,
        )
        if args.clean_java:
            java_metadata = None
            java_exists = False
            print("Java decompilation removed by explicit --clean-java.")
        elif java_exists:
            print("Java decompilation preserved.")

    common_dir.mkdir(parents=True, exist_ok=True)
    client_runtime = client_dir / "runtime"
    server_runtime = server_dir / "runtime"
    client_runtime.mkdir(parents=True, exist_ok=True)
    server_runtime.mkdir(parents=True, exist_ok=True)

    print("\nCopying compact common source snapshot...")
    common_count, common_bytes = copy_selected(selected, common_dir)

    print("Copying client runtime metadata...")
    client_count, client_bytes, client_missing = copy_runtime_files(
        client_root,
        CLIENT_RUNTIME_FILES,
        client_runtime,
    )

    print("Copying dedicated-server runtime metadata...")
    server_count, server_bytes, server_missing = copy_runtime_files(
        server_root,
        SERVER_RUNTIME_FILES,
        server_runtime,
    )

    # Reserve the target directory for the Java decompilation step.
    java_dir.mkdir(parents=True, exist_ok=True)

    write_build_info(
        common_dir=common_dir,
        client_dir=client_dir,
        server_dir=server_dir,
        build=build,
        client_root=client_root,
        server_root=server_root,
        client_jar_sha=client_jar_sha,
        server_jar_sha=server_jar_sha,
        selected_count=len(selected),
        java_decompilation_metadata=java_metadata,
    )

    common_manifest_count = write_manifest(common_dir)
    client_manifest_count = write_manifest(client_dir)
    server_manifest_count = write_manifest(server_dir)

    print("\nDone.")
    print(f"Common source : {common_count} files, {format_bytes(common_bytes)}")
    print(f"Client runtime: {client_count} files, {format_bytes(client_bytes)}")
    print(f"Server runtime: {server_count} files, {format_bytes(server_bytes)}")
    print(f"Common path   : {common_dir}")
    print(f"Client path   : {client_dir}")
    print(f"Server path   : {server_dir}")
    print(
        "Manifest files: "
        f"common={common_manifest_count}, "
        f"client={client_manifest_count}, "
        f"server={server_manifest_count}"
    )

    if client_missing:
        print("\nClient runtime files not present (non-fatal):")
        for name in client_missing:
            print(f"  - {name}")

    if server_missing:
        print("\nServer runtime files not present (non-fatal):")
        for name in server_missing:
            print(f"  - {name}")

    if directory_has_files(java_dir):
        print("\nJava decompilation is present and included in the common manifest.")
    else:
        print(
            "\nNext step: decompile the verified common projectzomboid.jar "
            f"into {java_dir}."
        )

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RuntimeError as exc:
        print(f"\nERROR: {exc}", file=sys.stderr)
        raise SystemExit(1)
