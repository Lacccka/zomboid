from __future__ import annotations

import os
from pathlib import Path
from collections import Counter
from datetime import datetime

# ============================================================
# НАСТРОЙКИ
# ============================================================

CLIENT_ROOT = Path(r"C:\Program Files (x86)\Steam\steamapps\common\ProjectZomboid")

SERVER_ROOT = Path(
    r"C:\Program Files (x86)\Steam\steamapps\common\Project Zomboid Dedicated Server"
)

OUTPUT_FILE = Path(__file__).resolve().parent / "pz_structure_report.txt"


# Файлы, которые полезно показать поимённо.
IMPORTANT_EXTENSIONS = {
    ".jar",
    ".class",
    ".bat",
    ".cmd",
    ".sh",
    ".json",
    ".xml",
    ".ini",
    ".cfg",
    ".properties",
    ".yml",
    ".yaml",
}

IMPORTANT_NAMES = {
    "projectzomboid.jar",
    "startserver64.bat",
    "startserver64_nosteam.bat",
    "projectzomboid64.exe",
}


# ============================================================
# ВСПОМОГАТЕЛЬНОЕ
# ============================================================


def human_size(size: int) -> str:
    units = ("B", "KB", "MB", "GB", "TB")
    value = float(size)

    for unit in units:
        if value < 1024 or unit == units[-1]:
            if unit == "B":
                return f"{int(value)} {unit}"
            return f"{value:.2f} {unit}"
        value /= 1024

    return f"{size} B"


def safe_file_size(path: Path) -> int:
    try:
        return path.stat().st_size
    except (OSError, PermissionError):
        return 0


def is_important_file(path: Path) -> bool:
    name = path.name.lower()

    return name in IMPORTANT_NAMES or path.suffix.lower() in IMPORTANT_EXTENSIONS


def relative_display(root: Path, path: Path) -> str:
    try:
        rel = path.relative_to(root)
        return "." if str(rel) == "." else str(rel)
    except ValueError:
        return str(path)


# ============================================================
# СКАНИРОВАНИЕ
# ============================================================


def scan_installation(label: str, root: Path) -> list[str]:
    out: list[str] = []

    out.append("=" * 100)
    out.append(label)
    out.append("=" * 100)
    out.append(f"ROOT: {root}")
    out.append("")

    if not root.exists():
        out.append("[ERROR] Directory does not exist.")
        out.append("")
        return out

    if not root.is_dir():
        out.append("[ERROR] Path is not a directory.")
        out.append("")
        return out

    total_files = 0
    total_dirs = 0
    total_size = 0

    global_extensions: Counter[str] = Counter()
    important_files: list[tuple[str, int]] = []

    tree_lines: list[str] = []

    for current_dir, dir_names, file_names in os.walk(
        root,
        topdown=True,
        followlinks=False,
    ):
        dir_names.sort(key=str.lower)
        file_names.sort(key=str.lower)

        current = Path(current_dir)

        total_dirs += 1

        immediate_size = 0
        ext_counter: Counter[str] = Counter()

        for filename in file_names:
            file_path = current / filename
            size = safe_file_size(file_path)

            immediate_size += size
            total_size += size
            total_files += 1

            suffix = file_path.suffix.lower()
            extension = suffix if suffix else "<no extension>"

            ext_counter[extension] += 1
            global_extensions[extension] += 1

            if is_important_file(file_path):
                important_files.append((relative_display(root, file_path), size))

        rel = relative_display(root, current)

        if rel == ".":
            depth = 0
            display_name = root.name
        else:
            depth = len(Path(rel).parts)
            display_name = Path(rel).name

        indent = "    " * depth

        ext_summary = ", ".join(
            f"{ext}={count}" for ext, count in ext_counter.most_common()
        )

        info = (
            f"dirs={len(dir_names)}, "
            f"files={len(file_names)}, "
            f"immediate-size={human_size(immediate_size)}"
        )

        if ext_summary:
            info += f", extensions: {ext_summary}"

        tree_lines.append(f"{indent}{display_name}/  [{info}]")

    # --------------------------------------------------------
    # ОБЩАЯ СТАТИСТИКА
    # --------------------------------------------------------

    out.append("SUMMARY")
    out.append("-" * 100)
    out.append(f"Directories : {total_dirs}")
    out.append(f"Files       : {total_files}")
    out.append(f"Total size  : {human_size(total_size)}")
    out.append("")

    out.append("FILE EXTENSIONS")
    out.append("-" * 100)

    for ext, count in global_extensions.most_common():
        out.append(f"{ext:<20} {count}")

    out.append("")

    # --------------------------------------------------------
    # ВАЖНЫЕ ФАЙЛЫ
    # --------------------------------------------------------

    out.append("IMPORTANT / SOURCE / CONFIG FILES")
    out.append("-" * 100)

    if important_files:
        for path, size in sorted(
            important_files,
            key=lambda x: x[0].lower(),
        ):
            out.append(f"{path}  [{human_size(size)}]")
    else:
        out.append("<none>")

    out.append("")

    # --------------------------------------------------------
    # ДЕРЕВО
    # --------------------------------------------------------

    out.append("DIRECTORY TREE")
    out.append("-" * 100)
    out.extend(tree_lines)
    out.append("")

    return out


# ============================================================
# MAIN
# ============================================================


def main() -> None:
    lines: list[str] = []

    lines.append("PROJECT ZOMBOID INSTALLATION STRUCTURE REPORT")
    lines.append(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    lines.append("")
    lines.append("This report does NOT modify or copy any game files.")
    lines.append("")

    lines.extend(
        scan_installation(
            "PROJECT ZOMBOID CLIENT",
            CLIENT_ROOT,
        )
    )

    lines.extend(
        scan_installation(
            "PROJECT ZOMBOID DEDICATED SERVER",
            SERVER_ROOT,
        )
    )

    OUTPUT_FILE.write_text(
        "\n".join(lines),
        encoding="utf-8-sig",
    )

    print()
    print("Done.")
    print(f"Report: {OUTPUT_FILE}")
    print()


if __name__ == "__main__":
    main()
