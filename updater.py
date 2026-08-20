import shutil
import subprocess
import threading
import tkinter as tk
import zipfile

from datetime import datetime
from pathlib import Path
from tkinter import messagebox, ttk

# ============================================================
# Lacccka B42.20 Local Server Tools
# ============================================================

# ------------------------------------------------------------
# Patch paths
# ------------------------------------------------------------

REPOSITORY_ROOT = Path(r"C:\zomboid")
SOURCE_ROOT = Path(r"C:\zomboid\WorkshopPatches")
MODS_ROOT = Path(r"C:\Users\user\Zomboid\mods")


# ------------------------------------------------------------
# Project Zomboid Dedicated Server
# ------------------------------------------------------------

SERVER_BAT = Path(
    r"C:\Program Files (x86)\Steam\steamapps\common"
    r"\Project Zomboid Dedicated Server\StartServer64.bat"
)


# ------------------------------------------------------------
# Logs
# ------------------------------------------------------------

ZOMBOID_ROOT = Path(r"C:\Users\user\Zomboid")
LOGS_ROOT = ZOMBOID_ROOT / "Logs"

SERVER_CONSOLE_LOG = ZOMBOID_ROOT / "server-console.txt"
CLIENT_CONSOLE_LOG = ZOMBOID_ROOT / "console.txt"

LOG_ARCHIVE_ROOT = REPOSITORY_ROOT / "CollectedLogs"


# ============================================================
# Helpers
# ============================================================


def is_subpath(path: Path, root: Path) -> bool:
    """
    Safety check:
    path must be located inside root, but must not be root itself.
    """

    try:
        resolved_path = path.resolve()
        resolved_root = root.resolve()

        if resolved_path == resolved_root:
            return False

        resolved_path.relative_to(resolved_root)
        return True

    except ValueError:
        return False


def replace_directory(source: Path, destination: Path) -> None:
    """
    Completely replace one mod directory.

    Only directories inside MODS_ROOT may be deleted/replaced.
    """

    if not source.is_dir():
        raise RuntimeError(f"Источник не найден:\n{source}")

    if not is_subpath(destination, MODS_ROOT):
        raise RuntimeError(f"Небезопасный путь назначения:\n{destination}")

    destination.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    if destination.exists():
        shutil.rmtree(destination)

    shutil.copytree(
        source,
        destination,
    )


def get_latest_file(directory: Path, pattern: str):
    """
    Return the newest file matching pattern.

    Search is NOT recursive.
    Only files directly inside 'directory' are checked.
    """

    if not directory.is_dir():
        return None

    files = [path for path in directory.glob(pattern) if path.is_file()]

    if not files:
        return None

    return max(
        files,
        key=lambda path: path.stat().st_mtime,
    )


# ============================================================
# Application
# ============================================================


class ServerToolsApp(tk.Tk):

    def __init__(self):
        super().__init__()

        self.title("Lacccka B42.20 Server Tools")
        self.geometry("980x650")
        self.minsize(880, 580)

        self.status_var = tk.StringVar(value="Ожидание")

        self.progress_var = tk.DoubleVar(value=0)

        # Server process started from this GUI.
        self.server_process = None

        self.build_ui()
        self.update_server_buttons()

    # ========================================================
    # UI
    # ========================================================

    def build_ui(self):

        main = ttk.Frame(
            self,
            padding=14,
        )

        main.pack(
            fill="both",
            expand=True,
        )

        # ----------------------------------------------------
        # Title
        # ----------------------------------------------------

        title = ttk.Label(
            main,
            text="Lacccka B42.20 Local Server Tools",
            font=("Segoe UI", 16, "bold"),
        )

        title.pack(
            anchor="w",
            pady=(0, 12),
        )

        # ----------------------------------------------------
        # Paths
        # ----------------------------------------------------

        paths = ttk.LabelFrame(
            main,
            text="Пути",
            padding=10,
        )

        paths.pack(
            fill="x",
            pady=(0, 12),
        )

        self.add_path_row(
            paths,
            0,
            "Патчи:",
            SOURCE_ROOT,
        )

        self.add_path_row(
            paths,
            1,
            "Mods:",
            MODS_ROOT,
        )

        self.add_path_row(
            paths,
            2,
            "Сервер:",
            SERVER_BAT,
        )

        self.add_path_row(
            paths,
            3,
            "Логи:",
            LOGS_ROOT,
        )

        self.add_path_row(
            paths,
            4,
            "Архивы:",
            LOG_ARCHIVE_ROOT,
        )

        # ----------------------------------------------------
        # Patch controls
        # ----------------------------------------------------

        patch_frame = ttk.LabelFrame(
            main,
            text="Compatibility Patch",
            padding=10,
        )

        patch_frame.pack(
            fill="x",
            pady=(0, 10),
        )

        self.update_button = ttk.Button(
            patch_frame,
            text="Обновить mods",
            command=self.start_update,
            width=24,
        )

        self.update_button.pack(
            side="left",
        )

        # ----------------------------------------------------
        # Server controls
        # ----------------------------------------------------

        server_frame = ttk.LabelFrame(
            main,
            text="Dedicated Server",
            padding=10,
        )

        server_frame.pack(
            fill="x",
            pady=(0, 10),
        )

        self.start_server_button = ttk.Button(
            server_frame,
            text="Запустить сервер",
            command=self.start_server,
            width=24,
        )

        self.start_server_button.pack(
            side="left",
        )

        self.stop_server_button = ttk.Button(
            server_frame,
            text="Остановить сервер",
            command=self.stop_server,
            width=24,
        )

        self.stop_server_button.pack(
            side="left",
            padx=(8, 0),
        )

        # ----------------------------------------------------
        # Logs controls
        # ----------------------------------------------------

        logs_frame = ttk.LabelFrame(
            main,
            text="Логи",
            padding=10,
        )

        logs_frame.pack(
            fill="x",
            pady=(0, 10),
        )

        self.collect_logs_button = ttk.Button(
            logs_frame,
            text="Собрать логи ZIP",
            command=self.start_collect_logs,
            width=24,
        )

        self.collect_logs_button.pack(
            side="left",
        )

        # ----------------------------------------------------
        # Status
        # ----------------------------------------------------

        status_frame = ttk.Frame(main)

        status_frame.pack(
            fill="x",
            pady=(2, 6),
        )

        ttk.Label(
            status_frame,
            text="Статус:",
        ).pack(
            side="left",
        )

        ttk.Label(
            status_frame,
            textvariable=self.status_var,
            font=("Segoe UI", 9, "bold"),
        ).pack(
            side="left",
            padx=(6, 0),
        )

        # ----------------------------------------------------
        # Progress
        # ----------------------------------------------------

        self.progress = ttk.Progressbar(
            main,
            variable=self.progress_var,
            maximum=100,
            mode="determinate",
        )

        self.progress.pack(
            fill="x",
            pady=(0, 10),
        )

        # ----------------------------------------------------
        # Application log
        # ----------------------------------------------------

        log_frame = ttk.LabelFrame(
            main,
            text="Журнал",
            padding=8,
        )

        log_frame.pack(
            fill="both",
            expand=True,
        )

        self.log_text = tk.Text(
            log_frame,
            wrap="word",
            state="disabled",
            font=("Consolas", 9),
        )

        self.log_text.pack(
            side="left",
            fill="both",
            expand=True,
        )

        scrollbar = ttk.Scrollbar(
            log_frame,
            orient="vertical",
            command=self.log_text.yview,
        )

        scrollbar.pack(
            side="right",
            fill="y",
        )

        self.log_text.configure(yscrollcommand=scrollbar.set)

    def add_path_row(
        self,
        parent,
        row,
        label,
        path,
    ):

        ttk.Label(
            parent,
            text=label,
            width=12,
        ).grid(
            row=row,
            column=0,
            sticky="w",
            pady=2,
        )

        ttk.Label(
            parent,
            text=str(path),
        ).grid(
            row=row,
            column=1,
            sticky="w",
            pady=2,
        )

    # ========================================================
    # UI helpers
    # ========================================================

    def log(self, text):

        def append():

            self.log_text.configure(state="normal")

            self.log_text.insert(
                "end",
                text + "\n",
            )

            self.log_text.see("end")

            self.log_text.configure(state="disabled")

        self.after(
            0,
            append,
        )

    def clear_log(self):

        self.log_text.configure(state="normal")

        self.log_text.delete(
            "1.0",
            "end",
        )

        self.log_text.configure(state="disabled")

    def set_status(self, text):

        self.after(
            0,
            lambda: self.status_var.set(text),
        )

    def set_progress(self, value):

        self.after(
            0,
            lambda: self.progress_var.set(value),
        )

    def set_update_button_enabled(
        self,
        enabled,
    ):

        state = "normal" if enabled else "disabled"

        self.after(
            0,
            lambda: self.update_button.configure(state=state),
        )

    # ========================================================
    # Patch updater
    # ========================================================

    def get_projects(self):

        if not SOURCE_ROOT.is_dir():
            raise RuntimeError(f"Не найдена папка:\n{SOURCE_ROOT}")

        projects = sorted(
            [path for path in SOURCE_ROOT.iterdir() if path.is_dir()],
            key=lambda path: path.name.lower(),
        )

        if not projects:
            raise RuntimeError(f"В {SOURCE_ROOT} нет папок патчей.")

        return projects

    def build_jobs(self, projects):
        """
        Only copy:

        WorkshopPatches
          └─ Project
              └─ Contents
                  └─ mods
                      └─ SomeMod

        to:

        Zomboid
          └─ mods
              └─ SomeMod
        """

        jobs = []

        for project in projects:

            source_mods = project / "Contents" / "mods"

            if not source_mods.is_dir():
                continue

            mod_directories = sorted(
                [path for path in source_mods.iterdir() if path.is_dir()],
                key=lambda path: path.name.lower(),
            )

            for mod_dir in mod_directories:

                jobs.append(
                    (
                        mod_dir,
                        MODS_ROOT / mod_dir.name,
                    )
                )

        return jobs

    def start_update(self):

        self.set_update_button_enabled(False)

        self.progress_var.set(0)

        self.status_var.set("Обновление mods...")

        self.clear_log()

        thread = threading.Thread(
            target=self.update_all,
            daemon=True,
        )

        thread.start()

    def update_all(self):

        try:

            projects = self.get_projects()

            jobs = self.build_jobs(projects)

            if not jobs:
                raise RuntimeError(
                    "В Contents\\mods не найдено " "ни одной модификации."
                )

            self.log(f"Источник: {SOURCE_ROOT}")

            self.log(f"Mods: {MODS_ROOT}")

            self.log(f"Найдено проектов: " f"{len(projects)}")

            self.log(f"Найдено модов: " f"{len(jobs)}")

            self.log("")

            for index, (
                source,
                destination,
            ) in enumerate(
                jobs,
                start=1,
            ):

                self.set_status(
                    f"Mods: " f"{destination.name} " f"({index}/{len(jobs)})"
                )

                self.log(f"[MOD] {source}")

                self.log(f"   -> {destination}")

                replace_directory(
                    source,
                    destination,
                )

                self.log("   OK")

                self.log("")

                self.set_progress(index / len(jobs) * 100)

            self.set_status("Mods обновлены")

            self.log("Обновление локальных " "модов завершено успешно.")

            self.after(
                0,
                lambda: messagebox.showinfo(
                    "Готово",
                    "Локальные моды обновлены.",
                ),
            )

        except Exception as exc:

            self.set_status("Ошибка обновления")

            self.log("")
            self.log(f"[ERROR] {exc}")

            self.after(
                0,
                lambda exc=exc: messagebox.showerror(
                    "Ошибка обновления",
                    str(exc),
                ),
            )

        finally:

            self.set_update_button_enabled(True)

    # ========================================================
    # Server
    # ========================================================

    def server_is_running(self):

        return self.server_process is not None and self.server_process.poll() is None

    def update_server_buttons(self):

        running = self.server_is_running()

        start_state = "disabled" if running else "normal"

        stop_state = "normal" if running else "disabled"

        self.start_server_button.configure(state=start_state)

        self.stop_server_button.configure(state=stop_state)

    def start_server(self):

        if self.server_is_running():

            messagebox.showinfo(
                "Сервер",
                "Сервер уже запущен " "из этой программы.",
            )

            return

        if not SERVER_BAT.is_file():

            messagebox.showerror(
                "Ошибка",
                "Не найден StartServer64.bat:\n\n" f"{SERVER_BAT}",
            )

            return

        try:

            self.log("")
            self.log("========================================")

            self.log("[SERVER] Запуск сервера")

            self.log(f"[SERVER] {SERVER_BAT}")

            creation_flags = getattr(
                subprocess,
                "CREATE_NEW_PROCESS_GROUP",
                0,
            )

            self.server_process = subprocess.Popen(
                [
                    "cmd.exe",
                    "/d",
                    "/c",
                    f'call "{SERVER_BAT}"',
                ],
                cwd=str(SERVER_BAT.parent),
                stdin=subprocess.PIPE,
                # Сам Dedicated Server пишет свои
                # нормальные логи в Zomboid.
                #
                # Не держим PIPE для stdout/stderr,
                # чтобы процесс не завис из-за
                # заполненного буфера.
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                text=True,
                creationflags=creation_flags,
            )

            self.set_status("Сервер запущен")

            self.log(f"[SERVER] PID launcher: " f"{self.server_process.pid}")

            self.update_server_buttons()

            # Watch server without blocking UI.
            thread = threading.Thread(
                target=self.watch_server,
                args=(self.server_process,),
                daemon=True,
            )

            thread.start()

        except Exception as exc:

            self.server_process = None

            self.update_server_buttons()

            self.set_status("Ошибка запуска сервера")

            self.log(f"[SERVER ERROR] {exc}")

            messagebox.showerror(
                "Ошибка запуска сервера",
                str(exc),
            )

    def watch_server(
        self,
        process,
    ):

        exit_code = process.wait()

        # Make sure this is still the same
        # server process.
        if self.server_process is process:

            self.server_process = None

        self.log(f"[SERVER] Процесс завершён. " f"Код: {exit_code}")

        self.set_status("Сервер остановлен")

        self.after(
            0,
            self.update_server_buttons,
        )

    def stop_server(self):

        if not self.server_is_running():

            self.server_process = None

            self.update_server_buttons()

            messagebox.showinfo(
                "Сервер",
                "Сервер, запущенный из этой " "программы, не найден.",
            )

            return

        process = self.server_process

        try:

            if process.stdin is None:

                raise RuntimeError("stdin сервера недоступен.")

            # Project Zomboid Dedicated Server:
            # graceful shutdown command.
            process.stdin.write("quit\n")

            process.stdin.flush()

            self.set_status("Остановка сервера...")

            self.stop_server_button.configure(state="disabled")

            self.log("")
            self.log("[SERVER] Отправлена команда: quit")

            self.log("[SERVER] Сервер сохраняет мир " "и завершает работу...")

        except Exception as exc:

            self.log(f"[SERVER ERROR] " f"Не удалось отправить quit: " f"{exc}")

            self.set_status("Ошибка остановки")

            self.update_server_buttons()

            messagebox.showerror(
                "Ошибка остановки сервера",
                "Не удалось корректно отправить " "серверу команду quit.\n\n" f"{exc}",
            )

    # ========================================================
    # Log collector
    # ========================================================

    def start_collect_logs(self):

        self.collect_logs_button.configure(state="disabled")

        self.set_status("Сбор логов...")

        thread = threading.Thread(
            target=self.collect_logs,
            daemon=True,
        )

        thread.start()

    def collect_logs(self):

        try:

            LOG_ARCHIVE_ROOT.mkdir(
                parents=True,
                exist_ok=True,
            )

            files_to_archive = []

            # ------------------------------------------------
            # Current console files
            # ------------------------------------------------

            for log_file in (
                SERVER_CONSOLE_LOG,
                CLIENT_CONSOLE_LOG,
            ):

                if log_file.is_file():

                    files_to_archive.append(log_file)

                else:

                    self.log(f"[LOGS] Не найден: " f"{log_file}")

            # ------------------------------------------------
            # Latest server DebugLog
            #
            # Example:
            # 2026-08-20_19-19_DebugLog-server.txt
            # ------------------------------------------------

            latest_server_debug = get_latest_file(
                LOGS_ROOT,
                "*_DebugLog-server.txt",
            )

            if latest_server_debug:

                files_to_archive.append(latest_server_debug)

            else:

                self.log("[LOGS] Не найден " "*_DebugLog-server.txt")

            # ------------------------------------------------
            # Latest client DebugLog
            #
            # Example:
            # 2026-08-20_19-21_DebugLog.txt
            # ------------------------------------------------

            latest_client_debug = get_latest_file(
                LOGS_ROOT,
                "*_DebugLog.txt",
            )

            if latest_client_debug:

                files_to_archive.append(latest_client_debug)

            else:

                self.log("[LOGS] Не найден " "*_DebugLog.txt")

            # ------------------------------------------------
            # Remove accidental duplicates
            # ------------------------------------------------

            unique_files = []

            seen = set()

            for path in files_to_archive:

                resolved = path.resolve()

                if resolved in seen:
                    continue

                seen.add(resolved)

                unique_files.append(path)

            files_to_archive = unique_files

            if not files_to_archive:

                raise RuntimeError("Не найдено ни одного " "файла для архивации.")

            # ------------------------------------------------
            # Archive
            # ------------------------------------------------

            timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

            archive_path = LOG_ARCHIVE_ROOT / f"ZomboidLogs_{timestamp}.zip"

            self.log("")
            self.log("========================================")

            self.log("[LOGS] Сбор актуальных логов")

            self.log(f"[LOGS] Архив: {archive_path}")

            self.log("")

            with zipfile.ZipFile(
                archive_path,
                mode="w",
                compression=zipfile.ZIP_DEFLATED,
            ) as archive:

                for log_file in files_to_archive:

                    self.log(f"[LOGS] + {log_file}")

                    archive.write(
                        log_file,
                        arcname=log_file.name,
                    )

            archive_size_mb = archive_path.stat().st_size / 1024 / 1024

            self.log("")

            self.log(f"[LOGS] Готово: " f"{archive_path.name}")

            self.log(f"[LOGS] Файлов: " f"{len(files_to_archive)}")

            self.log(f"[LOGS] Размер: " f"{archive_size_mb:.2f} MB")

            self.set_status("Логи собраны")

            self.after(
                0,
                lambda: messagebox.showinfo(
                    "Логи собраны",
                    "Архив создан:\n\n" f"{archive_path}",
                ),
            )

        except Exception as exc:

            self.set_status("Ошибка сбора логов")

            self.log("")
            self.log(f"[LOG ERROR] {exc}")

            self.after(
                0,
                lambda exc=exc: messagebox.showerror(
                    "Ошибка сбора логов",
                    str(exc),
                ),
            )

        finally:

            self.after(
                0,
                lambda: self.collect_logs_button.configure(state="normal"),
            )


# ============================================================
# Main
# ============================================================

if __name__ == "__main__":

    app = ServerToolsApp()
    app.mainloop()
