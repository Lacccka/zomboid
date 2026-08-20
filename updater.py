import queue
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

REPOSITORY_ROOT = Path(r"C:\zomboid")
SOURCE_ROOT = REPOSITORY_ROOT / "WorkshopPatches"
MODS_ROOT = Path(r"C:\Users\user\Zomboid\mods")

SERVER_BAT = Path(
    r"C:\Program Files (x86)\Steam\steamapps\common"
    r"\Project Zomboid Dedicated Server\StartServer64.bat"
)
SERVER_NAME = "servertest"

ZOMBOID_ROOT = Path(r"C:\Users\user\Zomboid")
LOGS_ROOT = ZOMBOID_ROOT / "Logs"
SERVER_CONSOLE_LOG = ZOMBOID_ROOT / "server-console.txt"
CLIENT_CONSOLE_LOG = ZOMBOID_ROOT / "console.txt"
LOG_ARCHIVE_ROOT = REPOSITORY_ROOT / "CollectedLogs"

# GUI log buffering. Server output can be very chatty on large modpacks, so
# background threads enqueue lines and Tkinter renders them in batches.
LOG_FLUSH_INTERVAL_MS = 150
MAX_GUI_LOG_LINES = 5000
MAX_LOG_BATCH = 1000


def is_subpath(path: Path, root: Path) -> bool:
    """Return True only when path is a child of root (not root itself)."""
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
    """Completely replace one local mod directory."""
    if not source.is_dir():
        raise RuntimeError(f"Источник не найден:\n{source}")

    if not is_subpath(destination, MODS_ROOT):
        raise RuntimeError(f"Небезопасный путь назначения:\n{destination}")

    destination.parent.mkdir(parents=True, exist_ok=True)

    if destination.exists():
        shutil.rmtree(destination)

    shutil.copytree(source, destination)


def get_latest_file(directory: Path, pattern: str):
    """Return the newest matching file directly inside directory."""
    if not directory.is_dir():
        return None

    files = [path for path in directory.glob(pattern) if path.is_file()]
    if not files:
        return None

    return max(files, key=lambda path: path.stat().st_mtime)


def is_direct_mod_root(project: Path) -> bool:
    """Detect a direct or Build-42-versioned Project Zomboid mod root."""
    if (project / "mod.info").is_file():
        return True

    try:
        children = project.iterdir()
    except OSError:
        return False

    for child in children:
        if child.is_dir() and (child / "mod.info").is_file():
            return True

    return False


def decode_server_line(raw_line: bytes) -> str:
    """Decode mixed Java/CMD output without crashing the reader thread."""
    for encoding in ("utf-8", "cp866", "cp1251"):
        try:
            return raw_line.decode(encoding).rstrip("\r\n")
        except UnicodeDecodeError:
            pass

    return raw_line.decode("utf-8", errors="replace").rstrip("\r\n")


class ServerToolsApp(tk.Tk):
    def __init__(self):
        super().__init__()

        self.title("Lacccka B42.20 Server Tools")
        self.geometry("980x650")
        self.minsize(880, 580)

        self.status_var = tk.StringVar(value="Ожидание")
        self.progress_var = tk.DoubleVar(value=0)

        self.server_process = None
        self.server_pause_released = False
        self.server_java_exit_code = None

        # Thread-safe buffer for all GUI log messages. Background workers never
        # touch Tk widgets directly; the main Tk loop renders queued lines in
        # batches to reduce CPU usage and redraw overhead during server log spam.
        self.log_queue = queue.SimpleQueue()
        self.gui_log_line_count = 0

        self.build_ui()
        self.update_server_buttons()
        self.after(LOG_FLUSH_INTERVAL_MS, self.flush_log_queue)

    # ========================================================
    # UI
    # ========================================================

    def build_ui(self):
        main = ttk.Frame(self, padding=14)
        main.pack(fill="both", expand=True)

        ttk.Label(
            main,
            text="Lacccka B42.20 Local Server Tools",
            font=("Segoe UI", 16, "bold"),
        ).pack(anchor="w", pady=(0, 12))

        paths = ttk.LabelFrame(main, text="Пути", padding=10)
        paths.pack(fill="x", pady=(0, 12))

        self.add_path_row(paths, 0, "Патчи:", SOURCE_ROOT)
        self.add_path_row(paths, 1, "Mods:", MODS_ROOT)
        self.add_path_row(paths, 2, "Сервер:", SERVER_BAT)
        self.add_path_row(paths, 3, "Профиль:", SERVER_NAME)
        self.add_path_row(paths, 4, "Логи:", LOGS_ROOT)
        self.add_path_row(paths, 5, "Архивы:", LOG_ARCHIVE_ROOT)

        patch_frame = ttk.LabelFrame(main, text="Compatibility Patch", padding=10)
        patch_frame.pack(fill="x", pady=(0, 10))

        self.update_button = ttk.Button(
            patch_frame,
            text="Обновить mods",
            command=self.start_update,
            width=24,
        )
        self.update_button.pack(side="left")

        server_frame = ttk.LabelFrame(main, text="Dedicated Server", padding=10)
        server_frame.pack(fill="x", pady=(0, 10))

        self.start_server_button = ttk.Button(
            server_frame,
            text="Запустить сервер",
            command=self.start_server,
            width=24,
        )
        self.start_server_button.pack(side="left")

        self.stop_server_button = ttk.Button(
            server_frame,
            text="Остановить сервер",
            command=self.stop_server,
            width=24,
        )
        self.stop_server_button.pack(side="left", padx=(8, 0))

        logs_frame = ttk.LabelFrame(main, text="Логи", padding=10)
        logs_frame.pack(fill="x", pady=(0, 10))

        self.collect_logs_button = ttk.Button(
            logs_frame,
            text="Собрать логи ZIP",
            command=self.start_collect_logs,
            width=24,
        )
        self.collect_logs_button.pack(side="left")

        status_frame = ttk.Frame(main)
        status_frame.pack(fill="x", pady=(2, 6))

        ttk.Label(status_frame, text="Статус:").pack(side="left")
        ttk.Label(
            status_frame,
            textvariable=self.status_var,
            font=("Segoe UI", 9, "bold"),
        ).pack(side="left", padx=(6, 0))

        self.progress = ttk.Progressbar(
            main,
            variable=self.progress_var,
            maximum=100,
            mode="determinate",
        )
        self.progress.pack(fill="x", pady=(0, 10))

        log_frame = ttk.LabelFrame(main, text="Журнал", padding=8)
        log_frame.pack(fill="both", expand=True)

        self.log_text = tk.Text(
            log_frame,
            wrap="word",
            state="disabled",
            font=("Consolas", 9),
        )
        self.log_text.pack(side="left", fill="both", expand=True)

        scrollbar = ttk.Scrollbar(
            log_frame,
            orient="vertical",
            command=self.log_text.yview,
        )
        scrollbar.pack(side="right", fill="y")
        self.log_text.configure(yscrollcommand=scrollbar.set)

    def add_path_row(self, parent, row, label, path):
        ttk.Label(parent, text=label, width=12).grid(
            row=row,
            column=0,
            sticky="w",
            pady=2,
        )
        ttk.Label(parent, text=str(path)).grid(
            row=row,
            column=1,
            sticky="w",
            pady=2,
        )

    # ========================================================
    # UI helpers
    # ========================================================

    def log(self, text):
        """Queue one log message without touching Tkinter from worker threads."""
        self.log_queue.put(str(text))

    def flush_log_queue(self):
        """Render queued log messages in one Tkinter update."""
        lines = []

        for _ in range(MAX_LOG_BATCH):
            try:
                lines.append(self.log_queue.get_nowait())
            except queue.Empty:
                break

        if lines:
            batch_text = "\n".join(lines) + "\n"
            added_line_count = batch_text.count("\n")

            self.log_text.configure(state="normal")
            self.log_text.insert("end", batch_text)
            self.gui_log_line_count += added_line_count

            # Keep the GUI as a lightweight tail view. Full logs remain on disk.
            excess_lines = self.gui_log_line_count - MAX_GUI_LOG_LINES
            if excess_lines > 0:
                self.log_text.delete("1.0", f"{excess_lines + 1}.0")
                self.gui_log_line_count -= excess_lines

            self.log_text.see("end")
            self.log_text.configure(state="disabled")

        # If the server produced more than one batch, continue draining quickly;
        # otherwise stay on the low-frequency timer while idle.
        next_delay = 10 if not self.log_queue.empty() else LOG_FLUSH_INTERVAL_MS
        self.after(next_delay, self.flush_log_queue)

    def clear_log(self):
        # Drop messages waiting for display so an old operation cannot appear
        # immediately after the user clears the journal for a new operation.
        while True:
            try:
                self.log_queue.get_nowait()
            except queue.Empty:
                break

        self.log_text.configure(state="normal")
        self.log_text.delete("1.0", "end")
        self.log_text.configure(state="disabled")
        self.gui_log_line_count = 0

    def set_status(self, text):
        self.after(0, lambda: self.status_var.set(text))

    def set_progress(self, value):
        self.after(0, lambda: self.progress_var.set(value))

    def set_update_button_enabled(self, enabled):
        state = "normal" if enabled else "disabled"
        self.after(0, lambda: self.update_button.configure(state=state))

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
        """Build copy jobs for Workshop-ready and direct/versioned mods."""
        jobs = []
        skipped = []

        for project in projects:
            source_mods = project / "Contents" / "mods"

            if source_mods.is_dir():
                mod_directories = sorted(
                    [path for path in source_mods.iterdir() if path.is_dir()],
                    key=lambda path: path.name.lower(),
                )

                if not mod_directories:
                    skipped.append(
                        (project, "Contents\\mods существует, но не содержит модов")
                    )
                    continue

                for mod_dir in mod_directories:
                    jobs.append((mod_dir, MODS_ROOT / mod_dir.name))

                continue

            if is_direct_mod_root(project):
                jobs.append((project, MODS_ROOT / project.name))
                continue

            skipped.append(
                (project, "не найден Contents\\mods или mod.info в корне/версии")
            )

        destinations = {}
        for source, destination in jobs:
            key = str(destination).casefold()
            previous = destinations.get(key)

            if previous is not None:
                raise RuntimeError(
                    "Два источника пытаются обновить один каталог:\n"
                    f"{previous}\n{source}\n-> {destination}"
                )

            destinations[key] = source

        return jobs, skipped

    def start_update(self):
        self.set_update_button_enabled(False)
        self.progress_var.set(0)
        self.status_var.set("Обновление mods...")
        self.clear_log()

        threading.Thread(
            target=self.update_all,
            daemon=True,
        ).start()

    def update_all(self):
        try:
            projects = self.get_projects()
            jobs, skipped = self.build_jobs(projects)

            if not jobs:
                raise RuntimeError("Не найдено ни одной модификации для копирования.")

            self.log(f"Источник: {SOURCE_ROOT}")
            self.log(f"Mods: {MODS_ROOT}")
            self.log(f"Найдено проектов: {len(projects)}")
            self.log(f"Найдено модов: {len(jobs)}")

            if skipped:
                self.log(f"Пропущено каталогов: {len(skipped)}")
                for project, reason in skipped:
                    self.log(f"[SKIP] {project.name}: {reason}")

            self.log("")

            for index, (source, destination) in enumerate(jobs, start=1):
                self.set_status(
                    f"Mods: {destination.name} ({index}/{len(jobs)})"
                )

                self.log(f"[MOD] {source}")
                self.log(f"   -> {destination}")

                replace_directory(source, destination)

                self.log("   OK")
                self.log("")
                self.set_progress(index / len(jobs) * 100)

            self.set_status("Mods обновлены")
            self.log("Обновление локальных модов завершено успешно.")

            self.after(
                0,
                lambda: messagebox.showinfo(
                    "Готово",
                    f"Локальные моды обновлены: {len(jobs)}.",
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
        self.start_server_button.configure(
            state="disabled" if running else "normal"
        )
        self.stop_server_button.configure(
            state="normal" if running else "disabled"
        )

    def start_server(self):
        if self.server_is_running():
            messagebox.showinfo(
                "Сервер",
                "Сервер уже запущен из этой программы.",
            )
            return

        if not SERVER_BAT.is_file():
            messagebox.showerror(
                "Ошибка",
                f"Не найден StartServer64.bat:\n\n{SERVER_BAT}",
            )
            return

        try:
            self.log("")
            self.log("========================================")
            self.log("[SERVER] Запуск сервера")
            self.log(f"[SERVER] {SERVER_BAT}")
            self.log(f"[SERVER] Профиль: {SERVER_NAME}")

            # Let Windows execute the BAT through COMSPEC.  Using shell=True
            # avoids the cmd /c + call quoting problem for Program Files (x86).
            command = subprocess.list2cmdline(
                [str(SERVER_BAT), "-servername", SERVER_NAME]
            )

            creation_flags = getattr(subprocess, "CREATE_NO_WINDOW", 0)

            self.server_pause_released = False
            self.server_java_exit_code = None
            self.server_process = subprocess.Popen(
                command,
                cwd=str(SERVER_BAT.parent),
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                shell=True,
                text=False,
                bufsize=0,
                creationflags=creation_flags,
            )

            self.set_status("Сервер запускается...")
            self.log(f"[SERVER] PID launcher: {self.server_process.pid}")
            self.update_server_buttons()

            threading.Thread(
                target=self.read_server_output,
                args=(self.server_process,),
                daemon=True,
            ).start()

            threading.Thread(
                target=self.watch_server,
                args=(self.server_process,),
                daemon=True,
            ).start()

        except Exception as exc:
            self.server_process = None
            self.update_server_buttons()
            self.set_status("Ошибка запуска сервера")
            self.log(f"[SERVER ERROR] {exc}")

            messagebox.showerror(
                "Ошибка запуска сервера",
                str(exc),
            )

    def read_server_output(self, process):
        if process.stdout is None:
            return

        try:
            while True:
                raw_line = process.stdout.readline()
                if not raw_line:
                    break

                line = decode_server_line(raw_line)
                if line:
                    self.log(line)

                    # This line is printed by the custom BAT only after Java has
                    # already exited. Release its final `pause` automatically.
                    if (
                        "Press any key to close this window" in line
                        and not self.server_pause_released
                    ):
                        self.server_pause_released = True
                        try:
                            if process.stdin is not None:
                                process.stdin.write(b"\n")
                                process.stdin.flush()
                        except (BrokenPipeError, OSError):
                            pass

                    marker = "Server process exited with code "
                    if marker in line:
                        value = line.rsplit(marker, 1)[1].strip().rstrip(".")
                        try:
                            self.server_java_exit_code = int(value)
                        except ValueError:
                            pass

                    if line.strip().startswith("Exit code:"):
                        value = line.split(":", 1)[1].strip()
                        try:
                            self.server_java_exit_code = int(value)
                        except ValueError:
                            pass

                    lowered = line.lower()
                    if (
                        "server started" in lowered
                        or "listening on port" in lowered
                        or "steam game server initialized" in lowered
                    ):
                        self.set_status("Сервер запущен")

        except Exception as exc:
            self.log(f"[SERVER OUTPUT ERROR] {exc}")

    def watch_server(self, process):
        shell_exit_code = process.wait()
        java_exit_code = self.server_java_exit_code

        if self.server_process is process:
            self.server_process = None

        effective_exit_code = (
            java_exit_code if java_exit_code is not None else shell_exit_code
        )

        if effective_exit_code == 0:
            self.log(
                f"[SERVER] Процесс завершён. Код Java: {effective_exit_code}; "
                f"код launcher: {shell_exit_code}"
            )
            self.set_status("Сервер остановлен")
        else:
            self.log(
                f"[SERVER ERROR] Сервер завершён. Код Java: {java_exit_code}; "
                f"код launcher: {shell_exit_code}"
            )
            self.log(
                "[SERVER ERROR] Причина завершения находится выше в журнале; "
                "stdout/stderr сервера теперь не скрываются."
            )
            self.set_status(f"Ошибка сервера ({effective_exit_code})")

        self.after(0, self.update_server_buttons)

    def stop_server(self):
        if not self.server_is_running():
            self.server_process = None
            self.update_server_buttons()

            messagebox.showinfo(
                "Сервер",
                "Сервер, запущенный из этой программы, не найден.",
            )
            return

        process = self.server_process

        try:
            if process.stdin is None:
                raise RuntimeError("stdin сервера недоступен.")

            process.stdin.write(b"quit\n")
            process.stdin.flush()

            self.set_status("Остановка сервера...")
            self.stop_server_button.configure(state="disabled")

            self.log("")
            self.log("[SERVER] Отправлена команда: quit")
            self.log("[SERVER] Сервер сохраняет мир и завершает работу...")

        except Exception as exc:
            self.log(f"[SERVER ERROR] Не удалось отправить quit: {exc}")
            self.set_status("Ошибка остановки")
            self.update_server_buttons()

            messagebox.showerror(
                "Ошибка остановки сервера",
                "Не удалось корректно отправить серверу команду quit.\n\n"
                f"{exc}",
            )

    # ========================================================
    # Log collector
    # ========================================================

    def start_collect_logs(self):
        self.collect_logs_button.configure(state="disabled")
        self.set_status("Сбор логов...")

        threading.Thread(
            target=self.collect_logs,
            daemon=True,
        ).start()

    def collect_logs(self):
        try:
            LOG_ARCHIVE_ROOT.mkdir(parents=True, exist_ok=True)
            files_to_archive = []

            for log_file in (SERVER_CONSOLE_LOG, CLIENT_CONSOLE_LOG):
                if log_file.is_file():
                    files_to_archive.append(log_file)
                else:
                    self.log(f"[LOGS] Не найден: {log_file}")

            latest_server_debug = get_latest_file(
                LOGS_ROOT,
                "*_DebugLog-server.txt",
            )
            if latest_server_debug:
                files_to_archive.append(latest_server_debug)
            else:
                self.log("[LOGS] Не найден *_DebugLog-server.txt")

            latest_client_debug = get_latest_file(
                LOGS_ROOT,
                "*_DebugLog.txt",
            )
            if latest_client_debug:
                files_to_archive.append(latest_client_debug)
            else:
                self.log("[LOGS] Не найден *_DebugLog.txt")

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
                raise RuntimeError("Не найдено ни одного файла для архивации.")

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
                    archive.write(log_file, arcname=log_file.name)

            archive_size_mb = archive_path.stat().st_size / 1024 / 1024

            self.log("")
            self.log(f"[LOGS] Готово: {archive_path.name}")
            self.log(f"[LOGS] Файлов: {len(files_to_archive)}")
            self.log(f"[LOGS] Размер: {archive_size_mb:.2f} MB")

            self.set_status("Логи собраны")

            self.after(
                0,
                lambda: messagebox.showinfo(
                    "Логи собраны",
                    f"Архив создан:\n\n{archive_path}",
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


if __name__ == "__main__":
    app = ServerToolsApp()
    app.mainloop()
